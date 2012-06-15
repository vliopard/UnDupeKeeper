package settings;
/**
 * 
 * @author vliopard
 */
public class Settings
{
    // TODO: Menu -> Source - > Externalize Strings
    public static String         CypherMethodList[]       =
                                                          {
            "CRC32",
            "MD5",
            "SHA1",
            "SHA-1",
            "SHA-2",
            "SHA-224",
            "SHA-256",
            "SHA-384",
            "SHA-512"
                                                          };
    public static String         CypherMethod             =CypherMethodList[Settings.CypherMethodSHA512];
    public static final int      CypherMethodCRC32        =0;
    public static final int      CypherMethodMD5          =1;
    public static final int      CypherMethodSHA          =2;
    public static final int      CypherMethodSHA1         =3;
    public static final int      CypherMethodSHA2         =4;
    public static final int      CypherMethodSHA224       =5;
    public static final int      CypherMethodSHA256       =6;
    public static final int      CypherMethodSHA384       =7;
    public static final int      CypherMethodSHA512       =8;
    public static final int      TotalArguments           =2;
    public static final int      KeepWorking              =0;
    public static final int      StopWorking              =1;
    public static final int      WorkerStopped            =2;
    public static final int      BlinkerStopped           =3;
    public static final int      WorkerStopSignal         =0;
    public static final int      FileCreated              =1;
    public static final int      FileModified             =2;
    public static final int      FileDeleted              =3;
    public static final int      FileRenamed              =4;
    public static final int      LookGTK                  =0;
    public static final int      LookMotif                =1;
    public static final int      LookNimbus               =2;
    public static final int      LookWindows              =3;
    public static final int      LookBasic                =4;
    public static final int      LookMetal                =5;
    public static final int      LookMulti                =6;
    public static final int      LookNimbux               =7;
    public static final int      LookSynth                =8;
    public static final int      IconDnaColor             =0;
    public static final int      IconDnaGray              =1;
    public static final int      IconDnaGreen             =2;
    public static final int      IconDnaRed               =3;
    public static final int      IconDupeArrow            =4;
    public static final int      IconDupeFile             =5;
    public static final int      IconDupeLoupe            =6;
    public static final int      IconHeight               =16;
    public static final int      IconWidth                =16;
    public static final int      WaitForFileTimeOut       =50;
    public static final int      ExitSleepTime            =100;
    public static final int      TraySleepTime            =2000;
    public static final int      ThreadSleepTime          =5000;
    public static final byte[]   HEX_CHAR_TABLE           =
                                                          {
            (byte)'0',
            (byte)'1',
            (byte)'2',
            (byte)'3',
            (byte)'4',
            (byte)'5',
            (byte)'6',
            (byte)'7',
            (byte)'8',
            (byte)'9',
            (byte)'a',
            (byte)'b',
            (byte)'c',
            (byte)'d',
            (byte)'e',
            (byte)'f'
                                                          };
    public static final String   RootDir                  ="/";
    public static final String   Recursive                ="-r";
    public static final String   FileExtension            =".jpg";
    public static final String   WorkerPrepareToExit      ="ExitSignal";
    public static final String   HexHashValues            ="0123456789ABCDEF";
    public static final String   WatchedDirectoryName     ="UnDupeKeeper.dir";
    public static final String   UnDupeKeeperDatabaseName ="UnDupeKeeper.hdb";
    public static final String   UnDupeKeeperSettings     ="UnDupeKeeper.set";
    public static final String   UnDupeKeeperExtension    =".(Dup3K33p)";
    public static final String   UnDupeKeeperSignature    =" repeeKepuDnU{.-::![|@|]!::-.}UnDupeKeeper ";
    public static final String   SystemTrayIconSize       ="16";
    public static final String   SystemTrayImageDirectory ="/images/"+
                                                           SystemTrayIconSize+
                                                           "/";
    public static final String   Plaf                     ="com.sun.java.swing.plaf.";
    public static final String   Plafx                    ="javax.swing.plaf.";
    public static final String[] LookAndFeelNames         =
                                                          {
            "GTK",
            "Motif",
            "Nimbus",
            "Windows",
            "Basic",
            "Metal",
            "Multi",
            "Nimbux",
            "Synth"
                                                          };
    public static final String[] LookAndFeelPackages      =
                                                          {
            Plaf+
                    "gtk.GTKLookAndFeel",
            Plaf+
                    "motif.MotifLookAndFeel",
            Plaf+
                    "nimbus.NimbusLookAndFeel",
            Plaf+
                    "windows.WindowsLookAndFeel",
            Plafx+
                    "basic.BasicLookAndFeel",
            Plafx+
                    "metal.MetalLookAndFeel",
            Plafx+
                    "multi.MultiLookAndFeel",
            Plafx+
                    "nimbus.NimbusLookAndFeel",
            Plafx+
                    "synth.SynthLookAndFeel"
                                                          };
    public static final String[] iconList                 =
                                                          {
            SystemTrayImageDirectory+
                    "dnaColor"+
                    SystemTrayIconSize+
                    FileExtension,
            SystemTrayImageDirectory+
                    "dnaGray"+
                    SystemTrayIconSize+
                    FileExtension,
            SystemTrayImageDirectory+
                    "dnaGreen"+
                    SystemTrayIconSize+
                    FileExtension,
            SystemTrayImageDirectory+
                    "dnaRed"+
                    SystemTrayIconSize+
                    FileExtension,
            SystemTrayImageDirectory+
                    "dupeArrow"+
                    SystemTrayIconSize+
                    FileExtension,
            SystemTrayImageDirectory+
                    "dupeFile"+
                    SystemTrayIconSize+
                    FileExtension,
            SystemTrayImageDirectory+
                    "dupeLoupe"+
                    SystemTrayIconSize+
                    FileExtension
                                                          };
}
