package dev.atomshake.jellybeat;
/** Intersect pixel clipping with the actual pass, not a potentially stale window snapshot. */
public final class GuiClip {
 public record Rect(int x,int y,int width,int height){public boolean empty(){return width<=0||height<=0;}}
 public static Rect intersect(int x,int y,int width,int height,int ax,int ay,int aw,int ah){
  long left=Math.max((long)x,ax),bottom=Math.max((long)y,ay);
  long right=Math.min((long)x+Math.max(0,width),(long)ax+Math.max(0,aw));
  long top=Math.min((long)y+Math.max(0,height),(long)ay+Math.max(0,ah));
  if(right<=left||top<=bottom)return new Rect(0,0,0,0);
  return new Rect((int)left,(int)bottom,(int)(right-left),(int)(top-bottom));
 }
}
