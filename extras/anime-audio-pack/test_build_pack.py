"""Exercise the real pack CLI with original, decodable Vorbis fixtures."""

import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[2]
BUILDER = Path(__file__).with_name("build_pack.py")
FIXTURE = ROOT / "src/main/resources/assets/jojosmod/sounds/star_platinum/original/swing_1.ogg"


class AudioPackTest(unittest.TestCase):
    def test_single_ora_and_partial_impact_are_independent(self):
        with tempfile.TemporaryDirectory() as temporary:
            source = Path(temporary) / "clips"
            source.mkdir()
            for name in ("ora", "impact_2"):
                shutil.copyfile(FIXTURE, source / (name + ".ogg"))
            output = Path(temporary) / "pack.zip"
            result = subprocess.run([sys.executable, str(BUILDER), "--source-dir", str(source),
                                     "--output", str(output)], capture_output=True, text=True)
            self.assertEqual(result.returncode, 0, result.stderr)
            with ZipFile(output) as archive:
                sounds = json.loads(archive.read("assets/jojosmod/sounds.json"))
                self.assertEqual(set(sounds), {"star_platinum.single_ora", "star_platinum.impact"})
                self.assertTrue(sounds["star_platinum.single_ora"]["replace"])
                self.assertEqual(sounds["star_platinum.single_ora"]["sounds"][0]["name"],
                                 "jojosmod:star_platinum/ora")
                self.assertEqual(len(sounds["star_platinum.impact"]["sounds"]), 1)
                self.assertEqual(archive.read("assets/jojosmod/sounds/star_platinum/ora.ogg"),
                                 FIXTURE.read_bytes())

    def test_renamed_mp3_is_rejected_before_writing_archive(self):
        with tempfile.TemporaryDirectory() as temporary:
            source = Path(temporary) / "clips"
            source.mkdir()
            (source / "ora.ogg").write_bytes(b"ID3not-vorbis")
            output = Path(temporary) / "pack.zip"
            result = subprocess.run([sys.executable, str(BUILDER), "--source-dir", str(source),
                                     "--output", str(output)], capture_output=True, text=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertFalse(output.exists())


if __name__ == "__main__":
    unittest.main()
