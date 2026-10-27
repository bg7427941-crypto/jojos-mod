#!/usr/bin/env python3
"""Part 3 models and editable Blockbench sources, following the Star Platinum workflow.
Three credited Roundabout adaptations, two original sculptures and original action clips.
Requires Pillow; preview.py additionally requires NumPy.
"""
from pathlib import Path
import importlib.util,json,math,uuid,hashlib
import adapt_models
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2]; HERE=Path(__file__).resolve().parent
ASSETS=ROOT/'src/main/resources/assets/jojosmod'
spec=importlib.util.spec_from_file_location('cube_tools',ROOT/'art/hierophant_green/build_assets.py')
tools=importlib.util.module_from_spec(spec);spec.loader.exec_module(tools)
TOOLS=tools.export
KINDS=['silver_chariot','magicians_red','hermit_purple','the_fool','the_world']
PALETTES={
'silver_chariot':[(184,207,224),(229,238,239),(44,64,82),(157,221,246),(209,184,100)],
'magicians_red':[(194,48,38),(244,176,56),(52,26,31),(255,222,76),(235,91,32)],
'hermit_purple':[(114,44,151),(201,139,224),(42,20,56),(227,176,250),(164,82,192)],
'the_fool':[(187,156,97),(233,215,165),(44,51,57),(117,199,211),(147,126,77)],
'the_world':[(210,177,51),(248,224,112),(56,77,65),(112,220,168),(144,125,55)]}

def bones(m,quad=False):
    for name,pivot,parent in [('root',[0,0,0],None),('body',[0,12,0],'root'),('torso',[0,22,0],'body'),('head',[0,25,0],'torso')]:m.bone(name,pivot,parent)
    for side,p in [(-1,'right'),(1,'left')]:
        for name,pivot,parent in [(p+'_arm',[side*5,23,0],'torso'),(p+'_forearm',[side*5,18,0],p+'_arm'),(p+'_hand',[side*5,13,0],p+'_forearm'),(p+'_leg',[side*1.9,12,0],'body'),(p+'_shin',[side*1.9,6,0],p+'_leg')]:m.bone(name,pivot,parent)

def box(m,name,bone,pos,size,material='plate',rotation=None):m.cube(name,bone,pos,size,material,rotation)



def purple(m):
    # Actual vine manifestation, no humanoid placeholder body.
    for side,p in [(-1,'right'),(1,'left')]:
        for i in range(18):
            a=i*.68;x=side*5+math.cos(a)*1.45;y=10.6+i*.68;z=math.sin(a)*1.45
            bone=p+'_forearm' if i<9 else p+'_arm'
            box(m,f'{p}_vine_{i}',bone,[x-.575,y,z-.575],[1.15,1.35,1.15],'plate')
            if i%2==0:box(m,f'{p}_thorn_{i}',bone,[x+.3,y+.3,z-.2],[.5,.75,.5],'ivory',[0,0,side*-40])
        for i in range(4):box(m,f'{p}_tendril_{i}',p+'_hand',[side*5-.4,9-i*1.1,-1-i*.65],[.8,1.5,.8],'gold',[15+i*8,0,side*10])

def fool(m):
    box(m,'sand_body','body',[-4,5,-4],[8,6,9])
    box(m,'carapace','torso',[-4.5,9,-3.5],[9,3,7],'ivory')
    box(m,'neck','head',[-2.5,8.7,-6],[5,4,3])
    box(m,'mask','head',[-3.2,9,-8],[6.4,5,3.3],'ivory')
    box(m,'muzzle','head',[-2.5,8.8,-9.2],[5,2.2,2],'plate')
    box(m,'nose','head',[-1.2,10.3,-9.5],[2.4,1.1,.5],'dark')
    for side,p in [(-1,'right'),(1,'left')]:
        box(m,p+'_eye_socket','head',[side*1.8-.85,12,-8.2],[1.7,1,.5],'dark')
        box(m,p+'_eye','head',[side*1.8-.4,12.3,-8.6],[.8,.4,.5],'gem')
        box(m,p+'_horn','head',[side*3-.55,13,-6.6],[1.1,3.8,1.5],'plate',[0,0,side*-24])
        box(m,p+'_wing','torso',[side*6-2.5,8,0],[5,1.1,5],'ivory',[0,0,side*-18])
        box(m,p+'_hind_leg',p+'_leg',[side*3-1.2,2,2],[2.4,5,2.4],'plate')
        box(m,p+'_front_leg',p+'_arm',[side*3-1.2,2,-4.3],[2.4,5,2.4],'plate')
        for axle,bone,z in [('front',p+'_arm',-3.3),('back',p+'_leg',3.3)]:
            box(m,p+'_'+axle+'_wheel',bone,[side*4-1,0,z-1.8],[2,3.6,3.6],'dark')
            box(m,p+'_'+axle+'_hub',bone,[side*4-1.2,.8,z-1],[2.4,2,2],'gem')
        box(m,p+'_fang','head',[side*1.7-.4,8,-9.1],[.8,1.5,.8],'ivory')
    for i in range(4):box(m,f'spine_{i}','torso',[-.55,11.5,-1+i*1.7],[1.1,1.8,1.1],'gold')
    box(m,'tail','body',[-.8,6,4.5],[1.6,1.6,5],'plate',[-16,0,0])


GEOMETRY={'hermit_purple':purple,'the_fool':fool}
def keys(*pairs):return {str(t):v for t,v in pairs}
def clips(kind,model):
    root_idle={'root':{'position':keys((0,[0,0,0]),(1.2,[0,.25,0]),(2.4,[0,0,0]))}}
    move={'root':{'rotation':[5,0,0]}}
    for side,p in [(-1,'right'),(1,'left')]:
        move[p+'_leg']={'rotation':keys((0,[side*15,0,0]),(.4,[-side*15,0,0]),(.8,[side*15,0,0]))}
        move[p+'_arm']={'rotation':keys((0,[-side*10,0,0]),(.4,[side*10,0,0]),(.8,[-side*10,0,0]))}
    wind={'silver_chariot':.15,'magicians_red':.3,'hermit_purple':.3,'the_fool':.3,'the_world':.2}[kind]
    attack={'torso':{'rotation':keys((0,[0,0,0]),(wind*.6,[0,-14,0]),(wind,[0,14,0]),(.6,[0,0,0]))}}
    attack['right_arm']={'rotation':keys((0,[-10,0,-5]),(wind*.6,[-30,15,-25]),(wind,[-92,0,-5]),(.6,[-10,0,-5]))}
    attack['right_forearm']={'rotation':keys((0,[0,0,0]),(wind*.6,[-65,0,0]),(wind,[-4,0,0]),(.6,[0,0,0]))}
    if kind=='silver_chariot':
        attack['right_arm']={'rotation':keys((0,[-5,0,-5]),(.1,[-14,8,-12]),(.15,[0,0,-5]),(.6,[-5,0,-5]))}
        attack['right_forearm']={'rotation':keys((0,[-8,0,0]),(.1,[-18,0,0]),(.15,[0,0,0]),(.6,[-8,0,0]))}
        attack['rapier']={'position':keys((0,[0,0,0]),(.1,[0,0,1]),(.15,[0,0,-1.5]),(.6,[0,0,0]))}
    if kind in ['magicians_red','the_fool','hermit_purple']:
        attack['left_arm']={'rotation':keys((0,[-10,0,5]),(wind*.6,[-40,0,25]),(wind,[-85,0,5]),(.6,[-10,0,5]))}
    duration={'silver_chariot':.95,'magicians_red':2.25,'hermit_purple':.6,'the_fool':2,'the_world':1.35}[kind]
    secondary={}
    if kind in ['silver_chariot','the_world']:
        for side,p in [(-1,'right'),(1,'left')]:
            track={'0':[-10,0,side*6]}
            for i in range(6 if kind=='silver_chariot' else 10):track[str(round(.15+i*.1,3))]=[-91 if (i+side)%2 else -40,0,side*7]
            track[str(duration)]=[-10,0,side*6]
            secondary[p+'_arm']={'rotation':track}
    else:
        for side,p in [(-1,'right'),(1,'left')]:secondary[p+'_arm']={'rotation':keys((0,[-10,0,side*5]),(.25,[-85,0,side*20]),(max(.35,duration-.15),[-85,0,side*20]),(duration,[-10,0,side*5]))}
    if kind=='silver_chariot':
        thrust={'0':[0,0,0]}
        for i in range(6):
            t=round(.15+i*.1,3);thrust[str(round(t-.04,3))]=[0,0,1];thrust[str(t)]=[0,0,-1.5]
        thrust[str(duration)]=[0,0,0]
        secondary={'rapier':{'position':thrust},'right_arm':{'rotation':[-6,0,-5]},'right_forearm':{'rotation':[-8,0,0]},'left_arm':{'rotation':[-15,5,12]}}
    utility_duration=3 if kind=='the_fool' else 2 if kind in ['magicians_red','the_world'] else .6
    utility={'right_arm':{'rotation':keys((0,[0,0,0]),(.2,[-65,-25,-15]),(utility_duration-.1,[-65,-25,-15]),(utility_duration,[0,0,0]))},'left_arm':{'rotation':keys((0,[0,0,0]),(.2,[-65,25,15]),(utility_duration-.1,[-65,25,15]),(utility_duration,[0,0,0]))}}
    if kind=='the_fool':utility['torso']={'rotation':keys((0,[0,0,0]),(.25,[-15,0,0]),(2.8,[-15,0,0]),(3,[0,0,0]))}
    data={'idle':(2.4,True,root_idle),'move':(.8,True,move),'attack':(.6,False,attack),'secondary':(duration,False,secondary),'utility':(utility_duration,False,utility),
    'summon':(.25,False,{'root':{'scale':keys((0,[.02]*3),(.25,[1]*3)),'position':keys((0,[0,-2,0]),(.25,[0,0,0]))}}),
    'dismiss':(.2,False,{'root':{'scale':keys((0,[1]*3),(.2,[.01]*3))}}),
    'recover':(.2,False,{'right_arm':{'rotation':keys((0,[-45,0,-8]),(.2,[0,0,0]))},'left_arm':{'rotation':keys((0,[-45,0,8]),(.2,[0,0,0]))}}),
    'time_stop':(.7,False,{'torso':{'rotation':keys((0,[0,0,0]),(.3,[-12,0,0]),(.7,[0,0,0]))},'right_arm':{'rotation':keys((0,[0,0,0]),(.3,[-50,0,-80]),(.7,[0,0,0]))},'left_arm':{'rotation':keys((0,[0,0,0]),(.3,[-50,0,80]),(.7,[0,0,0]))}})}
    if kind=='silver_chariot':
        armor={b['name'] for b in model.sculpture.bones if b['name'].startswith('armor_')}
        for name,scale in [('armor_on',1),('armor_off',.001)]:data[name]=(1,True,{b:{'scale':[scale]*3} for b in sorted(armor)})
    return {'format_version':'1.8.0','animations':{'animation.'+kind+'.'+n:{'animation_length':length,'loop':loop,'bones':b} for n,(length,loop,b) in data.items()}}
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(TOOLS.tidy(data),ensure_ascii=False,indent=2)+'\n')
def sprite(kind,palette):
    img=Image.new('RGBA',(32,32));d=ImageDraw.Draw(img);a,b,c,e,g=palette
    d.ellipse((2,2,29,29),fill=c,outline=b,width=2)
    if kind=='silver_chariot':
        d.line((9,25,24,6),fill=b,width=3);d.line((6,18,15,25),fill=g,width=2);d.line((9,24,7,27),fill=e,width=2)
    elif kind=='magicians_red':d.polygon([(15,5),(13,13),(6,18),(12,27),(21,26),(27,17),(21,19),(20,9),(16,15)],fill=a,outline=b);d.polygon([(16,14),(12,22),(16,26),(21,21)],fill=e)
    elif kind=='hermit_purple':
        d.arc((7,5,23,19),10,300,fill=a,width=3);d.arc((7,13,25,27),120,410,fill=b,width=3)
        for x,y in [(11,9),(21,15),(13,24)]:d.polygon([(x,y),(x-3,y-3),(x-3,y+2)],fill=g)
    elif kind=='the_fool':
        d.polygon([(6,10),(11,6),(15,11),(22,7),(26,12),(24,24),(15,27),(8,23)],fill=a,outline=b);d.rectangle((9,14,13,17),fill=c);d.rectangle((19,14,23,17),fill=c);d.polygon([(12,21),(20,21),(16,25)],fill=e)
    else:
        d.ellipse((7,6,25,24),fill=a,outline=b,width=2);d.line((16,9,16,16,22,16),fill=c,width=2);d.polygon([(10,24),(14,22),(17,25),(21,22),(24,25),(17,29)],fill=e)
    return img

def main():
    manifest={'version':'0.12.0','license':'Roundabout License v1.2 for the three adapted models/atlases; MIT for original assets, animations and generators; character rights retained by their owners','authorship':'Three pinned Roundabout geometries/UVs/anime atlases adapted through the Star Platinum conversion workflow; original Hermit Purple/The Fool sculptures, original action keyframes and core sprites. No upstream animation keyframes imported.','models':{},'files':{},'upstream':adapt_models.pinned_sources()}
    for kind in KINDS:
        TOOLS.stable_id=lambda name,k=kind:str(uuid.uuid5(uuid.NAMESPACE_URL,'jojosmod/part-three/v1/'+k+'/'+name))
        palette=PALETTES[kind];tools.PALETTE=dict(zip(['plate','ivory','dark','gem','gold'],palette));tools.SIZE=512
        model=tools.Model();model.atlas=Image.new('RGBA',(512,512));bones(model)
        if kind=='the_fool':
            pivots={'body':[0,7,0],'torso':[0,10,0],'head':[0,11,-6]}
            for side,prefix in [(-1,'right'),(1,'left')]:
                for limb,y,z in [('arm',6,-3.3),('forearm',3,-3.3),('hand',1,-3.3),('leg',6,3.3),('shin',2,3.3)]:pivots[prefix+'_'+limb]=[side*3,y,z]
            for bone in model.sculpture.bones:
                if bone['name'] in pivots:bone['pivot']=pivots[bone['name']]
        if kind in adapt_models.ADAPTED: model=adapt_models.sculpt(kind,model)
        else: GEOMETRY[kind](model)
        animation=clips(kind,model)
        folder=HERE/kind;folder.mkdir(exist_ok=True)
        tex=ASSETS/f'textures/entity/{kind}.png'
        if kind in adapt_models.ADAPTED:
            tex.write_bytes((adapt_models.HERE/f'upstream/{kind}.png').read_bytes())
            (folder/f'{kind}.png').write_bytes(tex.read_bytes())
        else:
            model.atlas.save(tex);model.atlas.save(folder/f'{kind}.png')
        geo=ASSETS/f'geckolib/models/entity/{kind}.geo.json';anim=ASSETS/f'geckolib/animations/entity/{kind}.animation.json'
        write(geo,{'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.'+kind,'texture_width':model.atlas.width,'texture_height':model.atlas.height,'visible_bounds_width':3,'visible_bounds_height':3,'visible_bounds_offset':[0,1,0]},'bones':model.sculpture.bones}]})
        write(anim,animation)
        project=TOOLS.blockbench(model.sculpture,animation,tex.read_bytes(),model.atlas.width);project.update(name=kind.replace('_',' ').title(),model_identifier=kind,credit=adapt_models.CREDIT if kind in adapt_models.ADAPTED else "Original JoJo's Mod model/atlas/keyframes. Character by Hirohiko Araki.");project['textures'][0].update(name=kind+'.png',path=kind+'.png');write(folder/f'{kind}.bbmodel',project)
        icon=ASSETS/f'textures/item/{kind}_core.png';sprite(kind,palette).save(icon)
        write(ASSETS/f'items/{kind}_core.json',{'model':{'type':'minecraft:model','model':'jojosmod:item/'+kind+'_core'}})
        write(ASSETS/f'models/item/{kind}_core.json',{'parent':'minecraft:item/generated','textures':{'layer0':'jojosmod:item/'+kind+'_core'}})
        for p in [geo,anim,tex,icon]:manifest['files'][str(p.relative_to(ROOT))]=hashlib.sha256(p.read_bytes()).hexdigest()
        manifest['models'][kind]={'cubes':len(model.sculpture.sources),'bones':len(model.sculpture.bones),'clips':len(animation['animations']),'source':kind+'/'+kind+'.bbmodel','atlas':[model.atlas.width,model.atlas.height],'credit':adapt_models.CREDIT if kind in adapt_models.ADAPTED else "Original JoJo's Mod geometry/atlas/animation" }
        print(kind,manifest['models'][kind])
    write(HERE/'asset-provenance.json',manifest)
if __name__=='__main__':main()
