package dev.atomshake.jellybeat;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.phys.AABB;
import com.mojang.blaze3d.vertex.*;
/** Cached static lighting/geometry; at most three batched submissions, no per-frame tessellation. */
public final class SurfaceDance {
 private record Quad(float x,float y,float z,BakedQuad model,QuadInstance attributes){}
 private record Tile(BlockPos pos,BlockState state,Map<ChunkSectionLayer,List<Quad>> mesh,long baked){}
 private final Map<BlockPos,Tile> cache=new HashMap<>();
 private List<BlockPos> candidates=List.of();
 private volatile List<Tile> tiles=List.of();
 private Object world,models;
 private long nextScan;
 public long scanCount,bakeCount,submitCount;
 public int tileCount(){return tiles.size();}
 public boolean containsTile(BlockPos pos){return tiles.stream().anyMatch(t->t.pos.equals(pos));}
 public static boolean eligible(BlockGetter level,BlockPos pos,BlockState state){
  if(state.isAir()||state.hasBlockEntity()||!state.getFluidState().isEmpty()||state.getRenderShape()!=RenderShape.MODEL)return false;
  // Open models (plants, fences, torches) can be visible from the sides even in stacks.
  if(!state.isCollisionShapeFullBlock(level,pos))return true;
  BlockPos above=pos.above();var cover=level.getBlockState(above);
  return cover.getFluidState().isEmpty()&&!cover.isCollisionShapeFullBlock(level,above);
 }
 public int quadCount(BlockPos pos){Tile t=cache.get(pos);return t==null?0:t.mesh.values().stream().mapToInt(List::size).sum();}
 public void clear(){tiles=List.of();candidates=List.of();cache.clear();world=null;models=null;nextScan=0;}
 public void extract(LevelExtractionContext ctx){
  if(JellyBeat.CONFIG==null||!JellyBeat.CONFIG.blocks||!JellyBeat.CONFIG.enabled||!JellyBeat.AUDIO.isPlaying())return;
  var mc=Minecraft.getInstance();var level=ctx.level();var set=mc.getModelManager().getBlockStateModelSet();
  if(world!=level||models!=set){clear();world=level;models=set;}
  long now=System.nanoTime();var camera=ctx.camera().position();int r=JellyBeat.CONFIG.radius;
  if(now>=nextScan){
   scanCount++;nextScan=now+250_000_000L*Math.max(1,(r*r+399)/400);int cx=(int)Math.floor(camera.x),cz=(int)Math.floor(camera.z);List<BlockPos> selected=new ArrayList<>();
   for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
    if(dx*dx+dz*dz>r*r)continue;int x=cx+dx,z=cz+dz;if(!level.hasChunk(x>>4,z>>4))continue;
    int bottom=Math.max(level.getMinY(),(int)Math.floor(camera.y)-12),top=Math.min(level.getMaxY()-1,(int)Math.floor(camera.y)+12);
    BlockPos.MutableBlockPos probe=new BlockPos.MutableBlockPos();
    for(int y=bottom;y<=top;y++){
     probe.set(x,y,z);var state=level.getBlockState(probe);
     if(!eligible(level,probe,state))continue;
     selected.add(probe.immutable());
    }
   }
   selected.sort(Comparator.comparingDouble(p->p.distToCenterSqr(camera)));
   candidates=List.copyOf(selected.subList(0,Math.min(selected.size(),JellyBeat.CONFIG.maxBlocks)));cache.keySet().retainAll(new HashSet<>(candidates));
  }
  int budget=8;List<Tile> ready=new ArrayList<>(candidates.size());
  ModelBlockRenderer baker=null;
  // Fill missing geometry before refreshing existing tiles, so large limits cannot starve.
  List<BlockPos> bakeOrder=new ArrayList<>(candidates);
  bakeOrder.sort(Comparator.comparingInt(pos->cache.containsKey(pos)?1:0));
  for(BlockPos pos:bakeOrder){
   var state=level.getBlockState(pos);if(!eligible(level,pos,state)){cache.remove(pos);continue;}Tile tile=cache.get(pos);
   if(tile!=null&&tile.state!=state){cache.remove(pos);tile=null;}
   if((tile==null||now-tile.baked>2_000_000_000L)&&budget>0){
    budget--;bakeCount++;
    if(baker==null)baker=new ModelBlockRenderer(true,false,mc.getBlockColors());
    MovingBlockRenderState view=new MovingBlockRenderState();view.blockPos=pos;view.randomSeedPos=pos;view.blockState=state;view.biome=level.getBiome(pos);view.cardinalLighting=level.cardinalLighting();view.lightEngine=level.getLightEngine();
    Map<ChunkSectionLayer,List<Quad>> mesh=new EnumMap<>(ChunkSectionLayer.class);
    baker.tesselateBlock((x,y,z,q,attrs)->{
     if(q.direction()==Direction.DOWN&&state.isCollisionShapeFullBlock(level,pos))return;
     QuadInstance copy=new QuadInstance();for(int v=0;v<4;v++){copy.setColor(v,attrs.getColor(v));copy.setLightCoords(v,attrs.getLightCoords(v));}copy.setOverlayCoords(attrs.overlayCoords());
     mesh.computeIfAbsent(q.materialInfo().layer(),k->new ArrayList<>()).add(new Quad(x,y,z,q,copy));
    },0,0,0,view,pos,state,set.get(state),state.getSeed(pos));
    Map<ChunkSectionLayer,List<Quad>> frozen=new EnumMap<>(ChunkSectionLayer.class);mesh.forEach((k,v)->frozen.put(k,List.copyOf(v)));
    tile=new Tile(pos,state,Map.copyOf(frozen),now);cache.put(pos,tile);
   }
   if(tile!=null)ready.add(tile);
  }
  tiles=List.copyOf(ready);
 }
 public void render(LevelRenderContext ctx){
  float power=JellyBeat.strength(DanceTarget.BLOCKS);if(!JellyBeat.CONFIG.blocks||power<.002f)return;
  var camera=ctx.levelState().cameraRenderState.pos;var frustum=ctx.levelState().cameraRenderState.cullFrustum;
  double time=JellyBeat.AUDIO.seconds();List<Tile> visible=new ArrayList<>();
  for(Tile tile:tiles)if(frustum==null||frustum.isVisible(new AABB(tile.pos).inflate(Math.max(.7,power*.36))))visible.add(tile);
  if(visible.isEmpty())return;
  for(ChunkSectionLayer layer:ChunkSectionLayer.values()){
   if(visible.stream().noneMatch(t->t.mesh.containsKey(layer)))continue;
   RenderType type=switch(layer){case SOLID->RenderTypes.solidMovingBlock();case CUTOUT->RenderTypes.cutoutMovingBlock();case TRANSLUCENT->RenderTypes.translucentMovingBlock();};
   submitCount++;
   ctx.submitNodeCollector().submitCustomGeometry(ctx.poseStack(),type,(base,vertices)->{
    PoseStack pose=new PoseStack();pose.mulPose(base.pose());
    for(Tile tile:visible){List<Quad> quads=tile.mesh.get(layer);if(quads==null)continue;
     var p=tile.pos;double wave=.5+.5*Math.sin(time*8+p.getX()*.55+p.getZ()*.45);
     float height=(float)(1.002+power*(.06+.30*wave)),width=(float)(1.002+power*.025*(1-wave));
     pose.pushPose();pose.translate(p.getX()-camera.x+.5,p.getY()-camera.y,p.getZ()-camera.z+.5);pose.scale(width,height,width);pose.translate(-.5,0,-.5);
     for(Quad q:quads){pose.pushPose();pose.translate(q.x,q.y,q.z);vertices.putBakedQuad(pose.last(),q.model,q.attributes);pose.popPose();}
     pose.popPose();
    }
   });
  }
 }
}
