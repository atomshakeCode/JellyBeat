package dev.atomshake.jellybeat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
public final class VoiceBeats {
 public static volatile boolean available;
 public static volatile UUID localPlayer;
 private static volatile Set<UUID> players=Set.of();
 private static final ConcurrentHashMap<UUID,Beat> beats=new ConcurrentHashMap<>();
 private record Beat(float level,long time){}
 public record Sample(float level,float weight,double seconds){
  public static final Sample NONE=new Sample(0,0,0);
  public float override(float music){return music*(1-weight)+level*weight;}
 }
 public static void tick(Minecraft mc){if(mc.level==null){clear();localPlayer=null;players=Set.of();return;}localPlayer=mc.player==null?null:mc.player.getUUID();var ids=new HashSet<UUID>();for(var p:mc.level.players())ids.add(p.getUUID());players=Set.copyOf(ids);long now=System.nanoTime();beats.entrySet().removeIf(e->now-e.getValue().time>500_000_000L);if(!JellyBeat.CONFIG.voicechat)clear();}
 public static boolean knownPlayer(UUID id){return players.contains(id);}
 public static void accept(UUID id,short[] pcm){acceptAt(id,pcm,System.nanoTime());}
 public static void acceptAt(UUID id,short[] pcm,long now){if(id==null||pcm==null||pcm.length==0)return;double sum=0;for(short v:pcm){double x=v/32768.0;sum+=x*x;}double rms=Math.sqrt(sum/pcm.length);if(rms<.002)return;if(beats.size()>=1024&&!beats.containsKey(id))return;float level=(float)Math.min(1,rms*8);beats.compute(id,(k,old)->new Beat(old==null?level:old.level+(level-old.level)*(level>old.level?.8f:.35f),now));}
 public static Sample sample(UUID id){return sampleAt(id,System.nanoTime());}
 public static Sample sampleAt(UUID id,long now){Beat b=beats.get(id);if(b==null)return Sample.NONE;double age=Math.max(0,(now-b.time)/1e9);float weight=(float)Math.clamp((.30-age)/.20,0,1);return weight==0?Sample.NONE:new Sample(b.level,weight,now/1e9);}
 public static void remove(UUID id){if(id!=null)beats.remove(id);}
 public static void clear(){beats.clear();}
}
