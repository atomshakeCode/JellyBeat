package dev.atomshake.jellybeat;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.nio.file.*;
import java.util.List;
import java.util.function.*;
public final class JellyScreen extends Screen {
 private final Screen parent;private int left,top,wide;
 public JellyScreen(Screen parent){super(Component.literal(Ui.t("JellyBeat · желейная дискотека")));this.parent=parent;}
 private Button button(String label,int x,int y,int w,Runnable action){return addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,20).build());}
 private void toggle(String label,int x,int y,int w,BooleanSupplier get,Consumer<Boolean> set){
  addRenderableWidget(Button.builder(Component.literal(label+": "+(get.getAsBoolean()?Ui.t("ВКЛ"):Ui.t("ВЫКЛ"))),b->{set.accept(!get.getAsBoolean());JellyBeat.CONFIG.save();b.setMessage(Component.literal(label+": "+(get.getAsBoolean()?Ui.t("ВКЛ"):Ui.t("ВЫКЛ"))));}).bounds(x,y,w,20).build());
 }
 private void slider(String label,int x,int y,int w,double initial,double min,double max,DoubleConsumer setter){
  addRenderableWidget(new MotionSlider(label,x,y,w,initial,min,max,label.equals(Ui.t("Лимит блоков"))?512:20,true,setter));
 }
 protected void init(){
  var c=JellyBeat.CONFIG;wide=Math.min(440,width-24);left=(width-wide)/2;top=Math.max(16,(height-252)/2);int half=(wide-6)/2;
  button(Ui.t("Выход: ")+font.plainSubstrByWidth(JellyBeat.AUDIO.selectedName(),wide-80),left,top,wide,()->minecraft.gui.setScreen(new DeviceScreen(this)));
  int gap=5,bw=(wide-2*gap)/3;
  button(Ui.t("Подключить"),left,top+24,bw,()->JellyBeat.AUDIO.connect());
  button(Ui.t("Отключить"),left+bw+gap,top+24,bw,()->JellyBeat.AUDIO.stop());
  button(Ui.t("Тест 120 BPM"),left+2*(bw+gap),top+24,bw,()->JellyBeat.AUDIO.demo());
  toggle(Ui.t("Танцы"),left,top+48,half,()->c.enabled,v->c.enabled=v);
  if(VoiceBeats.available)toggle("Voice Chat",left+half+6,top+48,half,()->c.voicechat,v->c.voicechat=v);else button(Ui.t("Voice Chat не установлен"),left+half+6,top+48,half,()->{});
  toggle(Ui.t("Блоки поверхности"),left,top+72,half,()->c.blocks,v->c.blocks=v);
  toggle(Ui.t("Мобы / сущности"),left+half+6,top+72,half,()->c.entities,v->c.entities=v);
  toggle(Ui.t("Игроки"),left,top+96,half,()->c.players,v->c.players=v);
  toggle(Ui.t("Лодки / плоты"),left+half+6,top+96,half,()->c.boats,v->c.boats=v);
  button(Ui.t("Чувствительность и желе → по объектам"),left,top+120,wide,()->minecraft.gui.setScreen(new MotionScreen(this)));
  slider(Ui.t("Лимит блоков"),left,top+144,half,c.maxBlocks,16,3072,v->c.maxBlocks=(int)Math.round(v));
  slider(Ui.t("Радиус блоков"),left+half+6,top+144,half,c.radius,2,60,v->c.radius=(int)Math.round(v));
  button(Ui.t("Готово"),left,top+202,wide,this::onClose);
 }
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(0,0,width,height,0xE5111420);g.centeredText(font,title,width/2,top-13,0xFF92F5D1);
  super.extractRenderState(g,mx,my,delta);
  String status=JellyBeat.AUDIO.status();g.text(font,font.plainSubstrByWidth(status,wide),left,top+170,0xFFE6E8EF);
  g.text(font,Ui.t("Слушает выбранный выход ПК · звук не записывается"),left,top+182,0xFF9EA9BF);
  g.fill(left,top+194,left+(int)(wide*Math.min(1,JellyBeat.AUDIO.pulse())),top+197,0xFF64EDB4);
  if(height>=top+236)g.centeredText(font,Ui.t("J — меню · K — танцы · громкость меняется в плеере"),width/2,top+226,0xFF9EA9BF);
 }

 public boolean isPauseScreen(){return false;}
 public void onClose(){JellyBeat.CONFIG.save();minecraft.gui.setScreen(parent);}
}
