#!/usr/bin/env python3
"""Export the editable Star Platinum project to GeckoLib's Bedrock JSON.

Default: python art/star_platinum/export_roundabout.py
Recreate the initial adaptation:
  python art/star_platinum/export_roundabout.py --import-upstream MODEL --texture ATLAS

Original model and anime atlas: Hydraheads/RoundaboutMod, pinned at
cdd6dc35aead479aef4cebce873c1e532c0eccf3. Modeling credits include GenericName.
Paths: bbmodels/Stands/Star Platinum/star_platinum.bbmodel
       common/src/main/resources/assets/roundabout/textures/stand/star_platinum/anime.png
These assets retain Roundabout License v1.2; see THIRD_PARTY_NOTICES.
The timing curves below are authored for this mod; the frontal guard pose adapts
the upstream Block pose. No upstream Java or sound files are used.

Coordinate/UV conventions were checked against Blockbench's official exporter:
js/formats/bedrock/bedrock.js (cube/group compilation) and
js/animations/keyframe.js (transform keyframe compilation), revision
ec0b2827104d0799c153bfb5f867579c0c44652f. This is an independent Python exporter.
It mirrors X for pivots/origins/positions, X/Y for rotations, and preserves the
signed per-face UV rectangles, including Bedrock's reversed up/down convention.
The project uses numeric transforms; sound/particle timing remains in game code.
"""

import argparse
import base64
import copy
import hashlib
import json
from pathlib import Path
import struct
import uuid

ROOT = Path(__file__).resolve().parents[2]
ART = ROOT / "art/star_platinum"
ASSETS = ROOT / "src/main/resources/assets/jojosmod"
PROJECT = ART / "star_platinum.bbmodel"
PREFIX = "animation.star_platinum."
EXCLUDED = {"BAM", "kick_barrage", "finger"}
UPSTREAM_MODEL_SHA = "9f53f81eea39e4a5ba6f37f211a426f7f921ad86"
UPSTREAM_TEXTURE_SHA = "154bfe9038ffeb2a77ea3767d186bfef92124aa1"


def identity(name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "jojosmod/star_platinum/presentation/" + name))


def blob_sha(data):
    return hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()


def tidy(value):
    if isinstance(value, float):
        return round(value, 6) if value else 0
    if isinstance(value, list):
        return [tidy(v) for v in value]
    if isinstance(value, dict):
        return {k: tidy(v) for k, v in value.items()}
    return value


def channel(anchors, subdivisions=3):
    """Bake smoothstep segments, retaining every authored contact timestamp."""
    result = {}
    for (t0, v0), (t1, v1) in zip(anchors, anchors[1:]):
        for step in range(subdivisions):
            t = t0 + (t1 - t0) * step / subdivisions
            u = step / subdivisions
            weight = u * u * (3 - 2 * u)
            result[round(t, 6)] = [a + (b - a) * weight for a, b in zip(v0, v1)]
    result[anchors[-1][0]] = anchors[-1][1]
    return sorted(result.items())


def authored_animations():
    result = {}

    def clip(name, length, tracks, loop=False):
        result[name] = {"length": length, "loop": loop, "tracks": tracks}

    def curve(*anchors):
        return channel(anchors)

    idle_right = [6, -3, 7]
    idle_left = [6, 3, -7]
    idle_elbow = [17, 0, 0]
    clip("idle", 2.4, {
        "stand2": {"position": curve((0, [0, 0, 0]), (1.2, [0, .23, 0]), (2.4, [0, 0, 0]))},
        "upper_chest": {"rotation": curve((0, [-1, 0, 0]), (1.2, [-2.3, 0, 0]), (2.4, [-1, 0, 0]))},
        "head": {"rotation": curve((0, [0, -1.2, 0]), (1.2, [-.7, 1.2, 0]), (2.4, [0, -1.2, 0]))},
        "right_arm": {"rotation": curve((0, idle_right), (1.2, [7.5, -3, 8]), (2.4, idle_right))},
        "left_arm": {"rotation": curve((0, idle_left), (1.2, [7.5, 3, -8]), (2.4, idle_left))},
        "lower_right_arm": {"rotation": curve((0, idle_elbow), (1.2, [19, 0, 0]), (2.4, idle_elbow))},
        "lower_left_arm": {"rotation": curve((0, idle_elbow), (1.2, [19, 0, 0]), (2.4, idle_elbow))},
        "front_belt": {"rotation": curve((0, [2, 0, 0]), (1.2, [-2, 0, 0]), (2.4, [2, 0, 0]))},
        "back_belt": {"rotation": curve((0, [-2, 0, 0]), (1.2, [2, 0, 0]), (2.4, [-2, 0, 0]))},
    }, True)

    clip("move", .8, {
        "stand2": {"position": curve((0, [0, .05, 0]), (.4, [0, .45, 0]), (.8, [0, .05, 0]))},
        "upper_chest": {"rotation": curve((0, [-8, -2, 0]), (.4, [-10, 2, 0]), (.8, [-8, -2, 0]))},
        "head": {"rotation": [[0, [5, 0, 0]]]},
        "right_arm": {"rotation": curve((0, [22, -8, 11]), (.4, [29, -8, 13]), (.8, [22, -8, 11]))},
        "left_arm": {"rotation": curve((0, [29, 8, -13]), (.4, [22, 8, -11]), (.8, [29, 8, -13]))},
        "lower_right_arm": {"rotation": [[0, [28, 0, 0]]]},
        "lower_left_arm": {"rotation": [[0, [28, 0, 0]]]},
        "right_leg": {"rotation": curve((0, [12, 0, 3]), (.4, [-8, 0, 3]), (.8, [12, 0, 3]))},
        "left_leg": {"rotation": curve((0, [-8, 0, -3]), (.4, [12, 0, -3]), (.8, [-8, 0, -3]))},
        "lower_right_leg": {"rotation": curve((0, [15, 0, 0]), (.4, [24, 0, 0]), (.8, [15, 0, 0]))},
        "lower_left_leg": {"rotation": curve((0, [24, 0, 0]), (.4, [15, 0, 0]), (.8, [24, 0, 0]))},
        "front_belt": {"rotation": curve((0, [18, 0, -3]), (.4, [24, 0, 3]), (.8, [18, 0, -3]))},
        "back_belt": {"rotation": curve((0, [25, 0, 3]), (.4, [18, 0, -3]), (.8, [25, 0, 3]))},
    }, True)

    clip("summon", .4, {
        "stand": {"scale": curve((0, [.06, .06, .06]), (.25, [1.035, 1.035, 1.035]), (.4, [1, 1, 1]))},
        "stand2": {"position": curve((0, [0, -2.2, .8]), (.25, [0, .2, 0]), (.4, [0, 0, 0]))},
        "upper_chest": {"rotation": curve((0, [7, -14, 0]), (.25, [-3, 2, 0]), (.4, [-1, 0, 0]))},
        "head": {"rotation": curve((0, [-12, 14, 0]), (.4, [0, 0, 0]))},
        "right_arm": {"rotation": curve((0, [45, -20, 27]), (.25, [10, -7, 12]), (.4, idle_right))},
        "left_arm": {"rotation": curve((0, [45, 20, -27]), (.25, [10, 7, -12]), (.4, idle_left))},
        "lower_right_arm": {"rotation": curve((0, [75, 0, 0]), (.4, idle_elbow))},
        "lower_left_arm": {"rotation": curve((0, [75, 0, 0]), (.4, idle_elbow))},
        "front_belt": {"rotation": curve((0, [22, 0, 0]), (.25, [-9, 0, 0]), (.4, [2, 0, 0]))},
        "back_belt": {"rotation": curve((0, [-18, 0, 0]), (.4, [-2, 0, 0]))},
    })
    clip("dismiss", .2, {
        "stand": {"scale": curve((0, [1, 1, 1]), (.2, [.02, .02, .02]))},
        "stand2": {"position": curve((0, [0, 0, 0]), (.2, [0, 2.4, .8]))},
        "upper_chest": {"rotation": curve((0, [-1, 0, 0]), (.2, [4, -12, 0]))},
        "right_arm": {"rotation": curve((0, idle_right), (.2, [30, -15, 20]))},
        "left_arm": {"rotation": curve((0, idle_left), (.2, [30, 15, -20]))},
    })

    # Full right extension first reaches contact at 0.2 s (server tick 4).
    clip("strike", .6, {
        "stand2": {"position": curve((0, [0, 0, 0]), (.14, [0, -.2, .4]), (.2, [0, -.1, -.7]), (.26, [0, -.1, -.65]), (.6, [0, 0, 0]))},
        "upper_chest": {"rotation": curve((0, [-1, 0, 0]), (.14, [-3, -20, -2]), (.2, [-8, 17, 2]), (.26, [-7, 15, 2]), (.42, [-3, 4, 0]), (.6, [-1, 0, 0]))},
        "lower_torso": {"rotation": curve((0, [0, 0, 0]), (.14, [0, 7, 0]), (.2, [0, -6, 0]), (.6, [0, 0, 0]))},
        "head": {"rotation": curve((0, [0, 0, 0]), (.14, [2, 12, 0]), (.2, [4, -10, 0]), (.6, [0, 0, 0]))},
        "right_arm": {
            "rotation": curve((0, idle_right), (.14, [23, -18, 20]), (.2, [95, 0, -3]), (.235, [95, 0, -3]), (.36, [55, 3, 7]), (.48, [18, 0, 7]), (.6, idle_right)),
            "position": curve((0, [0, 0, 0]), (.14, [.25, .2, .8]), (.2, [0, 0, -1.4]), (.235, [0, 0, -1.4]), (.48, [0, 0, 0]), (.6, [0, 0, 0])),
        },
        "lower_right_arm": {"rotation": curve((0, idle_elbow), (.14, [88, 0, -7]), (.2, [0, 0, 0]), (.235, [3, 0, 0]), (.36, [64, 0, 0]), (.6, idle_elbow))},
        "left_arm": {"rotation": curve((0, idle_left), (.14, [55, -18, -8]), (.26, [58, -18, -8]), (.6, idle_left))},
        "lower_left_arm": {"rotation": curve((0, idle_elbow), (.14, [82, 0, -4]), (.26, [84, 0, -4]), (.6, idle_elbow))},
        "right_leg": {"rotation": curve((0, [0, 0, 0]), (.2, [-7, 0, 3]), (.6, [0, 0, 0]))},
        "left_leg": {"rotation": curve((0, [0, 0, 0]), (.2, [8, 0, -3]), (.6, [0, 0, 0]))},
        "front_belt": {"rotation": curve((0, [2, 0, 0]), (.14, [-8, 0, -4]), (.24, [17, 0, 5]), (.42, [-7, 0, -2]), (.6, [2, 0, 0]))},
        "back_belt": {"rotation": curve((0, [-2, 0, 0]), (.24, [15, 0, -4]), (.6, [-2, 0, 0]))},
    })

    arm = {side: [(0, idle_right if side == "right" else idle_left), (.1, [55, -8 if side == "right" else 8, 8 if side == "right" else -8])] for side in ["right", "left"]}
    elbow = {side: [(0, idle_elbow), (.1, [82, 0, 0])] for side in arm}
    arm_pos = {side: [(0, [0, 0, 0]), (.1, [0, 0, 0])] for side in arm}
    chest = [(0, [-1, 0, 0]), (.1, [-8, 0, 0])]
    cloth = [(0, [2, 0, 0]), (.1, [12, 0, 0])]
    for pulse in range(8):
        t = round(.2 + pulse * .15, 3)
        side = "right" if pulse % 2 == 0 else "left"
        sign = 1 if side == "right" else -1
        arm[side] += [(t - .04, [58, -8 * sign, 9 * sign]), (t, [96, -3 * sign, -2 * sign]), (t + .035, [93, -3 * sign, -2 * sign]), (t + .085, [55, -8 * sign, 8 * sign])]
        elbow[side] += [(t - .04, [94, 0, 0]), (t, [3, 0, 0]), (t + .035, [8, 0, 0]), (t + .085, [82, 0, 0])]
        arm_pos[side] += [(t - .04, [0, 0, .35]), (t, [0, 0, -.9]), (t + .035, [0, 0, -.85]), (t + .085, [0, 0, 0])]
        chest += [(t, [-8, 9 * sign, sign]), (t + .085, [-7, 0, 0])]
        cloth += [(t, [16, 0, -3 * sign]), (t + .085, [8, 0, 0])]
    tracks = {
        "stand2": {"position": curve((0, [0, 0, 0]), (.16, [0, -.2, -.6]), (1.34, [0, -.2, -.6]), (1.5, [0, 0, 0]))},
        "upper_chest": {"rotation": channel(chest + [(1.5, [-1, 0, 0])], 2)},
        "head": {"rotation": [[0, [3, 0, 0]], [1.5, [0, 0, 0]]]},
        "front_belt": {"rotation": channel(cloth + [(1.5, [2, 0, 0])], 2)},
        "back_belt": {"rotation": curve((0, [-2, 0, 0]), (.16, [15, 0, 0]), (1.34, [15, 0, 0]), (1.5, [-2, 0, 0]))},
        "right_leg": {"rotation": [[0, [-5, 0, 3]], [1.34, [-5, 0, 3]], [1.5, [0, 0, 0]]]},
        "left_leg": {"rotation": [[0, [8, 0, -3]], [1.34, [8, 0, -3]], [1.5, [0, 0, 0]]]},
    }
    for side in arm:
        rest = idle_right if side == "right" else idle_left
        tracks[side + "_arm"] = {
            "rotation": channel(arm[side] + [(1.42, [35, 0, 6 if side == "right" else -6]), (1.5, rest)], 2),
            "position": channel(arm_pos[side] + [(1.5, [0, 0, 0])], 2),
        }
        tracks["lower_" + side + "_arm"] = {"rotation": channel(elbow[side] + [(1.42, [45, 0, 0]), (1.5, idle_elbow)], 2)}
    clip("barrage", 1.5, tracks)

    # Upper-arm and elbow stance is adapted from the upstream Block pose.
    clip("guard", 1, {
        "stand2": {"position": curve((0, [0, -.35, 0]), (.5, [0, -.18, 0]), (1, [0, -.35, 0]))},
        "upper_chest": {"rotation": curve((0, [-4, 0, 0]), (.5, [-5.2, 0, 0]), (1, [-4, 0, 0]))},
        "head": {"rotation": [[0, [3, 0, 0]]]},
        "right_arm": {"rotation": curve((0, [58.5707, 25.0459, 9.3417]), (.5, [60, 25.0459, 9.3417]), (1, [58.5707, 25.0459, 9.3417]))},
        "left_arm": {"rotation": curve((0, [58.5707, -25.0459, -9.3417]), (.5, [60, -25.0459, -9.3417]), (1, [58.5707, -25.0459, -9.3417]))},
        "lower_right_arm": {"rotation": [[0, [90, 0, 0]]]},
        "lower_left_arm": {"rotation": [[0, [90, 0, 0]]]},
        "right_leg": {"rotation": [[0, [-7, 0, 4]]]},
        "left_leg": {"rotation": [[0, [9, 0, -4]]]},
        "lower_right_leg": {"rotation": [[0, [9, 0, 0]]]},
        "lower_left_leg": {"rotation": [[0, [8, 0, 0]]]},
        "front_belt": {"rotation": curve((0, [7, 0, -2]), (.5, [10, 0, 2]), (1, [7, 0, -2]))},
        "back_belt": {"rotation": curve((0, [-7, 0, 2]), (.5, [-4, 0, -2]), (1, [-7, 0, 2]))},
    }, True)
    # Unkeyed arms return naturally through GeckoLib's bone reset interpolation.
    clip("recover", .2, {
        "stand2": {"position": curve((0, [0, -.15, -.25]), (.2, [0, 0, 0]))},
        "upper_chest": {"rotation": curve((0, [-3, 0, 0]), (.2, [-1, 0, 0]))},
        "head": {"rotation": curve((0, [2, 0, 0]), (.2, [0, 0, 0]))},
        "front_belt": {"rotation": curve((0, [10, 0, 0]), (.2, [2, 0, 0]))},
        "back_belt": {"rotation": curve((0, [6, 0, 0]), (.2, [-2, 0, 0]))},
    })
    return result


def build_project(upstream, atlas):
    groups = {group["uuid"]: group for group in upstream.get("groups", [])}
    elements = {element["uuid"]: copy.deepcopy(element) for element in upstream["elements"]}
    kept_elements = []

    def nested(nodes):
        result = []
        for node in nodes:
            if isinstance(node, str):
                element = elements[node]
                element["box_uv"] = False
                for face in element.get("faces", {}).values():
                    if face.get("texture") is not None:
                        face["texture"] = 0
                kept_elements.append(element)
                result.append(node)
                continue
            group = groups.get(node["uuid"], node)
            if group["name"] in EXCLUDED:
                continue
            result.append({
                "name": group["name"], "uuid": group["uuid"],
                "origin": group["origin"], "rotation": group.get("rotation", [0, 0, 0]),
                "export": True, "visibility": True,
                "children": nested(node.get("children", [])),
            })
        return result

    outliner = nested(upstream["outliner"])
    group_ids = {}

    def collect(nodes):
        for node in nodes:
            if isinstance(node, dict):
                group_ids[node["name"]] = node["uuid"]
                collect(node["children"])

    collect(outliner)
    animations = []
    for name, animation in authored_animations().items():
        animators = {}
        for bone, channels in animation["tracks"].items():
            assert bone in group_ids, bone
            keyframes = []
            for key, values in channels.items():
                for time, vector in values:
                    keyframes.append({
                        "uuid": identity(name + bone + key + str(time)),
                        "channel": key, "time": round(time, 6), "interpolation": "linear",
                        "data_points": [dict(zip(("x", "y", "z"), map(str, tidy(vector))))],
                    })
            animators[group_ids[bone]] = {"name": bone, "type": "bone", "keyframes": keyframes}
        animations.append({
            "name": PREFIX + name, "uuid": identity(name),
            "loop": "loop" if animation["loop"] else "once",
            "length": animation["length"], "snapping": 60, "animators": animators,
        })
    return {
        "meta": {"format_version": "4.10", "model_format": "geckolib_model", "box_uv": False},
        "name": "Star Platinum — Roundabout adaptation for Part 3",
        "model_identifier": "star_platinum", "visible_box": [3.4, 3.6, 1.4],
        "credit": "Model and anime atlas: Hydraheads, GenericName and Roundabout contributors; Roundabout License v1.2. Timing curves: JoJo's Mod. Guard pose adapted from Roundabout Block.",
        "resolution": {"width": 128, "height": 128}, "elements": kept_elements,
        "outliner": outliner,
        "textures": [{
            "name": "star_platinum.png", "id": "0", "uuid": identity("anime_atlas"),
            "mode": "bitmap", "uv_width": 128, "uv_height": 128,
            "source": "data:image/png;base64," + base64.b64encode(atlas).decode(),
        }],
        "animations": animations,
    }


def export_project(project):
    elements = {element["uuid"]: element for element in project["elements"]}
    groups = {group["uuid"]: group for group in project.get("groups", [])}
    bones = []
    group_names = {}

    def walk(nodes, parent=None):
        for node in nodes:
            if isinstance(node, str):
                continue
            group = groups.get(node["uuid"], node)
            name = group["name"]
            group_names[group["uuid"]] = name
            assert name not in {bone["name"] for bone in bones}, name
            pivot = group["origin"]
            bone = {"name": name, "pivot": [-pivot[0], pivot[1], pivot[2]]}
            if parent:
                bone["parent"] = parent
            rotation = group.get("rotation", [0, 0, 0])
            if any(rotation):
                bone["rotation"] = [-rotation[0], -rotation[1], rotation[2]]
            cubes = []
            for child in node.get("children", []):
                if not isinstance(child, str):
                    continue
                element = elements[child]
                if not element.get("export", True):
                    continue
                lower, upper = element["from"], element["to"]
                size = [b - a for a, b in zip(lower, upper)]
                cube = {"origin": [-upper[0], lower[1], lower[2]], "size": size}
                if element.get("inflate"):
                    cube["inflate"] = element["inflate"]
                rotation = element.get("rotation", [0, 0, 0])
                if any(rotation):
                    pivot = element.get("origin", [0, 0, 0])
                    cube["pivot"] = [-pivot[0], pivot[1], pivot[2]]
                    cube["rotation"] = [-rotation[0], -rotation[1], rotation[2]]
                cube["uv"] = {}
                for face_name, face in element["faces"].items():
                    if face.get("texture") is None:
                        continue
                    u0, v0, u1, v1 = face["uv"]
                    if face_name in ("up", "down"):
                        u0, v0, u1, v1 = u1, v1, u0, v0
                    face_uv = {"uv": [u0, v0], "uv_size": [u1 - u0, v1 - v0]}
                    if face.get("rotation"):
                        face_uv["uv_rotation"] = face["rotation"]
                    cube["uv"][face_name] = face_uv
                cubes.append(cube)
            if cubes:
                bone["cubes"] = cubes
            bones.append(bone)
            walk(node.get("children", []), name)

    walk(project["outliner"])
    geometry = {"format_version": "1.12.0", "minecraft:geometry": [{
        "description": {"identifier": "geometry.star_platinum", "texture_width": 128,
                        "texture_height": 128, "visible_bounds_width": 3.4,
                        "visible_bounds_height": 3.6, "visible_bounds_offset": [0, 1.4, 0]},
        "bones": bones,
    }]}
    animations = {}
    for animation in project["animations"]:
        entry = {"loop": animation["loop"] == "loop", "animation_length": animation["length"], "bones": {}}
        for uid, animator in animation["animators"].items():
            name = group_names[uid]
            channels = {}
            for frame in animator.get("keyframes", []):
                key = frame["channel"]
                assert key in ("rotation", "position", "scale"), key
                vector = [float(frame["data_points"][0][axis]) for axis in ("x", "y", "z")]
                if key in ("rotation", "position"):
                    vector[0] *= -1
                if key == "rotation":
                    vector[1] *= -1
                value = vector
                if frame.get("interpolation") == "catmullrom":
                    value = {"post": vector, "lerp_mode": "catmullrom"}
                channels.setdefault(key, {})[str(frame["time"])] = value
            entry["bones"][name] = channels
        animations[animation["name"]] = entry
    atlas = base64.b64decode(project["textures"][0]["source"].split(",", 1)[1])
    return tidy(geometry), tidy({"format_version": "1.8.0", "animations": animations}), atlas


def audit(project, geometry, animations, atlas):
    assert atlas[:8] == b"\x89PNG\r\n\x1a\n"
    assert struct.unpack(">II", atlas[16:24]) == (128, 128)
    bones = geometry["minecraft:geometry"][0]["bones"]
    names = {bone["name"] for bone in bones}
    assert len(names) == len(bones)
    assert not (EXCLUDED & names)
    for bone in bones:
        assert not bone.get("parent") or bone["parent"] in names
        for cube in bone.get("cubes", []):
            for uv in cube["uv"].values():
                start, size = uv["uv"], uv["uv_size"]
                assert all(0 <= edge <= 128 for edge in (*start, start[0] + size[0], start[1] + size[1])), uv
    assert set(animations["animations"]) == {PREFIX + name for name in authored_animations()}
    for entry in animations["animations"].values():
        assert set(entry["bones"]) <= names
        for channels in entry["bones"].values():
            for track in channels.values():
                assert all(0 <= float(time) <= entry["animation_length"] for time in track)
    strike = animations["animations"][PREFIX + "strike"]["bones"]["right_arm"]["rotation"]
    assert strike["0.2"][0] == -95
    barrage = animations["animations"][PREFIX + "barrage"]["bones"]
    for pulse in range(8):
        side = "right" if pulse % 2 == 0 else "left"
        time = str(round(.2 + pulse * .15, 3))
        assert barrage[side + "_arm"]["rotation"][time][0] == -96
    assert len(project["elements"]) == sum(len(bone.get("cubes", [])) for bone in bones)
    return {"bones": len(bones), "cubes": len(project["elements"]),
            "animations": len(animations["animations"]), "atlas": "128x128",
            "contact": .2, "barrage_pulses": [.2, .35, .5, .65, .8, .95, 1.1, 1.25]}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--import-upstream", type=Path)
    parser.add_argument("--texture", type=Path)
    options = parser.parse_args()
    if options.import_upstream:
        parser.error("--texture is required with --import-upstream") if not options.texture else None
        source, atlas = options.import_upstream.read_bytes(), options.texture.read_bytes()
        assert blob_sha(source) == UPSTREAM_MODEL_SHA, "Unexpected upstream model revision"
        assert blob_sha(atlas) == UPSTREAM_TEXTURE_SHA, "Unexpected upstream anime atlas revision"
        project = tidy(build_project(json.loads(source), atlas))
        PROJECT.write_text(json.dumps(project, ensure_ascii=False, indent=2) + "\n")
    else:
        project = json.loads(PROJECT.read_text())
    geometry, animations, atlas = export_project(project)
    summary = audit(project, geometry, animations, atlas)
    paths = {
        ASSETS / "geckolib/models/entity/star_platinum.geo.json": geometry,
        ASSETS / "geckolib/animations/entity/star_platinum.animation.json": animations,
    }
    for path, content in paths.items():
        path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + "\n")
    (ASSETS / "textures/entity/star_platinum.png").write_bytes(atlas)
    print(json.dumps(summary))


if __name__ == "__main__":
    main()
