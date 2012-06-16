package tools;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;
import java.io.Serializable;
import settings.Settings;

// TODO: JAVADOC
/**
 * 
 * @author vliopard
 */
public class SettingsHandler implements
                            Serializable
{
    private boolean           settingsChanged   =false;
    private boolean           directoryChanged  =false;
    private boolean           encryptionChanged =false;
    private int               lookAndFeel;
    private int               encryptionMethod;
    private int               xScreenAxis;
    private int               yScreenAxis;
    private String            directoryToWatch;
    private static final long serialVersionUID  =-5557071137249098782L;
    transient InputStream     is                =System.in;

    /**
     * 
     */
    public SettingsHandler()
    {
        final Toolkit toolKit=Toolkit.getDefaultToolkit();
        final Dimension screenSize=toolKit.getScreenSize();
        directoryToWatch=Settings.RootDir;
        lookAndFeel=Settings.LookNimbus;
        encryptionMethod=Settings.CypherMethodSHA512;
        xScreenAxis=(screenSize.width-320)/2;
        yScreenAxis=(screenSize.height-110)/2;
        settingsChanged=false;
        directoryChanged=false;
    }

    /**
     * 
     * @param directory
     * @param lookNfeel
     * @param encryptionAlgorithm
     * @param xScreenPosition
     * @param yScreenPosition
     */
    public SettingsHandler(String directory,
                           int lookNfeel,
                           int encryptionAlgorithm,
                           int xScreenPosition,
                           int yScreenPosition)
    {
        directoryToWatch=directory;
        lookAndFeel=lookNfeel;
        encryptionMethod=encryptionAlgorithm;
        xScreenAxis=xScreenPosition;
        yScreenAxis=yScreenPosition;
    }

    /**
     * 
     * @param xScreenPosition
     * @param yScreenPosition
     */
    public void setXY(int xScreenPosition,
                      int yScreenPosition)
    {
        xScreenAxis=xScreenPosition;
        yScreenAxis=yScreenPosition;
    }

    /**
     * 
     * @param directory
     */
    public void setDirectory(String directory)
    {
        directoryToWatch=directory;
        directoryChanged=true;
    }

    /**
     * 
     * @param lookNfeel
     */
    public void setLookAndFeel(int lookNfeel)
    {
        lookAndFeel=lookNfeel;
    }

    /**
     * 
     * @param encryptionAlgorithm
     */
    public void setEncryptionMethod(int encryptionAlgorithm)
    {
        if(encryptionMethod!=encryptionAlgorithm)
        {
            encryptionChanged=true;
        }
        else
        {
            encryptionChanged=false;
        }
        encryptionMethod=encryptionAlgorithm;
    }

    /**
     * 
     * @param xScreenPosition
     */
    public void setX(int xScreenPosition)
    {
        xScreenAxis=xScreenPosition;
    }

    /**
     * 
     * @param yScreenPosition
     */
    public void setY(int yScreenPosition)
    {
        yScreenAxis=yScreenPosition;
    }

    /**
     * 
     * @return Returns an <code>String</code> containing a path to the directory
     *         to be observed.
     */
    public String getDirectory()
    {
        return directoryToWatch;
    }

    /**
     * 
     * @return Returns an <code>int</code> value that represents a 'Look and
     *         Feel' from "<code>Settings.LookAndFeelNames[]</code>"
     */
    public int getLookAndFeel()
    {
        return lookAndFeel;
    }

    /**
     * 
     * @return Returns an <code>int</code> value that represents an encryption
     *         method from "<code>Settings.CypherMethodList[]</code>"
     */
    public int getEncryptionMethod()
    {
        return encryptionMethod;
    }

    /**
     * 
     * @return Returns an <code>int</code> value of the last X axis settings
     *         screen
     *         position.
     */
    public int getX()
    {
        return xScreenAxis;
    }

    /**
     * 
     * @return Returns an <code>int</code> value of the last Y axis settings
     *         screen
     *         position.
     */
    public int getY()
    {
        return yScreenAxis;
    }

    /**
     * 
     * @param confirmation
     */
    public void setChanged(boolean confirmation)
    {
        settingsChanged=confirmation;
    }

    /**
     * 
     */
    public void resetEncryptionChanged()
    {
        encryptionChanged=false;
    }

    /**
     * 
     * @return Returns <code>true</code> if settings were changed. Returns
     *         <code>false</code> if settings remain unchanged.
     */
    public boolean isChanged()
    {
        return settingsChanged;
    }

    /**
     * 
     * @return Returns <code>true</code> if directory has never changed before.
     *         Returns <code>false</code> if directory has already being changed
     *         later.
     */
    public boolean isDirectoryFirstTime()
    {
        return !directoryChanged;
    }

    /**
     * 
     * @return Returns <code>true</code> if encryption method is changed.
     *         Returns <code>false</code> if encryption method remains
     *         unchanged.
     */
    public boolean isEncryptionChanged()
    {
        return encryptionChanged;
    }
}
