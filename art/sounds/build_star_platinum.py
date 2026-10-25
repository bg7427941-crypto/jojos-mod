#!/usr/bin/env python3
"""Rebuild the edited CC0 Kenney clips; needs Python 3, FFmpeg and FFprobe.

Use --source-dir with a local cache containing the original pack-relative paths
for an offline rebuild. Without it, only the five pinned inputs are downloaded.
"""

import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import urllib.request


ROOT = Path(__file__).resolve().parents[2]
DESTINATION = ROOT / "src/main/resources/assets/jojosmod/sounds/star_platinum"
COMMIT = "9662dc1bed3b06ae753045915f9accfac044c7ce"
BASE_URL = f"https://raw.githubusercontent.com/GeorgeQLe/assets-sfx-combat/{COMMIT}/assets/kenney/"
SOURCES = {
    "impact-sounds/Audio/impactPunch_heavy_000.ogg": "b33a8f14068aec24ec69ba85e5e87fdc41228975f6a1a3e44a6e7d6fc3d9f8d8",
    "impact-sounds/Audio/impactPunch_medium_001.ogg": "e71d23abcc3f10d9d6bc9615b2a431fff70b12333a5fcdd9964457564ace4349",
    "impact-sounds/Audio/impactMetal_medium_000.ogg": "a96f879fec0864a8938e0c745b6996a6c5679c16a234ce31a01cc995e8401003",
    "rpg-audio/Audio/knifeSlice.ogg": "4cd96dc630bed9840c15f1dd2306da2cc56a4da26a5d3f1a03c5a7265ac5e54f",
    "sci-fi-sounds/Audio/forceField_000.ogg": "c2916f2a062c8ddd1aca2826d134fe90847037db31342726ffb0f9097afe339c",
}
FIELD = "sci-fi-sounds/Audio/forceField_000.ogg"
CLIPS = {
    "summon": (FIELD, "atrim=start=0.02:end=0.67,asetpts=PTS-STARTPTS,highpass=f=80,lowpass=f=6000,afade=t=in:d=0.03,afade=t=out:st=0.4:d=0.25"),
    "dismiss": (FIELD, "atrim=start=0.02:end=0.37,asetpts=PTS-STARTPTS,areverse,highpass=f=80,lowpass=f=5000,afade=t=in:d=0.01,afade=t=out:st=0.17:d=0.18"),
    "swing": ("rpg-audio/Audio/knifeSlice.ogg", "atrim=start=0.14:end=0.30,asetpts=PTS-STARTPTS,atempo=1.333333,atrim=duration=0.12,highpass=f=300,lowpass=f=6500,afade=t=in:d=0.008,afade=t=out:st=0.095:d=0.025"),
    "impact_1": ("impact-sounds/Audio/impactPunch_heavy_000.ogg", "atrim=start=0.015:end=0.195,asetpts=PTS-STARTPTS,highpass=f=60,lowpass=f=7500,afade=t=in:d=0.003,afade=t=out:st=0.11:d=0.07"),
    "impact_2": ("impact-sounds/Audio/impactPunch_medium_001.ogg", "atrim=start=0:end=0.18,asetpts=PTS-STARTPTS,highpass=f=60,lowpass=f=7500,afade=t=in:d=0.003,afade=t=out:st=0.11:d=0.07"),
    "guard": (FIELD, "atrim=start=0.02:end=0.24,asetpts=PTS-STARTPTS,highpass=f=400,lowpass=f=9000,afade=t=in:d=0.015,afade=t=out:st=0.12:d=0.10"),
    "guard_block": ("impact-sounds/Audio/impactMetal_medium_000.ogg", "atrim=start=0:end=0.18,asetpts=PTS-STARTPTS,highpass=f=180,lowpass=f=8000,afade=t=in:d=0.003,afade=t=out:st=0.10:d=0.08"),
}


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command):
    return subprocess.run(command, check=True, capture_output=True, text=True)


def peak_db(path):
    result = run(["ffmpeg", "-nostdin", "-i", str(path), "-af", "volumedetect", "-f", "null", "-"])
    match = re.search(r"max_volume: (-?[0-9.]+) dB", result.stderr)
    if match is None:
        raise RuntimeError(f"Missing or silent audio: {path}")
    return float(match.group(1))


def build(source_dir, temporary):
    for relative, checksum in SOURCES.items():
        source = source_dir / relative
        if not source.exists():
            source.parent.mkdir(parents=True, exist_ok=True)
            request = urllib.request.Request(BASE_URL + relative, headers={"User-Agent": "jojosmod-audio-build"})
            with urllib.request.urlopen(request, timeout=30) as response:
                source.write_bytes(response.read())
        if sha256(source) != checksum:
            raise RuntimeError(f"Source checksum mismatch: {source}")

    DESTINATION.mkdir(parents=True, exist_ok=True)
    for notice in Path(__file__).parent.glob("kenney-*-license.txt"):
        shutil.copyfile(notice, DESTINATION / notice.name)
    records = []
    for name, (relative, filters) in CLIPS.items():
        wave = temporary / f"{name}.wav"
        run(["ffmpeg", "-v", "error", "-nostdin", "-y", "-i", str(source_dir / relative),
             "-af", "aformat=channel_layouts=mono,aresample=48000," + filters,
             "-ar", "48000", "-ac", "1", "-c:a", "pcm_f32le", str(wave)])
        # Vorbis can overshoot the source waveform, so measure the decoded file
        # and correct its gain rather than trusting the pre-encode peak alone.
        gain = -6.0 - peak_db(wave)
        destination = DESTINATION / f"{name}.ogg"
        for attempt in range(3):
            run(["ffmpeg", "-v", "error", "-nostdin", "-y", "-fflags", "+bitexact", "-i", str(wave),
                 "-map_metadata", "-1", "-af", f"volume={gain:.3f}dB", "-ar", "48000", "-ac", "1",
                 "-c:a", "libvorbis", "-q:a", "5", "-flags:a", "+bitexact", str(destination)])
            final_peak = peak_db(destination)
            if -3.5 <= final_peak <= -2.5 or attempt == 2:
                break
            gain += -3.0 - final_peak
        probe = json.loads(run(["ffprobe", "-v", "quiet", "-show_streams", "-show_format", "-of", "json", str(destination)]).stdout)
        stream = probe["streams"][0]
        duration = float(probe["format"]["duration"])
        if stream["codec_name"] != "vorbis" or stream["channels"] != 1 or int(stream["sample_rate"]) != 48000:
            raise RuntimeError(f"Unexpected codec or spatial-audio format: {destination}")
        if final_peak > -1.0:
            raise RuntimeError(f"Insufficient peak headroom: {destination}: {final_peak} dBFS")
        records.append({
            "file": f"star_platinum/{name}.ogg", "source": relative,
            "source_url": BASE_URL + relative, "source_sha256": SOURCES[relative],
            "license": "CC0-1.0", "filters": filters,
            "normalization_gain_db": round(gain, 3), "duration_seconds": round(duration, 6),
            "codec": "vorbis", "channels": 1, "sample_rate": 48000,
            "decoded_peak_dbfs": final_peak, "sha256": sha256(destination),
        })
        print(f"{name}: {duration:.3f}s, mono 48 kHz, peak {final_peak:.1f} dBFS")
    manifest = {"author": "Kenney (Kenney Vleugels)", "license": "CC0-1.0",
                "mirror_commit": COMMIT, "clips": records}
    (Path(__file__).parent / "provenance.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, help="Local cache for the original pack-relative input paths")
    arguments = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix="jojosmod-star-audio-") as temporary_dir:
        temporary = Path(temporary_dir)
        build(arguments.source_dir or temporary / "sources", temporary)


if __name__ == "__main__":
    main()
