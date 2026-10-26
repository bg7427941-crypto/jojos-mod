#!/usr/bin/env python3
"""Build a local resource pack from OGG clips supplied by the player.

No audio is bundled or downloaded. Python's standard library is sufficient.
Minecraft 26.3 resource format 97.1 was checked against version.json in the
NeoForge 26.3.0.95 patched Minecraft JAR and its PackFormat metadata codec.
"""

import argparse
import json
from pathlib import Path
import sys
from zipfile import ZIP_DEFLATED, ZipFile


EVENTS = {
    "summon": ("summon",),
    "dismiss": ("dismiss",),
    "swing": ("swing",),
    "impact": ("impact_1", "impact_2"),
    "guard": ("guard",),
    "guard_block": ("guard_block",),
    "barrage_start": ("barrage_start",),
    "star_finger": ("star_finger",),
    "intercept": ("intercept",),
    "intercept_catch": ("intercept_catch",),
    "leap": ("leap",),
    "leap_land": ("leap_land",),
}
CLIPS = {name for variants in EVENTS.values() for name in variants}
AUDIO_EXTENSIONS = {".ogg", ".oga", ".opus", ".mp3", ".wav", ".flac", ".m4a", ".aac", ".aiff"}


def check_vorbis(path):
    """Check the first Ogg page and Vorbis identification header, not a full decode."""
    with path.open("rb") as stream:
        page = stream.read(27)
        if len(page) != 27 or page[:4] != b"OggS" or page[4] != 0:
            raise ValueError(f"{path.name}: no tiene una cabecera Ogg válida.")
        lacing = stream.read(page[26])
        if len(lacing) != page[26] or not lacing:
            raise ValueError(f"{path.name}: cabecera Ogg incompleta.")
        body = stream.read(sum(lacing))
        if len(body) != sum(lacing) or len(body) < 30 or not body.startswith(b"\x01vorbis"):
            raise ValueError(f"{path.name}: se necesita OGG Vorbis; cambiar la extensión no convierte el audio.")
        if int.from_bytes(body[7:11], "little") != 0 or body[11] == 0:
            raise ValueError(f"{path.name}: identificación Vorbis no válida.")
        if int.from_bytes(body[12:16], "little") == 0:
            raise ValueError(f"{path.name}: frecuencia de audio no válida.")
        return body[11]


def collect_clips(source):
    clips = {}
    for path in sorted(source.iterdir()):
        if not path.is_file():
            continue
        name = path.stem.lower()
        extension = path.suffix.lower()
        if extension not in AUDIO_EXTENSIONS and name not in CLIPS:
            continue
        if extension != ".ogg":
            raise ValueError(f"{path.name}: convierte el archivo a OGG Vorbis antes de construir el pack.")
        if name not in CLIPS:
            raise ValueError(f"{path.name}: nombre no reconocido. Permitidos: {', '.join(sorted(CLIPS))}.")
        if name in clips:
            raise ValueError(f"Hay dos archivos para {name}; conserva solo uno.")
        if path.resolve().parent != source:
            raise ValueError(f"{path.name}: el archivo debe estar dentro de la carpeta indicada.")
        channels = check_vorbis(path)
        if channels != 1:
            print(f"Aviso: {path.name} tiene {channels} canales; usa mono para el sonido espacial.", file=sys.stderr)
        clips[name] = path
    if not clips:
        raise ValueError("No hay clips OGG reconocidos. Añade al menos uno de los nombres de la guía.")
    return clips


def sounds_for(clips):
    result = {}
    for event, variants in EVENTS.items():
        supplied = [name for name in variants if name in clips]
        if supplied:
            result[f"star_platinum.{event}"] = {
                "replace": True,
                "subtitle": f"subtitles.jojosmod.star_platinum.{event}",
                "sounds": [
                    {"name": f"jojosmod:star_platinum/{name}", "attenuation_distance": 16}
                    for name in supplied
                ],
            }
    return result


def main():
    parser = argparse.ArgumentParser(description="Construye un pack local con tus clips autorizados; no descarga audio.")
    parser.add_argument("--source-dir", required=True, type=Path, help="Carpeta con los OGG indicados en la guía.")
    parser.add_argument("--output", required=True, type=Path, help="Ruta del ZIP que se creará.")
    parser.add_argument("--overwrite", action="store_true", help="Permite sustituir un ZIP que ya exista.")
    args = parser.parse_args()
    try:
        source = args.source_dir.expanduser().resolve(strict=True)
        if not source.is_dir():
            raise ValueError("--source-dir debe indicar una carpeta.")
        output = args.output.expanduser().absolute()
        if output.suffix.lower() != ".zip":
            raise ValueError("--output debe terminar en .zip.")
        if output.exists() and not args.overwrite:
            raise ValueError("El ZIP ya existe; usa otro nombre o añade --overwrite para sustituirlo.")
        clips = collect_clips(source)
        sounds = sounds_for(clips)
        metadata = Path(__file__).with_name("pack.mcmeta").read_bytes()
        json.loads(metadata)
        output.parent.mkdir(parents=True, exist_ok=True)
        with ZipFile(output, "w" if args.overwrite else "x", compression=ZIP_DEFLATED) as archive:
            archive.writestr("pack.mcmeta", metadata)
            archive.writestr("assets/jojosmod/sounds.json", json.dumps(sounds, ensure_ascii=False, indent=2) + "\n")
            for name, path in clips.items():
                archive.write(path, f"assets/jojosmod/sounds/star_platinum/{name}.ogg")
    except (OSError, ValueError) as error:
        parser.error(str(error))
    print(f"Creado: {output}")
    print(f"{len(clips)} clips; {len(sounds)} eventos sustituidos. Los demás conservan los sonidos del mod.")


if __name__ == "__main__":
    main()
