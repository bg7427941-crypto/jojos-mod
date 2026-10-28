#!/usr/bin/env python3
"""Build an original player-proportioned Hierophant Green, and its six clips. Inventory sprites live in art/items.

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
SIZE = 512
spec = importlib.util.spec_from_file_location("model_export", ROOT / "art/star_platinum/model_export.py")
export = importlib.util.module_from_spec(spec)
spec.loader.exec_module(export)
export.stable_id = lambda name: str(uuid.uuid5(uuid.NAMESPACE_URL, "jojosmod/hierophant-green/v2/" + name))

PALETTE = {"web": (27, 127, 83), "plate": (25, 147, 101), "ivory": (223, 231, 216),
           "dark": (10, 44, 38), "gem": (82, 224, 157), "gold": (248, 218, 68)}


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
                vein = material == "web" and ((x + y) % 10 < 2 or (x - y) % 10 < 2)
                base = (204, 229, 203) if vein else color
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
    # Player-based humanoid proportions with a lithe segmented silhouette.
    m.cube("pelvis", "body", [-3.1, 11.3, -1.7], [6.2, 2.4, 3.4])
    m.cube("abdomen", "torso", [-2.7, 13.5, -1.65], [5.4, 4.2, 3.3])
    m.cube("ribcage", "torso", [-3.7, 17.6, -2.05], [7.4, 6.0, 4.1])
    m.cube("neck", "head", [-1.2, 23.6, -1.2], [2.4, 2.4, 2.4], "ivory")
    # White segmented mask and emerald melon-patterned crown; no human face underneath.
    m.cube("cranium", "head", [-2.75, 26, -2.05], [5.5, 5.7, 4.6])
    m.cube("crown_cap", "head", [-2.2, 31.3, -1.5], [4.4, .8, 3.4])
    m.cube("mask_center", "head", [-1.45, 26.3, -2.65], [2.9, 4.1, 1.0], "ivory")
    m.cube("upper_brow", "head", [-2.4, 30.1, -2.55], [4.8, .55, .8], "ivory")
    m.cube("nose_keel", "head", [-.4, 27.25, -3.1], [.8, 2.3, .65], "ivory")
    m.cube("lower_mask", "head", [-1.65, 25.7, -2.7], [3.3, 1.3, .7], "ivory")
    m.cube("mouth_seam", "head", [-.95, 26.5, -3.04], [1.9, .13, .12], "dark")
    for side, prefix in [(-1, "right"), (1, "left")]:
        m.cube(prefix+"_face_ridge", "head", [side*1.85-.5, 26.8, -2.8], [1, 3.8, .65], "ivory", [0, 0, side*-9])
        m.cube(prefix+"_eye_recess", "head", [side*1.3-.6, 29.0, -3.08], [1.2, 1, .3], "dark", [0, 0, side*12])
        m.cube(prefix+"_yellow_eye", "head", [side*1.3-.41, 29.18, -3.29], [.82, .56, .2], "gold", [0, 0, side*12])
        for stripe in range(3):
            m.cube(f"{prefix}_eye_slit_{stripe}", "head", [side*1.3-.43+stripe*.3, 29.15, -3.42], [.08, .63, .05], "dark", [0, 0, side*12])
        m.cube(prefix+"_temple", "head", [side*2.6-.45, 26.7, -.9], [.9, 3.7, 2.6], "ivory")
        m.cube(prefix+"_ear_center", "head", [side*3-.3, 27.6, -.25], [.6, 1.8, 1.2], "dark")
        m.cube(prefix+"_ear_plate", "head", [side*3.15-.2, 27.8, -.15], [.4, 1.4, 1], "ivory")
        # Pale wrapping bands cross the torso instead of a generic armored breastplate.
        for band in range(3):
            m.cube(f"{prefix}_pectoral_band_{band}", "torso", [side*1.8-1.7, 18.5+band*1.35, -2.21], [3.4, .52, .28], "ivory", [0, 0, side*-18])
        m.cube(prefix+"_flank_band", "torso", [side*2.65-.26, 14.4, -1.87], [.52, 3.2, .25], "ivory", [0, 0, side*12])
        m.cube(prefix+"_hip_band", "body", [side*2.15-.3, 11.25, -1.92], [.6, 2.1, .3], "ivory", [0, 0, side*-28])
        x=side*4.9
        m.cube(prefix+"_deltoid", prefix+"_arm", [x-1.65, 21.7, -1.8], [3.3, 2.8, 3.6])
        m.cube(prefix+"_shoulder_band", prefix+"_arm", [x-1.75, 22.6, -1.95], [3.5, .62, 3.9], "ivory")
        m.cube(prefix+"_biceps", prefix+"_arm", [x-1.35, 18, -1.55], [2.7, 4.25, 3.1])
        m.cube(prefix+"_elbow_joint", prefix+"_forearm", [x-1.2, 17.45, -1.5], [2.4, 1.0, 3], "ivory")
        m.cube(prefix+"_forearm", prefix+"_forearm", [x-1.35, 13.1, -1.65], [2.7, 4.5, 3.3])
        for band in range(2):
            m.cube(f"{prefix}_forearm_band_{band}", prefix+"_forearm", [x-1.45, 14.1+band*1.6, -1.75], [2.9, .4, 3.5], "ivory")
        m.cube(prefix+"_wrist", prefix+"_hand", [x-1.25, 12.35, -1.45], [2.5, .9, 2.9], "ivory")
        m.cube(prefix+"_palm", prefix+"_hand", [x-1.25, 10.7, -1.25], [2.5, 1.8, 2.5])
        for finger in range(4):
            m.cube(f"{prefix}_finger_{finger}", prefix+"_hand", [x-1.2+finger*.64, 8.95, -.8], [.48, 1.85, 1.4], "ivory")
        m.cube(prefix+"_thumb", prefix+"_hand", [x+side*1.25-.3, 10.3, -.8], [.6, 1.6, 1.5], "ivory", [0, 0, side*20])
        lx=side*1.8
        m.cube(prefix+"_thigh", prefix+"_leg", [lx-1.5, 6.2, -1.7], [3, 6.1, 3.4])
        m.cube(prefix+"_thigh_band", prefix+"_leg", [lx-1.6, 10.1, -1.8], [3.2, .55, 3.6], "ivory")
        m.cube(prefix+"_knee", prefix+"_shin", [lx-1.4, 5.8, -1.9], [2.8, 1.1, 3.5], "ivory")
        m.cube(prefix+"_calf", prefix+"_shin", [lx-1.3, 1.15, -1.55], [2.6, 4.8, 3.1])
        m.cube(prefix+"_ankle", prefix+"_shin", [lx-1.4, 1, -1.65], [2.8, .55, 3.3], "ivory")
        m.cube(prefix+"_foot", prefix+"_shin", [lx-1.45, 0, -2.3], [2.9, 1.2, 4], "ivory")
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
    files = [geo, anim, texture]
    manifest = {"version": "0.13.0", "license": "MIT for original resources and generator; character rights remain with their owners",
        "visual_references": ["https://jojowiki.com/File:Hierophant_Green_anime_ref_(3).jpg", "https://jojowiki.com/File:US_BD_Set_2_MS_20.png"],
        "authorship": "Original rebuilt player-based silhouette with segmented white mask, yellow slotted eyes, emerald lattice and wrapping bands; hand-painted procedural jade lattice atlas, six keyframed clips. Inventory sprites are maintained separately in art/items. No imported model, texture, animation or recording inputs.",
        "export_utilities": "../star_platinum/model_export.py (project MIT conversion helpers only)",
        "item_sprites": "art/items/asset-provenance.json",
        "source": "hierophant_green.bbmodel", "cubes": len(model.sculpture.sources), "bones": len(model.sculpture.bones),
        "texture": [SIZE, SIZE], "emerald_pulse_times_seconds": [.3, .4, .5],
        "files": {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in files}}
    write_json(HERE / "asset-provenance.json", manifest)
    print(f"Hierophant Green: {manifest['cubes']} cubes, {manifest['bones']} bones, {len(animations['animations'])} animations")


if __name__ == "__main__": main()
