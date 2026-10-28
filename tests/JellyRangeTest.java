package dev.atomshake.jellybeat.test;
import dev.atomshake.jellybeat.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractSliderButton;
public class JellyRangeTest implements ClientModInitializer {
 int stage,ticks;
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(stage==0&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){
   var c=JellyBeat.CONFIG;c.radius=60;c.maxBlocks=3072;c.save();var loaded=Config.load();check(loaded.radius==60&&loaded.maxBlocks==3072,"Persist increased limits");
   check(MotionSlider.loadColor(512,512,3072)==0xFFFFFF,"Safe white");check(MotionSlider.loadColor(1792,512,3072)==0xFFFF55,"Mid yellow");check(MotionSlider.loadColor(3072,512,3072)==0xFF5555,"Max red");check(MotionSlider.loadColor(20,20,60)==0xFFFFFF&&MotionSlider.loadColor(40,20,60)==0xFFFF55&&MotionSlider.loadColor(60,20,60)==0xFF5555,"Radius colors");
   var menu=new JellyScreen(null);menu.init(550,380);var sliders=menu.children().stream().filter(w->w instanceof AbstractSliderButton).map(w->(AbstractSliderButton)w).toList();check(sliders.size()==2,"Two load sliders");for(var b:sliders)check(b.getMessage().getStyle().getColor().getValue()==0xFF5555,"Max slider red");
   System.out.println("JELLY_V060 PASS limits persistence and actual slider colors");stage=1;mc.createWorldOpenFlows().openWorld("JellyBeatTest",()->{});
  }
  if(stage==1&&mc.level!=null&&mc.player!=null){stage=2;JellyBeat.AUDIO.demo();}
  if(stage==2){ticks++;if(ticks==600){check(JellyBeat.SURFACE.tileCount()==3072,"Actual 3072 geometry, got "+JellyBeat.SURFACE.tileCount());System.out.println("JELLY_V060 PASS actual 3072 cached blocks at radius 60");mc.options.languageCode="ru_ru";mc.gui.setScreen(new JellyScreen(null));}
   if(ticks==620)Screenshot.grab(mc.gameDirectory,"jelly-v060-max.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   if(ticks==630){JellyBeat.CONFIG.radius=40;JellyBeat.CONFIG.maxBlocks=1792;mc.gui.setScreen(new JellyScreen(null));}
   if(ticks==650)Screenshot.grab(mc.gameDirectory,"jelly-v060-yellow.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   if(ticks==660){System.out.println("JELLY_V060 ALL PASS");mc.stop();stage=3;}
  }
 });}
}
