package dev.atomshake.jellybeat;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.function.DoubleConsumer;
public final class MotionSlider extends AbstractSliderButton {
 private final String label;private final double min,max,threshold;private final boolean integer;private final DoubleConsumer setter;
 public MotionSlider(String label,int x,int y,int w,double initial,double min,double max,double threshold,boolean integer,DoubleConsumer setter){super(x,y,w,20,Component.empty(),(initial-min)/(max-min));this.label=label;this.min=min;this.max=max;this.threshold=threshold;this.integer=integer;this.setter=setter;updateMessage();}
 public static int loadColor(double value,double safe,double max){
  double t=Math.clamp((value-safe)/(max-safe),0,1);
  int green=t<=.5?255:(int)Math.round(255+(85-255)*(t-.5)*2);
  int blue=t<=.5?(int)Math.round(255+(85-255)*t*2):85;
  return (255<<16)|(green<<8)|blue;
 }
 private double shown(){double n=min+value*(max-min);return integer?Math.round(n):Math.round(n*100)/100.0;}
 protected void updateMessage(){double n=shown();var text=Component.literal(label+": "+(integer?Long.toString(Math.round(n)):String.format(java.util.Locale.ROOT,"%.2f",n)));if(integer)text.withStyle(style->style.withColor(loadColor(n,threshold,max)));else if(n>threshold)text.withStyle(ChatFormatting.RED);setMessage(text);}
 protected void applyValue(){setter.accept(shown());JellyBeat.CONFIG.save();}
}
