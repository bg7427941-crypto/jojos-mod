#!/usr/bin/env python3
"""Reproducible edits of the explicitly requested recordings. See sources and notices."""
from pathlib import Path
import hashlib
import json
import math
import subprocess
from array import array

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / 'src/main/resources/assets/jojosmod'
SLOTS = {
    'sp_rush': ('star_platinum/anime/barrage', 2.25),
    'world_rush': ('the_world/anime/barrage', 2.3),
    'world_strike': ('the_world/anime/strike', .5),
    'chariot_rush': ('silver_chariot/anime/barrage', 1.95),
    'chariot_strike': ('silver_chariot/anime/strike', 1.034),
    'chariot_armor': ('silver_chariot/anime/armor', 1.33),
    'chariot_impact': ('silver_chariot/anime/impact', .24),
    'red_shoot': ('magicians_red/anime/shoot', .34),
    'red_impact': ('magicians_red/anime/impact', .45),
    'world_time': ('the_world/anime/time_stop', 2.559),
    'star_time': ('star_platinum/anime/time_stop', 2.354),
    'world_resume': ('the_world/anime/resume', 1.251),
    'emerald_call': ('hierophant_green/anime/splash_call', 1.401),
    'emerald_shoot': ('hierophant_green/anime/shoot', .18),
    'crossfire_call': ('magicians_red/anime/crossfire_call', 1.996),
    'red_bind_call': ('magicians_red/anime/bind_call', 1.297),
    'purple_call': ('hermit_purple/anime/call', 1.781),
}


def main():
    records = json.loads((HERE / 'sources.json').read_text())['sources']
    clips = []
    for record in records:
        source = ROOT / record['local']
        assert hashlib.sha256(source.read_bytes()).hexdigest() == record['sha256']
        path, duration = SLOTS[record['id']]
        destination = ASSETS / f'sounds/{path}.ogg'
        destination.parent.mkdir(parents=True, exist_ok=True)
        # Bound peak/RMS together; preserve the actor's pitch and the original playback speed.
        pcm = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(source), '-t', str(duration),
                                       '-ac', '1', '-ar', '48000', '-f', 'f32le', '-'])
        samples = array('f'); samples.frombytes(pcm)
        peak = max(abs(v) for v in samples)
        rms = math.sqrt(sum(v*v for v in samples) / len(samples))
        gain = min(4, .88 / peak, .18 / rms)
        fade = min(.035, duration / 5)
        subprocess.run(['ffmpeg', '-v', 'error', '-i', str(source), '-t', str(duration), '-ac', '1', '-ar', '48000',
                        '-af', f'volume={gain:.10f},afade=t=in:d=0.005,afade=t=out:st={duration-fade:.6f}:d={fade:.6f}',
                        '-c:a', 'libvorbis', '-q:a', '5', '-fflags', '+bitexact', '-flags:a', '+bitexact',
                        '-y', str(destination)], check=True)
        clips.append({'source': record['id'], 'file': str(destination.relative_to(ROOT)), 'start_seconds': 0,
                      'duration_seconds': duration, 'gain': gain,
                      'sha256': hashlib.sha256(destination.read_bytes()).hexdigest()})
    manifest = {'version': '0.13.0', 'authorship': 'Edited third-party JoJo recordings, not original project synthesis.',
                'terms': 'Retain source repository terms and underlying recording rights; see sources.json and THIRD_PARTY_NOTICES.md.',
                'clips': clips}
    (HERE / 'provenance.json').write_text(json.dumps(manifest, indent=2) + '\n')
    print(len(clips), 'recording clips converted to mono 48k Vorbis.')


if __name__ == '__main__': main()
