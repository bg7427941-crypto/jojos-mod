#!/usr/bin/env python3
"""Verify the actual public artifact and produce its checksum and evidence."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import tomllib
from zipfile import ZipFile

from public_audio import ASSETS, ROOT, SUBTITLES, original_clips, public_events, read_json, write_json


def require(condition, reason):
    if not condition:
        raise ValueError(reason)


def verify(path):
    properties = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines()
                      if '=' in line and not line.lstrip().startswith('#'))
    protocol = re.search(r'PROTOCOL_VERSION = "(\d+)"',
                        (ROOT / 'src/main/java/io/github/bg7427941/jojosmod/network/ModNetworking.java').read_text()).group(1)
    clips = original_clips()
    expected_events = public_events(read_json(ROOT / 'src/main/resources' / ASSETS / 'sounds.json'), clips)
    with ZipFile(path) as jar:
        entries = jar.namelist()
        names = set(entries)
        require(len(names) == len(entries), 'Duplicate ZIP entries')
        require(jar.testzip() is None, 'Broken ZIP entry')
        require(all('..' not in Path(name).parts and not name.startswith('/') for name in names), 'Unsafe archive path')
        audio = {name for name in names if Path(name).suffix.lower() in {'.ogg', '.wav', '.mp3', '.flac', '.opus'}}
        require(audio == clips.keys(), f'Unexpected/missing public audio: {audio ^ clips.keys()}')
        for name, clip in clips.items():
            content = jar.read(name)
            require(hashlib.sha256(content).hexdigest() == clip['sha256'], f'Changed public audio: {name}')
            header = content.find(b'\x01vorbis', 0, 100)
            require(content.startswith(b'OggS') and header >= 0, f'Not Vorbis: {name}')
            require(content[header + 11] == 1, f'Non-mono audio: {name}')
        require(json.loads(jar.read(str(ASSETS / 'sounds.json'))) == expected_events, 'Public event mapping mismatch')
        manifest = json.loads(jar.read('META-INF/jojosmod/public-audio.json'))
        require(manifest['clips'] == clips and manifest['events'] == sorted(expected_events), 'Public provenance mismatch')
        require(not any('/anime/' in n or 'anime-audio-' in n or 'sound-collected-preview' in n or '/audio-licenses/' in n
                        for n in names), 'Internal recording metadata in public artifact')
        for lang in (ROOT / 'src/main/resources' / ASSETS / 'lang').glob('*.json'):
            expected = read_json(lang)
            expected.update({'subtitles.jojosmod.' + k: v for k, v in SUBTITLES.get(lang.stem, {}).items()})
            require(json.loads(jar.read(str(ASSETS / 'lang' / lang.name))) == expected, f'Language mismatch: {lang.name}')
            for definition in expected_events.values():
                require(definition['subtitle'] in expected, f'Missing subtitle in {lang.name}')
        metadata = tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode())
        require(metadata['mods'][0]['version'] == properties['mod_version'], 'Wrong mod version')
        require('Public alpha with original synthesized effects' in metadata['mods'][0]['description'], 'Wrong distribution description')
        require(f'Network protocol {protocol}.' in metadata['mods'][0]['description'], 'Stale network description')
        require('GPL' not in metadata['license'] and 'Roundabout' in metadata['license'], 'Wrong public license declaration')
        required_dependencies = {dep['modId']: dep for dep in metadata['dependencies'][properties['mod_id']]}
        for mod, key in [('minecraft', 'minecraft_version'), ('neoforge', 'neo_version'), ('geckolib', 'geckolib_version')]:
            require(properties[key] in required_dependencies[mod]['versionRange'], f'Wrong {mod} dependency')
        require('JoJosMod-Distribution: public-alpha' in jar.read('META-INF/MANIFEST.MF').decode(), 'Missing public build marker')
        for target, source in [('LICENSE', 'LICENSE'), ('LICENSE-MDK.txt', 'LICENSE-MDK.txt'),
                               ('LICENSE-Roundabout.txt', 'art/star_platinum/upstream/LICENSE-Roundabout.txt'),
                               ('THIRD_PARTY_NOTICES.md', 'distribution/PUBLIC_NOTICES.md')]:
            require(jar.read(target) == (ROOT / source).read_bytes(), f'Missing/changed license: {target}')
        # All shipping classes must equal main output; no test/attach helpers.
        classes = {p.relative_to(ROOT / 'build/classes/java/main').as_posix(): p
                   for p in (ROOT / 'build/classes/java/main').rglob('*.class')}
        require({n for n in names if n.endswith('.class')} == classes.keys(), 'Main classes missing, or QA/dependency classes bundled')
        for name, source in classes.items():
            require(jar.read(name) == source.read_bytes(), f'Changed class: {name}')
        resources = ROOT / 'src/main/resources'
        for source in resources.rglob('*'):
            if not source.is_file():
                continue
            relative = source.relative_to(resources).as_posix()
            if relative == str(ASSETS / 'sounds.json') or relative.startswith((str(ASSETS / 'sounds') + '/', str(ASSETS / 'lang') + '/')):
                continue
            require(jar.read(relative) == source.read_bytes(), f'Changed/missing runtime resource: {relative}')
        for name in names:
            if name.endswith('.json'):
                json.loads(jar.read(name))
    return {'version': properties['mod_version'], 'protocol': protocol, 'distribution': 'public-alpha',
            'artifact': path.name, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
            'bytes': path.stat().st_size, 'original_audio_clips': len(clips), 'sound_events': len(expected_events),
            'main_classes': len(classes), 'checks': ['original audio hashes and exhaustive allowlist', 'complete sound mapping and subtitles',
                'version, protocol and dependencies', 'retained license texts', 'main classes only', 'unchanged gameplay resources', 'ZIP/JSON integrity']}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('jar', type=Path)
    args = parser.parse_args()
    result = verify(args.jar)
    destination = ROOT / 'build/publicAlphaVerification'
    write_json(destination / 'verification.json', result)
    (destination / 'SHA256SUMS.txt').write_text(f"{result['sha256']}  {args.jar.name}\n", encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False))
