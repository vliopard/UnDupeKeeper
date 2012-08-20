package tools;
import settings.Settings;

/**
 * Logger class is responsible for centralize debug and message mechanism,
 * allowing developer to use filters and levels of displaying message,
 * controlling source code by sectors.
 * 
 * @author vliopard
 */
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

    /**
     * This method displays a message using the default output system.
     * 
     * @param message
     *            A <code>String</code> containing the message to display.
     */
    public static void msg(String message)
    {
        System.out.println(message);
    }

    /**
     * This method displays an error message using the default error output
     * system.
     * 
     * @param errorMessage
     *            A <code>String</code> containing the error message to display.
     */
    public static void err(String errorMessage)
    {
        System.err.println(errorMessage);
    }

    /**
     * This is a formatter that places a log message with two tab separated
     * values.
     * 
     * @param debugModule
     *            A <code>String</code> that contains the module identification
     *            that will be displayed.
     * @param debugMessage
     *            A <code>String</code> that contains the log message.
     */
    private static void messageFormat(String debugModule,
                                      String debugMessage)
    {
        System.out.println(debugModule+
                           Settings.Tab+
                           debugMessage);
    }

    /**
     * This is the main log handler. It decides what to display according to the
     * developer settings.
     * 
     * @param threadStackTrace
     *            A <code>Thread</code> object from parent object that contains
     *            a stack trace.
     * @param logMessage
     *            A <code>String</code> containing the log message that will be
     *            displayed.
     * @param systemModule
     *            An <code>int</code> value that represents the module that is
     *            being executed.
     */
    public static void log(Thread threadStackTrace,
                           String logMessage,
                           int systemModule)
    {
        if(((!COMPLETE_DISABLED)&&(!logMessage.startsWith("*")))||
           (logMessage.startsWith("!")))
        {
            if(logMessage.startsWith("!"))
            {
                systemModule=ALL_SOFTWARE;
                if(ALL_SOFTWARE_ENABLED==false)
                {
                    ALL_SOFTWARE_ENABLED=true;
                    undo=true;
                }
            }
            String debugModuleName=null;
            if(logMessage.startsWith(Settings.Blank)||
               logMessage.startsWith("!"))
            {
                debugModuleName=threadStackTrace.getStackTrace()[2].getClassName();
                debugModuleName="["+
                                debugModuleName.substring(debugModuleName.lastIndexOf(".")+1)+
                                "."+
                                threadStackTrace.getStackTrace()[3].getMethodName()+
                                "]"+
                                Settings.Tab;
            }
            else
            {
                debugModuleName="[___________]"+
                                Settings.Tab;
            }
            switch(systemModule)
            {
                case UNDUPEKEEPER:
                    if(UNDUPEKEEPER_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                case DATABASE:
                    if(DATABASE_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                case WORKER:
                    if(WORKER_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                case CHECKSUM:
                    if(CHECKSUM_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                case MONITOR:
                    if(MONITOR_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                case USERINTERFACE:
                    if(USERINTERFACE_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                case TRAYIMAGE:
                    if(TRAYIMAGE_ENABLED||
                       ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
                                      logMessage);
                    }
                break;
                default:
                    if(ALL_SOFTWARE_ENABLED)
                    {
                        messageFormat(debugModuleName,
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
