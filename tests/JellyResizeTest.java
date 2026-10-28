package dev.atomshake.jellybeat.test;
import dev.atomshake.jellybeat.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;
public class JellyResizeTest implements ClientModInitializer {
 int ticks;boolean started;int cycles;
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
  if(!mc.isGameLoadFinished()||mc.gui.overlay()!=null)return;
  if(!started){started=true;var c=GuiClip.intersect(2028,1350,1068,120,0,0,5120,1440);check(c.x()==2028&&c.y()==1350&&c.width()==1068&&c.height()==90,"Reported crash rectangle");check(GuiClip.intersect(6000,1500,20,20,0,0,5120,1440).empty(),"Offscreen skip");check(GuiClip.intersect(-10,-20,40,50,0,0,5120,1440).equals(new GuiClip.Rect(0,0,30,30)),"Negative origin");check(GuiClip.intersect(0,0,0,20,0,0,5120,1440).empty(),"Zero width");System.out.println("JELLY_V071 PASS exact crash coordinates and clipping edge cases");mc.gui.setScreen(new JellyScreen(null));}
  ticks++;
  if(ticks==20||ticks==70||ticks==120){cycles++;long win=mc.getWindow().handle();GLFW.glfwSetWindowSize(win,cycles==2?1280:640,cycles==2?720:360);if(cycles==2)mc.gui.setScreen(new MotionScreen(null));if(cycles==3)mc.gui.setScreen(new DeviceScreen(null));}
  if(ticks==30||ticks==80||ticks==130){GLFW.glfwIconifyWindow(mc.getWindow().handle());java.util.concurrent.CompletableFuture.delayedExecutor(1,java.util.concurrent.TimeUnit.SECONDS).execute(()->mc.execute(()->{GLFW.glfwRestoreWindow(mc.getWindow().handle());GLFW.glfwSetWindowSize(mc.getWindow().handle(),1100,760);System.out.println("JELLY_V071 restored window");}));}
  if(ticks==180){check(cycles==3,"Three cycles");System.out.println("JELLY_V071 ALL PASS resize/minimize/restore on all three settings screens");mc.stop();}
 });}
}
