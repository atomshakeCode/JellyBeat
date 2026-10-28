"""Build against an installed unobfuscated Minecraft 26.2 client; no Gradle or remapping needed."""
import argparse,json,pathlib,subprocess,zipfile,os
p=argparse.ArgumentParser()
p.add_argument('--jdk',required=True,help='JDK 25 directory')
p.add_argument('--meta',default=str(pathlib.Path(os.environ.get('APPDATA',''))/'ModrinthApp/meta'))
p.add_argument('--version',default='26.2-0.19.5',help='Installed Modrinth version directory')
p.add_argument('--fabric-api',required=True,help='Fabric API 26.2 JAR')
p.add_argument('--voicechat',required=True,help='Simple Voice Chat JAR (compile-only)')
a=p.parse_args();root=pathlib.Path(__file__).resolve().parent;meta=pathlib.Path(a.meta)
build=root/'build';classes=build/'classes-v071';classes.mkdir(parents=True,exist_ok=True)
v=json.loads((meta/'versions'/a.version/(a.version+'.json')).read_text())
libs=[meta/'versions'/a.version/(a.version+'.jar')]
for lib in v['libraries']:
 bits=lib['name'].split(':');group,artifact,version=bits[:3]
 f=meta/'libraries'/group.replace('.','/')/artifact/version/(artifact+'-'+version+('-'+bits[3] if len(bits)>3 else '')+'.jar')
 if f.exists():libs.append(f)
api=build/'fabric-api';api.mkdir(exist_ok=True)
with zipfile.ZipFile(a.fabric_api) as z:
 for name in z.namelist():
  if name.endswith('.jar'):
   f=api/pathlib.Path(name).name;f.write_bytes(z.read(name));libs.append(f)
libs.append(pathlib.Path(a.voicechat))
cp=os.pathsep.join(map(str,libs));jdk=pathlib.Path(a.jdk)/'bin';suffix='.exe' if os.name=='nt' else ''
args=['-encoding','UTF-8','--release','25','-proc:none','-classpath',cp,'-d',str(classes)]
args += [str(f) for f in (root/'src/main/java').rglob('*.java')]
argfile=build/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8')
subprocess.run([str(jdk/('javac'+suffix)),'@'+str(argfile)],check=True)
jar=build/'jellybeat-0.7.1+26.2.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
 for folder in [classes,root/'src/main/resources']:
  for f in folder.rglob('*'):
   if f.is_file() and 'SelfTest' not in f.name:z.write(f,f.relative_to(folder).as_posix())
 for name in ['LICENSE','THIRD-PARTY.txt']:z.write(root/name,'META-INF/licenses/'+name)
print(jar)
