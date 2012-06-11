package tools;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import javax.swing.ImageIcon;
import main.UnDupeKeeper;
import settings.Settings;
import settings.Strings;

public class TrayImage
{
    public static Image setSystemTrayImage(int iconIndex)
    {
        Image trayIconImage=createImage(Settings.iconList[iconIndex],
                                        Strings.ukSystemTrayIconTooltip);
        if(trayIconImage==null)
        {
            Toolkit toolKit=Toolkit.getDefaultToolkit();
            byte[] byteArray=new byte[]
            {
                0
            };
            trayIconImage=toolKit.createImage(byteArray,
                                              Settings.IconWidth,
                                              Settings.IconHeight);
        }
        return trayIconImage;
    }

    public static TrayIcon setSystemTrayIcon(int iconIndex)
    {
        return new TrayIcon(setSystemTrayImage(iconIndex));
    }

    protected static Image createImage(String resourceImagePath,
                                       String toolTipDescription)
    {
        URL imageURL=UnDupeKeeper.class.getResource(resourceImagePath);
        if(imageURL==null)
        {
            err(Strings.ukResourceNotFound+
                resourceImagePath);
            return null;
        }
        else
        {
            return (new ImageIcon(imageURL,
                                  toolTipDescription)).getImage();
        }
    }

    private static void err(String errorMessage)
    {
        Logger.err(errorMessage);
    }
}
