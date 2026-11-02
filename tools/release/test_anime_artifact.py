"""Check the anime variant keeps recordings, mappings and attribution intact."""
import unittest
import test_public_artifact as public_gate
from public_audio import ROOT
from verify_anime_alpha import verify


class AnimeArtifactTests(unittest.TestCase):
    corrupt = public_gate.ReleaseGateTests.corrupt

    @classmethod
    def setUpClass(cls):
        properties = dict(line.split('=', 1) for line in (ROOT / 'gradle.properties').read_text().splitlines()
                          if '=' in line and not line.lstrip().startswith('#'))
        cls.artifact = ROOT / 'build/libs' / f"{properties['mod_id']}-{properties['mod_version']}-anime-alpha.jar"
        if not cls.artifact.is_file():
            raise RuntimeError('Build animeAlphaJar first')

    def test_valid_artifact(self):
        self.assertEqual(verify(self.artifact)['recorded_clips'], 25)

    def test_unlisted_recording_is_rejected(self):
        path = self.corrupt(add=('assets/jojosmod/sounds/unlisted.ogg', b'OggS'))
        with self.assertRaisesRegex(ValueError, 'unexpected anime-alpha audio'):
            verify(path)

    def test_substituted_audio_is_rejected(self):
        path = self.corrupt(replace=('assets/jojosmod/sounds/star_platinum/anime/barrage.ogg', b'OggS'))
        with self.assertRaisesRegex(ValueError, 'Audio hash mismatch'):
            verify(path)

    def test_missing_audio_license_is_rejected(self):
        path = self.corrupt(remove='META-INF/jojosmod/audio-licenses/LICENSE-GPL-3.0.txt')
        with self.assertRaisesRegex(ValueError, 'Missing/changed audio credit'):
            verify(path)

    def test_qa_class_is_rejected(self):
        path = self.corrupt(add=('AttachDriver.class', b'not a runtime class'))
        with self.assertRaisesRegex(ValueError, 'bundled QA class'):
            verify(path)

    def test_broken_sound_map_is_rejected(self):
        path = self.corrupt(replace=('assets/jojosmod/sounds.json', b'{}'))
        with self.assertRaisesRegex(ValueError, 'Anime event mappings changed'):
            verify(path)

    def test_stale_network_metadata_is_rejected(self):
        from zipfile import ZipFile
        with ZipFile(self.artifact) as jar:
            metadata = jar.read('META-INF/neoforge.mods.toml').replace(b'Network protocol 13.', b'Network protocol 12.')
        with self.assertRaisesRegex(ValueError, 'Wrong anime-alpha metadata'):
            verify(self.corrupt(replace=('META-INF/neoforge.mods.toml', metadata)))


if __name__ == '__main__':
    unittest.main()
