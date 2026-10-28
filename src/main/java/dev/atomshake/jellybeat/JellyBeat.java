package dev.atomshake.jellybeat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
public final class JellyBeat implements ClientModInitializer {
 public static Config CONFIG;
 public static final AudioEngine AUDIO=new AudioEngine();
 public static final SurfaceDance SURFACE=new SurfaceDance();
 public void onInitializeClient(){
  CONFIG=Config.load();AUDIO.refreshDevices();
  var category=KeyMapping.Category.register(Identifier.fromNamespaceAndPath("jellybeat","controls"));
  var settings=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jellybeat.settings",GLFW.GLFW_KEY_J,category));
  var toggle=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.jellybeat.toggle",GLFW.GLFW_KEY_K,category));
  ClientTickEvents.END_CLIENT_TICK.register(mc->{VoiceBeats.tick(mc);while(settings.consumeClick())mc.gui.setScreen(new JellyScreen(mc.gui.screen()));while(toggle.consumeClick()){CONFIG.enabled=!CONFIG.enabled;CONFIG.save();}if(mc.level==null)SURFACE.clear();});
  LevelExtractionEvents.END_EXTRACTION.register(SURFACE::extract);
  LevelRenderEvents.COLLECT_SUBMITS.register(SURFACE::render);
  ClientLifecycleEvents.CLIENT_STOPPING.register(mc->AUDIO.close());
 }
 public static float strength(DanceTarget target){return CONFIG==null?0:CONFIG.power(target,AUDIO.pulse());}
}

