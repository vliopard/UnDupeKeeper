package settings;
 
/**
 * Settings class contains all the system values for easy access. This is a
 * place to keep constants.
 * 
 * @author vliopard
 */
public class Settings
{
    public static String         os                       = System.getProperty("os.name").toLowerCase();
    public static String         undupeVersion            ="21.01.25.09.43";
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
    public static boolean        comparisonIsON           =false;
    public static String         CypherMethod             =CypherMethodList[Settings.CypherMethodMD5];
    public static final int      CypherMethodCRC32        =0;
    public static final int      CypherMethodMD5          =1;
    public static final int      CypherMethodSHA          =2;
    public static final int      CypherMethodSHA1         =3;
    public static final int      CypherMethodSHA2         =4;
    public static final int      CypherMethodSHA224       =5;
    public static final int      CypherMethodSHA256       =6;
    public static final int      CypherMethodSHA384       =7;
    public static final int      CypherMethodSHA512       =8;
    public static final int      TotalArguments           =3;
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
    public static final int      IconDupeCalc             =7;
    public static final int      IconDnaMini              =8;
    public static final int      IconYellow               =9;
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
    public static final char     cRootDir                 ='/';
    public static final char     cSlash                   =os.indexOf("win") >= 0 ? '\\' : cRootDir;
    public static final String   Empty                    ="";
    public static final String   Blank                    =" ";
    public static final String   Dot                      =".";
    public static final String   Quote                    ="\"";
    public static final String   QuoteFile                =os.indexOf("win") >= 0 ? "\"" : "";
    public static final String   RootDir                  ="/";
    public static final String   Slash                    =os.indexOf("win") >= 0 ? "\\" : RootDir;
    public static final String   Tab                      ="\t";
    public static final String   endl                     ="\n";
    public static final String   EndLine                  ="\r\n";
    public static final String   delimiter                =os.indexOf("win") >= 0 ? EndLine : endl;
    public static final String   Recursive                ="-r";
    public static final String   TextFileList             ="-f";
    public static final String   FileExtension            =".jpg";
    public static final String   CompareAsc               ="keep_first";
    public static final String   CompareDesc              ="keep_last";
    public static final String   CompareRecursive         ="recursive";
    public static final String   SysNatCompareCommand     =os.indexOf("win") >= 0 ? "fc /B " : "cmp -b ";
    public static final String   SysExeCompareCommand     =os.indexOf("win") >= 0 ? "comp /m " : "diff --brief ";
    public static final String   CompareNatCommandResult  =os.indexOf("win") >= 0 ? "FC: no differences encountered" : Empty;
    public static final String   CompareExeCommandResult  =os.indexOf("win") >= 0 ? "Files compare OK" : Empty;
    public static final String   WorkerPrepareToExit      ="ExitSignal";
    public static final String   HexHashValues            ="0123456789ABCDEF";
    public static final String   WatchedDirectoryName     ="UnDupeKeeper.dir";
    public static final String   UnDupeKeeperDatabaseName ="UnDupeKeeper.hdb";
    public static final String   UnDupeKeeperDatabaseMap  ="UnDupeKeeper.map";
    public static final String   UnDupeKeeperSettings     ="UnDupeKeeper.set";
    public static final String   UnDupeKeeperTextFile     ="_text_file_list_(Dup3K33p).txt";
    public static final String   UnDupeKeeperExtension    =".(Dup3K33p)";
    public static final String   UnDupeKeeperMarker       ="[_REMOVE_]_{Dup3K33p}";
    public static final String   UnDupeKeeperNoRename       ="_N0R3n@me_";
    public static final String   UnDupeKeeperSignature    =" repeeKepuDnU{.-::![|@|]!::-.}UnDupeKeeper ";
    public static final String   SystemTrayIconSize       ="16";
    public static final String   LanguagePackage          ="languages.";
    public static final String   SystemTrayImageDirectory ="/images/" + SystemTrayIconSize + "/";
    public static final String   Plaf                     ="com.sun.java.swing.plaf.";
    public static final String   Plafx                    ="javax.swing.plaf.";

    public static final String   Separator                ="_____________________";
    public static final String   SeparatorSingle          ="---------------------";
    public static final String   SeparatorDouble          ="=====================";
    public static final String   SeparatorVertical        ="|||||||||||||||||||||";

    public static final String   languageList[]           =
                                                          {
            "English",
            "Portuguese"
                                                          };
    public static final String   languageValues[]         =
                                                          {
            "en_us",
            "pt_br"
                                                          };
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
            Plaf  + "gtk.GTKLookAndFeel",
            Plaf  + "motif.MotifLookAndFeel",
            Plafx + "nimbus.NimbusLookAndFeel",
            Plaf  + "windows.WindowsLookAndFeel",
            Plafx + "basic.BasicLookAndFeel",
            Plafx + "metal.MetalLookAndFeel",
            Plafx + "multi.MultiLookAndFeel",
            Plafx + "nimbus.NimbusLookAndFeel",
            Plafx + "synth.SynthLookAndFeel"
                                                          };
    public static final String[] iconList                 =
                                                          {
            SystemTrayImageDirectory + "dnaColor"  + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dnaGray"   + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dnaGreen"  + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dnaRed"    + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dupeArrow" + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dupeFile"  + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dupeLoupe" + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dupeCalc"  + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "dnaMini"   + SystemTrayIconSize + FileExtension,
            SystemTrayImageDirectory + "yellow"    + SystemTrayIconSize + FileExtension
                                                          };
}
