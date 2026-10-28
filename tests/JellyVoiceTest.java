
package dev.atomshake.jellybeat.test;
import dev.atomshake.jellybeat.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.entity.state.*;
public class JellyVoiceTest implements ClientModInitializer {
 int stage,ticks;
 static void check(boolean c,String msg){if(!c)throw new AssertionError(msg);}
 static boolean red(AbstractSliderButton b){var c=b.getMessage().getStyle().getColor();return c!=null&&c.getValue()==0xFF5555;}
 static void move(MotionSlider slider,double normalized)throws Exception{var f=AbstractSliderButton.class.getDeclaredField("value");f.setAccessible(true);f.setDouble(slider,normalized);var m=MotionSlider.class.getDeclaredMethod("applyValue");m.setAccessible(true);m.invoke(slider);}
 void validate(){try{
  var mc=net.minecraft.client.Minecraft.getInstance();
  for(String lang:new String[]{"en_us","de_de","uk_ua"}){mc.options.languageCode=lang;var menu=new JellyScreen(null);menu.init(550,380);for(var w:menu.children())if(w instanceof net.minecraft.client.gui.components.AbstractWidget a)check(!a.getMessage().getString().matches(".*[А-Яа-яЁё].*"),"Non-English label: "+a.getMessage().getString());check(Ui.t("Блоки").equals("Blocks"),"English fallback");}
  mc.options.languageCode="ru_ru";check(Ui.t("Блоки").equals("Блоки"),"Russian UI");
  java.util.UUID a=java.util.UUID.randomUUID(),other=java.util.UUID.randomUUID();short[] pcm=new short[960];java.util.Arrays.fill(pcm,(short)4096);long now=1_000_000_000L;VoiceBeats.acceptAt(a,pcm,now);var sample=VoiceBeats.sampleAt(a,now);check(sample.override(.2f)==1,"Voice overrides music");check(VoiceBeats.sampleAt(other,now).override(.2f)==.2f,"Other player unchanged");check(VoiceBeats.sampleAt(a,now+200_000_000L).weight()>.49&&VoiceBeats.sampleAt(a,now+200_000_000L).weight()<.51,"Voice fade");check(VoiceBeats.sampleAt(a,now+301_000_000L).override(.2f)==.2f,"Return to music");check(pcm[0]==4096,"PCM not modified");VoiceBeats.clear();
  System.out.println("JELLY_V050 PASS language ru/en/de/uk, voice override, speaker isolation, release, no PCM mutation; plugin="+VoiceBeats.available);

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
  System.out.println("JELLY_V050 PASS migration, independent gain/toggles, category dispatch, eight slider bindings, persistence, 512/513/1024 colors");
 }catch(Exception e){throw new RuntimeException(e);}}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(stage==0&&mc.isGameLoadFinished()&&mc.gui.overlay()==null){validate();stage=1;mc.createWorldOpenFlows().openWorld("JellyBeatTest",()->{});}
  if(stage==1&&mc.level!=null&&mc.player!=null){stage=2;ticks=0;var c=JellyBeat.CONFIG;c.radius=20;c.maxBlocks=1024;for(var t:DanceTarget.values()){c.motion(t).intensity=.65;c.motion(t).sensitivity=1;}JellyBeat.AUDIO.demo();}
  if(stage==2){ticks++;short[] speech=new short[960];java.util.Arrays.fill(speech,(short)2048);VoiceBeats.accept(mc.player.getUUID(),speech);
   if(ticks==100){if(VoiceBeats.available)try{var type=Class.forName("de.maxhenkel.voicechat.plugins.ClientPluginManager");var manager=type.getMethod("instance").invoke(null);var id=java.util.UUID.randomUUID();short[] audio=new short[960];java.util.Arrays.fill(audio,(short)4096);var result=(short[])type.getMethod("onReceiveEntityClientSound",java.util.UUID.class,java.util.UUID.class,short[].class,boolean.class,float.class).invoke(manager,java.util.UUID.randomUUID(),id,audio,false,48f);check(VoiceBeats.sample(id).weight()>0,"Real Voice Chat event dispatch");check(result[0]==4096,"Event PCM unchanged");System.out.println("JELLY_V050 PASS real Voice Chat event dispatch and entity/channel UUID mapping");}catch(Exception e){throw new RuntimeException(e);}
var state=mc.getEntityRenderDispatcher().extractEntity(mc.player,0);check(((VoiceRenderState)state).jellybeat$voice().weight()>0,"Player render voice snapshot");System.out.println("JELLY_V050 PASS player render-state voice injection");}
   if(ticks==185){mc.options.languageCode="de_de";mc.gui.setScreen(new JellyScreen(null));}
if(ticks==140){check(JellyBeat.SURFACE.tileCount()==1024,"1024 actual cached blocks, got "+JellyBeat.SURFACE.tileCount());System.out.println("JELLY_V050 PASS actual 1024 tiles rendered");JellyBeat.CONFIG.blocksMotion.intensity=2;JellyBeat.CONFIG.boatsMotion.sensitivity=4;mc.gui.setScreen(new MotionScreen(null));}
   if(ticks==160)Screenshot.grab(mc.gameDirectory,"jelly-v050-profiles.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   if(ticks==175)mc.gui.setScreen(new JellyScreen(null));
   if(ticks==195)Screenshot.grab(mc.gameDirectory,"jelly-v050-limit.png",mc.gameRenderer.mainRenderTarget(),1,c->{});
   if(ticks==215){System.out.println("JELLY_V050 ALL PASS");mc.stop();stage=3;}
  }
 });}
}
