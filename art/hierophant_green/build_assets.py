#!/usr/bin/env python3
"""Build an original player-proportioned Hierophant Green, its six clips and core sprite.

Requires Pillow. Only the project's MIT export utilities are shared; no geometry,
pixels, keyframes or recordings from another Stand or mod are inputs.
"""

import hashlib
import importlib.util
import json
import math
from pathlib import Path
import random
import uuid
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / "src/main/resources/assets/jojosmod"
SIZE = 256
spec = importlib.util.spec_from_file_location("model_export", ROOT / "art/star_platinum/model_export.py")
export = importlib.util.module_from_spec(spec)
spec.loader.exec_module(export)
export.stable_id = lambda name: str(uuid.uuid5(uuid.NAMESPACE_URL, "jojosmod/hierophant-green/v1/" + name))

PALETTE = {"web": (33, 133, 91), "plate": (44, 163, 113), "ivory": (212, 230, 185),
           "dark": (10, 44, 38), "gem": (75, 247, 188), "gold": (184, 189, 99)}


class Model:
    def __init__(self):
        self.sculpture = export.Sculpture()
        self.atlas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        self.x, self.y, self.row_height = 2, 2, 0

    def bone(self, name, pivot, parent=None):
        self.sculpture.bone(name, pivot, parent)

    def face(self, width, height, material, seed):
        w, h = max(1, math.ceil(width * 2)), max(1, math.ceil(height * 2))
        if self.x + w + 2 > SIZE:
            self.x, self.y, self.row_height = 2, self.y + self.row_height + 2, 0
        assert self.y + h + 2 <= SIZE, "Atlas exhausted"
        x0, y0 = self.x, self.y
        self.x += w + 2
        self.row_height = max(self.row_height, h)
        rng = random.Random(seed)
        color = PALETTE[material]
        for y in range(h):
            for x in range(w):
                vein = material == "web" and ((x + y) % 12 == 0 or (x - y) % 12 == 0)
                base = (177, 221, 178) if vein else color
                shade = .80 + .19 * (1 - x / max(1, w - 1)) + .08 * (1 - y / max(1, h - 1))
                if x == w - 1 or y == h - 1: shade *= .83
                if material == "gem": shade += .13 * (x < w / 2)
                noise = rng.randrange(-3, 4)
                self.atlas.putpixel((x0 + x, y0 + y), tuple(max(0, min(255, round(v * shade) + noise)) for v in base) + (255,))
        return {"uv": [x0, y0], "uv_size": [w, h]}

    def cube(self, name, bone, origin, size, material="web", rotation=None):
        w, h, d = size
        uv = {}
        for face, width, height in [("north", w, h), ("south", w, h), ("east", d, h),
                                    ("west", d, h), ("up", w, d), ("down", w, d)]:
            seed = int(hashlib.sha256((name + face).encode()).hexdigest()[:8], 16)
            uv[face] = self.face(width, height, material, seed)
        cube = {"origin": origin, "size": size, "uv": uv}
        if rotation:
            cube["rotation"] = rotation
            cube["pivot"] = [origin[i] + size[i] / 2 for i in range(3)]
        self.sculpture.lookup[bone].setdefault("cubes", []).append(cube)
        self.sculpture.sources.append({"name": name, "bone": bone, "cube": cube})


def geometry():
    m = Model()
    for name, pivot, parent in [
        ("root", [0, 0, 0], None), ("body", [0, 13, 0], "root"),
        ("torso", [0, 21, 0], "body"), ("head", [0, 25, 0], "torso"),
        ("right_arm", [-4.9, 23, 0], "torso"), ("left_arm", [4.9, 23, 0], "torso"),
        ("right_forearm", [-4.9, 18, 0], "right_arm"), ("left_forearm", [4.9, 18, 0], "left_arm"),
        ("right_hand", [-4.9, 13.2, 0], "right_forearm"), ("left_hand", [4.9, 13.2, 0], "left_forearm"),
        ("right_leg", [-1.8, 12.3, 0], "body"), ("left_leg", [1.8, 12.3, 0], "body"),
        ("right_shin", [-1.8, 6.4, 0], "right_leg"), ("left_shin", [1.8, 6.4, 0], "left_leg")]:
        m.bone(name, pivot, parent)
    m.cube("waist", "body", [-3.25, 11.5, -1.8], [6.5, 2.2, 3.6], "plate")
    m.cube("abdomen", "torso", [-2.8, 13.5, -1.65], [5.6, 4.2, 3.3])
    m.cube("chest", "torso", [-3.5, 17.7, -2], [7, 6.6, 4])
    m.cube("neck", "head", [-1.4, 24.3, -1.3], [2.8, 1.4, 2.6], "ivory")
    for side in [-1, 1]:
        m.cube(f"chest_plate_{side}", "torso", [side * 1.6 - 1.25, 20, -2.25], [2.5, 3.4, .45], "ivory", [0, 0, side * -8])
        m.cube(f"rib_{side}", "torso", [side * 2.65 - .4, 17.5, -2.05], [.8, 3.5, .5], "plate")
        m.cube(f"hip_{side}", "body", [side * 3.1 - .8, 11.7, -1.7], [1.6, 2.4, 3.4], "plate", [0, 0, side * -8])
    m.cube("sternum_gem", "torso", [-.7, 18.2, -2.65], [1.4, 1.4, .8], "gem", [0, 0, 45])
    m.cube("helmet", "head", [-2.8, 25.3, -2.4], [5.6, 6.2, 4.8], "plate")
    m.cube("crown", "head", [-2.25, 31.5, -1.9], [4.5, .9, 3.8])
    m.cube("face", "head", [-2.25, 26.1, -2.75], [4.5, 4.65, .5], "ivory")
    m.cube("jaw", "head", [-1.6, 25.6, -2.9], [3.2, 1.35, .6], "plate")
    m.cube("nose_bridge", "head", [-.32, 27.3, -3.03], [.64, 2.1, .5], "plate")
    m.cube("brow", "head", [-2.6, 30.15, -2.94], [5.2, .7, .55], "plate")
    for side in [-1, 1]:
        m.cube(f"temple_{side}", "head", [side * 2.9 - .55, 26.3, -1.8], [1.1, 4.3, 3.6], "plate")
        m.cube(f"ear_{side}", "head", [side * 3.15 - .45, 27.4, -1.05], [.9, 2.1, 2.1], "ivory")
        m.cube(f"eye_socket_{side}", "head", [side * 1.25 - .73, 28.7, -3.02], [1.46, .8, .18], "dark")
        m.cube(f"eye_{side}", "head", [side * 1.25 - .42, 28.92, -3.13], [.84, .35, .15], "gem")
    for side, prefix in [(-1, "right"), (1, "left")]:
        x = side * 4.9
        m.cube(prefix + "_shoulder", prefix + "_arm", [x - 1.9, 22.5, -2.05], [3.8, 2.8, 4.1], "plate")
        m.cube(prefix + "_upper_arm", prefix + "_arm", [x - 1.35, 18, -1.65], [2.7, 5.1, 3.3])
        m.cube(prefix + "_elbow", prefix + "_forearm", [x - 1.25, 17.1, -1.6], [2.5, 1.6, 3.2], "ivory")
        m.cube(prefix + "_forearm", prefix + "_forearm", [x - 1.4, 13.1, -1.7], [2.8, 4.4, 3.4])
        m.cube(prefix + "_wrist_plate", prefix + "_hand", [x - 1.3, 12.2, -1.55], [2.6, 1.25, 3.1], "plate")
        m.cube(prefix + "_palm", prefix + "_hand", [x - 1.15, 10.4, -1.2], [2.3, 2.0, 2.4], "plate")
        for finger in range(4):
            m.cube(f"{prefix}_finger_{finger}", prefix + "_hand", [x - 1.15 + finger * .61, 8.7, -.8], [.45, 1.8, 1.4], "ivory")
        m.cube(prefix + "_thumb", prefix + "_hand", [x + side * 1.15 - .35, 10.3, -.8], [.7, 1.5, 1.6], "ivory", [0, 0, side * 20])
        lx = side * 1.8
        m.cube(prefix + "_thigh", prefix + "_leg", [lx - 1.4, 6.2, -1.65], [2.8, 6.1, 3.3])
        m.cube(prefix + "_knee", prefix + "_shin", [lx - 1.5, 5.7, -1.9], [3, 1.6, 3.7], "plate")
        m.cube(prefix + "_shin", prefix + "_shin", [lx - 1.25, 1, -1.55], [2.5, 4.8, 3.1])
        m.cube(prefix + "_foot", prefix + "_shin", [lx - 1.45, 0, -2.4], [2.9, 1.4, 4.1], "plate")
    return m


def animation_data():
    def keys(*values): return {str(time): value for time, value in values}
    idle = {"root": {"position": keys((0, [0, 0, 0]), (1.2, [0, .35, 0]), (2.4, [0, 0, 0]))},
            "head": {"rotation": keys((0, [0, -2, 0]), (1.2, [1, 2, 0]), (2.4, [0, -2, 0]))},
            "right_arm": {"rotation": [-3, 0, -5]}, "left_arm": {"rotation": [-3, 0, 5]}}
    move = {"torso": {"rotation": [6, 0, 0]}, "root": {"position": keys((0, [0, 0, 0]), (.4, [0, .3, 0]), (.8, [0, 0, 0]))}}
    for side, prefix in [(-1, "right"), (1, "left")]:
        move[prefix + "_leg"] = {"rotation": keys((0, [side * 13, 0, 0]), (.4, [-side * 13, 0, 0]), (.8, [side * 13, 0, 0]))}
        move[prefix + "_arm"] = {"rotation": keys((0, [-side * 9, 0, side * 5]), (.4, [side * 9, 0, side * 5]), (.8, [-side * 9, 0, side * 5]))}
    splash = {"torso": {"rotation": keys((0, [0, 0, 0]), (.2, [4, 0, 0]), (.3, [-5, 0, 0]), (.68, [-4, 0, 0]), (.9, [0, 0, 0]))}}
    for side, prefix in [(-1, "right"), (1, "left")]:
        splash[prefix + "_arm"] = {"rotation": keys((0, [-3, 0, side * 5]), (.2, [-58, side * -18, side * 15]),
                (.3, [-82, side * -10, side * 8]), (.4, [-76, side * -8, side * 10]), (.5, [-82, side * -10, side * 8]),
                (.68, [-65, side * -12, side * 12]), (.9, [-3, 0, side * 5]))}
        splash[prefix + "_forearm"] = {"rotation": keys((0, [0, 0, 0]), (.2, [-32, 0, 0]), (.3, [-5, 0, 0]),
                (.4, [-12, 0, 0]), (.5, [-5, 0, 0]), (.68, [-12, 0, 0]), (.9, [0, 0, 0]))}
        splash[prefix + "_hand"] = {"rotation": keys((0, [0, 0, 0]), (.2, [12, side * 12, 0]),
                (.3, [-18, side * 8, 0]), (.4, [3, side * 8, 0]), (.5, [-18, side * 8, 0]), (.9, [0, 0, 0]))}
    clips = {
        "idle": (2.4, True, idle), "move": (.8, True, move), "splash": (.9, False, splash),
        "summon": (.25, False, {"root": {"scale": keys((0, [.05, .05, .05]), (.25, [1, 1, 1])),
                "position": keys((0, [0, -3, 0]), (.25, [0, 0, 0]))}}),
        "dismiss": (.2, False, {"root": {"scale": keys((0, [1, 1, 1]), (.2, [.01, .01, .01])),
                "position": keys((0, [0, 0, 0]), (.2, [0, 2, 0]))}}),
        "recover": (.2, False, {"right_arm": {"rotation": keys((0, [-30, 0, -8]), (.2, [-3, 0, -5]))},
                "left_arm": {"rotation": keys((0, [-30, 0, 8]), (.2, [-3, 0, 5]))}})
    }
    return {"format_version": "1.8.0", "animations": {"animation.hierophant_green." + name:
            {"animation_length": length, "loop": loop, "bones": bones} for name, (length, loop, bones) in clips.items()}}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(export.tidy(value), ensure_ascii=False, indent=2) + "\n")


def main():
    model = geometry()
    import sys
    sys.path.insert(0, str(ROOT / 'art/part_three'))
    import motion
    animations = motion.hierophant(animation_data(), model.sculpture.bones)
    runtime = {"format_version": "1.12.0", "minecraft:geometry": [{"description": {
        "identifier": "geometry.hierophant_green", "texture_width": SIZE, "texture_height": SIZE,
        "visible_bounds_width": 2.4, "visible_bounds_height": 2.8, "visible_bounds_offset": [0, 1, 0]},
        "bones": model.sculpture.bones}]}
    texture = ASSETS / "textures/entity/hierophant_green.png"
    texture.parent.mkdir(parents=True, exist_ok=True)
    model.atlas.save(texture)
    model.atlas.save(HERE / "hierophant_green.png")
    geo = ASSETS / "geckolib/models/entity/hierophant_green.geo.json"
    anim = ASSETS / "geckolib/animations/entity/hierophant_green.animation.json"
    write_json(geo, runtime)
    write_json(anim, animations)
    project = export.blockbench(model.sculpture, animations, texture.read_bytes(), SIZE)
    project.update(name="Hierophant Green", model_identifier="hierophant_green", credit="Original geometry, atlas and six animations by JoJo's Mod. Character by Hirohiko Araki.")
    project["textures"][0].update(name="hierophant_green.png", path="hierophant_green.png")
    write_json(HERE / "hierophant_green.bbmodel", project)
    core = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(core)
    draw.ellipse((3, 3, 28, 28), fill=(10, 43, 34), outline=(204, 230, 172), width=2)
    draw.polygon([(16, 5), (26, 15), (16, 27), (6, 15)], fill=(28, 145, 94), outline=(120, 244, 177), width=1)
    draw.polygon([(16, 7), (16, 24), (9, 15)], fill=(70, 217, 147))
    draw.line((10, 14, 22, 14), fill=(226, 250, 199), width=1)
    draw.line((16, 8, 16, 23), fill=(226, 250, 199), width=1)
    core_path = ASSETS / "textures/item/hierophant_green_core.png"
    core.save(core_path)
    write_json(ASSETS / "items/hierophant_green_core.json", {"model": {"type": "minecraft:model", "model": "jojosmod:item/hierophant_green_core"}})
    write_json(ASSETS / "models/item/hierophant_green_core.json", {"parent": "minecraft:item/generated", "textures": {"layer0": "jojosmod:item/hierophant_green_core"}})
    files = [geo, anim, texture, core_path]
    manifest = {"version": "0.12.1", "license": "MIT for original resources and generator; character rights remain with their owners",
        "authorship": "Original player-proportioned geometry, hand-painted procedural jade lattice atlas, six keyframed clips and core sprite. No imported model, texture, animation or recording inputs.",
        "export_utilities": "../star_platinum/model_export.py (project MIT conversion helpers only)",
        "source": "hierophant_green.bbmodel", "cubes": len(model.sculpture.sources), "bones": len(model.sculpture.bones),
        "texture": [SIZE, SIZE], "emerald_pulse_times_seconds": [.3, .4, .5],
        "files": {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in files}}
    write_json(HERE / "asset-provenance.json", manifest)
    print(f"Hierophant Green: {manifest['cubes']} cubes, {manifest['bones']} bones, {len(animations['animations'])} animations")


if __name__ == "__main__": main()
