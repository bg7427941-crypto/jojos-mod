#!/usr/bin/env python3
"""Synthesize five original jade/crystal cues from seeded noise and oscillators.

No recording inputs. Needs FFmpeg/FFprobe. The generator and its output use MIT.
"""
from array import array
import hashlib
import json
import math
from pathlib import Path
import random
import subprocess
import sys

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
DEST = ROOT / "src/main/resources/assets/jojosmod/sounds/hierophant_green"
RATE = 48000
CLIPS = {"summon": (.55, 11001), "dismiss": (.20, 11002), "charge": (.30, 11003),
         "splash": (.14, 11004), "impact": (.12, 11005)}


def synthesize(name, duration, seed):
    rng = random.Random(seed)
    samples = []
    air = 0
    for i in range(round(duration * RATE)):
        t = i / RATE
        u = t / duration
        air = .74 * air + .26 * rng.uniform(-1, 1)
        envelope = min(1, t / .003) * min(1, (duration - t) / .02)
        if name == "summon":
            value = sum(math.sin(2 * math.pi * f * t + 100 * u * u) * math.exp(-3 * u) / (j + 2)
                        for j, f in enumerate([430, 811, 1291, 1987])) + air * .16
        elif name == "dismiss":
            value = math.sin(2 * math.pi * (740 * t - 900 * t * t)) * (1 - u) * .4 + air * .2
        elif name == "charge":
            value = sum(math.sin(2 * math.pi * (f * t + 220 * t * t)) / (j + 3)
                        for j, f in enumerate([340, 510, 997])) * (.25 + .6 * u) + air * .12
        elif name == "splash":
            value = (math.sin(2 * math.pi * 980 * t) * .3 + math.sin(2 * math.pi * 1571 * t) * .2
                     + air * .6) * math.exp(-6 * u)
        else:
            value = sum(math.sin(2 * math.pi * f * t) / (j + 2) for j, f in enumerate([860, 1381, 2333])) * math.exp(-7 * u) + air * .23 * math.exp(-12 * u)
        samples.append(value * envelope)
    peak = max(abs(x) for x in samples)
    rms = math.sqrt(sum(x * x for x in samples) / len(samples))
    gain = min(.78 / peak, .14 / rms)
    pcm = array("h", [round(x * gain * 32767) for x in samples])
    if sys.byteorder != "little": pcm.byteswap()
    return pcm.tobytes()


def main():
    DEST.mkdir(parents=True, exist_ok=True)
    clips = []
    for name, (duration, seed) in CLIPS.items():
        path = DEST / (name + ".ogg")
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-f", "s16le", "-ar", str(RATE), "-ac", "1", "-i", "pipe:0",
                        "-c:a", "libvorbis", "-q:a", "4", "-fflags", "+bitexact", "-flags:a", "+bitexact", "-y", str(path)],
                       input=synthesize(name, duration, seed), check=True)
        probe = json.loads(subprocess.check_output(["ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries",
                "stream=codec_name,channels,sample_rate", "-of", "json", str(path)]))["streams"][0]
        assert probe["codec_name"] == "vorbis" and probe["channels"] == 1 and probe["sample_rate"] == str(RATE)
        subprocess.run(["ffmpeg", "-v", "error", "-i", str(path), "-f", "null", "-"], check=True)
        clips.append({"file": str(path.relative_to(ROOT)), "seed": seed, "duration_seconds": duration,
                      "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), **probe})
    manifest = {"license": "MIT", "authorship": "Original mathematical synthesis by JoJo's Mod; no recording inputs.",
                "generator": "build_sounds.py", "ffmpeg": subprocess.check_output(["ffmpeg", "-version"], text=True).splitlines()[0], "clips": clips}
    (HERE / "sound-provenance.json").write_text(json.dumps(manifest, indent=2) + "\n")
    print("Cinco efectos originales: Vorbis, mono/48 kHz y decodificación completa verificados.")


if __name__ == "__main__": main()
