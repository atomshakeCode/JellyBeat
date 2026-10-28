
package dev.atomshake.jellybeat.test;
import dev.atomshake.jellybeat.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Screenshot;
public class JellyExtremeTest implements ClientModInitializer {
 int stage=0,ticks=0;
 void colors(JellyScreen screen,boolean red){int checked=0;for(var child:screen.children())if(child instanceof AbstractSliderButton slider){String text=slider.getMessage().getString();if(text.startsWith("Сила желе")||text.startsWith("Чувствительность")){var color=slider.getMessage().getStyle().getColor();boolean actual=color!=null&&color.getValue()==0xFF5555;if(actual!=red)throw new AssertionError("Color: "+text);checked++;}}if(checked!=2)throw new AssertionError("Sliders missing");}
 public void onInitializeClient(){
  for(int i=0;i<200000;i++){var s=DanceMath.shape(i*.013,i*.3,(i%10001)/100f);if(!Float.isFinite(s.x())||s.y()<.125||s.y()>8||Math.abs(s.x()*s.y()*s.z()-1)>.00001)throw new AssertionError("Extreme shape");}
  ClientTickEvents.END_CLIENT_TICK.register(mc->{
   if(stage==0&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){stage=1;mc.createWorldOpenFlows().openWorld("JellyBeatTest",()->{});}
   if(stage==1&&mc.level!=null&&mc.player!=null){stage=2;ticks=0;var c=JellyBeat.CONFIG;c.enabled=c.blocks=true;c.radius=6;c.maxBlocks=448;c.intensity=.65;c.sensitivity=1;JellyBeat.AUDIO.demo();var server=mc.getSingleplayerServer();server.execute(()->{var src=server.createCommandSourceStack();for(String cmd:new String[]{"tp @a 0 -60 0 0 20","fill -5 -56 -5 0 -56 5 oak_leaves[persistent=true]","fill 1 -56 -5 5 -56 5 stone","fill -5 -55 -5 5 -52 5 air"})server.getCommands().performPrefixedCommand(src,cmd);});}
   if(stage==2){ticks++;
    if(ticks==100){boolean leaves=JellyBeat.SURFACE.containsTile(new BlockPos(-2,-61,0)),roof=JellyBeat.SURFACE.containsTile(new BlockPos(2,-61,0));System.out.println("JELLY_V030 ground_under_leaves="+leaves+" ground_under_roof="+roof+" tiles="+JellyBeat.SURFACE.tileCount());if(!leaves||!roof)throw new AssertionError("Covered surface missing");
     JellyBeat.CONFIG.intensity=1.5;JellyBeat.CONFIG.sensitivity=3;var screen=new JellyScreen(null);mc.gui.setScreen(screen);colors(screen,false);System.out.println("JELLY_V030 normal threshold colors PASS");}
    if(ticks==110){JellyBeat.CONFIG.intensity=1.51;JellyBeat.CONFIG.sensitivity=3.01;var screen=new JellyScreen(null);mc.gui.setScreen(screen);colors(screen,true);System.out.println("JELLY_V030 above threshold colors PASS");}
    if(ticks==120){JellyBeat.CONFIG.intensity=10;JellyBeat.CONFIG.sensitivity=10;JellyBeat.CONFIG.save();var c=Config.load();if(c.intensity!=10||c.sensitivity!=10)throw new AssertionError("Clamp rejected 10");var screen=new JellyScreen(null);mc.gui.setScreen(screen);colors(screen,true);}
    if(ticks==150)Screenshot.grab(mc.gameDirectory,"jelly-v030-extreme.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
    if(ticks==170){System.out.println("JELLY_V030 PASS: math, covered surfaces, threshold colors, max settings roundtrip and rendering");mc.stop();stage=3;}
   }
  });
 }
}

