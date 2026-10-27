#!/usr/bin/env python3
"""Adapt Roundabout's credited Star Platinum model to our GeckoLib action rig.

Offline, deterministic conversion. The pinned upstream project supplies shape,
UVs and bind rotations; its Part 3 anime atlas supplies pixels. Its animation
keyframes and barrage duplicates are deliberately omitted. Our own action clips
retain server contact times. Requires Python 3, Pillow and NumPy.
"""

from copy import deepcopy
import hashlib
import json
from pathlib import Path
import shutil

import numpy as np
from PIL import Image

from model_export import Sculpture, blockbench, tidy
from original_animations import build_animations, audit_animations
from preview_model import rotation, translation

ROOT = Path(__file__).resolve().parents[2]
ART = ROOT / "art/star_platinum"
ASSETS = ROOT / "src/main/resources/assets/jojosmod"
UPSTREAM = ART / "upstream"
COMMIT = "f2390f1240e9b49444d6013e79bfda2038a542fb"
MODEL_BLOB = "9f53f81eea39e4a5ba6f37f211a426f7f921ad86"
TEXTURE_BLOB = "154bfe9038ffeb2a77ea3767d186bfef92124aa1"
LICENSE_BLOB = "c80e33350fff9f22310edbf51ca40134b940e625"
BASE_URL = f"https://github.com/Hydraheads/RoundaboutMod/blob/{COMMIT}/"
CREDIT = "Star Platinum geometry, UVs and anime atlas adapted from Roundabout by Hydraheads and contributors; JoJo's Mod rig adaptation and original animation; character by Hirohiko Araki."


def git_blob(path):
    data = path.read_bytes()
    return hashlib.sha1(f"blob {len(data)}\0".encode() + data).hexdigest()


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def rig():
    s = Sculpture()
    s.bone("root", [0, 0, 0])
    s.bone("hover", [0, 0, 0], "root")
    s.bone("pelvis", [0, 12, 0], "hover")
    s.bone("waist", [0, 12, 0], "pelvis")
    s.bone("chest", [0, 18, 0], "waist")
    s.bone("neck", [0, 24, 0], "chest")
    s.bone("head", [0, 24, 0], "neck")
    s.bone("hair", [0, 31, 0], "head")
    # Reserved scarf joints keep the action contract; its red collar is painted.
    s.bone("scarf", [0, 24, 0], "neck")
    for side, sign in (("left", 1), ("right", -1)):
        s.bone("scarf_tail_" + side, [sign * 1.7, 23.8, 2], "scarf")
        s.bone(side + "_upper_arm", [sign * 5, 22, 0], "chest")
        s.bone(side + "_forearm", [sign * 6, 18, 0], side + "_upper_arm")
        s.bone(side + "_hand", [sign * 6, 14, 0], side + "_forearm")
        s.bone(side + "_thigh", [sign * 2, 12, 0], "pelvis")
        s.bone(side + "_shin", [sign * 2, 6, 0], side + "_thigh")
        s.bone(side + "_foot", [sign * 2, 2, 0], side + "_shin")
    s.bone("right_finger_index", [-6.7, 13, -.5], "right_hand")
    s.bone("right_finger_middle", [-5.3, 13, -.5], "right_hand")
    for part, pivot in (("front", [0, 13.125, -2.7]), ("back", [0, 13.1, 2.7]),
                        ("left", [4, 12, 0]), ("right", [-4, 12, 0])):
        s.bone("loin_" + part, pivot, "pelvis")
    return s


def euler(matrix):
    """Recover XYZ Euler angles from Rz @ Ry @ Rx, checking the round trip."""
    m = matrix[:3, :3]
    y = np.arcsin(np.clip(-m[2, 0], -1, 1))
    if abs(np.cos(y)) > 1e-7:
        x, z = np.arctan2(m[2, 1], m[2, 2]), np.arctan2(m[1, 0], m[0, 0])
    else:
        x, z = np.arctan2(-m[1, 2], m[1, 1]), 0
    angles = np.degrees([x, y, z])
    assert np.allclose(rotation(angles)[:3, :3], m, atol=1e-7)
    return angles


def append(s, bone, name, cube, source=None):
    s.lookup[bone].setdefault("cubes", []).append(cube)
    s.sources.append({"name": name, "bone": bone, "cube": cube, "upstream_uuid": source})


def convert_cube(element, transform):
    """Flatten bind rotations, then reflect X into our authoring convention.

    Inflations are baked into the bounds so the runtime and editable export
    share exactly the same box. Per-face UV signs and zero-thickness cloth
    remain intact. UVs are sampled from the pinned anime atlas unchanged.
    """
    low, high = np.array(element["from"], dtype=float), np.array(element["to"], dtype=float)
    inflation = element.get("inflate", 0)
    low -= inflation
    high += inflation
    assert np.all(high >= low), element["uuid"]
    pivot = np.array(element.get("origin", [0, 0, 0]))
    local = translation(pivot) @ rotation(element.get("rotation", [0, 0, 0])) @ translation(-pivot)
    composite = transform @ local
    center = (composite @ np.append((low + high) / 2, 1))[:3]
    center[0] *= -1
    size = high - low
    angles = euler(composite) * [-1, -1, 1]
    cube = {"origin": (center - size / 2).tolist(), "size": size.tolist(), "uv": {}}
    if np.max(np.abs(angles)) > 1e-7:
        cube.update(rotation=angles.tolist(), pivot=center.tolist())
    for face, value in element["faces"].items():
        assert value.get("rotation", 0) == 0, (element["uuid"], face)
        u, v, u2, v2 = value["uv"]
        if face in {"up", "down"}:
            u, v, u2, v2 = u2, v2, u, v
        cube["uv"][face] = {"uv": [u, v], "uv_size": [u2 - u, v2 - v]}
    return cube


def split_y(cube, cut):
    """Cut a glove/boot at a joint, cropping vertical UVs without repainting."""
    assert not cube.get("rotation")
    y, height = cube["origin"][1], cube["size"][1]
    assert y < cut < y + height
    lower, upper = deepcopy(cube), deepcopy(cube)
    lower["size"][1] = cut - y
    upper["origin"][1], upper["size"][1] = cut, y + height - cut
    fraction = (y + height - cut) / height
    for face in ("north", "south", "east", "west"):
        v, dv = cube["uv"][face]["uv"][1], cube["uv"][face]["uv_size"][1]
        upper["uv"][face]["uv_size"][1] = dv * fraction
        lower["uv"][face]["uv"][1] = v + dv * fraction
        lower["uv"][face]["uv_size"][1] = dv * (1 - fraction)
    return lower, upper


def destination(path):
    if "hair" in path: return "hair"
    if "head" in path: return "head"
    if "front_belt" in path: return "loin_front"
    if "back_belt" in path: return "loin_back"
    if "belt" in path: return "pelvis"
    if "lower_torso" in path: return "waist"
    for side in ("left", "right"):
        if side + "_arm" in path:
            return side + ("_forearm" if "lower_" + side + "_arm" in path else "_upper_arm")
        if side + "_leg" in path:
            return side + ("_shin" if "lower_" + side + "_leg" in path else "_thigh")
    if "upper_chest_only" in path: return "chest"
    raise ValueError(f"Unmapped source group: {path}")


def sculpt():
    source = json.loads((UPSTREAM / "star_platinum.bbmodel").read_text())
    groups = {item["uuid"]: item for item in source["groups"]}
    elements = {item["uuid"]: item for item in source["elements"]}
    s = rig()

    def walk(nodes, path=(), transform=None):
        transform = np.eye(4) if transform is None else transform
        for node in nodes:
            if isinstance(node, str):
                element = elements[node]
                if not element.get("export", True): continue
                cube = convert_cube(element, transform)
                bone = destination(path)
                name = "roundabout_" + node
                if bone.endswith(("_forearm", "_shin")):
                    cut = 14 if bone.endswith("_forearm") else 2
                    if cube["origin"][1] < cut < cube["origin"][1] + cube["size"][1]:
                        lower, upper = split_y(cube, cut)
                        append(s, bone, name + "_upper", upper, node)
                        target = bone.replace("_forearm", "_hand").replace("_shin", "_foot")
                        append(s, target, name + "_lower", lower, node)
                        continue
                append(s, bone, name, cube, node)
            else:
                group = groups[node["uuid"]]
                # Source barrage duplicates are animation helpers, not body parts.
                # Replace its single finger with our two server-timed fingers.
                if group["name"] in {"BAM", "kick_barrage", "finger"}: continue
                p = np.array(group["origin"])
                local = translation(p) @ rotation(group.get("rotation", [0, 0, 0])) @ translation(-p)
                walk(node["children"], path + (group["name"],), transform @ local)

    walk(source["outliner"])
    # Own two-finger extension, borrowing the source's finger UV patch only.
    finger_uv = convert_cube(next(e for e in elements.values() if e["name"] == "finger_cube"), np.eye(4))["uv"]
    for name, x in (("right_finger_index", -6.7), ("right_finger_middle", -5.3)):
        append(s, name, name + "_extension", {"origin": [x - .35, 12, -.85],
               "size": [.7, 1, .7], "uv": deepcopy(finger_uv)})
    return s


def main():
    for name, blob in (("star_platinum.bbmodel", MODEL_BLOB), ("anime.png", TEXTURE_BLOB),
                       ("LICENSE-Roundabout.txt", LICENSE_BLOB)):
        assert git_blob(UPSTREAM / name) == blob, (name, "pinned source changed")
    sculpture = sculpt()
    animation_data = build_animations()
    audit = audit_animations(animation_data, sculpture.bones)
    with Image.open(UPSTREAM / "anime.png") as image:
        assert image.size == (128, 128)
    geometry = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.star_platinum", "texture_width": 128,
                        "texture_height": 128, "visible_bounds_width": 13,
                        "visible_bounds_height": 4, "visible_bounds_offset": [0, 1.5, 0]},
        "bones": sculpture.bones}]}
    texture_path = ASSETS / "textures/entity/star_platinum.png"
    shutil.copyfile(UPSTREAM / "anime.png", texture_path)
    project = blockbench(sculpture, animation_data, texture_path.read_bytes(), atlas_size=128)
    project.update(name="Star Platinum — Roundabout adaptation", credit=CREDIT)
    geometry_path = ASSETS / "geckolib/models/entity/star_platinum.geo.json"
    animation_path = ASSETS / "geckolib/animations/entity/star_platinum.animation.json"
    for path, data in ((geometry_path, geometry), (animation_path, animation_data)):
        path.write_text(json.dumps(tidy(data), indent=2) + "\n", encoding="utf-8")
    (ART / "star_platinum.bbmodel").write_text(json.dumps(tidy(project), separators=(",", ":"), ensure_ascii=False) + "\n", encoding="utf-8")
    uv_records = [{"cube": item["name"], "upstream_uuid": item["upstream_uuid"], "face": face, **uv}
                  for item in sculpture.sources for face, uv in item["cube"]["uv"].items()]
    (ART / "uv-layout.json").write_text(json.dumps(tidy(uv_records), indent=2) + "\n", encoding="utf-8")
    manifest = {"credit": CREDIT, "license": "Roundabout License v1.2 for adapted geometry/UVs/atlas; MIT for our adaptation source and original animation; character rights retained by their holders",
        "method": "Pinned Roundabout geometry and Part 3 anime atlas, bind transforms baked, wrists and ankles split with cropped UVs, mapped to our 29-bone rig and 14 original action clips.",
        "upstream": {"repository": "https://github.com/Hydraheads/RoundaboutMod", "commit": COMMIT,
            "model": {"url": BASE_URL + "bbmodels/Stands/Star%20Platinum/star_platinum.bbmodel", "blob": MODEL_BLOB, "sha256": sha256(UPSTREAM / "star_platinum.bbmodel")},
            "texture": {"url": BASE_URL + "common/src/main/resources/assets/roundabout/textures/stand/star_platinum/anime.png", "blob": TEXTURE_BLOB, "sha256": sha256(UPSTREAM / "anime.png")},
            "license": {"url": BASE_URL + "LICENSE", "blob": LICENSE_BLOB, "local": "upstream/LICENSE-Roundabout.txt"}},
        "modifications": ["Flattened static bind rotations into cube transforms", "Mapped body and cloth to the existing action rig", "Split gloves at Y=14 and boots at Y=2 with cropped UVs", "Omitted source barrage/kick duplicates and single finger", "Added two right-hand finger extensions", "Retained our 14 action clips; no upstream keyframes imported", "Anime atlas copied byte-for-byte"],
        "bones": len(sculpture.bones), "cubes": len(sculpture.sources), "atlas": [128, 128],
        "animations": len(animation_data["animations"]), "animation_audit": audit,
        "texture_sha256": sha256(texture_path), "geometry_sha256": sha256(geometry_path),
        "animation_sha256": sha256(animation_path), "model_source_sha256": sha256(Path(__file__)),
        "animation_source_sha256": sha256(ART / "original_animations.py"),
        "export_source_sha256": sha256(ART / "model_export.py"),
        "transform_source_sha256": sha256(ART / "preview_model.py")}
    (ART / "asset-provenance.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(manifest))


if __name__ == "__main__": main()
