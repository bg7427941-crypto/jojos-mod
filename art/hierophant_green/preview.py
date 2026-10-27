#!/usr/bin/env python3
"""Render the actual Hierophant assets using the project's MIT orthographic preview tool."""
import importlib.util
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / "src/main/resources/assets/jojosmod"
spec = importlib.util.spec_from_file_location("preview", ROOT / "art/star_platinum/preview_model.py")
preview = importlib.util.module_from_spec(spec)
spec.loader.exec_module(preview)


def main():
    bones = json.loads((ASSETS / "geckolib/models/entity/hierophant_green.geo.json").read_text())["minecraft:geometry"][0]["bones"]
    animations = json.loads((ASSETS / "geckolib/animations/entity/hierophant_green.animation.json").read_text())["animations"]
    atlas = np.array(Image.open(ASSETS / "textures/entity/hierophant_green.png").convert("RGBA"))
    for pose, time, filename in [("idle", .35, "preview.png"), ("splash", .4, "emerald-splash.png")]:
        sheet = Image.new("RGBA", (1680, 650), (224, 223, 218, 255))
        draw = ImageDraw.Draw(sheet)
        for i, (yaw, label) in enumerate([(0, "FRONT"), (28, "THREE QUARTER"), (90, "SIDE"), (180, "BACK")]):
            sheet.paste(preview.render(bones, atlas, animations["animation.hierophant_green." + pose], time, yaw), (i * 420, 25))
            draw.text((i * 420 + 18, 8), label + " / " + pose, fill=(38, 35, 47, 255))
        sheet.convert("RGB").save(HERE / filename)
        print(HERE / filename)


if __name__ == "__main__": main()
