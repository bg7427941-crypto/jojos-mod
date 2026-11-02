#!/usr/bin/env python3
"""Stage original audio only; never alter the owner's development resources."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parents[2]
ASSETS = Path('assets/jojosmod')
SOURCES = (
    'art/sounds/original-provenance.json',
    'art/hierophant_green/sound-provenance.json',
    'art/part_three/sound-provenance.json',
    'art/part_three/time-effects-provenance.json',
)

# Keep event identifiers, timing, owner channels and resource-pack overrides.
# Speech slots become synthesized action cues in this distribution.
REPLACEMENTS = {
    'star_platinum.summon': ['star_platinum/original/summon'],
    'star_platinum.single_ora': ['star_platinum/original/swing_2'],
    'star_platinum.impact': ['star_platinum/original/impact_1', 'star_platinum/original/impact_2'],
    'star_platinum.guard': ['star_platinum/original/guard'],
    'star_platinum.guard_block': ['star_platinum/original/guard_block'],
    'star_platinum.barrage_start': ['star_platinum/original/barrage_start'],
    'star_platinum.star_finger': ['star_platinum/original/star_finger'],
    'star_platinum.time_stop': ['star_platinum/time_stop'],
    'hierophant_green.splash': ['hierophant_green/splash'],
    'hierophant_green.splash_call': ['hierophant_green/charge'],
    'silver_chariot.impact': ['silver_chariot/impact'],
    'silver_chariot.strike_call': ['silver_chariot/cast'],
    'silver_chariot.barrage_call': ['silver_chariot/cast'],
    'silver_chariot.armor_release': ['silver_chariot/summon'],
    'magicians_red.impact': ['magicians_red/impact'],
    'magicians_red.shoot': ['magicians_red/cast'],
    'magicians_red.crossfire_call': ['magicians_red/summon'],
    'magicians_red.bind_call': ['magicians_red/cast'],
    'hermit_purple.call': ['hermit_purple/cast'],
    'the_fool.impact': ['the_fool/impact'],
    'the_world.impact': ['the_world/impact'],
    'the_world.strike_call': ['the_world/cast'],
    'the_world.barrage_call': ['the_world/cast'],
    'the_world.time_stop': ['the_world/time_stop'],
    'the_world.resume': ['the_world/resume'],
}
SUBTITLES = {
    'es_es': {
        'star_platinum.single_ora': 'Star Platinum golpea',
        'the_world.strike_call': 'The World prepara un golpe',
        'magicians_red.crossfire_call': 'Se concentra Crossfire Hurricane',
        'magicians_red.bind_call': 'Se prepara Red Bind',
        'hierophant_green.splash_call': 'Se prepara Emerald Splash',
        'hermit_purple.call': 'Hermit Purple extiende sus lianas',
    },
    'en_us': {
        'star_platinum.single_ora': 'Star Platinum strikes',
        'the_world.strike_call': 'The World readies a strike',
        'magicians_red.crossfire_call': 'Crossfire Hurricane gathers',
        'magicians_red.bind_call': 'Red Bind readies',
        'hierophant_green.splash_call': 'Emerald Splash readies',
        'hermit_purple.call': 'Hermit Purple extends its vines',
    },
}


def read_json(path):
    return json.loads(path.read_text(encoding='utf-8'))


def original_clips(root=ROOT):
    """A new sound is publishable only after its original manifest is reviewed."""
    clips = {}
    for source in SOURCES:
        data = read_json(root / source)
        if data.get('license') != 'MIT':
            raise ValueError(f'Non-original audio manifest: {source}')
        for clip in data['clips']:
            name = clip['file']
            path = Path(name) if name.startswith('src/main/resources/') else Path('src/main/resources') / ASSETS / 'sounds' / name
            if not path.is_relative_to(Path('src/main/resources') / ASSETS / 'sounds') or '..' in path.parts:
                raise ValueError(f'Unexpected audio path: {path}')
            content = (root / path).read_bytes()
            if hashlib.sha256(content).hexdigest() != clip['sha256']:
                raise ValueError(f'Original audio hash mismatch: {path}')
            entry = path.relative_to('src/main/resources').as_posix()
            if entry in clips:
                raise ValueError(f'Duplicate original audio: {entry}')
            clips[entry] = {'sha256': clip['sha256'], 'source_manifest': source}
    return dict(sorted(clips.items()))


def public_events(events, clips):
    unknown = set(REPLACEMENTS) - events.keys()
    if unknown:
        raise ValueError(f'Obsolete sound mappings: {sorted(unknown)}')
    result = {}
    for event, definition in events.items():
        definition = dict(definition)
        if event in REPLACEMENTS:
            definition['sounds'] = [{'name': 'jojosmod:' + name, 'attenuation_distance': 24}
                                    for name in REPLACEMENTS[event]]
        definition['replace'] = True
        for sound in definition['sounds']:
            name = sound if isinstance(sound, str) else sound['name']
            if not name.startswith('jojosmod:') or str(ASSETS / 'sounds' / (name.split(':', 1)[1] + '.ogg')) not in clips:
                raise ValueError(f'Unapproved public sound in {event}: {name}')
        result[event] = definition
    return result


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def stage(output):
    clips = original_clips()
    events = public_events(read_json(ROOT / 'src/main/resources' / ASSETS / 'sounds.json'), clips)
    # Stage only beneath build/, never delete a user-specified arbitrary folder.
    output = output.resolve()
    if not output.is_relative_to((ROOT / 'build').resolve()) or output == (ROOT / 'build').resolve():
        raise ValueError('Output must be a subdirectory of this project\'s build directory')
    if output.exists():
        shutil.rmtree(output)
    for entry in clips:
        target = output / entry
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(ROOT / 'src/main/resources' / entry, target)
    write_json(output / ASSETS / 'sounds.json', events)
    for lang in sorted((ROOT / 'src/main/resources' / ASSETS / 'lang').glob('*.json')):
        data = read_json(lang)
        data.update({'subtitles.jojosmod.' + key: value for key, value in SUBTITLES.get(lang.stem, {}).items()})
        write_json(output / ASSETS / 'lang' / lang.name, data)
    write_json(output / 'META-INF/jojosmod/public-audio.json', {
        'distribution': 'public-alpha', 'license': 'MIT',
        'description': 'Original synthesized effects only; speech slots use action cues. No collected recordings.',
        'clips': clips, 'events': sorted(events),
    })
    print(f'Public alpha audio: {len(clips)} original clips, {len(events)} events; development audio unchanged.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', required=True, type=Path)
    args = parser.parse_args()
    stage(args.output)
