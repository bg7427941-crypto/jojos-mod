#!/usr/bin/env python3
"""Verify the alpha with the owner's existing recordings; do not relicense them."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import tomllib
from zipfile import ZipFile

from public_audio import ASSETS, ROOT, original_clips, read_json, write_json
from verify_public_alpha import require


def recorded_clips():
    clips = {}
    for clip in read_json(ROOT / 'art/anime_audio/provenance.json')['clips']:
        clips[Path(clip['file']).relative_to('src/main/resources').as_posix()] = clip['sha256']
    for clip in read_json(ROOT / 'art/sounds/collected-preview-provenance.json')['clips']:
        clips[str(ASSETS / 'sounds/star_platinum' / (clip['slot'] + '.ogg'))] = clip['sha256']
    for entry, digest in clips.items():
        require(hashlib.sha256((ROOT / 'src/main/resources' / entry).read_bytes()).hexdigest() == digest,
                f'Recorded source hash mismatch: {entry}')
    return clips


def verify(path):
    properties = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines()
                      if '=' in line and not line.lstrip().startswith('#'))
    protocol = re.search(r'PROTOCOL_VERSION = "(\d+)"',
                        (ROOT / 'src/main/java/io/github/bg7427941/jojosmod/network/ModNetworking.java').read_text()).group(1)
    recordings = recorded_clips()
    originals = original_clips()
    expected_audio = {**{entry: clip['sha256'] for entry, clip in originals.items()}, **recordings}
    events = read_json(ROOT / 'src/main/resources' / ASSETS / 'sounds.json')
    with ZipFile(path) as jar:
        entries = jar.namelist()
        names = set(entries)
        require(len(entries) == len(names) and jar.testzip() is None, 'Duplicate or broken ZIP entry')
        require(all('..' not in Path(n).parts and not n.startswith('/') for n in names), 'Unsafe archive path')
        audio = {n for n in names if Path(n).suffix.lower() in {'.ogg', '.wav', '.mp3', '.flac', '.opus'}}
        require(audio == expected_audio.keys(), 'Missing or unexpected anime-alpha audio')
        for entry, digest in expected_audio.items():
            require(hashlib.sha256(jar.read(entry)).hexdigest() == digest, f'Audio hash mismatch: {entry}')
        require(json.loads(jar.read(str(ASSETS / 'sounds.json'))) == events, 'Anime event mappings changed')
        for event in events.values():
            for sound in event['sounds']:
                name = sound if isinstance(sound, str) else sound['name']
                require(name.startswith('jojosmod:') and str(ASSETS / 'sounds' / (name.split(':', 1)[1] + '.ogg')) in audio,
                        f'Missing sound file: {name}')
        for target, source in [
            ('LICENSE', 'LICENSE'), ('LICENSE-MDK.txt', 'LICENSE-MDK.txt'),
            ('LICENSE-Roundabout.txt', 'art/star_platinum/upstream/LICENSE-Roundabout.txt'),
            ('THIRD_PARTY_NOTICES.md', 'THIRD_PARTY_NOTICES.md'),
            ('META-INF/jojosmod/audio-licenses/LICENSE-GPL-3.0.txt', 'art/anime_audio/LICENSE-GPL-3.0.txt'),
            ('META-INF/jojosmod/anime-audio-sources.json', 'art/anime_audio/sources.json'),
            ('META-INF/jojosmod/anime-audio-provenance.json', 'art/anime_audio/provenance.json'),
            ('META-INF/jojosmod/sound-collected-preview.json', 'art/sounds/collected-preview-provenance.json'),
        ]:
            require(target in names and jar.read(target) == (ROOT / source).read_bytes(), f'Missing/changed audio credit: {target}')
        for source in read_json(ROOT / 'art/anime_audio/sources.json')['sources']:
            require(hashlib.sha256((ROOT / source['local']).read_bytes()).hexdigest() == source['sha256'],
                    f'Editable upstream audio changed: {source["id"]}')
        metadata = tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode())
        require(metadata['mods'][0]['version'] == properties['mod_version'], 'Wrong alpha version')
        description = metadata['mods'][0]['description']
        require('previously collected anime recordings' in description and f'Network protocol {protocol}.' in description,
                'Wrong anime-alpha metadata')
        require('GPL-3.0' in metadata['license'] and 'Roundabout' in metadata['license'], 'Missing mixed-license declaration')
        require('JoJosMod-Distribution: anime-alpha' in jar.read('META-INF/MANIFEST.MF').decode(), 'Wrong artifact marker')
        dependencies = {d['modId']: d for d in metadata['dependencies'][properties['mod_id']]}
        for mod, key in [('minecraft', 'minecraft_version'), ('neoforge', 'neo_version'), ('geckolib', 'geckolib_version')]:
            require(properties[key] in dependencies[mod]['versionRange'], f'Wrong {mod} dependency')
        classes = {p.relative_to(ROOT / 'build/classes/java/main').as_posix(): p
                   for p in (ROOT / 'build/classes/java/main').rglob('*.class')}
        require({n for n in names if n.endswith('.class')} == classes.keys(), 'Missing runtime class or bundled QA class')
        for entry, source in classes.items():
            require(jar.read(entry) == source.read_bytes(), f'Changed class: {entry}')
        for source in (ROOT / 'src/main/resources').rglob('*'):
            if source.is_file():
                entry = source.relative_to(ROOT / 'src/main/resources').as_posix()
                require(jar.read(entry) == source.read_bytes(), f'Changed runtime resource: {entry}')
        for entry in names:
            if entry.endswith('.json'):
                json.loads(jar.read(entry))
    return {'version': properties['mod_version'], 'protocol': protocol, 'distribution': 'anime-alpha',
            'artifact': path.name, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size,
            'recorded_clips': len(recordings), 'original_clips': len(originals), 'sound_events': len(events),
            'main_classes': len(classes), 'checks': ['recording and original audio hashes', 'unchanged attack assignments',
                'source provenance and licenses', 'editable source hashes', 'unchanged classes and runtime resources', 'ZIP integrity']}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('jar', type=Path)
    args = parser.parse_args()
    result = verify(args.jar)
    destination = ROOT / 'build/animeAlphaVerification'
    write_json(destination / 'verification.json', result)
    (destination / 'SHA256SUMS.txt').write_text(f"{result['sha256']}  {args.jar.name}\n", encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False))
