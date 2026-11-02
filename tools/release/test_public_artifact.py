"""Negative release-gate checks using a built public JAR, never synthetic audio."""
import tempfile
import unittest
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

from public_audio import ROOT
from verify_public_alpha import verify


class ReleaseGateTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        properties = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines()
                          if '=' in line and not line.lstrip().startswith('#'))
        cls.artifact = ROOT / 'build/libs' / f"{properties['mod_id']}-{properties['mod_version']}-public-alpha.jar"
        if not cls.artifact.is_file():
            raise RuntimeError('Build publicAlphaJar for the current version first')

    def corrupt(self, *, remove=None, replace=None, add=None):
        folder = tempfile.TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        path = Path(folder.name) / 'corrupt.jar'
        with ZipFile(self.artifact) as original, ZipFile(path, 'w', ZIP_DEFLATED) as target:
            for info in original.infolist():
                if info.filename != remove:
                    target.writestr(info, replace[1] if replace and info.filename == replace[0] else original.read(info))
            if add:
                target.writestr(*add)
        return path

    def test_valid_artifact(self):
        self.assertEqual(verify(self.artifact)['distribution'], 'public-alpha')

    def test_unlisted_recording_is_rejected(self):
        path = self.corrupt(add=('assets/jojosmod/sounds/new_recording.ogg', b'OggS'))
        with self.assertRaisesRegex(ValueError, 'Unexpected/missing public audio'):
            verify(path)

    def test_substituted_audio_is_rejected(self):
        path = self.corrupt(replace=('assets/jojosmod/sounds/star_platinum/original/summon.ogg', b'OggS'))
        with self.assertRaisesRegex(ValueError, 'Changed public audio'):
            verify(path)

    def test_missing_model_license_is_rejected(self):
        path = self.corrupt(remove='LICENSE-Roundabout.txt')
        with self.assertRaises((KeyError, ValueError)):
            verify(path)

    def test_qa_class_is_rejected(self):
        path = self.corrupt(add=('JourneyAttach.class', b'not a runtime class'))
        with self.assertRaisesRegex(ValueError, 'QA/dependency classes'):
            verify(path)

    def test_broken_sound_map_is_rejected(self):
        path = self.corrupt(replace=('assets/jojosmod/sounds.json', b'{}'))
        with self.assertRaisesRegex(ValueError, 'event mapping mismatch'):
            verify(path)

    def test_stale_network_metadata_is_rejected(self):
        with ZipFile(self.artifact) as jar:
            metadata = jar.read('META-INF/neoforge.mods.toml').replace(b'Network protocol 13.', b'Network protocol 12.')
        path = self.corrupt(replace=('META-INF/neoforge.mods.toml', metadata))
        with self.assertRaisesRegex(ValueError, 'Stale network description'):
            verify(path)


if __name__ == '__main__':
    unittest.main()
