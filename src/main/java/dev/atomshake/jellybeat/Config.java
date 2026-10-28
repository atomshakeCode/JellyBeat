package dev.atomshake.jellybeat;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
public final class Config {
 public boolean voicechat=true;
 public boolean enabled=true, blocks=true, entities=true, players=true, boats=true;
 public double intensity=0.65, sensitivity=1.0, noiseGate=0.004;
 public static final class Motion {
  public double intensity=.65, sensitivity=1;
  public Motion(){}
  public Motion(double intensity,double sensitivity){this.intensity=intensity;this.sensitivity=sensitivity;}
  void clamp(){intensity=finite(intensity,0,10,.65);sensitivity=finite(sensitivity,.25,10,1);}
 }
 public Motion blocksMotion,boatsMotion,entitiesMotion,playersMotion;
 public Motion motion(DanceTarget target){return switch(target){case BLOCKS->blocksMotion;case BOATS->boatsMotion;case ENTITIES->entitiesMotion;case PLAYERS->playersMotion;};}
 public float power(DanceTarget target,float raw){boolean selected=switch(target){case BLOCKS->blocks;case BOATS->boats;case ENTITIES->entities;case PLAYERS->players;};if(!enabled||!selected)return 0;Motion m=motion(target);return (float)(Math.clamp(raw*m.sensitivity,0,10)*m.intensity);}
 public int radius=5, maxBlocks=96;
 public String audioDeviceId="default";
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("jellybeat.json");
 public static Config load(){
  try { if(Files.exists(FILE)){Config c=new GsonBuilder().create().fromJson(Files.readString(FILE),Config.class);if(c!=null){c.clamp();return c;}} }
  catch(Exception e){System.err.println("[JellyBeat] Config: "+e.getMessage());}
  Config fresh=new Config();fresh.clamp();return fresh;
 }
 public void clamp(){intensity=finite(intensity,0,10,.65);sensitivity=finite(sensitivity,.25,10,1);noiseGate=finite(noiseGate,0,.05,.004);maxBlocks=Math.clamp(maxBlocks,16,3072);radius=Math.clamp(radius,2,60);if(blocksMotion==null)blocksMotion=new Motion(intensity,sensitivity);if(boatsMotion==null)boatsMotion=new Motion(intensity,sensitivity);if(entitiesMotion==null)entitiesMotion=new Motion(intensity,sensitivity);if(playersMotion==null)playersMotion=new Motion(intensity,sensitivity);for(DanceTarget t:DanceTarget.values())motion(t).clamp();if(audioDeviceId==null||audioDeviceId.isBlank())audioDeviceId="default";}
 private static double finite(double v,double lo,double hi,double fallback){return Double.isFinite(v)?Math.clamp(v,lo,hi):fallback;}
 public void save(){clamp();try{Files.createDirectories(FILE.getParent());Files.writeString(FILE,new GsonBuilder().setPrettyPrinting().create().toJson(this));}catch(Exception e){System.err.println("[JellyBeat] Save: "+e.getMessage());}}
}
