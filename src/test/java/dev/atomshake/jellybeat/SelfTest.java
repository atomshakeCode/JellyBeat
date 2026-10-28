package dev.atomshake.jellybeat;
public class SelfTest {
 public static void main(String[] args){
  for(int i=0;i<200000;i++){var s=DanceMath.shape(i*.01,i*.5,(i%10001)/100f);if(!(s.x()>0&&s.y()>0&&s.z()>0)||Math.abs(s.x()*s.y()*s.z()-1)>1e-5)throw new AssertionError("Unsafe deformation");}
  var idle=DanceMath.shape(40,30,0);if(idle.x()!=1||idle.y()!=1||idle.z()!=1||idle.lift()!=0||idle.yaw()!=0||idle.tilt()!=0)throw new AssertionError("Non-neutral idle");
  System.out.println("PASS: positive scales, conserved volume, neutral idle");
 }
}
