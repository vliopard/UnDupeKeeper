package tools;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;
import java.io.Serializable;
import settings.Settings;

/**
 * SettingsHandler class is the main object to store all settings from
 * UnDupeKeeper for save and restore settings data in just only one place.
 * 
 * @author vliopard
 */
public class SettingsHandler implements Serializable
{
    private boolean           settingsChanged         =false;
    private boolean           directoryChanged        =false;
    private boolean           encryptionChanged       =false;
    private boolean           comparisonMethodChanged =false;
    private boolean           comparisonMethod;
    private int               lookAndFeel;
    private int               languageIndex;
    private int               encryptionMethod;
    private int               xScreenAxis;
    private int               yScreenAxis;
    private String            directoryToWatch;
    private String            language;
    private static final long serialVersionUID        =-5557071137249098782L;
    transient InputStream     is                      =System.in;

    /**
     * SettingsHandler Constructor - It starts a new fresh and clean Settings
     * Object with default values.
     */
    public SettingsHandler()
    {
        final Toolkit toolKit=Toolkit.getDefaultToolkit();
        final Dimension screenSize=toolKit.getScreenSize();
        directoryToWatch=Settings.RootDir;
        lookAndFeel=Settings.LookNimbus;
        language=Settings.languageValues[0];
        languageIndex=0;
        encryptionMethod=Settings.CypherMethodSHA512;
        comparisonMethod=Settings.comparisonIsON;
        xScreenAxis=(screenSize.width-320)/2;
        yScreenAxis=(screenSize.height-110)/2;
        settingsChanged=false;
        directoryChanged=false;
    }

    /**
     * SettingsHandler Constructor - It starts a new Settings Object starting
     * with the passed values as parameters.
     * 
     * @param directory
     *            A <code>String</code> value that points to the current working
     *            directory.
     * @param lookNfeel
     *            An <code>int</code> value that informs the current look and
     *            feel of the system.
     * @param encryptionAlgorithm
     *            An <code>int</code> value that informs the current encryption
     *            algorithm of the system.
     * @param xScreenPosition
     *            An <code>int</code> value that informs the horizontal position
     *            of the settings dialog.
     * @param yScreenPosition
     *            An <code>int</code> value that informs the vertical position
     *            of the settings dialog.
     */
    public SettingsHandler(String directory,
                           int idiomIndex,
                           int lookNfeel,
                           int encryptionAlgorithm,
                           int xScreenPosition,
                           int yScreenPosition,
                           boolean compareMethod)
    {
        directoryToWatch=directory;
        languageIndex=idiomIndex;
        language=Settings.languageValues[idiomIndex];
        lookAndFeel=lookNfeel;
        encryptionMethod=encryptionAlgorithm;
        comparisonMethod=compareMethod;
        xScreenAxis=xScreenPosition;
        yScreenAxis=yScreenPosition;
    }

    /**
     * This method sets the position of the settings dialog screen.
     * 
     * @param xScreenPosition
     *            An <code>int</code> value that informs the horizontal position
     *            of the settings dialog.
     * @param yScreenPosition
     *            An <code>int</code> value that informs the vertical position
     *            of the settings dialog.
     */
    public void setXY(int xScreenPosition, int yScreenPosition)
    {
        xScreenAxis=xScreenPosition;
        yScreenAxis=yScreenPosition;
    }

    /**
     * This method sets the working directory of UnDupeKeeper.
     * 
     * @param directory
     *            A <code>String</code> value that represents the path to an
     *            observed directory to keep unduplicated.
     */
    public void setDirectory(String directory)
    {
        directoryToWatch=directory;
        directoryChanged=true;
    }

    /**
     * This method sets the look and feel for the GUIs.
     * 
     * @param lookNfeel
     *            An <code>int</code> value obtained from
     *            <code>Settings.LookAndFeelNames[]</code> list.
     */
    public void setLookAndFeel(int lookNfeel)
    {
        lookAndFeel=lookNfeel;
    }

    /**
     * This method sets the language for the GUIs.
     * 
     * @param idiomIndex
     *            An <code>int</code> value obtained from
     *            <code>Settings.LanguageValues[]</code> list.
     */
    public void setLanguage(int idiomIndex)
    {
        language=Settings.languageValues[idiomIndex];
        languageIndex=idiomIndex;
    }

    /**
     * This method sets the encryption method for getting files checksum.
     * 
     * @param encryptionAlgorithm
     *            An <code>int</code> value obtained from
     *            <code>Settings.CypherMethodList[]</code> list.
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
     * This method sets the comparison method for getting files binary compred.
     * 
     * @param compareMethod
     *            A <code>boolean</code> value obtained from
     *            <code>Settings.notComparing</code>.
     */
    public void setComparisonMethod(boolean compareMethod)
    {
        if(comparisonMethod!=compareMethod)
        {
            comparisonMethodChanged=true;
        }
        else
        {
            comparisonMethodChanged=false;
        }
        comparisonMethod=compareMethod;
    }

    /**
     * This method sets the individual horizontal position of the dialog screen.
     * 
     * @param xScreenPosition
     *            An <code>int</code> value that informs the horizontal position
     *            of the settings dialog.
     */
    public void setX(int xScreenPosition)
    {
        xScreenAxis=xScreenPosition;
    }

    /**
     * This method sets the individual vertical position of the dialog screen.
     * 
     * @param yScreenPosition
     *            An <code>int</code> value that informs the vertical position
     *            of the settings dialog.
     */
    public void setY(int yScreenPosition)
    {
        yScreenAxis=yScreenPosition;
    }

    /**
     * This method returns the current working directory observed by the Monitor
     * and Worker threads.
     * 
     * @return Returns an <code>String</code> containing a path to the directory
     *         to be observed.
     */
    public String getDirectory()
    {
        return directoryToWatch;
    }

    /**
     * This method returns the current look and feel of the system.
     * 
     * @return Returns an <code>int</code> value that represents a 'Look and
     *         Feel' from "<code>Settings.LookAndFeelNames[]</code>"
     */
    public int getLookAndFeel()
    {
        return lookAndFeel;
    }

    /**
     * This method returns the current language of the system.
     * 
     * @return Returns a <code>String</code> value that represents a language
     *         from "<code>Settings.LanguageValues[]</code>"
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * This method returns the current language index of the system.
     * 
     * @return Returns an <code>int</code> value that represents a language
     *         from "<code>Settings.LanguageValues[]</code>"
     */
    public int getLanguageIndex()
    {
        return languageIndex;
    }

    /**
     * This method returns the current encryption method used by the system.
     * 
     * @return Returns an <code>int</code> value that represents an encryption
     *         method from "<code>Settings.CypherMethodList[]</code>"
     */
    public int getEncryptionMethod()
    {
        return encryptionMethod;
    }

    /**
     * This method returns the current comparison method used by the system.
     * 
     * @return Returns a <code>boolean</code> value that indicates if comparison
     *         is on or off.
     */
    public boolean getComparisonMethod()
    {
        return comparisonMethod;
    }

    /**
     * This method returns the current horizontal position used for placing the
     * settings dialog.
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
     * This method returns the current vertical position used for placing the
     * settings dialog.
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
     * This method sets if there is a changed settings or not on Settings
     * Dialog.
     * 
     * @param confirmation
     *            A <code>boolean</code> value that informs <code>true</code> if
     *            some value has been changed or <code>false</code> if any value
     *            has not been changed.
     */
    public void setChanged(boolean confirmation)
    {
        settingsChanged=confirmation;
    }

    /**
     * This method sets the encryption method to be unchanged status.
     */
    public void resetEncryptionChanged()
    {
        encryptionChanged=false;
    }

    /**
     * This method sets the comparison method to be unchanged status.
     */
    public void resetComparisonChanged()
    {
        comparisonMethodChanged=false;
    }

    /**
     * This method returns if some changes were detected on settings dialog.
     * 
     * @return Returns <code>true</code> if settings were changed. Returns
     *         <code>false</code> if settings remain unchanged.
     */
    public boolean isChanged()
    {
        return settingsChanged;
    }

    /**
     * This method informs whether user has already selected a directory to be
     * monitored or not.
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
     * This method informs if the encryption method were changed from settings
     * dialog screen.
     * 
     * @return Returns <code>true</code> if encryption method is changed.
     *         Returns <code>false</code> if encryption method remains
     *         unchanged.
     */
    public boolean isEncryptionChanged()
    {
        return encryptionChanged;
    }

    /**
     * This method informs if the comparison method were changed from settings
     * dialog screen.
     * 
     * @return Returns <code>true</code> if comparison method is changed.
     *         Returns <code>false</code> if comparison method remains
     *         unchanged.
     */
    public boolean isComparisonChanged()
    {
        return comparisonMethodChanged;
    }
}
