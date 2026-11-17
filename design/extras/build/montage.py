import sys,glob
from PIL import Image
files=sys.argv[2:]; out=sys.argv[1]
ims=[Image.open(f).convert('RGBA') for f in files]
cols=4; pad=16
rows=[ims[i:i+cols] for i in range(0,len(ims),cols)]
W=max(sum(i.width for i in r)+pad*(len(r)+1) for r in rows); H=sum(max(i.height for i in r)+pad for r in rows)+pad
bg=Image.new('RGBA',(W,H),(180,180,180,255)); y=pad
for r in rows:
  x=pad
  for i in r: bg.alpha_composite(i,(x,y)); x+=i.width+pad
  y+=max(i.height for i in r)+pad
bg.save(out)
