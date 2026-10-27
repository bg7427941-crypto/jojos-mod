#!/usr/bin/env python3
"""Synthesize original positional cues for five material identities; no recording inputs."""
from array import array
import math,random,subprocess,json,hashlib,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
KINDS=['silver_chariot','magicians_red','hermit_purple','the_fool','the_world'];RATE=48000

def pcm(kind,cue,duration,seed):
    rng=random.Random(seed);samples=[];air=0
    for i in range(round(RATE*duration)):
        t=i/RATE;u=t/duration;air=.7*air+.3*rng.uniform(-1,1)
        env=min(1,t/.004)*min(1,(duration-t)/.025)
        decay=math.exp(-(3 if cue in ['summon','cast'] else 8)*u)
        if kind=='silver_chariot':value=sum(math.sin(2*math.pi*f*t)/(j+2) for j,f in enumerate([710,1331,2197,3511]))*decay+air*.16*math.exp(-14*u)
        elif kind=='magicians_red':value=(air*(.8+.2*math.sin(2*math.pi*38*t))+.23*math.sin(2*math.pi*(85*t+60*t*t)))*decay
        elif kind=='hermit_purple':value=(.5*air*math.sin(2*math.pi*61*t)+.28*math.sin(2*math.pi*(490*t-240*t*t))+.17*math.sin(2*math.pi*980*t))*decay
        elif kind=='the_fool':value=(air*.9+.22*math.sin(2*math.pi*104*t))*decay
        else:value=(.5*math.sin(2*math.pi*(150*t+150*t*t))+.18*math.sin(2*math.pi*307*t)+air*.35)*decay
        if cue=='dismiss':value*=1-u
        if cue=='time_stop':value=(math.sin(2*math.pi*(570*t-230*t*t))*.45+math.sin(2*math.pi*81*t)*.3+air*.2)*math.exp(-2*u)
        if cue=='resume':value=(math.sin(2*math.pi*(110*t+330*t*t))*.45+air*.25)*math.exp(-3*u)
        samples.append(value*env)
    peak=max(abs(x) for x in samples);rms=math.sqrt(sum(x*x for x in samples)/len(samples));gain=min(.77/peak,.13/rms)
    values=array('h',[round(x*gain*32767) for x in samples])
    if sys.byteorder!='little':values.byteswap()
    return values.tobytes()

def main():
    clips=[]
    for index,kind in enumerate(KINDS):
        for number,(cue,duration) in enumerate([('summon',.42),('dismiss',.2),('cast',.3),('impact',.15)]):
            seed=12000+index*100+number;path=ROOT/f'src/main/resources/assets/jojosmod/sounds/{kind}/{cue}.ogg';path.parent.mkdir(parents=True,exist_ok=True)
            subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-f','s16le','-ar',str(RATE),'-ac','1','-i','pipe:0','-c:a','libvorbis','-q:a','4','-fflags','+bitexact','-flags:a','+bitexact','-y',str(path)],input=pcm(kind,cue,duration,seed),check=True)
            probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-select_streams','a:0','-show_entries','stream=codec_name,channels,sample_rate','-of','json',str(path)]))['streams'][0]
            assert probe=={'codec_name':'vorbis','sample_rate':'48000','channels':1}
            clips.append({'file':str(path.relative_to(ROOT)),'seed':seed,'duration_seconds':duration,'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),**probe})
    manifest={'license':'MIT','authorship':'Original mathematical synthesis; no recording inputs. Metallic rapier, fire/air, violet vine, sand and golden time cues.','generator':'build_sounds.py','ffmpeg':subprocess.check_output(['ffmpeg','-version'],text=True).splitlines()[0],'clips':clips}
    (HERE/'sound-provenance.json').write_text(json.dumps(manifest,indent=2)+'\n');print(len(clips),'original mono 48k Vorbis cues generated and verified.')
if __name__=='__main__':main()
