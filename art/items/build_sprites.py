#!/usr/bin/env python3
"""Export the retained original item atlas to inventory textures.

ImageMagick 7 extracts cells and resamples with nearest neighbour. Pillow only
reads final pixels for validation and editable SVG export. No network needed.
"""
from pathlib import Path
import hashlib
import json
import subprocess
import tempfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ART = ROOT / 'art/items'
ASSETS = ROOT / 'src/main/resources/assets/jojosmod'
SIZE = 64
ITEMS = [
    ('stand_arrow', 'Stand Arrow'), ('stardust_arrow', 'Stardust Arrow'),
    ('stand_disc_fragment', 'Disc fragment'), ('empty_stand_disc', 'Empty disc'),
    ('stand_disc', 'Stand disc'), ('star_platinum_core', 'Star Platinum'),
    ('hierophant_green_core', 'Hierophant Green'), ('silver_chariot_core', 'Silver Chariot'),
    ('magicians_red_core', "Magician's Red"), ('hermit_purple_core', 'Hermit Purple'),
    ('the_fool_core', 'The Fool'), ('the_world_core', 'The World'),
    ('echoes_act_1_core', 'Echoes ACT 1'), ('little_feet_core', 'Little Feet'),
]

def run(*args):
    return subprocess.run(['magick', *map(str, args)], check=True, capture_output=True).stdout

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')

def svg_export(path):
    """Horizontal runs of editable pixels; no hidden raster or remote inputs."""
    with Image.open(path) as image:
        assert image.mode == 'RGBA' and image.size == (SIZE, SIZE)
        pixels = image.load()
        rects = []
        for y in range(SIZE):
            x = 0
            while x < SIZE:
                r, g, b, a = pixels[x, y]
                start = x
                x += 1
                while x < SIZE and pixels[x, y] == (r, g, b, a):
                    x += 1
                assert a in (0, 255), 'Item edges must be binary alpha'
                if a:
                    rects.append(f'<rect x="{start}" y="{y}" width="{x-start}" height="1" fill="#{r:02x}{g:02x}{b:02x}"/>')
        bounds = image.getbbox()
        assert bounds and bounds[0] >= 3 and bounds[1] >= 3
        assert bounds[2] <= 61 and bounds[3] <= 61
    (ART / (path.stem + '.svg')).write_text(
        '<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64" shape-rendering="crispEdges">'
        + ''.join(rects) + '</svg>\n')

def main():
    source = ART / 'source-atlas.png'
    with Image.open(source) as image:
        width, height = image.size
        assert width == height
    manifest = {
        'version': '0.13.1',
        'authorship': "Original AI-assisted JoJo's Mod inventory artwork, generated with OpenAI image generation and exported from the retained master atlas. No sprites imported from another mod or anime frame.",
        'license': 'Project artwork distributed under MIT; character rights remain with their owners.',
        'source': 'art/items/source-atlas.png', 'source_sha256': digest(source),
        'source_size': [width, height], 'texture_size': [SIZE, SIZE], 'grid': [4, 4],
        'export': {'fit': [58, 58], 'filter': 'point', 'alpha_threshold': '50%',
                   'padding': 'centered transparent to 64x64', 'tool': run('-version').decode().splitlines()[0]},
        'items': {},
    }
    with tempfile.TemporaryDirectory() as tmp:
        tmp = Path(tmp)
        tiles = []
        for index, (name, title) in enumerate(ITEMS):
            x, y = index % 4, index // 4
            left, top = width*x//4, height*y//4
            right, bottom = width*(x+1)//4, height*(y+1)//4
            texture = ASSETS / 'textures/item' / (name + '.png')
            texture.parent.mkdir(parents=True, exist_ok=True)
            # Mechanical export: preserve the generated silhouette and palette.
            run(source, '-crop', f'{right-left}x{bottom-top}+{left}+{top}', '+repage',
                '-channel', 'A', '-threshold', '50%', '+channel', '-trim', '+repage',
                '-filter', 'point', '-resize', '58x58', '-channel', 'A', '-threshold', '50%', '+channel',
                '-background', 'none', '-gravity', 'center', '-extent', '64x64',
                '-strip', '-define', 'png:color-type=6', '-define', 'png:exclude-chunk=date,time', 'PNG32:' + str(texture))
            svg_export(texture)
            write_json(ASSETS / 'models/item' / (name + '.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'jojosmod:item/' + name}})
            write_json(ASSETS / 'items' / (name + '.json'), {'model': {'type': 'minecraft:model', 'model': 'jojosmod:item/' + name}})
            manifest['items'][name] = {'title': title, 'cell': [x, y], 'crop': [left, top, right, bottom],
                                      'texture': str(texture.relative_to(ROOT)), 'sha256': digest(texture), 'editable_svg': 'art/items/' + name + '.svg'}
            tile = tmp / f'{index:02}.png'
            panels = []
            for n, bg in enumerate(('#202638', '#d6d6df')):
                panel = tmp / f'{index:02}-{n}.png'
                run(texture, '-filter', 'point', '-resize', '128x128', '-background', bg, '-alpha', 'remove', '-alpha', 'off', panel)
                panels.append(panel)
            run(*panels, '+append', '-background', '#202638', '-gravity', 'south', '-splice', '0x22',
                '-font', 'DejaVu-Sans', '-pointsize', '13', '-fill', '#f1ebfa', '-annotate', '+0+4', title, tile)
            tiles.append(tile)
        run('montage', *tiles, '-tile', '4x', '-geometry', '+6+6', '-background', '#141a27',
            '-strip', '-define', 'png:exclude-chunk=date,time', ART / 'preview.png')
    write_json(ART / 'asset-provenance.json', manifest)
    print(f'Exported {len(ITEMS)} RGBA64 item textures, editable SVGs, model links, preview and hashes.')

if __name__ == '__main__':
    main()
