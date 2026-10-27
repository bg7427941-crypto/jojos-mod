#!/usr/bin/env python3
"""Render actual runtime geometry/atlas/keyframes; not a replacement for GeckoLib validation."""
from pathlib import Path
import importlib.util,json
import numpy as np
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent;A=ROOT/'src/main/resources/assets/jojosmod'
spec=importlib.util.spec_from_file_location('preview',ROOT/'art/star_platinum/preview_model.py');preview=importlib.util.module_from_spec(spec);spec.loader.exec_module(preview)
KINDS=['silver_chariot','magicians_red','hermit_purple','the_fool','the_world']
def main():
    overview=Image.new('RGB',(1680,650*len(KINDS)),(224,223,218))
    for row,kind in enumerate(KINDS):
        bones=json.loads((A/f'geckolib/models/entity/{kind}.geo.json').read_text())['minecraft:geometry'][0]['bones']
        clips=json.loads((A/f'geckolib/animations/entity/{kind}.animation.json').read_text())['animations'];atlas=np.array(Image.open(A/f'textures/entity/{kind}.png').convert('RGBA'))
        sheet=Image.new('RGB',(1680,650),(224,223,218));d=ImageDraw.Draw(sheet)
        for i,(yaw,label,pose,time) in enumerate([(0,'FRONT','idle',.2),(28,'THREE QUARTER','idle',.2),(90,'SIDE','idle',.2),(28,'ATTACK','attack',.3)]):
            sheet.paste(preview.render(bones,atlas,clips[f'animation.{kind}.{pose}'],time,yaw),(i*420,25));d.text((i*420+18,8),kind.upper()+' / '+label,fill=(38,35,47))
        sheet.save(HERE/kind/'preview.png');overview.paste(sheet,(0,row*650));print(kind)
    overview.save(HERE/'overview.png')
if __name__=='__main__':main()
