#!/usr/bin/env python3
"""Original player-proportioned DIO and robed service NPC, with editable GeckoLib rigs.
Creates new geometry/texture canvases; only the project's MIT export helpers are reused.
"""
from pathlib import Path
import importlib.util,sys,json,hashlib,uuid
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
ASSETS=ROOT/'src/main/resources/assets/jojosmod'
spec=importlib.util.spec_from_file_location('journey_mesh',ROOT/'art/hierophant_green/build_assets.py')
tools=importlib.util.module_from_spec(spec);spec.loader.exec_module(tools)
sys.path.insert(0,str(ROOT/'art/part_three'));import motion
SIZE=256;tools.SIZE=SIZE
tools.PALETTE={'skin':(217,170,126),'hair':(235,196,67),'gold':(208,155,39),'cloth':(32,31,39),'green':(76,170,107),'eye':(190,40,37),'ivory':(225,218,192),'robe':(87,65,111),'dark':(22,21,32),'plate':(170,141,71)}
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(tools.export.tidy(data),ensure_ascii=False,indent=2)+'\n')
def rig():
 m=tools.Model()
 for n,p,parent in [('root',[0,0,0],None),('body',[0,12,0],'root'),('torso',[0,22,0],'body'),('head',[0,25,0],'torso')]:m.bone(n,p,parent)
 for side,name in [(-1,'right'),(1,'left')]:
  for n,p,parent in [(name+'_arm',[side*5,23,0],'torso'),(name+'_forearm',[side*5,18,0],name+'_arm'),(name+'_hand',[side*5,13,0],name+'_forearm'),(name+'_leg',[side*2,12,0],'body'),(name+'_shin',[side*2,6,0],name+'_leg')]:m.bone(n,p,parent)
 return m

def model(kind):
 m=rig();dio=kind=='dio';coat='gold' if dio else 'robe'
 m.cube('hips','body',[-4,10,-2],[8,3,4],coat)
 m.cube('undershirt','torso',[-3.3,13,-1.9],[6.6,10,3.8],'cloth')
 for side,name in [(-1,'right'),(1,'left')]:
  m.cube(name+'_jacket','torso',[side*3.1-1.1,13,-2.1],[2.2,10.8,4.4],coat)
  m.cube(name+'_lapel','torso',[side*2.5-.65,18.3,-2.5],[1.3,5.6,.6],coat,[0,0,side*14])
  m.cube(name+'_deltoid',name+'_arm',[side*5-1.7,20,-2],[3.4,4,4],coat)
  m.cube(name+'_bicep',name+'_arm',[side*5-1.4,18,-1.7],[2.8,3,3.4],coat)
  m.cube(name+'_forearm',name+'_forearm',[side*5-1.35,13,-1.6],[2.7,5,3.2],'skin' if dio else coat)
  m.cube(name+'_cuff',name+'_hand',[side*5-1.45,12.2,-1.75],[2.9,1,3.5],'green' if dio else 'plate')
  m.cube(name+'_hand',name+'_hand',[side*5-1.25,10,-1.5],[2.5,2.3,3],'skin')
  m.cube(name+'_thigh',name+'_leg',[side*2-1.7,6,-1.8],[3.4,6,3.6],coat)
  m.cube(name+'_shin',name+'_shin',[side*2-1.5,1,-1.65],[3,5,3.3],coat)
  m.cube(name+'_boot',name+'_shin',[side*2-1.65,0,-2.4],[3.3,2,4.1],'cloth' if dio else 'dark')
  if dio:m.cube(name+'_knee',''+name+'_shin',[side*2-1.25,5.3,-2.1],[2.5,1.7,.6],'green')
 m.cube('neck','head',[-1.4,23,-1.2],[2.8,2.7,2.4],'skin')
 m.cube('face','head',[-3.2,25,-2.8],[6.4,6.2,5.6],'skin')
 m.cube('nose','head',[-.45,27,-3.2],[.9,1.3,.6],'skin')
 m.cube('mouth','head',[-1,26,-2.96],[2,.18,.13],'dark')
 for side,name in [(-1,'right'),(1,'left')]:
  m.cube(name+'_eye_white','head',[side*1.4-.7,28.6,-2.97],[1.4,.65,.2],'ivory')
  m.cube(name+'_iris','head',[side*1.4-.24,28.7,-3.19],[.48,.45,.13],'eye' if dio else 'dark')
  m.cube(name+'_brow','head',[side*1.4-.8,29.4,-3.06],[1.6,.25,.2],'hair' if dio else 'dark',[0,0,side*8])
 if dio:
  m.cube('hair_cap','head',[-3.35,30.3,-2.75],[6.7,1.5,5.7],'hair')
  m.cube('hair_back','head',[-3.4,27.5,2.25],[6.8,4.4,1.2],'hair')
  m.cube('headband','head',[-3.45,29.65,-2.95],[6.9,.5,6],'green')
  for i,(x,z) in enumerate([(-2.7,-2),(-1.4,-2.2),(0,-2.1),(1.3,-2),(2.6,-1.7),(-2.4,.5),(0,1.2),(2.3,.7)]):
   m.cube('hair_lock_'+str(i),'head',[x-.6,30.7,z-.65],[1.2,1.8,1.3],'hair',[0,0,(i%3-1)*17])
  m.cube('waist_band','body',[-4.1,11,-2.1],[8.2,.75,4.2],'green')
  m.cube('heart_upper_l','body',[-1.1,10.9,-2.45],[1.1,.9,.5],'green',[0,0,-22])
  m.cube('heart_upper_r','body',[0,10.9,-2.45],[1.1,.9,.5],'green',[0,0,22])
  m.cube('heart_point','body',[-.55,10.15,-2.45],[1.1,.9,.5],'green',[0,0,45])
 else:
  m.cube('robe_skirt','body',[-4.3,2.05,-2.4],[8.6,10.45,4.8],'robe')
  m.cube('robe_front','body',[-.45,2.05,-2.55],[.9,10.75,.35],'plate')
  m.cube('hood_cap','head',[-3.8,30.7,-3.4],[7.6,1.6,7.1],'robe')
  m.cube('hood_back','head',[-3.8,24.7,2.7],[7.6,6.1,1],'robe')
  for side,name in [(-1,'right'),(1,'left')]:m.cube(name+'_hood','head',[side*3.4-.45,24.5,-3.4],[.9,6.8,6.2],'robe')
  m.cube('medallion','torso',[-.9,19,-2.4],[1.8,1.8,.5],'plate',[0,0,45])
 return m

def clips(kind,m):
 base={b['name']:{'rotation':[0,0,0]} for b in m.sculpture.bones};base['root'].update(position=[0,0,0],scale=[1,1,1])
 idle=motion.pose(base,{'head':[1,2,0],'left_arm':[-3,0,2],'right_arm':[-3,0,-2]})
 move=motion.pose(base,{'right_arm':[25,0,0],'left_arm':[-25,0,0],'right_leg':[-25,0,0],'left_leg':[25,0,0]})
 other=motion.pose(base,{'right_arm':[-25,0,0],'left_arm':[25,0,0],'right_leg':[25,0,0],'left_leg':[-25,0,0]})
 attack=motion.pose(base,{'torso':[-6,12,0],'right_arm':[-80,0,-12],'right_forearm':[-12,0,0]})
 knives=motion.pose(base,{'torso':[0,-18,0],'left_arm':[-70,0,45],'right_arm':[-70,0,-45]})
 time=motion.pose(base,{'torso':[-8,0,0],'head':[-8,0,0],'left_arm':[-30,0,75],'right_arm':[-30,0,-75]})
 data={'idle':motion.bake(2.4,[(0,base),(1.2,idle),(2.4,base)],True),'move':motion.bake(.8,[(0,move),(.4,other),(.8,move)],True),
 'attack':motion.bake(1.2,[(0,base),(.35,motion.pose(base,{'right_arm':[-45,25,-20]})),(.6,attack),(1.2,base)]),
 'knives':motion.bake(1.8,[(0,base),(.7,knives),(1,attack),(1.8,base)]),'time_stop':motion.bake(2,[(0,base),(.9,time),(1.4,time),(2,base)])}
 return {'format_version':'1.8.0','animations':{'animation.'+kind+'.'+k:v for k,v in data.items()}}

def main():
 manifest={'version':'0.14.0','license':'MIT for original project art/generator; character rights retained by Hirohiko Araki and their holders','authorship':'Original player-based DIO and robed fictional disc service NPC. New cube geometry, UV palette atlas and keyframes; no geometry or pixels imported from other mods.','references':['https://jojo-portal.com/anime/sc/character/07/'],'files':{},'models':{}}
 for kind in ['dio','disc_keeper']:
  tools.export.stable_id=lambda name,k=kind:str(uuid.uuid5(uuid.NAMESPACE_URL,'jojosmod/journey/v1/'+k+'/'+name))
  m=model(kind);a=clips(kind,m);folder=HERE/kind;folder.mkdir(parents=True,exist_ok=True)
  tex=ASSETS/f'textures/entity/{kind}.png';m.atlas.save(tex);m.atlas.save(folder/f'{kind}.png')
  geo=ASSETS/f'geckolib/models/entity/{kind}.geo.json';anim=ASSETS/f'geckolib/animations/entity/{kind}.animation.json'
  write(geo,{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.'+kind,'texture_width':SIZE,'texture_height':SIZE,'visible_bounds_width':3,'visible_bounds_height':3,'visible_bounds_offset':[0,1,0]},'bones':m.sculpture.bones}]})
  write(anim,a);project=tools.export.blockbench(m.sculpture,a,tex.read_bytes(),SIZE);project.update(name=kind,model_identifier=kind,credit='Original JoJo\'s Mod geometry, texture and keyframes. DIO by Hirohiko Araki.');project['textures'][0].update(name=kind+'.png',path=kind+'.png');write(folder/f'{kind}.bbmodel',project)
  for p in [geo,anim,tex]:manifest['files'][str(p.relative_to(ROOT))]=hashlib.sha256(p.read_bytes()).hexdigest()
  manifest['models'][kind]={'cubes':len(m.sculpture.sources),'bones':len(m.sculpture.bones),'source':str((folder/f'{kind}.bbmodel').relative_to(ROOT))}
 write(HERE/'asset-provenance.json',manifest);print(manifest['models'])
if __name__=='__main__':main()
