using System;
using System.IO;
using System.Text;
using System.Threading;
using System.Runtime.InteropServices;
using System.Globalization;
[assembly:System.Reflection.AssemblyVersion("0.2.0.0")]
[ComImport,Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")] class EnumeratorClass{}
[ComImport,Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface IEnum {
 [PreserveSig]int EnumAudioEndpoints(int flow,uint state,out ICollection devices);
 [PreserveSig]int GetDefaultAudioEndpoint(int flow,int role,out IDevice device);
 [PreserveSig]int GetDevice([MarshalAs(UnmanagedType.LPWStr)]string id,out IDevice device);
 [PreserveSig]int RegisterEndpointNotificationCallback(IntPtr p);
 [PreserveSig]int UnregisterEndpointNotificationCallback(IntPtr p);
}
[ComImport,Guid("0BD7A1BE-7A1A-44DB-8397-CC5392387B5E"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface ICollection {
 [PreserveSig]int GetCount(out uint n);[PreserveSig]int Item(uint n,out IDevice d);
}
[ComImport,Guid("D666063F-1587-4E43-81F1-B948E807363F"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface IDevice {
 [PreserveSig]int Activate(ref Guid id,uint context,IntPtr p,[MarshalAs(UnmanagedType.IUnknown)]out object value);
 [PreserveSig]int OpenPropertyStore(uint access,out IProps props);
 [PreserveSig]int GetId([MarshalAs(UnmanagedType.LPWStr)]out string id);
 [PreserveSig]int GetState(out uint state);
}
[StructLayout(LayoutKind.Sequential)] struct Key {public Guid fmtid;public uint pid;}
[StructLayout(LayoutKind.Explicit,Size=24)] struct Variant {[FieldOffset(0)]public ushort type;[FieldOffset(8)]public IntPtr ptr;}
[ComImport,Guid("886D8EEB-8CF2-4446-8D02-CDBA1DBDCF99"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface IProps {
 [PreserveSig]int GetCount(out uint n);[PreserveSig]int GetAt(uint i,out Key key);[PreserveSig]int GetValue(ref Key k,out Variant v);[PreserveSig]int SetValue(ref Key k,ref Variant v);[PreserveSig]int Commit();
}
[ComImport,Guid("1CB9AD4C-DBFA-4C32-B178-C2F568A703B2"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface IAudio {
 [PreserveSig]int Initialize(int mode,uint flags,long duration,long periodicity,IntPtr format,IntPtr guid);
 [PreserveSig]int GetBufferSize(out uint n);[PreserveSig]int GetStreamLatency(out long n);[PreserveSig]int GetCurrentPadding(out uint n);
 [PreserveSig]int IsFormatSupported(int mode,IntPtr f,out IntPtr closest);[PreserveSig]int GetMixFormat(out IntPtr f);
 [PreserveSig]int GetDevicePeriod(out long a,out long b);[PreserveSig]int Start();[PreserveSig]int Stop();[PreserveSig]int Reset();[PreserveSig]int SetEventHandle(IntPtr h);
 [PreserveSig]int GetService(ref Guid id,[MarshalAs(UnmanagedType.IUnknown)]out object service);
}
[ComImport,Guid("C8ADBD64-E71E-48A0-A4DE-185C395CD317"),InterfaceType(ComInterfaceType.InterfaceIsIUnknown)] interface ICapture {
 [PreserveSig]int GetBuffer(out IntPtr data,out uint frames,out uint flags,out ulong position,out ulong clock);
 [PreserveSig]int ReleaseBuffer(uint frames);[PreserveSig]int GetNextPacketSize(out uint frames);
}
class Capture {
 static volatile bool running=true;
 [DllImport("ole32.dll")]static extern int PropVariantClear(ref Variant value);
 static void Check(int hr){if(hr<0)Marshal.ThrowExceptionForHR(hr);}
 static void Release(object o){if(o!=null&&Marshal.IsComObject(o))Marshal.ReleaseComObject(o);}
 static string B64(string s){return Convert.ToBase64String(Encoding.UTF8.GetBytes(s));}
 static string Name(IDevice d){IProps p=null;Variant v=new Variant();try{Check(d.OpenPropertyStore(0,out p));Key k=new Key{fmtid=new Guid("A45C254E-DF1C-4EFD-8020-67D146A850E0"),pid=14};Check(p.GetValue(ref k,out v));return v.type==31?Marshal.PtrToStringUni(v.ptr):"Audio device";}finally{PropVariantClear(ref v);Release(p);}}
 [MTAThread]static int Main(string[] args){
  Console.SetOut(new StreamWriter(Console.OpenStandardOutput(),new UTF8Encoding(false)){AutoFlush=true});
  IEnum enumeration=null;IDevice device=null;IAudio audio=null;ICapture capture=null;IntPtr format=IntPtr.Zero;
  try{
   enumeration=(IEnum)new EnumeratorClass();
   if(args.Length==0||args[0]=="--list"){
    ICollection devices=null;try{Check(enumeration.EnumAudioEndpoints(0,1,out devices));uint n;Check(devices.GetCount(out n));for(uint i=0;i<n;i++){IDevice d=null;try{Check(devices.Item(i,out d));string id;Check(d.GetId(out id));Console.WriteLine("DEVICE\t"+B64(id)+"\t"+Name(d).Replace('\t',' ').Replace('\n',' ').Replace('\r',' '));}finally{Release(d);}}}finally{Release(devices);}return 0;
   }
   if(args.Length<2||args[1]=="default")Check(enumeration.GetDefaultAudioEndpoint(0,0,out device));else Check(enumeration.GetDevice(Encoding.UTF8.GetString(Convert.FromBase64String(args[1])),out device));
   Guid iid=new Guid("1CB9AD4C-DBFA-4C32-B178-C2F568A703B2");object obj;Check(device.Activate(ref iid,23,IntPtr.Zero,out obj));audio=(IAudio)obj;Check(audio.GetMixFormat(out format));
   ushort tag=(ushort)Marshal.ReadInt16(format,0),channels=(ushort)Marshal.ReadInt16(format,2),align=(ushort)Marshal.ReadInt16(format,12),bits=(ushort)Marshal.ReadInt16(format,14);int rate=Marshal.ReadInt32(format,4);
   int kind=tag==65534?Marshal.ReadInt32(format,24):tag;
   if(!((kind==3&&bits==32)||(kind==1&&(bits==16||bits==24||bits==32))))throw new Exception("Unsupported device mix format");
   Check(audio.Initialize(0,0x20000,1000000,0,format,IntPtr.Zero));iid=new Guid("C8ADBD64-E71E-48A0-A4DE-185C395CD317");Check(audio.GetService(ref iid,out obj));capture=(ICapture)obj;Check(audio.Start());
   new Thread(()=>{try{Console.In.ReadLine();}catch{}running=false;}){IsBackground=true}.Start();
   Console.WriteLine("READY\t"+rate+"\t"+channels);
   double[] low=new double[channels];double alpha=1-Math.Exp(-2*Math.PI*180/rate),es=0,bs=0;long count=0;var timer=System.Diagnostics.Stopwatch.StartNew();long last=0;byte[] buffer=new byte[align*rate/2];
   while(running){uint frames;Check(capture.GetNextPacketSize(out frames));while(frames>0){IntPtr data;uint flags;ulong pos,clock;Check(capture.GetBuffer(out data,out frames,out flags,out pos,out clock));try{
     int length=checked((int)frames*align);if(length>buffer.Length)buffer=new byte[length];bool silent=(flags&2)!=0;if(!silent)Marshal.Copy(data,buffer,0,length);
     for(int f=0;f<(int)frames;f++){for(int c=0;c<channels;c++){int at=f*align+c*(bits/8);double value=0;
      if(!silent){if(kind==3)value=BitConverter.ToSingle(buffer,at);else if(bits==16)value=BitConverter.ToInt16(buffer,at)/32768.0;else if(bits==32)value=BitConverter.ToInt32(buffer,at)/2147483648.0;else{int n=buffer[at]|(buffer[at+1]<<8)|(buffer[at+2]<<16);if((n&0x800000)!=0)n|=unchecked((int)0xff000000);value=n/8388608.0;}}
      if(double.IsNaN(value)||double.IsInfinity(value))value=0;low[c]+=alpha*(value-low[c]);es+=value*value;bs+=low[c]*low[c];count++;
     }}
    }finally{Check(capture.ReleaseBuffer(frames));}Check(capture.GetNextPacketSize(out frames));}
    if(timer.ElapsedMilliseconds-last>=20){double e=count>0?Math.Sqrt(es/count):0,b=count>0?Math.Sqrt(bs/count):0;Console.WriteLine("LEVEL\t"+e.ToString("F6",CultureInfo.InvariantCulture)+"\t"+b.ToString("F6",CultureInfo.InvariantCulture));es=bs=0;count=0;last=timer.ElapsedMilliseconds;}
    Thread.Sleep(5);
   }return 0;
  }catch(Exception e){Console.WriteLine("ERROR\t"+e.Message.Replace('\n',' ').Replace('\r',' '));return 1;}
  finally{if(audio!=null)audio.Stop();Release(capture);Release(audio);if(format!=IntPtr.Zero)Marshal.FreeCoTaskMem(format);Release(device);Release(enumeration);}
 }
}

