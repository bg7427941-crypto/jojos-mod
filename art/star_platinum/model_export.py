"""Shared deterministic GeckoLib/Blockbench export helpers.

Geometry and atlas authorship is supplied by the caller, not claimed here.
"""

import base64
import uuid

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


def blockbench(sculpture, animation_data, png, atlas_size=128):
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
            "name": "Star Platinum", "model_identifier": "star_platinum",
            "credit": "See asset-provenance.json for geometry, atlas and animation credits.",
            "resolution": {"width": atlas_size, "height": atlas_size}, "elements": elements, "outliner": roots,
            "textures": [{"name": "star_platinum.png", "id": "0", "uuid": stable_id("texture"),
                          "path": "star_platinum.png", "width": atlas_size, "height": atlas_size,
                          "uv_width": atlas_size, "uv_height": atlas_size,
                          "source": "data:image/png;base64," + base64.b64encode(png).decode()}], "animations": animations}

