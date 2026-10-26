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
    # Classic player bind pose: 8x8x8 head, 8x12x4 torso and 4x12x4 limbs.
    # Split the plain player volumes at elbows, wrists, knees and ankles so
    # the existing GeckoLib action rig retains its articulation.
    s.bone("root", [0, 0, 0])
    s.bone("hover", [0, 0, 0], "root")
    s.bone("pelvis", [0, 12, 0], "hover")
    s.bone("waist", [0, 12, 0], "pelvis")
    s.bone("chest", [0, 12, 0], "waist")
    s.bone("neck", [0, 24, 0], "chest")
    s.bone("head", [0, 24, 0], "neck")
    s.bone("hair", [0, 31, 0], "head")
    s.bone("scarf", [0, 24, 0], "neck")
    for side, sign in [("left", 1), ("right", -1)]:
        s.bone("scarf_tail_" + side, [sign * 1.7, 23.8, 2], "scarf")
        s.bone(side + "_upper_arm", [sign * 5, 22, 0], "chest")
        s.bone(side + "_forearm", [sign * 6, 18, 0], side + "_upper_arm")
        s.bone(side + "_hand", [sign * 6, 14, 0], side + "_forearm")
        s.bone(side + "_thigh", [sign * 2, 12, 0], "pelvis")
        s.bone(side + "_shin", [sign * 2, 6, 0], side + "_thigh")
        s.bone(side + "_foot", [sign * 2, 2, 0], side + "_shin")
    s.bone("right_finger_index", [-6.7, 13, -0.5], "right_hand")
    s.bone("right_finger_middle", [-5.3, 13, -0.5], "right_hand")
    for part, pivot in [("front", [0, 12, -2]), ("back", [0, 12, 2]),
                        ("left", [4, 12, 0]), ("right", [-4, 12, 0])]:
        s.bone("loin_" + part, pivot, "pelvis")

    # Rectangular player body; most musculature is painted rather than built
    # from a stack of disconnected small muscle cubes.
    s.box("chest", "player_body", [-4, 12, -2], [8, 12, 4], pattern="player_chest")
    s.box("neck", "neck_column", [-1.75, 23.7, -1.5], [3.5, 1, 3], "shadow_skin")
    for side, sign in [("left", 1), ("right", -1)]:
        x = .15 if sign > 0 else -3.9
        s.box("chest", side + "_chest_relief", [x, 20, -2.18], [3.75, 3.1, .3],
              pattern="pectoral_" + side)

    # Player-sized square head with facial detail on the texture.
    s.box("head", "player_head", [-4, 24, -4], [8, 8, 8], pattern="face")
    s.box("head", "nose", [-.35, 26.9, -4.24], [.7, 1.2, .3], pattern="nose")
    s.box("head", "crown_front", [-4.15, 29.8, -4.2], [8.3, .8, .35], "gold", pattern="crown")
    s.box("head", "crown_back", [-4.1, 29.8, 3.95], [8.2, .8, .3], "gold")
    for sign in [-1, 1]:
        s.box("head", "crown_side_" + str(sign), [sign * 4.05 - .15, 29.8, -4], [.3, .8, 8], "gold")
    s.box("head", "gem_setting", [-.9, 29.45, -4.4], [1.8, 1.55, .3], "dark")
    s.box("head", "silver_gem", [-.65, 29.6, -4.62], [1.3, 1.25, .3], "silver", pattern="gem")

    # Fewer, broader stepped locks keep the blocky player silhouette legible.
    s.box("hair", "hair_cap", [-4.15, 31.1, -3.8], [8.3, 1.25, 7.95], "hair")
    s.box("hair", "hair_back", [-4.15, 27, 3.55], [8.3, 4.5, 1.05], "hair", pattern="lock")
    for row, z in enumerate([-2.15, 1.0]):
        for column in range(4):
            x = (column - 1.5) * 2.05
            h = 1.75 + (1 if column in (1, 2) else .25) + row * .2
            p = [x, 31.8, z]
            s.box("hair", f"crest_{row}_{column}", [x - 1.05, 31.65, z - 1.1],
                  [2.1, h, 2.55], "hair", [12 + row * 14, 0, -(column - 1.5) * 8], p, "lock")
    for side, sign in [("left", 1), ("right", -1)]:
        s.box("hair", side + "_side_lock", [sign * 4.05 - .55, 26.7, 1.5],
              [1.1, 4.7, 2.55], "hair", [-12, 0, sign * 8], [sign * 4, 30.7, 2], "lock")
        s.box("hair", side + "_back_lock", [sign * 2.1 - 1.05, 25.8, 3.4],
              [2.1, 4.8, 1.45], "hair", [-16, 0, sign * 7], [sign * 2, 30.2, 3.5], "lock")

    s.box("scarf", "collar_wrap", [-3.05, 23.4, -2.45], [6.1, 1.25, 4.9], "red", pattern="cloth")
    s.box("scarf", "collar_fold", [-2.8, 23.05, -2.7], [5.6, .75, .65], "red", pattern="cloth")
    s.box("scarf", "rear_knot", [-1, 23.6, 2.05], [2, 1.3, 1.25], "red", [0, 8, 8], pattern="cloth")
    for side, sign in [("left", 1), ("right", -1)]:
        s.box("scarf_tail_" + side, side + "_scarf_tail", [sign * 1.9 - .95, 20.8, 2.3],
              [1.9, 3, .5], "red", [-24, sign * 10, sign * 15], [sign * 1.7, 23.8, 2], "cloth")

    for side, sign in [("left", 1), ("right", -1)]:
        arm, forearm, hand = [side + suffix for suffix in ["_upper_arm", "_forearm", "_hand"]]
        x = sign * 6
        s.box(arm, side + "_player_upper_arm", [x - 2, 18, -2], [4, 6, 4], pattern="wave_" + side)
        s.box(forearm, side + "_player_forearm", [x - 2, 14, -2], [4, 4, 4], pattern="wave_" + side)
        s.box(hand, side + "_player_hand", [x - 2, 12, -2], [4, 2, 4], "dark", pattern="glove")
        shoulder = [sign * 5, 22, 0]
        s.box(arm, side + "_pauldron", [x - 2.3, 22.5, -2.55], [4.6, 1.8, 5.1],
              "gold", [0, 0, -sign * 7], shoulder, "armor")
        s.box(arm, side + "_pauldron_top", [x - 1.95, 23.65, -2.25], [3.9, .7, 4.5],
              "pale_gold", [0, 0, -sign * 7], shoulder, "spiral")
        s.box(arm, side + "_pauldron_front", [x - 2.05, 22.6, -2.85], [4.1, 1.8, .4],
              "gold", [0, 0, -sign * 7], shoulder, "spiral")
        s.box(forearm, side + "_gauntlet", [x - 2.08, 13.95, -2.08], [4.16, 2.1, 4.16],
              "dark", pattern="glove")
        s.box(forearm, side + "_wrist_band", [x - 2.12, 15.65, -2.12], [4.24, .45, 4.24], "gold")
        for digit in range(4):
            s.box(hand, f"{side}_knuckle_{digit}", [x - 1.8 + digit * .95, 12.7, -2.2],
                  [.7, .65, .3], "pale_gold")
        s.box(hand, side + "_thumb", [x - sign * 1.7 - .4, 12.1, -2.2], [.8, 1.4, .7], "dark")

        thigh, shin, foot = [side + suffix for suffix in ["_thigh", "_shin", "_foot"]]
        leg = sign * 2
        s.box(thigh, side + "_player_thigh", [leg - 2, 6, -2], [4, 6, 4], pattern="wave_" + side)
        s.box(shin, side + "_player_shin", [leg - 2, 2, -2], [4, 4, 4], "dark", pattern="boot")
        s.box(foot, side + "_player_foot", [leg - 2, 0, -2], [4, 2, 4], "dark", pattern="boot")
        s.box(shin, side + "_knee_rim", [leg - 1.72, 5.45, -2.12], [3.44, 1.8, .35], "gold")
        s.box(shin, side + "_kneepad", [leg - 1.42, 5.65, -2.32], [2.84, 1.4, .35], "ivory", pattern="gem")
        s.box(foot, side + "_toe_gold", [leg - 2.02, .25, -2.18], [4.04, .35, .3], "gold")
        s.box(shin, side + "_ankle_gold", [leg - 2.02, 2, -2.03], [4.04, .35, 4.06], "gold")

    s.box("pelvis", "belt", [-4.1, 11.65, -2.12], [8.2, .85, 4.24], "ivory", pattern="cloth")
    for sign in [-1, 1]:
        s.box("pelvis", "belt_clasp_" + str(sign), [sign * 3.4 - .5, 11.45, -2.28], [1, 1.15, .5], "gold")
    s.box("loin_front", "front_loin", [-2.8, 8.4, -2.22], [5.6, 3.6, .45],
          "ivory", [-8, 0, 0], [0, 12, -2], "cloth")
    s.box("loin_back", "back_loin", [-2.75, 8.5, 1.95], [5.5, 3.5, .45],
          "ivory", [8, 0, 0], [0, 12, 2], "cloth")
    for side, sign in [("left", 1), ("right", -1)]:
        s.box("loin_" + side, side + "_loin_panel", [sign * 4 - .2, 9, -1.9], [.4, 3, 3.8],
              "ivory", [0, 0, sign * 12], [sign * 4, 12, 0], "cloth")
    for bone, x in [("right_finger_index", -6.7), ("right_finger_middle", -5.3)]:
        s.box(bone, bone + "_extension", [x - .35, 12, -.85], [.7, 1, .7], "shadow_skin", pattern="finger")
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
            bevel = (.97 if pattern.startswith("player_") else .87) if edge == 0 and min(width, height) > 3 else 1
            lift = .09 * (1 - y / max(1, height - 1)) + .03 * (1 - x / max(1, width - 1))
            if pattern == "lock": lift += .10 if x == max(1, width // 3) else 0
            if pattern in {"cloth", "glove", "boot"}: lift += .035 * math.sin(x * 1.4 + y * .35)
            if pattern in {"muscle", "pectoral_left", "pectoral_right", "abdominal", "rib"}:
                lift += .055 * math.sin(math.pi * (x + .5) / width) * math.sin(math.pi * (y + .5) / height)
            pixels[x, y] = (*color_scale(base, (face_light + lift + rng.uniform(-.012, .012)) * bevel), 255)
    draw = ImageDraw.Draw(image)
    if pattern == "player_chest" and face == "north":
        # Paint anatomy onto the plain player torso instead of narrowing its waist.
        dark = color_scale(base, .81)
        highlight = color_scale(base, 1.16)
        mid = width // 2
        pec_bottom = round(height * .36)
        draw.line([(mid - 1, 1), (mid - 1, pec_bottom), (1, pec_bottom + 1)], fill=dark)
        draw.line([(mid, 1), (mid, pec_bottom), (width - 2, pec_bottom + 1)], fill=dark)
        draw.line([(2, 1), (mid - 3, 1)], fill=highlight)
        draw.line([(mid + 2, 1), (width - 3, 1)], fill=highlight)
        muscle = color_scale(base, 1.08)
        for y in (round(height * .52), round(height * .69), round(height * .86)):
            for left, right in ((3, mid - 2), (mid + 1, width - 4)):
                draw.rectangle([left, y, right, y + 1], fill=muscle)
                draw.line([(left, y + 2), (right, y + 2)], fill=dark)
        draw.line([(mid - 1, pec_bottom + 2), (mid - 1, height - 2)], fill=dark)
    if pattern == "player_chest" and face == "south":
        mid = width // 2
        dark = color_scale(base, .82)
        draw.line([(mid, 2), (mid, height - 1)], fill=dark)
        draw.line([(1, 3), (mid - 2, height // 2), (mid - 2, height - 3)], fill=dark)
        draw.line([(width - 2, 3), (mid + 2, height // 2), (mid + 2, height - 3)], fill=dark)
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
    audit_animations(animation_data, sculpture.bones)
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
                "method": "Locally constructed classic player body proportions with original Star Platinum overlays, UVs, pixel art and animation. No imported model/texture/animation inputs.",
                "body_base": {"type": "minecraft_classic_player", "head": [8, 8, 8], "torso": [8, 12, 4],
                              "arm": [4, 12, 4], "leg": [4, 12, 4], "units": "model pixels in bind pose",
                              "split_joints": ["elbow", "wrist", "knee", "ankle"]},
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
