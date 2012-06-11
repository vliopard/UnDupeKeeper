package tools;
public class Logger
{
    public static final int ALL_SOFTWARE          =0;
    public static final int DATABASE              =1;
    public static final int WORKER                =2;
    public static final int UNDUPEKEEPER          =3;
    public static final int CHECKSUM              =4;
    public static final int MONITOR               =5;
    public static final int USERINTERFACE         =6;
    public static final int TRAYIMAGE             =7;
    /* ========================================================= */
    private static boolean  COMPLETE_DISABLED     =false;
    /* ========================================================= */
    private static boolean  ALL_SOFTWARE_ENABLED  =false;
    /* ========================================================= */
    private static boolean  UNDUPEKEEPER_ENABLED  =false;
    private static boolean  DATABASE_ENABLED      =false;
    private static boolean  WORKER_ENABLED        =false;
    private static boolean  CHECKSUM_ENABLED      =false;
    private static boolean  MONITOR_ENABLED       =false;
    private static boolean  USERINTERFACE_ENABLED =false;
    private static boolean  TRAYIMAGE_ENABLED     =false;
    private static boolean  undo                  =false;

    public static void msg(String message)
    {
        System.out.println(message);
    }

    public static void err(String errorMessage)
    {
        System.err.println(errorMessage);
    }

    private static void lprint(String type,
                               String message)
    {
        System.out.println(type+
                           "\t"+
                           message);
    }

    public static void log(Thread typo,
                           String logMessage,
                           int module)
    {
        if(((!COMPLETE_DISABLED)&&(!logMessage.startsWith("*")))||
           (logMessage.startsWith("!")))
        {
            if(logMessage.startsWith("!"))
            {
                module=ALL_SOFTWARE;
                if(ALL_SOFTWARE_ENABLED==false)
                {
                    ALL_SOFTWARE_ENABLED=true;
                    undo=true;
                }
            }
            String type=null;
            if(logMessage.startsWith(" ")||
               logMessage.startsWith("!"))
            {
                type=typo.getStackTrace()[2].getClassName();
                type="["+
                     type.substring(type.lastIndexOf(".")+1)+
                     "."+
                     typo.getStackTrace()[3].getMethodName()+
                     "]\t";
            }
            else
            {
                type="[___________]\t";
            }
            switch(module)
            {
                case UNDUPEKEEPER:
                    if(UNDUPEKEEPER_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                case DATABASE:
                    if(DATABASE_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                case WORKER:
                    if(WORKER_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                case CHECKSUM:
                    if(CHECKSUM_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                case MONITOR:
                    if(MONITOR_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                case USERINTERFACE:
                    if(USERINTERFACE_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                case TRAYIMAGE:
                    if(TRAYIMAGE_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
                break;
                default:
                    if(ALL_SOFTWARE_ENABLED)
                    {
                        lprint(type,
                               logMessage);
                    }
            }
            if(undo)
            {
                ALL_SOFTWARE_ENABLED=false;
            }
        }
    }
}
