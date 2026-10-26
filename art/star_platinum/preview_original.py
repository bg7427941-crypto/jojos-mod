#!/usr/bin/env python3
"""Render the actual original asset JSON/atlas with a small orthographic rasterizer.

This is a development preview, not a replacement for Minecraft/GeckoLib validation.
Requires Pillow and NumPy. Output is outside runtime resources by default.
"""

import argparse
import json
import math
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/jojosmod"


def translation(value):
    result = np.eye(4); result[:3, 3] = value
    return result


def rotation(value):
    x, y, z = np.radians(value)
    rx = np.array([[1, 0, 0], [0, math.cos(x), -math.sin(x)], [0, math.sin(x), math.cos(x)]])
    ry = np.array([[math.cos(y), 0, math.sin(y)], [0, 1, 0], [-math.sin(y), 0, math.cos(y)]])
    rz = np.array([[math.cos(z), -math.sin(z), 0], [math.sin(z), math.cos(z), 0], [0, 0, 1]])
    result = np.eye(4); result[:3, :3] = rz @ ry @ rx
    return result


def sample(track, time, default):
    if track is None: return np.array(default, dtype=float)
    if isinstance(track, list): return np.array(track, dtype=float)
    keys = sorted((float(k), np.array(v, dtype=float)) for k, v in track.items())
    if time <= keys[0][0]: return keys[0][1]
    for (a, av), (b, bv) in zip(keys, keys[1:]):
        if time <= b: return av + (bv - av) * ((time - a) / (b - a))
    return keys[-1][1]


def matrices(bones, clip, time):
    by_name = {bone["name"]: bone for bone in bones}
    output = {}
    def build(name):
        if name in output: return output[name]
        bone = by_name[name]
        parent = build(bone["parent"]) if bone.get("parent") else np.eye(4)
        channels = clip.get("bones", {}).get(name, {})
        position = sample(channels.get("position"), time, [0, 0, 0])
        angles = sample(channels.get("rotation"), time, [0, 0, 0])
        # Bake negates X/Y angles and reflects geometry X. In the unreflected
        # authoring coordinates this is (-X,+Y,-Z), including the X reflection.
        angles *= [-1, 1, -1]
        scale = sample(channels.get("scale"), time, [1, 1, 1])
        pivot = np.array(bone["pivot"])
        transform = translation(pivot + position) @ rotation(angles) @ np.diag([*scale, 1]) @ translation(-pivot)
        output[name] = parent @ transform
        return output[name]
    for bone in bones: build(bone["name"])
    return output


FACES = {
    "north": ([0, 1, 2, 3], [0, 0, -1]), "south": ([5, 4, 7, 6], [0, 0, 1]),
    "east": ([1, 5, 6, 2], [1, 0, 0]), "west": ([4, 0, 3, 7], [-1, 0, 0]),
    "up": ([3, 2, 6, 7], [0, 1, 0]), "down": ([4, 5, 1, 0], [0, -1, 0]),
}


def render(bones, atlas, clip, time, yaw, width=420, height=620):
    transforms = matrices(bones, clip, time)
    camera = np.array([math.sin(math.radians(yaw)), .13, -math.cos(math.radians(yaw))])
    camera /= np.linalg.norm(camera)
    forward = -camera
    right = np.cross(forward, [0, 1, 0]); right /= np.linalg.norm(right)
    up = np.cross(right, forward)
    light = np.array([-.45, .8, -.55]); light /= np.linalg.norm(light)
    pixels = np.zeros((height, width, 4), dtype=np.uint8)
    for y in range(height): pixels[y, :, :] = [round(224 - y / height * 21), round(223 - y / height * 22), round(218 - y / height * 21), 255]
    depth = np.full((height, width), np.inf)
    center = np.array([0, 17.6, 0]); zoom = 14.5

    def project(points):
        points = points - center
        return np.stack([points @ right * zoom + width / 2, height / 2 - points @ up * zoom, points @ forward], axis=1)

    def triangle(screen, texcoords, shade):
        lo = np.maximum([0, 0], np.floor(screen[:, :2].min(axis=0)).astype(int))
        hi = np.minimum([width - 1, height - 1], np.ceil(screen[:, :2].max(axis=0)).astype(int))
        if np.any(hi < lo): return
        xx, yy = np.meshgrid(np.arange(lo[0], hi[0] + 1) + .5, np.arange(lo[1], hi[1] + 1) + .5)
        x0, y0 = screen[0, :2]; x1, y1 = screen[1, :2]; x2, y2 = screen[2, :2]
        denominator = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
        if abs(denominator) < 1e-7: return
        a = ((y1 - y2) * (xx - x2) + (x2 - x1) * (yy - y2)) / denominator
        b = ((y2 - y0) * (xx - x2) + (x0 - x2) * (yy - y2)) / denominator
        c = 1 - a - b
        z = a * screen[0, 2] + b * screen[1, 2] + c * screen[2, 2]
        target_depth = depth[lo[1]:hi[1] + 1, lo[0]:hi[0] + 1]
        mask = (a >= -1e-5) & (b >= -1e-5) & (c >= -1e-5) & (z < target_depth)
        if not np.any(mask): return
        uv = a[..., None] * texcoords[0] + b[..., None] * texcoords[1] + c[..., None] * texcoords[2]
        uv = np.floor(uv).astype(int)
        texel = atlas[np.clip(uv[..., 1], 0, atlas.shape[0] - 1), np.clip(uv[..., 0], 0, atlas.shape[1] - 1)].copy()
        mask &= texel[..., 3] > 0
        texel[..., :3] = np.clip(texel[..., :3].astype(float) * shade, 0, 255).astype(np.uint8)
        target = pixels[lo[1]:hi[1] + 1, lo[0]:hi[0] + 1]
        target[mask] = texel[mask]; target_depth[mask] = z[mask]

    for bone in bones:
        parent = transforms[bone["name"]]
        if np.linalg.norm(parent[:3, :3]) < 1e-8: continue
        for cube in bone.get("cubes", []):
            origin = np.array(cube["origin"]); size = np.array(cube["size"])
            xyz = np.array([[0,0,0], [1,0,0], [1,1,0], [0,1,0], [0,0,1], [1,0,1], [1,1,1], [0,1,1]], dtype=float) * size + origin
            transform = parent
            if cube.get("rotation"):
                p = np.array(cube["pivot"])
                angles = np.array(cube["rotation"]) * [-1, 1, -1]
                transform = parent @ translation(p) @ rotation(angles) @ translation(-p)
            world = (transform @ np.column_stack([xyz, np.ones(8)]).T).T[:, :3]
            screen = project(world)
            for face, (indices, normal) in FACES.items():
                n = transform[:3, :3] @ normal
                n /= max(1e-8, np.linalg.norm(n))
                if n @ camera <= 0: continue
                shade = .74 + .26 * max(0, n @ light)
                uv = cube["uv"][face]
                u, v = uv["uv"]; du, dv = uv["uv_size"]
                tex = np.array([[u, v+dv-.001], [u+du-.001, v+dv-.001], [u+du-.001, v], [u, v]])
                for corners in [[0,1,2], [0,2,3]]:
                    triangle(screen[[indices[i] for i in corners]], tex[corners], shade)
    return Image.fromarray(pixels)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pose", default="idle")
    parser.add_argument("--time", type=float, default=.35)
    parser.add_argument("--output", type=Path, default=Path("/workspace/work/star-platinum-original/model-preview.png"))
    options = parser.parse_args()
    geometry = json.loads((ASSETS / "geckolib/models/entity/star_platinum.geo.json").read_text())
    animations = json.loads((ASSETS / "geckolib/animations/entity/star_platinum.animation.json").read_text())["animations"]
    clip = animations["animation.star_platinum." + options.pose]
    atlas = np.array(Image.open(ASSETS / "textures/entity/star_platinum.png").convert("RGBA"))
    sheet = Image.new("RGBA", (1260, 650), (224, 223, 218, 255))
    draw = ImageDraw.Draw(sheet)
    for i, (yaw, label) in enumerate([(0, "FRONT"), (28, "THREE QUARTER"), (180, "BACK")]):
        sheet.paste(render(geometry["minecraft:geometry"][0]["bones"], atlas, clip, options.time, yaw), (i * 420, 25))
        draw.text((i * 420 + 18, 8), label + " / " + options.pose, fill=(38, 35, 47, 255))
    options.output.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(options.output)
    print(options.output)


if __name__ == "__main__": main()
