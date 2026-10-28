package dev.atomshake.jellybeat.test;
import dev.atomshake.jellybeat.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.client.Screenshot;
import java.util.*;
public class JellyPlantsTest implements ClientModInitializer {
 int stage,ticks;final Map<BlockPos,BlockState> fixtures=new LinkedHashMap<>();
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(stage==0&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){stage=1;mc.createWorldOpenFlows().openWorld("JellyBeatTest",()->{});}
  if(stage==1&&mc.level!=null&&mc.player!=null){stage=2;var c=JellyBeat.CONFIG;c.enabled=c.blocks=true;c.radius=20;c.maxBlocks=3072;c.blocksMotion.intensity=2;c.blocksMotion.sensitivity=1;JellyBeat.AUDIO.demo();
   BlockState[] states={Blocks.TORCH.defaultBlockState(),Blocks.WALL_TORCH.defaultBlockState(),Blocks.OAK_FENCE.defaultBlockState().setValue(BlockStateProperties.NORTH,true),Blocks.DANDELION.defaultBlockState(),Blocks.SHORT_GRASS.defaultBlockState(),Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7,7),Blocks.SUGAR_CANE.defaultBlockState(),Blocks.TALL_GRASS.defaultBlockState(),Blocks.FERN.defaultBlockState(),Blocks.POPPY.defaultBlockState()};
   for(int i=0;i<states.length;i++)fixtures.put(new BlockPos(-5+i,-60,4),states[i]);
   fixtures.put(new BlockPos(1,-59,4),Blocks.SUGAR_CANE.defaultBlockState());fixtures.put(new BlockPos(1,-58,4),Blocks.SUGAR_CANE.defaultBlockState());fixtures.put(new BlockPos(2,-59,4),Blocks.TALL_GRASS.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,DoubleBlockHalf.UPPER));
  }
  if(stage==2){ticks++;
   if(ticks==30){for(var e:fixtures.entrySet())mc.level.setBlock(e.getKey(),e.getValue(),18);mc.level.setBlock(new BlockPos(-3,-59,4),Blocks.STONE.defaultBlockState(),18);JellyBeat.SURFACE.clear();for(var e:fixtures.entrySet())check(SurfaceDance.eligible(mc.level,e.getKey(),e.getValue()),"Eligibility "+e.getValue());check(!SurfaceDance.eligible(mc.level,new BlockPos(-3,-60,4),Blocks.AIR.defaultBlockState()),"Air excluded");check(!SurfaceDance.eligible(mc.level,new BlockPos(-3,-61,4),Blocks.WATER.defaultBlockState()),"Water excluded");System.out.println("JELLY_V070 PASS plant/torch/fence eligibility including covered fence and stacked plants");}
   if(ticks==300){for(var e:fixtures.entrySet()){check(JellyBeat.SURFACE.containsTile(e.getKey()),"Missing animated tile "+e);check(JellyBeat.SURFACE.quadCount(e.getKey())>0,"Empty mesh "+e);}System.out.println("JELLY_V070 PASS actual non-empty meshes for all 13 fixtures");Screenshot.grab(mc.gameDirectory,"jelly-v070-plants.png",mc.gameRenderer.mainRenderTarget(),1,c->{});}
   if(ticks==310){mc.level.setBlock(new BlockPos(-5,-60,4),Blocks.AIR.defaultBlockState(),18);}
   if(ticks==330){check(!JellyBeat.SURFACE.containsTile(new BlockPos(-5,-60,4)),"Removed torch stale geometry");System.out.println("JELLY_V070 ALL PASS");mc.stop();stage=3;}
  }
 });}
}
