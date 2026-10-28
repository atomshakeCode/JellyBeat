package dev.atomshake.jellybeat;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public final class MotionScreen extends Screen {
 private final Screen parent;private int top,left,wide;
 public MotionScreen(Screen parent){super(Component.literal(Ui.t("JellyBeat · настройки по объектам")));this.parent=parent;}
 protected void init(){wide=Math.min(440,width-24);left=(width-wide)/2;top=Math.max(24,(height-220)/2);int half=(wide-6)/2;
  int row=0;for(DanceTarget target:DanceTarget.values()){var m=JellyBeat.CONFIG.motion(target);int y=top+row*42+12;
   addRenderableWidget(new MotionSlider(Ui.t("Сила желе"),left,y,half,m.intensity,0,10,1.5,false,v->m.intensity=v));
   addRenderableWidget(new MotionSlider(Ui.t("Чувствительность"),left+half+6,y,half,m.sensitivity,.25,10,3,false,v->m.sensitivity=v));row++;
  }
  addRenderableWidget(Button.builder(Component.literal(Ui.t("Готово")),b->onClose()).bounds(left,top+184,wide,20).build());
 }
 public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float dt){g.fill(0,0,width,height,0xEF111420);g.centeredText(font,title,width/2,top-16,0xFF92F5D1);super.extractRenderState(g,x,y,dt);int row=0;for(DanceTarget target:DanceTarget.values())g.text(font,Ui.t(target.label),left,top+row++*42,0xFFE6E8EF);g.text(font,Ui.t("Красный: желе > 1.5 / чувствительность > 3.0"),left,top+170,0xFFAAAAAA);}
 public boolean isPauseScreen(){return false;}
 public void onClose(){JellyBeat.CONFIG.save();minecraft.gui.setScreen(parent);}
}
