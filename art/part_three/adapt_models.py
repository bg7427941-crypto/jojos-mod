#!/usr/bin/env python3
"""Pinned Roundabout shape/UV conversion, using the same audited transform path as Star Platinum.
Upstream resources retain Roundabout License v1.2. No upstream animation keyframes are used.
"""
from pathlib import Path
import sys,json,hashlib,re
import numpy as np
from PIL import Image
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT/'art/star_platinum'))
from build_roundabout import convert_cube,euler
from preview_model import rotation,translation
from model_export import Sculpture
ADAPTED={'silver_chariot','magicians_red','the_world'}
CREDIT='Geometry, UVs and Part 3 anime atlas adapted from Roundabout by Hydraheads and contributors; JoJo\'s Mod rig adaptation and original action keyframes. Character by Hirohiko Araki.'

def pinned_sources():
    data=json.loads((HERE/'upstream/sources.json').read_text())
    for f in data['files']:
        b=(ROOT/f['local']).read_bytes();sha=hashlib.sha1(f'blob {len(b)}\0'.encode()+b).hexdigest()
        assert sha==f['sha'],f['local']
    return data

def destination(path):
    names=[x.lower() for x in path];joined='/'.join(names)
    if any(x in names for x in ['bam','kick_barrage']) or 'left_sword' in names:return None
    if 'right_sword' in names or 'right_blade' in names or 'right_tip' in names:return 'rapier'
    if 'head' in joined or 'beak' in joined:return 'head'
    for side in ['right','left']:
        if side+'_arm' in joined:return side+('_forearm' if 'lower_'+side+'_arm' in joined else '_arm')
        if side+'_leg' in joined:return side+('_shin' if 'lower_'+side+'_leg' in joined else '_leg')
    if 'lower_torso' in names or 'belt' in names or 'lower_armor' in names:return 'body'
    return 'torso'

def add(model,path,name,cube,armor=False):
    base=destination(path)
    if base is None:return
    bone=base
    if armor:
        bone='armor_'+base
        if bone not in model.sculpture.lookup:model.bone(bone,model.sculpture.lookup[base]['pivot'],base)
    model.sculpture.lookup[bone].setdefault('cubes',[]).append(cube)
    model.sculpture.sources.append({'name':name,'bone':bone,'cube':cube})

def adapt_bbmodel(kind,model):
    source=json.loads((HERE/f'upstream/{kind}.bbmodel').read_text())
    groups={g['uuid']:g for g in source.get('groups',[])};elements={e['uuid']:e for e in source['elements']}
    def walk(nodes,path=(),transform=None):
        transform=np.eye(4) if transform is None else transform
        for node in nodes:
            if isinstance(node,str):
                element=elements[node]
                if element.get('export',True):add(model,path,'roundabout_'+node,convert_cube(element,transform))
            else:
                group=groups.get(node.get('uuid'),node);name=group['name']
                if name in ['BAM','kick_barrage']:continue
                pivot=np.array(group.get('origin',[0,0,0]),dtype=float)
                local=translation(pivot)@rotation(group.get('rotation',[0,0,0]))@translation(-pivot)
                walk(node.get('children',group.get('children',[])),path+(name,),transform@local)
    walk(source['outliner'])

def box_uv(u,v,dx,dy,dz,mirror):
    faces={'north':([u+dz,v+dz],[dx,dy]),'south':([u+2*dz+dx,v+dz],[dx,dy]),
           'west':([u,v+dz],[dz,dy]),'east':([u+dz+dx,v+dz],[dz,dy]),
           'up':([u+dz+dx,v+dz],[-dx,-dz]),'down':([u+2*dx+dz,v+dz],[-dx,-dz])}
    if mirror:
        faces['east'],faces['west']=faces['west'],faces['east']
        for face in ['north','south','up','down']:
            xy,size=faces[face];xy[0]+=size[0];size[0]*=-1
    return {name:{'uv':xy,'uv_size':size} for name,(xy,size) in faces.items()}

def adapt_chariot(model):
    source=(HERE/'upstream/SilverChariotModel.java').read_text()
    pattern=r'PartDefinition\s+(\w+)\s*=\s*(\w+)\.addOrReplaceChild\("([^"]+)",\s*(.*?),\s*PartPose\.(offset(?:AndRotation)?)\((.*?)\)\);'
    nodes={'partdefinition':(np.eye(4),())};count=0
    for variable,parent,name,builder,pose,values in re.findall(pattern,source,re.S):
        args=[float(x.strip().rstrip('Ff')) for x in values.split(',')]
        matrix=translation(args[:3])
        if len(args)==6:matrix=matrix@rotation(np.degrees(args[3:]))
        world,path=nodes[parent];world=world@matrix;path=path+(name,);nodes[variable]=(world,path)
        if destination(path) is None:continue
        for index,chunk in enumerate(re.split(r'\.texOffs\(',builder)[1:]):
            uv,rest=chunk.split(')',1);u,v=map(float,uv.split(','))
            match=re.search(r'\.addBox\((.*?),\s*new CubeDeformation\((.*?)\)\)',rest,re.S)
            assert match,(variable,chunk)
            nums=[float(x.strip().rstrip('Ff')) for x in match[1].split(',')]
            assert len(nums)==6
            low=np.array(nums[:3]);size=np.array(nums[3:]);inflate=float(match[2].rstrip('Ff'));high=low+size
            center=(world@np.append((low+high)/2,1))[:3];center[1]=24-center[1]
            cube={'origin':(center-(size+inflate*2)/2).tolist(),'size':(size+inflate*2).tolist(),
                  'uv':box_uv(u,v,*size,mirror='.mirror()' in rest[:match.start()])}
            assert all(x>=0 for x in cube['size'])
            angles=euler(world)*[-1,1,-1]
            if np.max(np.abs(angles))>1e-7:cube.update(rotation=angles.tolist(),pivot=center.tolist())
            add(model,path,'roundabout_'+name+'_'+str(index),cube,armor='armor' in name.lower())
            count+=1
    assert count>40,('Incomplete Java model import',count)

def sculpt(kind,model):
    pinned_sources()
    model.sculpture=Sculpture()
    for name,pivot,parent in [('root',[0,0,0],None),('body',[0,12,0],'root'),('torso',[0,18,0],'body'),('head',[0,24.15,0],'torso')]:model.bone(name,pivot,parent)
    for side,p in [(-1,'right'),(1,'left')]:
        for name,pivot,parent in [(p+'_arm',[side*4,23.25,0],'torso'),(p+'_forearm',[side*6,17.75,0],p+'_arm'),(p+'_hand',[side*6,13,0],p+'_forearm'),(p+'_leg',[side*2,13,0],'body'),(p+'_shin',[side*2,6,0],p+'_leg')]:model.bone(name,pivot,parent)
    if kind=='silver_chariot':
        model.bone('rapier',[-5.5,12.5,0],'right_forearm');adapt_chariot(model)
        # Replace source alpha blade planes with a readable original 3D rapier.
        model.sculpture.lookup['rapier']['cubes']=[]
        model.sculpture.sources=[item for item in model.sculpture.sources if item['bone']!='rapier']
        atlas=np.array(Image.open(HERE/'upstream/silver_chariot.png').convert('RGBA'))
        def solid_uv(color):
            distance=np.sum((atlas[:,:,:3].astype(float)-color)**2,axis=2);distance[atlas[:,:,3]<255]=1e12
            y,x=np.unravel_index(np.argmin(distance),distance.shape)
            return {face:{'uv':[int(x),int(y)],'uv_size':[1,1]} for face in ['north','south','east','west','up','down']}
        for name,origin,size,color in [
            ('grip',[-5.85,12.15,-1.2],[.7,.7,2.6],[46,65,76]),
            ('guard',[-6.7,11.3,-1.8],[2.4,2.4,.65],[169,199,211]),
            ('blade',[-5.65,12.35,-14],[.3,.3,12.2],[227,240,246]),
            ('tip',[-5.58,12.42,-15.1],[.16,.16,1.1],[204,230,239])]:
            add(model,('right_arm','right_sword'),'original_rapier_'+name,{'origin':origin,'size':size,'uv':solid_uv(color)})
    else:adapt_bbmodel(kind,model)
    model.atlas=Image.open(HERE/f'upstream/{kind}.png').convert('RGBA')
    return model
