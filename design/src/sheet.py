import sys,glob,os
from PIL import Image
pat=sys.argv[1]; out=sys.argv[2]; cols=int(sys.argv[3]) if len(sys.argv)>3 else 6
fs=sorted(glob.glob(pat))[:int(sys.argv[4]) if len(sys.argv)>4 else 24]
W=330
ims=[]
for f in fs:
    im=Image.open(f); r=W/im.width; ims.append(im.resize((W,int(im.height*r))))
H=max(i.height for i in ims); rows=(len(ims)+cols-1)//cols
S=Image.new('RGB',(cols*(W+8),rows*(H+8)),(128,128,128))
for k,i in enumerate(ims): S.paste(i,((k%cols)*(W+8),(k//cols)*(H+8)))
S.save(out); print(len(fs), out)
