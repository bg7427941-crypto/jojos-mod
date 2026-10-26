#!/usr/bin/env python3
"""Author the original Star Platinum cuboid rig, atlas and Blockbench project.

All shapes, UVs and pixels originate here. This script reads no model, texture,
animation or audio from another project. Python 3 + Pillow are sufficient.
Run from the repository root: python art/star_platinum/build_original.py
"""

import base64
import hashlib
import json
import math
from pathlib import Path
import random
import uuid
from PIL import Image, ImageDraw
from original_animations import build_animations, audit_animations

ROOT = Path(__file__).resolve().parents[2]
ART = ROOT / "art/star_platinum"
ASSETS = ROOT / "src/main/resources/assets/jojosmod"
ATLAS_SIZE = 256
DENSITY = 2
MATERIALS = {
    "skin": (140, 132, 194), "shadow_skin": (91, 79, 143),
    "hair": (25, 23, 37), "gold": (216, 170, 66),
    "pale_gold": (239, 211, 132), "ivory": (222, 220, 206),
    "dark": (31, 31, 43), "red": (164, 47, 57),
    "silver": (181, 203, 208),
}


def stable_id(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "jojosmod/original-star-platinum/v1/" + name))


def tidy(value):
    if isinstance(value, float): return round(value, 6) if value else 0
    if isinstance(value, list): return [tidy(x) for x in value]
    if isinstance(value, dict): return {k: tidy(v) for k, v in value.items()}
    return value


class Sculpture:
    def __init__(self):
        self.bones = []
        self.lookup = {}
        self.sources = []

    def bone(self, name, pivot, parent=None):
        entry = {"name": name, "pivot": pivot}
        if parent: entry["parent"] = parent
        self.bones.append(entry)
        self.lookup[name] = entry

    def box(self, bone, name, origin, size, material="skin", rotation=None, pivot=None, pattern="plain"):
        cube = {"origin": origin, "size": size}
        if rotation:
            # GeckoLib reflects geometry X, then negates Bedrock X/Y rotations.
            # Conjugating that transform back into our sculpting coordinates
            # makes JSON angles (-X,+Y,-Z) for RH/Y-up authored angles.
            cube["rotation"] = [-rotation[0], rotation[1], -rotation[2]]
            cube["pivot"] = pivot or [origin[i] + size[i] / 2 for i in range(3)]
        self.lookup[bone].setdefault("cubes", []).append(cube)
        self.sources.append({"name": name, "bone": bone, "material": material, "pattern": pattern, "cube": cube})


def sculpt():
    s = Sculpture()
    s.bone("root", [0, 0, 0])
    s.bone("hover", [0, 0, 0], "root")
    s.bone("pelvis", [0, 13, 0], "hover")
    s.bone("waist", [0, 14, 0], "pelvis")
    s.bone("chest", [0, 19, 0], "waist")
    s.bone("neck", [0, 25, 0], "chest")
    s.bone("head", [0, 25.1, 0], "neck")
    s.bone("hair", [0, 30.8, 0], "head")
    s.bone("scarf", [0, 25, 0], "neck")
    for side, sign in [("left", 1), ("right", -1)]:
        s.bone("scarf_tail_" + side, [sign * 1.8, 24.7, 2], "scarf")
        s.bone(side + "_upper_arm", [sign * 5.8, 23.4, 0], "chest")
        s.bone(side + "_forearm", [sign * 6.2, 18.6, 0], side + "_upper_arm")
        s.bone(side + "_hand", [sign * 6.2, 14.7, 0], side + "_forearm")
        s.bone(side + "_thigh", [sign * 2.2, 13.3, 0], "pelvis")
        s.bone(side + "_shin", [sign * 2.2, 7.1, 0], side + "_thigh")
        s.bone(side + "_foot", [sign * 2.2, 1.55, -.5], side + "_shin")
    s.bone("right_finger_index", [-6.85, 13, -.45], "right_hand")
    s.bone("right_finger_middle", [-5.95, 13, -.45], "right_hand")
    for part, pivot in [("front", [0, 13.7, -2]), ("back", [0, 13.7, 2]),
                        ("left", [2.4, 13.2, 0]), ("right", [-2.4, 13.2, 0])]:
        s.bone("loin_" + part, pivot, "pelvis")

    # Tapered anatomy is sculpted from overlapping inset volumes, rather than a rectangular torso.
    s.box("pelvis", "hip_core", [-3.45, 12.2, -1.8], [6.9, 2.45, 3.9])
    s.box("waist", "abdomen_core", [-2.8, 14, -1.85], [5.6, 5.3, 3.8], pattern="abdominal")
    s.box("chest", "rib_cage", [-4.4, 18.25, -1.8], [8.8, 6.2, 4.25], pattern="rib")
    s.box("chest", "upper_trapezius", [-3.7, 23.5, -1.35], [7.4, 1.4, 3.3])
    s.box("neck", "neck_column", [-1.75, 24.3, -1.55], [3.5, 2.35, 3.2], "shadow_skin")
    for side, sign in [("left", 1), ("right", -1)]:
        x = .25 if sign > 0 else -4.65
        s.box("chest", side + "_pectoral", [x, 20.4, -2.6], [4.4, 3.65, 1.5],
              rotation=[0, 0, sign * 3.5], pivot=[sign * 2.35, 22.2, -1.2], pattern="pectoral_" + side)
        s.box("chest", side + "_pectoral_lower", [x + .2, 19.9, -2.4], [4.0, .75, 1.0])
        s.box("waist", side + "_oblique", [sign * 2.75 - .5, 15, -1.65], [1.2, 4.1, 2.9],
              rotation=[0, 0, -sign * 8], pattern="wave_" + side)
        s.box("chest", side + "_scapula", [sign * 2.25 - 1.85, 20.5, 1.9], [3.7, 3.4, 1.0],
              rotation=[0, 0, -sign * 4], pattern="muscle")
        for row in range(3):
            s.box("waist", f"{side}_ab_{row}", [(.15 if sign > 0 else -2.45) + row * sign * -.05,
                  18.65 - row * 1.5, -2.22 + row * .09], [2.3, 1.35, .63], pattern="muscle")

    # A shaped jaw and painted brow/eyes keep the face human at inventory-camera distances.
    s.box("head", "skull", [-3.9, 25.7, -3.45], [7.8, 6.9, 6.9], pattern="face")
    s.box("head", "jaw", [-3.3, 25.4, -3.6], [6.6, 2.0, 5.95], "shadow_skin", pattern="jaw")
    s.box("head", "nose", [-.36, 27.85, -4.08], [.72, 1.55, .72], pattern="nose")
    for sign in [-1, 1]:
        s.box("head", "cheek_" + str(sign), [sign * 2.7 - .55, 27.25, -3.66], [1.1, 2.6, .32],
              "shadow_skin", rotation=[0, 0, -sign * 8])
        s.box("head", "ear_" + str(sign), [sign * 4.0 - .4, 28.2, -.95], [.8, 2.05, 1.4], "shadow_skin")
    s.box("head", "crown_front", [-4.0, 31.05, -3.78], [8, .85, .43], "gold", pattern="crown")
    s.box("head", "crown_back", [-3.7, 31.05, 3.25], [7.4, .75, .5], "gold")
    for sign in [-1, 1]:
        s.box("head", "crown_side_" + str(sign), [sign * 3.84 - .22, 31.05, -3.45], [.44, .8, 6.9], "gold")
    s.box("head", "gem_setting", [-.9, 30.72, -4.06], [1.8, 1.65, .4], "dark")
    s.box("head", "silver_gem", [-.66, 30.87, -4.28], [1.32, 1.36, .35], "silver", pattern="gem")

    # Hair uses independently placed stepped locks: a crest, swept crown and hanging back layers.
    s.box("hair", "hair_cap", [-4.0, 30.8, -2.6], [8, 2.0, 6.4], "hair")
    s.box("hair", "hair_back_mass", [-4.0, 28.4, 2.15], [8, 3.5, 2.2], "hair")
    for band, z in enumerate([-2.0, .35, 2.55]):
        for column in range(5):
            x = (column - 2) * 1.65
            height = 2.4 + (2 - abs(column - 2)) * .48 + (band % 2) * .35
            base_y = 31.7 + (1 if band == 1 else .2)
            pivot = [x, base_y, z]
            rotation = [12 + band * 9, (column - 2) * 4, -(column - 2) * 9]
            s.box("hair", f"crest_{band}_{column}", [x - .94, base_y - .15, z - .95],
                  [1.88, height, 2.05], "hair", rotation, pivot, "lock")
            s.box("hair", f"crest_tip_{band}_{column}", [x - .55, base_y + height - .3, z - .5],
                  [1.1, 1.12, 1.15], "hair", rotation, pivot, "lock")
    for column in range(5):
        x = (column - 2) * 1.68
        s.box("hair", f"back_lock_{column}", [x - .84, 26.3 + abs(column - 2) * .38, 2.6],
              [1.68, 4.4, 1.7], "hair", [-22, 0, (column - 2) * 5], [x, 30.5, 2.8], "lock")
    for sign in [-1, 1]:
        s.box("hair", "side_lock_" + str(sign), [sign * 4.1 - .65, 27.45, .4],
              [1.3, 4.0, 2.25], "hair", [-15, 0, sign * 14], [sign * 4, 31, 1], "lock")

    s.box("scarf", "collar_wrap", [-3.0, 24.5, -2.55], [6, 1.5, 5.0], "red", pattern="cloth")
    s.box("scarf", "collar_fold", [-2.7, 24.15, -2.95], [5.4, 1.0, 1.15], "red", [-7, 0, 0], pattern="cloth")
    s.box("scarf", "rear_knot", [-1.1, 24.6, 2.1], [2.2, 1.4, 1.4], "red", [0, 10, 10], pattern="cloth")
    for side, sign in [("left", 1), ("right", -1)]:
        s.box("scarf_tail_" + side, side + "_scarf_tail", [sign * 2.0 - 1.0, 22.4, 2.3],
              [2, 2.65, .58], "red", [-26, sign * 12, sign * 18], [sign * 1.8, 24.7, 2], "cloth")
        s.box("scarf_tail_" + side, side + "_scarf_tip", [sign * 2.1 - .78, 21.65, 3.0],
              [1.56, 1.4, .55], "red", [-35, sign * 12, sign * 22], [sign * 1.8, 24.7, 2], "cloth")

    # Limbs have true elbow/wrist/knee pivots; armor and knuckles are separately sculpted volumes.
    for side, sign in [("left", 1), ("right", -1)]:
        arm, forearm, hand = [side + suffix for suffix in ["_upper_arm", "_forearm", "_hand"]]
        center = sign * 6.2
        s.box(arm, side + "_deltoid", [center - 1.7, 20.5, -1.8], [3.4, 3.4, 3.8],
              rotation=[0, 0, -sign * 5], pattern="wave_" + side)
        s.box(arm, side + "_bicep", [center - 1.5, 18.4, -1.75], [3, 3.4, 3.4], pattern="muscle")
        s.box(arm, side + "_bicep_front", [center - 1.25, 19.0, -2.04], [2.5, 2.8, .7], pattern="muscle")
        shoulder_pivot = [sign * 5.8, 23.4, 0]
        s.box(arm, side + "_pauldron_rim", [center - 2.15, 23.0, -2.6], [4.3, 1.8, 5.25],
              "gold", [0, 0, -sign * 12], shoulder_pivot, "armor")
        s.box(arm, side + "_pauldron_top", [center - 1.8, 24.05, -2.35], [3.6, .9, 4.7],
              "pale_gold", [0, 0, -sign * 12], shoulder_pivot, "spiral")
        s.box(arm, side + "_pauldron_inlay", [center - 1.3, 24.6, -1.85], [2.6, .48, 3.7],
              "ivory", [0, 0, -sign * 12], shoulder_pivot, "spiral")
        s.box(arm, side + "_pauldron_front", [center - 1.95, 23.05, -2.98], [3.9, 1.95, .55],
              "gold", [0, 0, -sign * 12], shoulder_pivot, "spiral")
        s.box(forearm, side + "_elbow", [center - 1.2, 17.65, -1.5], [2.4, 1.35, 3.0], "shadow_skin")
        s.box(forearm, side + "_forearm", [center - 1.5, 15.2, -1.75], [3, 3.25, 3.5], pattern="wave_" + side)
        s.box(forearm, side + "_forearm_peak", [center - 1.2, 16.1, -2.06], [2.4, 1.9, .65], pattern="muscle")
        s.box(forearm, side + "_wrist_band", [center - 1.57, 14.55, -1.8], [3.14, .68, 3.6], "gold")
        s.box(hand, side + "_gauntlet", [center - 1.48, 12.5, -1.62], [2.96, 2.15, 3.24], "dark", pattern="glove")
        s.box(hand, side + "_fist", [center - 1.63, 11.6, -1.72], [3.26, 1.5, 3.42], "dark", pattern="glove")
        for digit in range(4):
            s.box(hand, f"{side}_knuckle_{digit}", [center - 1.44 + digit * .75, 12.5, -1.96],
                  [.55, .58, .35], "pale_gold")
        s.box(hand, side + "_thumb", [center - sign * 1.1 - .55, 12.05, -2.25], [1.1, 1.35, 1.1],
              "dark", [0, 0, sign * 18])
        thigh, shin, foot = [side + suffix for suffix in ["_thigh", "_shin", "_foot"]]
        leg_x = sign * 2.2
        s.box(thigh, side + "_thigh", [leg_x - 1.72, 7.3, -1.63], [3.44, 5.8, 3.45], pattern="wave_" + side)
        s.box(thigh, side + "_quadriceps", [leg_x - 1.4, 8.5, -1.98], [2.8, 3.6, .8], pattern="muscle")
        s.box(shin, side + "_knee_rim", [leg_x - 1.5, 6.3, -2.03], [3, 1.95, 1.1], "gold")
        s.box(shin, side + "_white_kneepad", [leg_x - 1.22, 6.48, -2.24], [2.44, 1.55, 1.0], "ivory", pattern="gem")
        s.box(shin, side + "_boot_calf", [leg_x - 1.32, 1.7, -1.4], [2.64, 4.9, 2.85], "dark", pattern="boot")
        s.box(shin, side + "_boot_instep", [leg_x - 1.5, 4.3, -1.53], [3, 1.65, 3.0], "dark", pattern="boot")
        s.box(foot, side + "_boot_sole", [leg_x - 1.58, .35, -2.65], [3.16, .6, 4.6], "dark")
        s.box(foot, side + "_boot_toe", [leg_x - 1.48, .85, -2.5], [2.96, 1.15, 4.15], "dark", pattern="boot")
        s.box(foot, side + "_toe_gold", [leg_x - 1.49, .65, -2.68], [2.98, .34, .3], "gold")
        s.box(foot, side + "_ankle_gold", [leg_x - 1.4, 1.8, -1.53], [2.8, .44, 3.1], "gold")

    s.box("pelvis", "belt", [-3.65, 13.1, -2.03], [7.3, .85, 4.2], "ivory", pattern="cloth")
    for sign in [-1, 1]:
        s.box("pelvis", "belt_clasp_" + str(sign), [sign * 3.2 - .55, 12.8, -2.25], [1.1, 1.3, .62], "gold", pattern="armor")
    s.box("loin_front", "front_loin_upper", [-2.65, 10.8, -2.27], [5.3, 2.7, .6], "ivory", [-8, 0, 0], [0, 13.7, -2], "cloth")
    s.box("loin_front", "front_loin_tip", [-2.2, 9.7, -2.6], [4.4, 1.6, .55], "ivory", [-10, 0, 0], [0, 13.7, -2], "cloth")
    s.box("loin_back", "back_loin", [-2.5, 10.0, 1.83], [5, 3.5, .55], "ivory", [8, 0, 0], [0, 13.7, 2], "cloth")
    for side, sign in [("left", 1), ("right", -1)]:
        s.box("loin_" + side, side + "_loin_panel", [sign * 3.05 - .27, 10.65, -1.75], [.54, 2.6, 3.6],
              "ivory", [0, 0, sign * 15], [sign * 2.4, 13.2, 0], "cloth")
    for bone, x in [("right_finger_index", -6.85), ("right_finger_middle", -5.95)]:
        s.box(bone, bone + "_extension", [x - .3, 12, -.75], [.6, 1, .6], "shadow_skin", pattern="finger")
    return s


def color_scale(color, multiplier):
    return tuple(max(0, min(255, round(c * multiplier))) for c in color)


def paint(material, face, width, height, pattern):
    """Author a small pixel-art surface from material, anatomy and edge lighting."""
    base = MATERIALS[material]
    face_light = {"north": 1.03, "south": .77, "east": .88, "west": .93, "up": 1.12, "down": .65}[face]
    seed = hashlib.sha256(f"{material}/{face}/{width}/{height}/{pattern}".encode()).digest()
    rng = random.Random(int.from_bytes(seed[:8], "big"))
    image = Image.new("RGBA", (width, height))
    pixels = image.load()
    for y in range(height):
        for x in range(width):
            edge = min(x, y, width - 1 - x, height - 1 - y)
            bevel = .87 if edge == 0 and min(width, height) > 3 else 1
            lift = .09 * (1 - y / max(1, height - 1)) + .03 * (1 - x / max(1, width - 1))
            if pattern == "lock": lift += .10 if x == max(1, width // 3) else 0
            if pattern in {"cloth", "glove", "boot"}: lift += .035 * math.sin(x * 1.4 + y * .35)
            if pattern in {"muscle", "pectoral_left", "pectoral_right", "abdominal", "rib"}:
                lift += .055 * math.sin(math.pi * (x + .5) / width) * math.sin(math.pi * (y + .5) / height)
            pixels[x, y] = (*color_scale(base, (face_light + lift + rng.uniform(-.012, .012)) * bevel), 255)
    draw = ImageDraw.Draw(image)
    if pattern.startswith("wave_") and face in {"north", "south", "east", "west"} and width > 2:
        points = [(round((.2 if pattern.endswith("left") else .8) * (width - 1)
                         + math.sin(y * 5 / max(1, height - 1)) * width * .15), y) for y in range(height)]
        draw.line(points, fill=MATERIALS["gold"], width=max(1, width // 10))
    if pattern.startswith("pectoral_") and face == "north":
        x = width - 2 if pattern.endswith("left") else 1
        draw.line([(x, 0), (x + (-1 if x > width / 2 else 1), height // 2), (x, height - 1)], fill=MATERIALS["gold"], width=1)
    if pattern == "spiral" and width > 3 and height > 3:
        # A newly drawn stepped ridge motif inspired by the canon's ridged pauldrons.
        draw.rectangle([1, 1, width - 2, height - 2], outline=color_scale(MATERIALS["gold"], .9))
        if width > 7 and height > 5:
            draw.line([(2, height - 3), (2, 2), (width - 3, 2), (width - 3, height - 3),
                       (width // 2, height - 3), (width // 2, height // 2)], fill=MATERIALS["ivory"], width=1)
    if pattern == "gem" and width > 3 and height > 3:
        draw.line([(1, height - 2), (1, 1), (width - 2, 1)], fill=(239, 248, 237, 255))
    if pattern == "crown" and width > 5:
        for x in range(3, width - 2, 5): draw.point((x, height // 2), fill=(106, 76, 29, 255))
    if pattern == "jaw" and face == "north" and width > 6:
        draw.line([(width // 2 - 2, 1), (width // 2 + 2, 1)], fill=(40, 31, 62, 255))
        draw.point((width // 2 - 3, min(2, height - 1)), fill=(59, 43, 89, 255))
        draw.point((width // 2 + 3, min(2, height - 1)), fill=(59, 43, 89, 255))
    if pattern == "face" and face == "north":
        def rect(x0, y0, x1, y1, color):
            draw.rectangle([round(x0 * (width - 1) / 15), round(y0 * (height - 1) / 13),
                            round(x1 * (width - 1) / 15), round(y1 * (height - 1) / 13)], fill=color)
        shadow = (73, 61, 117, 255)
        rect(0, 5, 1, 12, shadow); rect(14, 5, 15, 12, shadow)
        rect(2, 4, 6, 4, (25, 22, 40, 255)); rect(9, 4, 13, 4, (25, 22, 40, 255))
        rect(2, 5, 5, 6, (165, 228, 233, 255)); rect(10, 5, 13, 6, (165, 228, 233, 255))
        rect(4, 5, 4, 6, (37, 91, 113, 255)); rect(11, 5, 11, 6, (37, 91, 113, 255))
        rect(3, 5, 3, 5, (228, 255, 250, 255)); rect(10, 5, 10, 5, (228, 255, 250, 255))
        rect(5, 5, 6, 5, (34, 25, 51, 255)); rect(9, 5, 10, 5, (34, 25, 51, 255))
        rect(6, 10, 9, 10, (46, 38, 69, 255)); rect(5, 11, 5, 11, shadow); rect(10, 11, 10, 11, shadow)
        rect(6, 12, 9, 12, (174, 154, 205, 255))
    return image


def texture(sculpture):
    surfaces = {}
    assignments = []
    for record in sculpture.sources:
        w, h, d = record["cube"]["size"]
        for face, extent in {"north": (w, h), "south": (w, h), "east": (d, h),
                             "west": (d, h), "up": (w, d), "down": (w, d)}.items():
            width, height = [max(1, round(value * DENSITY)) for value in extent]
            pattern = record["pattern"]
            key = (record["material"], face, width, height, pattern)
            surfaces.setdefault(key, paint(*key[:2], width, height, pattern))
            assignments.append((record, face, key))
    canvas = Image.new("RGBA", (ATLAS_SIZE, ATLAS_SIZE))
    positions, x, y, shelf = {}, 0, 0, 0
    for key in sorted(surfaces, key=lambda k: (-k[3], -k[2], k)):
        tile = surfaces[key]; width, height = tile.size
        if x + width + 2 > ATLAS_SIZE: x, y, shelf = 0, y + shelf, 0
        if y + height + 2 > ATLAS_SIZE: raise RuntimeError("Original UV atlas overflow")
        canvas.paste(tile, (x + 1, y + 1))
        # A one-pixel bleed gutter prevents neighboring motifs leaking through mipmaps.
        canvas.paste(tile.crop((0, 0, width, 1)), (x + 1, y))
        canvas.paste(tile.crop((0, height - 1, width, height)), (x + 1, y + height + 1))
        canvas.paste(tile.crop((0, 0, 1, height)), (x, y + 1))
        canvas.paste(tile.crop((width - 1, 0, width, height)), (x + width + 1, y + 1))
        positions[key] = [x + 1, y + 1]
        x += width + 2; shelf = max(shelf, height + 2)
    uv_records = []
    for record, face, key in assignments:
        uv = {"uv": positions[key], "uv_size": list(surfaces[key].size)}
        record["cube"].setdefault("uv", {})[face] = uv
        uv_records.append({"cube": record["name"], "face": face, **uv})
    return canvas, uv_records


def blockbench(sculpture, animation_data, png):
    groups, elements = {}, []
    for bone in sculpture.bones:
        p = bone["pivot"]
        groups[bone["name"]] = {"name": bone["name"], "uuid": stable_id("bone/" + bone["name"]),
                                "origin": [-p[0], p[1], p[2]], "export": True, "visibility": True, "children": []}
    for record in sculpture.sources:
        c = record["cube"]; x, y, z = c["origin"]; w, h, d = c["size"]
        p = c.get("pivot", [x + w / 2, y + h / 2, z + d / 2])
        uid = stable_id("cube/" + record["name"])
        faces = {}
        for face, uv in c["uv"].items():
            u, v = uv["uv"]; du, dv = uv["uv_size"]
            target = face
            # Bedrock's top/bottom UV orientation reverses both rectangle edges.
            rectangle = [u + du, v + dv, u, v] if face in {"up", "down"} else [u, v, u + du, v + dv]
            faces[target] = {"uv": rectangle, "texture": 0}
        element = {"name": record["name"], "type": "cube", "uuid": uid, "box_uv": False,
                   "from": [-x - w, y, z], "to": [-x, y + h, z + d],
                   "origin": [-p[0], p[1], p[2]], "faces": faces, "export": True}
        if c.get("rotation"):
            r = c["rotation"]; element["rotation"] = [-r[0], -r[1], r[2]]
        elements.append(element)
        groups[record["bone"]]["children"].append(uid)
    roots = []
    for bone in sculpture.bones:
        if bone.get("parent"): groups[bone["parent"]]["children"].append(groups[bone["name"]])
        else: roots.append(groups[bone["name"]])
    animations = []
    for name, clip in animation_data["animations"].items():
        animators = {}
        for bone, channels in clip["bones"].items():
            frames = []
            for channel, keys in channels.items():
                if isinstance(keys, list): keys = {"0": keys}
                for timestamp, value in keys.items():
                    values = [-value[0], -value[1], value[2]] if channel == "rotation" else (
                        [-value[0], value[1], value[2]] if channel == "position" else value)
                    frames.append({"channel": channel, "data_points": [dict(zip("xyz", map(str, values)))],
                                   "uuid": stable_id(f"key/{name}/{bone}/{channel}/{timestamp}"),
                                   "time": float(timestamp), "interpolation": "linear"})
            animators[groups[bone]["uuid"]] = {"name": bone, "type": "bone", "keyframes": frames}
        animations.append({"name": name, "uuid": stable_id("animation/" + name),
                           "loop": "loop" if clip.get("loop") else "once", "override": False,
                           "length": clip["animation_length"], "animators": animators, "snapping": 20})
    return {"meta": {"format_version": "4.10", "model_format": "geckolib_model", "box_uv": False},
            "name": "Star Platinum — original Stardust rig", "model_identifier": "star_platinum",
            "credit": "Original geometry, UVs, texture and animation for JoJo's Mod; character by Hirohiko Araki.",
            "resolution": {"width": ATLAS_SIZE, "height": ATLAS_SIZE}, "elements": elements, "outliner": roots,
            "textures": [{"name": "star_platinum.png", "id": "0", "uuid": stable_id("texture"),
                          "path": "star_platinum.png", "width": ATLAS_SIZE, "height": ATLAS_SIZE,
                          "uv_width": ATLAS_SIZE, "uv_height": ATLAS_SIZE,
                          "source": "data:image/png;base64," + base64.b64encode(png).decode()}], "animations": animations}


def main():
    sculpture = sculpt()
    atlas, uvs = texture(sculpture)
    animation_data = build_animations()
    audit_animations(animation_data)
    names = set(sculpture.lookup)
    for clip in animation_data["animations"].values():
        assert set(clip["bones"]) <= names, set(clip["bones"]) - names
    geometry = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.star_platinum", "texture_width": ATLAS_SIZE,
                        "texture_height": ATLAS_SIZE, "visible_bounds_width": 13,
                        "visible_bounds_height": 4, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": sculpture.bones}]}
    texture_path = ASSETS / "textures/entity/star_platinum.png"
    atlas.save(texture_path, optimize=False)
    png = texture_path.read_bytes()
    project = blockbench(sculpture, animation_data, png)
    for path, data in [(ASSETS / "geckolib/models/entity/star_platinum.geo.json", geometry),
                       (ASSETS / "geckolib/animations/entity/star_platinum.animation.json", animation_data)]:
        path.write_text(json.dumps(tidy(data), indent=2) + "\n", encoding="utf-8")
    # The editable project is compact; the authored Python is the human-readable shape/pixel source.
    (ART / "star_platinum.bbmodel").write_text(json.dumps(tidy(project), separators=(",", ":"), ensure_ascii=False) + "\n", encoding="utf-8")
    (ART / "uv-layout.json").write_text(json.dumps(uvs, indent=2) + "\n", encoding="utf-8")
    manifest = {"author": "JoJo's Mod", "license": "MIT for original asset data and source; JoJo character rights retained by their holders",
                "method": "New cuboids, rig, UV allocation, procedural pixel art and authored numeric animation tracks. No imported model/texture/animation inputs.",
                "bones": len(sculpture.bones), "cubes": len(sculpture.sources), "atlas": [ATLAS_SIZE, ATLAS_SIZE],
                "animations": len(animation_data["animations"]),
                "texture_sha256": hashlib.sha256(png).hexdigest(),
                "geometry_sha256": hashlib.sha256((ASSETS / "geckolib/models/entity/star_platinum.geo.json").read_bytes()).hexdigest(),
                "animation_sha256": hashlib.sha256((ASSETS / "geckolib/animations/entity/star_platinum.animation.json").read_bytes()).hexdigest(),
                "model_source_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
                "animation_source_sha256": hashlib.sha256((ART / "original_animations.py").read_bytes()).hexdigest()}
    (ART / "original-provenance.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(manifest))


if __name__ == "__main__": main()
