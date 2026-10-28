package dev.atomshake.jellybeat;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public final class AudioEngine implements AutoCloseable {
 public record Device(String id,String name){}
 public volatile List<Device> devices=List.of(new Device("default","По умолчанию Windows"));
 public volatile String status="Выбери аудиовыход и нажми «Подключить»";
 private final ExecutorService worker=Executors.newCachedThreadPool(r->{Thread t=new Thread(r,"JellyBeat WASAPI");t.setDaemon(true);return t;});
 private final AtomicInteger generation=new AtomicInteger();
 private volatile Process capture;
 private volatile boolean active,demo;
 private volatile long startNanos,lastLevel;
 private volatile float envelope;
 private volatile boolean listing;
 private Path helper()throws Exception{
  if(!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")||!System.getProperty("os.arch").equals("amd64"))throw new IOException("Захват доступен для Windows x64");
  byte[] bytes;try(InputStream in=getClass().getResourceAsStream("/native/windows-x64/jellybeat-capture.exe")){if(in==null)throw new IOException("Нет модуля WASAPI");bytes=in.readAllBytes();}
  String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).substring(0,16);
  Path dir=FabricLoader.getInstance().getConfigDir().resolve("jellybeat/native");Files.createDirectories(dir);Path exe=dir.resolve("capture-"+hash+".exe");
  if(!Files.exists(exe)||!Arrays.equals(Files.readAllBytes(exe),bytes))Files.write(exe,bytes);
  return exe;
 }
 private Process process(String... args)throws Exception{
  List<String> command=new ArrayList<>();command.add(helper().toAbsolutePath().toString());command.addAll(List.of(args));
  return new ProcessBuilder(command).redirectErrorStream(true).start();
 }
 public void refreshDevices(){if(listing)return;listing=true;worker.submit(()->{
  Process p=null;try{p=process("--list");Process timeout=p;CompletableFuture.delayedExecutor(8,TimeUnit.SECONDS).execute(()->{if(timeout.isAlive())timeout.destroyForcibly();});
   var found=new ArrayList<Device>();found.add(new Device("default","По умолчанию Windows"));
   try(var reader=p.inputReader(StandardCharsets.UTF_8)){String line;while((line=reader.readLine())!=null){String[] fields=line.split("\t",3);if(fields.length==3&&fields[0].equals("DEVICE"))found.add(new Device(fields[1],fields[2]));else if(line.startsWith("ERROR"))throw new IOException(line);}}
   if(!p.waitFor(1,TimeUnit.SECONDS)||p.exitValue()!=0)throw new IOException("Не удалось перечислить устройства");devices=List.copyOf(found);
  }catch(Exception e){e.printStackTrace();status="Аудиоустройства: "+e.getMessage();}finally{if(p!=null&&p.isAlive())p.destroyForcibly();listing=false;}
 });}
 public String status(){if(status.startsWith("Слушаю: "))return Ui.t("Слушаю: ")+selectedName();if(status.startsWith("Захват: "))return Ui.t("Захват: ")+Ui.t("Ошибка (см. журнал)");if(status.startsWith("Аудиоустройства: "))return Ui.t("Аудиоустройства: ")+Ui.t("Ошибка (см. журнал)");return Ui.t(status);}
 public String selectedName(){String id=JellyBeat.CONFIG.audioDeviceId;return devices.stream().filter(d->d.id.equals(id)).map(d->Ui.t(d.name())).findFirst().orElse(Ui.t("Устройство недоступно — выбери другое"));}
 public void connect(){stop();int ticket=generation.get();String id=JellyBeat.CONFIG.audioDeviceId;status="Подключение к аудиовыходу...";
  worker.submit(()->{Process p=null;try{
   p=process("--capture",id);synchronized(this){if(ticket!=generation.get()){p.destroyForcibly();return;}capture=p;startNanos=System.nanoTime();}
   double reference=.05,previous=0,kick=0;Process timeout=p;
   CompletableFuture.delayedExecutor(8,TimeUnit.SECONDS).execute(()->{if(ticket==generation.get()&&!active&&timeout.isAlive())timeout.destroyForcibly();});
   try(var reader=p.inputReader(StandardCharsets.UTF_8)){String line;while((line=reader.readLine())!=null&&ticket==generation.get()){
    String[] fields=line.split("\t");
    if(fields[0].equals("READY")){active=true;status="Слушаю: "+selectedName();}
    else if(fields[0].equals("ERROR"))throw new IOException(fields.length>1?fields[1]:line);
    else if(fields.length==3&&fields[0].equals("LEVEL")){
     double rms=Double.parseDouble(fields[1]),bass=Double.parseDouble(fields[2]);if(!Double.isFinite(rms)||!Double.isFinite(bass))continue;
     reference=Math.max(.02,Math.max(bass,reference*.997));kick=Math.max(kick*.82,Math.min(1,Math.max(0,bass-previous)/reference*4));previous=bass;
     double target=rms<JellyBeat.CONFIG.noiseGate?0:Math.min(1,.18*Math.min(1,rms*4)+.42*Math.min(1,bass/reference)+.4*kick);
     envelope+=(float)((target-envelope)*(target>envelope?.7:.28));lastLevel=System.nanoTime();
    }
   }}
   if(ticket==generation.get()){active=false;envelope=0;status="Аудиовыход отключён. Подключись снова.";}
  }catch(Exception e){if(ticket==generation.get()){active=false;envelope=0;e.printStackTrace();status="Захват: "+e.getMessage();}}finally{if(p!=null&&p.isAlive())p.destroyForcibly();}});
 }
 public synchronized void demo(){stop();demo=true;active=true;startNanos=System.nanoTime();status="Визуальный тест 120 BPM (без звука)";}
 public boolean isPlaying(){return active;}
 public double seconds(){return startNanos==0?0:(System.nanoTime()-startNanos)/1e9;}
 public float pulse(){if(!active)return 0;if(demo)return (float)(.8*Math.exp(-(seconds()%.5)*12));if(System.nanoTime()-lastLevel>300_000_000L)return 0;return (float)Math.clamp(envelope,0,1);}
 public synchronized void stop(){generation.incrementAndGet();active=false;demo=false;envelope=0;Process p=capture;capture=null;status="Захват остановлен";
  if(p!=null)worker.submit(()->{try{p.getOutputStream().write("stop\n".getBytes(StandardCharsets.US_ASCII));p.getOutputStream().flush();if(!p.waitFor(700,TimeUnit.MILLISECONDS))p.destroyForcibly();}catch(Exception e){p.destroyForcibly();}});
 }
 public void close(){stop();worker.shutdown();}
}
