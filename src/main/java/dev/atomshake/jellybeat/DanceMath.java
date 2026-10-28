package dev.atomshake.jellybeat;
public final class DanceMath {
 public record Shape(float x,float y,float z,float tilt,float yaw,float lift){}
 public static Shape shape(double seconds,double phase,float strength){
  double wave=Math.sin(seconds*8.0+phase),swing=Math.sin(seconds*4.0+phase);
  float y=(float)Math.clamp(1+strength*.38*wave,.125,8),xz=(float)(1/Math.sqrt(y));
  return new Shape(xz,y,xz,(float)(strength*.16*swing),(float)(strength*.20*Math.cos(seconds*4+phase)),(float)(strength*.12*Math.max(0,wave)));
 }
}
