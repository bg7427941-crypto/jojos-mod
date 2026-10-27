#!/usr/bin/env python3
"""Original time-stop/resume audio and Hermit Purple arm atlas; no recording inputs."""
from array import array
from pathlib import Path
import hashlib
import json
import math
import random
import subprocess
import sys
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / 'src/main/resources/assets/jojosmod'
RATE = 48000


def sound(kind, resume):
    duration = .65 if resume else 1.25 if kind == 'the_world' else .95
    rng = random.Random(12100 + (kind == 'the_world') * 10 + resume)
    values, air, phase = [], 0.0, 0.0
    for i in range(round(duration * RATE)):
        t = i / RATE
        u = t / duration
        air = .82 * air + .18 * rng.uniform(-1, 1)
        if resume:
            frequency = 90 + 1100 * u * u
            envelope = math.sin(math.pi * u) ** .7
            value = .45 * math.sin(phase) + .3 * air + .18 * math.sin(phase * 2.01)
        elif kind == 'the_world':
            # Descending metallic pressure, bass thump, and a short suspended clock chime.
            frequency = 75 + 680 * math.exp(-8 * u)
            envelope = min(1, t / .007) * math.exp(-2.8 * u) * min(1, (duration - t) / .07)
            value = (.34 * math.sin(phase) + .17 * math.sin(phase * 2.73)
                     + .3 * math.sin(2 * math.pi * 54 * t) * math.exp(-6 * u) + .35 * air)
            if t > .18:
                chime = t - .18
                value += .18 * math.sin(2 * math.pi * 1417 * chime) * math.exp(-9 * chime)
        else:
            # Faster glassy double sweep for Star Platinum, with its own pitches and envelope.
            frequency = 110 + 970 * math.exp(-10 * u) + 95 * math.exp(-((u - .23) / .06) ** 2)
            envelope = min(1, t / .006) * math.exp(-3.2 * u) * min(1, (duration - t) / .07)
            value = (.37 * math.sin(phase) + .2 * math.sin(phase * 3.17)
                     + .22 * math.sin(2 * math.pi * 86 * t) + .28 * air)
        phase += 2 * math.pi * frequency / RATE
        values.append(math.tanh(value * 1.3) * envelope)
    peak = max(abs(v) for v in values)
    rms = math.sqrt(sum(v * v for v in values) / len(values))
    gain = min(.86 / peak, .18 / rms)
    data = array('h', (round(v * gain * 32767) for v in values))
    if sys.byteorder != 'little': data.byteswap()
    return data.tobytes(), duration


def main():
    clips = []
    for kind in ['the_world', 'star_platinum']:
        for resume in [False, True]:
            cue = 'resume' if resume else 'time_stop'
            path = ASSETS / f'sounds/{kind}/{cue}.ogg'
            path.parent.mkdir(parents=True, exist_ok=True)
            data, duration = sound(kind, resume)
            subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-f', 's16le', '-ar', str(RATE),
                            '-ac', '1', '-i', 'pipe:0', '-c:a', 'libvorbis', '-q:a', '5', '-fflags', '+bitexact',
                            '-flags:a', '+bitexact', '-y', str(path)], input=data, check=True)
            clips.append({'file': str(path.relative_to(ROOT)), 'duration_seconds': duration,
                          'channels': 1, 'sample_rate': RATE, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
    image = Image.new('RGBA', (64, 32))
    rng = random.Random(121)
    for y in range(32):
        for x in range(64):
            grain = rng.randint(-12, 12) + (8 if (x + y * 2) % 7 < 2 else 0)
            color = (127, 48, 167) if x < 32 else (211, 155, 233)
            image.putpixel((x, y), tuple(max(0, min(255, c + grain)) for c in color) + (255,))
    path = ASSETS / 'textures/entity/hermit_purple_arms.png'
    image.save(path)
    manifest = {'version': '0.12.1', 'license': 'MIT',
                'authorship': 'Original mathematical time audio and arm-vine atlas. No imported recording or image inputs.',
                'generator': 'art/part_three/build_time_effects.py', 'clips': clips,
                'texture': {'file': str(path.relative_to(ROOT)), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}}
    (HERE / 'time-effects-provenance.json').write_text(json.dumps(manifest, indent=2) + '\n')
    print('Four original time cues and arm-vine atlas generated.')


if __name__ == '__main__': main()
