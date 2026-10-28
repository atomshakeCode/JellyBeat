package dev.atomshake.jellybeat;
import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
public final class JellyVoicePlugin implements VoicechatPlugin {
 public String getPluginId(){return "jellybeat";}
 private boolean enabled(){return JellyBeat.CONFIG!=null&&JellyBeat.CONFIG.voicechat;}
 public void registerEvents(EventRegistration r){
  VoiceBeats.available=true;
  r.registerEvent(ClientReceiveSoundEvent.EntitySound.class,e->{if(enabled()&&!e.getVoicechat().isDisabled(e.getEntityId()))VoiceBeats.accept(e.getEntityId(),e.getRawAudio());});
  r.registerEvent(ClientReceiveSoundEvent.StaticSound.class,e->{if(enabled()&&VoiceBeats.knownPlayer(e.getId())&&!e.getVoicechat().isDisabled(e.getId()))VoiceBeats.accept(e.getId(),e.getRawAudio());});
  r.registerEvent(ClientSoundEvent.class,e->{if(enabled()&&!e.getVoicechat().isMuted()&&!e.getVoicechat().isDisabled())VoiceBeats.accept(VoiceBeats.localPlayer,e.getRawAudio());});
  r.registerEvent(ClientVoicechatConnectionEvent.class,e->{if(!e.isConnected())VoiceBeats.clear();});
  r.registerEvent(VoicechatDisableEvent.class,e->{if(e.isDisabled())VoiceBeats.clear();});
  r.registerEvent(MicrophoneMuteEvent.class,e->{if(e.isDisabled())VoiceBeats.remove(VoiceBeats.localPlayer);});
 }
}
