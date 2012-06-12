package tools;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;
import java.io.Serializable;
import settings.Settings;

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

    public void setXY(int xScreenPosition,
                      int yScreenPosition)
    {
        xScreenAxis=xScreenPosition;
        yScreenAxis=yScreenPosition;
    }

    public void setDirectory(String directory)
    {
        directoryToWatch=directory;
        directoryChanged=true;
    }

    public void setLookAndFeel(int lookNfeel)
    {
        lookAndFeel=lookNfeel;
    }

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

    public void setX(int xScreenPosition)
    {
        xScreenAxis=xScreenPosition;
    }

    public void setY(int yScreenPosition)
    {
        yScreenAxis=yScreenPosition;
    }

    public String getDirectory()
    {
        return directoryToWatch;
    }

    public int getLookAndFeel()
    {
        return lookAndFeel;
    }

    public int getEncryptionMethod()
    {
        return encryptionMethod;
    }

    public int getX()
    {
        return xScreenAxis;
    }

    public int getY()
    {
        return yScreenAxis;
    }

    public void setChanged(boolean confirmation)
    {
        settingsChanged=confirmation;
    }

    public void resetEncryptionChanged()
    {
        encryptionChanged=false;
    }

    public boolean isChanged()
    {
        return settingsChanged;
    }

    public boolean isDirectoryFirstTime()
    {
        return !directoryChanged;
    }

    public boolean isEncryptionChanged()
    {
        return encryptionChanged;
    }
}
