package dev.atomshake.jellybeat;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.List;
public final class DeviceScreen extends Screen {
 private final Screen parent;private int page;private List<AudioEngine.Device> shown=List.of();
 public DeviceScreen(Screen parent){super(Component.literal(Ui.t("Устройство воспроизведения Windows")));this.parent=parent;JellyBeat.AUDIO.refreshDevices();}
 private void button(String label,int x,int y,int w,Runnable r){addRenderableWidget(Button.builder(Component.literal(label),b->r.run()).bounds(x,y,w,20).build());}
 protected void init(){shown=JellyBeat.AUDIO.devices;int w=Math.min(width-24,440),x=(width-w)/2,y=Math.max(30,(height-200)/2);int pages=Math.max(1,(shown.size()+5)/6);page=Math.min(page,pages-1);
  for(int i=0;i<6&&page*6+i<shown.size();i++){var d=shown.get(page*6+i);String text=(d.id().equals(JellyBeat.CONFIG.audioDeviceId)?"✓ ":"")+Ui.t(d.name());
   var b=addRenderableWidget(Button.builder(Component.literal(font.plainSubstrByWidth(text,w-16)),v->{JellyBeat.CONFIG.audioDeviceId=d.id();JellyBeat.CONFIG.save();JellyBeat.AUDIO.stop();minecraft.gui.setScreen(parent);}).bounds(x,y+i*24,w,20).build());b.setTooltip(Tooltip.create(Component.literal(Ui.t(d.name()))));
  }
  button("←",x,y+150,45,()->{page=Math.max(0,page-1);rebuildWidgets();});button((page+1)+" / "+pages,x+50,y+150,70,()->{});
  button("→",x+125,y+150,45,()->{page=Math.min(pages-1,page+1);rebuildWidgets();});button(Ui.t("Обновить"),x+175,y+150,100,()->JellyBeat.AUDIO.refreshDevices());button(Ui.t("Назад"),x,y+176,w,this::onClose);
 }
 public void tick(){if(shown!=JellyBeat.AUDIO.devices)rebuildWidgets();}
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(0,0,width,height,0xEF111420);g.centeredText(font,title,width/2,Math.max(14,(height-200)/2-16),0xFF92F5D1);super.extractRenderState(g,x,y,dt);}
 public boolean isPauseScreen(){return false;}
 public void onClose(){minecraft.gui.setScreen(parent);}
}
