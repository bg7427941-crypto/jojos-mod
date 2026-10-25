#!/usr/bin/env python3
"""Draw original 32px JoJo's Mod item art from a fresh pixel canvas.

No source image is read or edited. Python standard library only. SVG pixel sources
and PNG runtime textures are written deterministically. Original art/code: MIT.
"""
from pathlib import Path
import json
import math
import struct
import zlib

ROOT = Path(__file__).resolve().parents[2]
ART = ROOT / 'art/items'
ASSETS = ROOT / 'src/main/resources/assets/jojosmod'
SIZE = 32
P = {
    'ink':'#17182c', 'shadow':'#303049', 'violet':'#7651ad', 'purple':'#ad78d5',
    'lavender':'#d3b0f1', 'pale':'#f4eafa', 'gold':'#d8a33c', 'darkgold':'#8b5d2d',
    'lightgold':'#ffe18a', 'white':'#f5f7ed', 'silver':'#aebdd3', 'steel':'#667d9a',
    'blue':'#7799c3', 'cyan':'#8ee1e3', 'teal':'#3e9f9a', 'red':'#ce5277',
    'pink':'#ff9cbd', 'green':'#42ad8a', 'jade':'#95e5a7', 'darkgreen':'#225760',
}

def rgba(color):
    color = P.get(color, color).lstrip('#')
    return tuple(int(color[i:i+2], 16) for i in (0, 2, 4)) + (255,)

class Canvas:
    def __init__(self, w=32, h=32, background=None):
        self.w, self.h = w, h
        self.pixels = [[rgba(background) if background else (0,0,0,0) for _ in range(w)] for _ in range(h)]
    def dot(self, x, y, color):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.pixels[y][x] = rgba(color) if color else (0,0,0,0)
    def rect(self, x, y, w, h, color):
        for yy in range(y, y+h):
            for xx in range(x, x+w): self.dot(xx, yy, color)
    def line(self, x0, y0, x1, y1, color, width=1):
        dx, dy = abs(x1-x0), -abs(y1-y0)
        sx, sy = 1 if x0<x1 else -1, 1 if y0<y1 else -1
        err = dx + dy
        while True:
            self.rect(x0-width//2, y0-width//2, width, width, color)
            if x0==x1 and y0==y1: break
            e2 = 2*err
            if e2>=dy: err += dy; x0 += sx
            if e2<=dx: err += dx; y0 += sy
    def poly(self, points, color):
        for y in range(max(0,math.floor(min(p[1] for p in points))), min(self.h,math.ceil(max(p[1] for p in points)))+1):
            for x in range(max(0,math.floor(min(p[0] for p in points))), min(self.w,math.ceil(max(p[0] for p in points)))+1):
                inside=False; j=len(points)-1
                for i in range(len(points)):
                    ax,ay=points[i];bx,by=points[j]
                    if (ay>y+.5)!=(by>y+.5) and x+.5<(bx-ax)*(y+.5-ay)/(by-ay)+ax: inside=not inside
                    j=i
                if inside:self.dot(x,y,color)
    def ellipse(self, cx, cy, rx, ry, color):
        for y in range(self.h):
            for x in range(self.w):
                if ((x+.5-cx)/rx)**2+((y+.5-cy)/ry)**2<=1:self.dot(x,y,color)
    def star(self, cx, cy, outer, inner, color, points=5, rotation=-math.pi/2):
        self.poly([(cx+math.cos(rotation+i*math.pi/points)*(outer if i%2==0 else inner),
                    cy+math.sin(rotation+i*math.pi/points)*(outer if i%2==0 else inner)) for i in range(points*2)],color)
    def png(self):
        def chunk(tag, data):return struct.pack('>I',len(data))+tag+data+struct.pack('>I',zlib.crc32(tag+data)&0xffffffff)
        raw=b''.join(b'\0'+b''.join(bytes(p) for p in row) for row in self.pixels)
        return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',self.w,self.h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw,9))+chunk(b'IEND',b'')
    def svg(self):
        rects=[]
        for y,row in enumerate(self.pixels):
            for x,p in enumerate(row):
                if p[3]:rects.append(f'<rect x="{x}" y="{y}" width="1" height="1" fill="#{p[0]:02x}{p[1]:02x}{p[2]:02x}"/>')
        return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {self.w} {self.h}" width="{self.w}" height="{self.h}" shape-rendering="crispEdges">'+''.join(rects)+'</svg>\n'

def glint(c,x,y,color='white'):
    c.dot(x,y,color);c.dot(x-1,y,color);c.dot(x+1,y,color);c.dot(x,y-1,color);c.dot(x,y+1,color)

def arrow(stardust=False):
    c=Canvas()
    # A long diagonal silhouette leaves a clean distinction from crystal cores.
    c.line(5,26,23,8,'ink',5)
    c.line(5,26,23,8,'darkgold' if not stardust else 'violet',3)
    c.line(5,25,22,8,'lightgold' if not stardust else 'silver')
    c.poly([(2,24),(5,24),(9,27),(8,30),(5,29),(2,27)],'ink')
    c.poly([(3,25),(5,25),(8,28),(7,29),(4,27)],'silver' if not stardust else 'lavender')
    c.line(4,25,7,28,'white')
    c.poly([(30,1),(27,15),(22,11),(17,6)],'ink')
    c.poly([(28,3),(26,12),(22,9),(19,6)],'darkgold')
    c.poly([(28,3),(25,10),(22,8),(20,6)],'gold')
    c.poly([(28,3),(24,8),(21,6)],'lightgold')
    c.line(27,4,24,9,'white')
    if stardust:
        c.star(23,9,8,3.8,'ink')
        c.star(23,9,6.3,3.1,'gold')
        c.star(23,9,4.8,2.1,'lightgold')
        c.poly([(23,5),(26,9),(23,13),(20,9)],'darkgold')
        c.poly([(23,6),(25,9),(23,11),(21,9)],'red')
        c.dot(23,7,'pink');c.dot(22,8,'white')
        c.line(9,21,11,23,'gold',2)
        c.line(14,16,16,18,'lightgold',2)
    else:
        c.line(22,11,25,8,'darkgold')
        c.dot(25,6,'white');c.line(10,20,12,22,'gold')
    return c

def disc(loaded=False):
    c=Canvas()
    for y in range(32):
        for x in range(32):
            dx,dy=x+.5-16,y+.5-16;radius=math.hypot(dx,dy)
            if radius>14:continue
            if radius>12.8:color='ink'
            elif radius>11.8:color='darkgold' if loaded else 'steel'
            elif loaded:
                color='violet' if dy<0 else 'shadow'
                if dx+dy<-7:color='lavender'
                elif dx+dy<-3:color='purple'
                elif dx+dy>10:color='violet'
                if 9.2<radius<9.8 or 6.7<radius<7.3:color='ink'
            else:
                color='silver' if dy<2 else 'blue'
                if dx+dy<-7:color='white'
                elif -4<dx+dy<-1:color='cyan'
                elif 3<dx+dy<6:color='lavender'
                if 9<radius<9.8 or 6.3<radius<7:color='steel'
            c.dot(x,y,color)
    if loaded:
        c.ellipse(16,16,5.6,5.6,'gold')
        c.ellipse(16,16,4.2,4.2,'ink')
        c.poly([(16,12),(20,16),(16,20),(12,16)],'cyan')
        c.poly([(16,14),(18,16),(16,18),(14,16)],'teal')
        c.rect(15,15,2,2,'white')
        for x,y in [(10,7),(24,11),(21,25)]:c.rect(x,y,2,2,'lightgold')
    else:
        c.ellipse(16,16,5,5,'steel');c.ellipse(16,16,3.8,3.8,'white');c.ellipse(16,16,2.6,2.6,'ink');c.ellipse(16,16,1.7,1.7,None)
    glint(c,8,7)
    return c

def fragment():
    c=Canvas()
    c.poly([(13,3),(22,3),(26,7),(22,11),(18,10),(14,12),(11,17),(13,21),(9,28),(5,24),(3,18),(4,10),(8,5)],'ink')
    c.poly([(13,5),(21,5),(23,7),(20,9),(17,8),(12,11),(9,16),(11,21),(8,25),(6,22),(5,17),(6,11),(10,7)],'steel')
    c.poly([(13,5),(21,5),(22,7),(16,7),(11,10),(8,15),(8,20),(6,20),(5,16),(7,10),(10,7)],'silver')
    c.poly([(11,7),(19,5),(22,6),(19,7),(15,8),(11,11),(8,16),(7,13),(9,9)],'white')
    c.poly([(7,16),(9,14),(10,17),(10,21),(8,25),(6,22)],'purple')
    c.line(7,22,8,24,'lavender');c.line(11,12,16,9,'cyan')
    c.poly([(24,16),(28,17),(27,22),(22,21)],'ink')
    c.poly([(25,17),(27,18),(26,20),(24,20)],'lavender')
    c.dot(25,18,'white')
    return c

def core(kind):
    c=Canvas()
    # A common socket says "core"; the gem, palette and glyph carry identity.
    c.poly([(8,23),(11,20),(21,20),(25,23),(23,29),(9,29),(6,26)],'ink')
    c.poly([(9,24),(13,22),(21,22),(23,24),(21,27),(10,27),(8,25)],'darkgold')
    c.line(10,23,21,23,'gold');c.rect(11,26,9,1,'lightgold')
    if kind=='star':
        c.star(16,13,13.5,7,'ink',points=6)
        c.star(16,13,11.4,6,'gold',points=6)
        c.poly([(16,3),(23,9),(22,18),(16,24),(9,18),(9,9)],'shadow')
        c.poly([(16,5),(21,10),(20,18),(16,21),(11,17),(11,10)],'violet')
        c.poly([(16,5),(20,10),(16,14),(11,10)],'lavender')
        c.poly([(11,11),(15,14),(15,20),(11,17)],'purple')
        c.star(16,13,6,2.4,'lightgold')
        c.star(16,13,3.2,1.2,'white')
        c.dot(16,6,'white');c.dot(24,11,'lightgold')
    elif kind=='echoes':
        c.ellipse(15,14,10.5,12,'ink');c.ellipse(15,14,8.8,10.4,'darkgreen');c.ellipse(14,13,7.5,9.3,'green')
        c.poly([(13,4),(18,6),(17,10),(12,13),(8,13),(9,8)],'jade')
        c.line(11,8,13,6,'white',2)
        # Two rising sound strokes, readable at a sixteen-pixel UI scale.
        c.line(11,16,14,13,'darkgreen',3);c.line(14,13,17,16,'darkgreen',3);c.line(17,16,20,13,'darkgreen',3)
        c.line(11,16,14,13,'pale');c.line(14,13,17,16,'pale');c.line(17,16,20,13,'pale')
        c.line(12,20,15,17,'jade');c.line(15,17,18,20,'jade')
        c.rect(23,9,2,3,'ink');c.rect(24,12,3,6,'ink');c.rect(23,18,2,3,'ink')
        c.line(24,12,25,13,'cyan');c.line(26,14,26,16,'cyan');c.line(24,19,25,18,'cyan')
    else:
        c.poly([(15,2),(24,7),(26,17),(20,24),(10,23),(5,17),(7,7)],'ink')
        c.poly([(15,4),(22,8),(23,16),(19,21),(11,21),(8,16),(9,8)],'violet')
        c.poly([(15,4),(21,8),(18,12),(10,10)],'pink')
        c.poly([(9,10),(14,12),(13,20),(10,18),(8,15)],'red')
        c.poly([(20,10),(23,16),(19,21),(14,21),(15,15)],'purple')
        c.ellipse(15,17,3.2,4,'ink');c.ellipse(15,16.5,2.1,3,'pale')
        c.ellipse(14,10.7,1.7,1.8,'ink');c.ellipse(17.7,11.3,1.5,1.5,'ink')
        c.dot(13,10,'white');c.dot(14,10,'white');c.dot(17,11,'white')
        c.dot(21,13,'lavender');c.dot(11,6,'white')
    c.rect(8,24,3,2,'gold');c.rect(21,24,3,2,'gold')
    return c

SPRITES = {
    'stand_arrow':arrow(), 'stardust_arrow':arrow(True), 'stand_disc_fragment':fragment(),
    'empty_stand_disc':disc(), 'stand_disc':disc(True),
    'star_platinum_core':core('star'), 'echoes_act_1_core':core('echoes'), 'little_feet_core':core('feet'),
}

def main():
    ART.mkdir(parents=True,exist_ok=True)
    for name,sprite in SPRITES.items():
        (ART/(name+'.svg')).write_text(sprite.svg())
        (ASSETS/'textures/item'/(name+'.png')).write_bytes(sprite.png())
        (ASSETS/'models/item'/(name+'.json')).write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'jojosmod:item/'+name}},indent=2)+'\n')
        (ASSETS/'items'/(name+'.json')).write_text(json.dumps({'model':{'type':'minecraft:model','model':'jojosmod:item/'+name}},indent=2)+'\n')
    # Gallery is generated from these same fresh canvases, never from image input.
    preview=Canvas(8*40,80,'#1d2538')
    for i,sprite in enumerate(SPRITES.values()):
        preview.rect(i*40,40,40,40,'#d8dce2')
        for y,row in enumerate(sprite.pixels):
            for x,p in enumerate(row):
                if p[3]:
                    preview.pixels[y+4][i*40+x+4]=p
                    preview.pixels[y+44][i*40+x+4]=p
    (ART/'preview.png').write_bytes(preview.png())
    print(f'{len(SPRITES)} original RGBA32 sprites, editable SVGs, item/model links and preview generated.')

if __name__=='__main__':main()
