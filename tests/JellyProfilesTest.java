
package dev.atomshake.jellybeat.test;
import dev.atomshake.jellybeat.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.entity.state.*;
public class JellyProfilesTest implements ClientModInitializer {
 int stage,ticks;
 static void check(boolean c,String msg){if(!c)throw new AssertionError(msg);}
 static boolean red(AbstractSliderButton b){var c=b.getMessage().getStyle().getColor();return c!=null&&c.getValue()==0xFF5555;}
 static void move(MotionSlider slider,double normalized)throws Exception{var f=AbstractSliderButton.class.getDeclaredField("value");f.setAccessible(true);f.setDouble(slider,normalized);var m=MotionSlider.class.getDeclaredMethod("applyValue");m.setAccessible(true);m.invoke(slider);}
 void validate(){try{
  Config c=JellyBeat.CONFIG;for(var t:DanceTarget.values())check(c.motion(t).intensity==.8&&c.motion(t).sensitivity==2,"Legacy migration "+t);
  check(EntityDanceTarget.of(new BoatRenderState())==DanceTarget.BOATS,"Boat category");check(EntityDanceTarget.of(new AvatarRenderState())==DanceTarget.PLAYERS,"Player category");check(EntityDanceTarget.of(new EntityRenderState())==DanceTarget.ENTITIES,"Other entity category");
  c.blocksMotion.intensity=.5;c.blocksMotion.sensitivity=2;c.boatsMotion.intensity=2;c.boatsMotion.sensitivity=.5;c.entitiesMotion.intensity=3;c.entitiesMotion.sensitivity=2;c.playersMotion.intensity=1;c.playersMotion.sensitivity=1;
  check(c.power(DanceTarget.BLOCKS,.4f)>.399&&c.power(DanceTarget.BLOCKS,.4f)<.401,"Block gain");check(Math.abs(c.power(DanceTarget.BOATS,.4f)-.4)<.001,"Boat gain");check(Math.abs(c.power(DanceTarget.ENTITIES,.4f)-2.4)<.001,"Entity gain");
  c.blocksMotion.sensitivity=10;check(Math.abs(c.power(DanceTarget.BOATS,.4f)-.4)<.001,"Cross-category sensitivity leak");
  c.boats=false;check(c.power(DanceTarget.BOATS,1)==0&&c.power(DanceTarget.ENTITIES,1)>0,"Independent toggle");c.boats=true;
  var screen=new MotionScreen(null);screen.init(550,380);var sliders=screen.children().stream().filter(s->s instanceof MotionSlider).map(s->(MotionSlider)s).toList();check(sliders.size()==8,"Eight sliders");
  move(sliders.get(0),.73);move(sliders.get(3),(4.0-.25)/9.75);check(c.blocksMotion.intensity==7.3&&c.boatsMotion.sensitivity==4,"UI profile binding");check(c.entitiesMotion.intensity==3&&c.playersMotion.sensitivity==1,"UI isolation");
  Config restored=Config.load();check(restored.blocksMotion.intensity==7.3&&restored.boatsMotion.sensitivity==4,"Persistence");
  for(int n:new int[]{512,513,1024}){c.maxBlocks=n;c.save();check(Config.load().maxBlocks==n,"Block limit persistence");var main=new JellyScreen(null);main.init(550,380);boolean found=false;for(var w:main.children())if(w instanceof AbstractSliderButton b&&b.getMessage().getString().startsWith("Лимит блоков")){check(red(b)==(n>512),"Limit threshold "+n);check(b.getMessage().getString().endsWith(Integer.toString(n)),"Integer display");found=true;}check(found,"Limit slider");}
  System.out.println("JELLY_V040 PASS migration, independent gain/toggles, category dispatch, eight slider bindings, persistence, 512/513/1024 colors");
 }catch(Exception e){throw new RuntimeException(e);}}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(stage==0&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){validate();stage=1;mc.createWorldOpenFlows().openWorld("JellyBeatTest",()->{});}
  if(stage==1&&mc.level!=null&&mc.player!=null){stage=2;ticks=0;var c=JellyBeat.CONFIG;c.radius=20;c.maxBlocks=1024;for(var t:DanceTarget.values()){c.motion(t).intensity=.65;c.motion(t).sensitivity=1;}JellyBeat.AUDIO.demo();}
  if(stage==2){ticks++;if(ticks==140){check(JellyBeat.SURFACE.tileCount()==1024,"1024 actual cached blocks, got "+JellyBeat.SURFACE.tileCount());System.out.println("JELLY_V040 PASS actual 1024 tiles rendered");JellyBeat.CONFIG.blocksMotion.intensity=2;JellyBeat.CONFIG.boatsMotion.sensitivity=4;mc.gui.setScreen(new MotionScreen(null));}
   if(ticks==160)Screenshot.grab(mc.gameDirectory,"jelly-v040-profiles.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   if(ticks==175)mc.gui.setScreen(new JellyScreen(null));
   if(ticks==195)Screenshot.grab(mc.gameDirectory,"jelly-v040-limit.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   if(ticks==215){System.out.println("JELLY_V040 ALL PASS");mc.stop();stage=3;}
  }
 });}
}
