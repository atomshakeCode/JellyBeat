"""Fetch pinned Minecraft/Fabric and Modrinth compile-only inputs for CI."""
import json
import pathlib
import urllib.request
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
META = ROOT / "build/meta"
DEPS = ROOT / "build/deps"
MC = "26.2"
LOADER = "0.19.5"
MC_ID = f"{MC}-{LOADER}"
API_VERSION = "0.155.2+26.2"
VOICE_VERSION = "fabric-2.6.21+26.2"


def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": "JellyBeat-CI/0.7.1 (source build)"})
    with urllib.request.urlopen(req, timeout=90) as res:
        return res.read()


def modrinth_file(project, version):
    releases = json.loads(get(f"https://api.modrinth.com/v2/project/{project}/version"))
    release = next((r for r in releases if r["version_number"] == version), None)
    if release is None:
        raise RuntimeError(f"Pinned dependency not found: {project} {version}")
    file = next((f for f in release["files"] if f.get("primary")), release["files"][0])
    return get(file["url"])


manifest = json.loads(get("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
entry = next((v for v in manifest["versions"] if v["id"] == MC), None)
if entry is None:
    raise RuntimeError(f"Minecraft {MC} is missing from the official version manifest")
vanilla = json.loads(get(entry["url"]))
fabric = json.loads(get(f"https://meta.fabricmc.net/v2/versions/loader/{MC}/{LOADER}/profile/json"))
version_json = dict(vanilla)
version_json["id"] = MC_ID
version_json["libraries"] = vanilla.get("libraries", []) + fabric.get("libraries", [])
version_dir = META / "versions" / MC_ID
version_dir.mkdir(parents=True, exist_ok=True)
(version_dir / f"{MC_ID}.json").write_text(json.dumps(version_json), encoding="utf-8")
jar_path = version_dir / f"{MC_ID}.jar"
jar_path.write_bytes(get(vanilla["downloads"]["client"]["url"]))

for library in version_json["libraries"]:
    parts = library["name"].split(":")
    if len(parts) < 3:
        continue
    group, artifact, version = parts[:3]
    filename = artifact + "-" + version + ("-" + parts[3] if len(parts) > 3 else "") + ".jar"
    base = library.get("url", "https://libraries.minecraft.net/")
    path = "/".join(group.split(".")) + f"/{artifact}/{version}/{filename}"
    target = META / "libraries" / pathlib.Path(*group.split(".")) / artifact / version / filename
    target.parent.mkdir(parents=True, exist_ok=True)
    if not target.exists():
        target.write_bytes(get(base.rstrip("/") + "/" + path))

DEPS.mkdir(parents=True, exist_ok=True)
fabric_path = DEPS / "fabric-api.jar"
fabric_path.write_bytes(modrinth_file("fabric-api", API_VERSION))
(DEPS / "voicechat.jar").write_bytes(modrinth_file("simple-voice-chat", VOICE_VERSION))
api_dir = ROOT / "build/fabric-api"
api_dir.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(fabric_path) as archive:
    for name in archive.namelist():
        if name.endswith(".jar"):
            (api_dir / pathlib.Path(name).name).write_bytes(archive.read(name))
print(f"Prepared Minecraft {MC}, Fabric Loader {LOADER}, Fabric API {API_VERSION}, Simple Voice Chat {VOICE_VERSION}")
