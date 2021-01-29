package tools;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.URL;
import javax.swing.ImageIcon;
import main.UnDupeKeeper;
import settings.Settings;
import settings.Strings;

/**
 * TrayImage class is responsible for creating and set the System Tray Icon image.
 * 
 * @author vliopard
 */
public class TrayImage
{
    /**
     * This is a method for setting the System Tray Image from an icon list from the system resources.
     * 
     * @param iconIndex
     *                      An <code>int</code> index that represents an item from <code>Settings.iconList[]</code>
     *                      array.
     * 
     * @return Returns an <code>Image</code> object from internal icon list or a new empty <code>Image</code> object.
     */
    public static Image setSystemTrayImage(int iconIndex)
    {
        Image trayIconImage = createImage(Settings.iconList[iconIndex], Strings.ukSystemTrayIconTooltip);
        if (trayIconImage == null)
        {
            trayIconImage = createNewImage( );
        }
        return trayIconImage;
    }

    /**
     * This is a method for creating a new empty image.
     * 
     * @return Returns an empty <code>Image</code> object.
     */
    public static Image createNewImage( )
    {
        Toolkit toolKit   = Toolkit.getDefaultToolkit( );
        byte[ ] byteArray = new byte[ ] {0
        };
        return toolKit.createImage(byteArray, Settings.IconWidth, Settings.IconHeight);
    }

    /**
     * This is a method for setting the System Tray Image from an icon list from the system resources.
     * 
     * @param iconIndex
     *                      An <code>int</code> index that represents an item from <code>Settings.iconList[]</code>
     *                      array.
     * 
     * @return Returns a <code>TrayIcon</code> object from an icon index;
     */
    public static TrayIcon setSystemTrayIcon(int iconIndex)
    {
        return new TrayIcon(setSystemTrayImage(iconIndex));
    }

    /**
     * This method creates an <code>Image</code> based on an internal resource picture file.
     * 
     * @param resourceImagePath
     *                               An <code>String</code> with the internal path to the resource file.
     * @param toolTipDescription
     *                               An <code>String</code> containing a text message to display in the tool tip.
     * 
     * @return Returns an <code>Image</code> from the internal resource file. Returns <code>null</code> if resource is
     *         not found.
     */
    protected static Image createImage(String resourceImagePath, String toolTipDescription)
    {
        URL imageURL = UnDupeKeeper.class.getResource(resourceImagePath);
        if (null == imageURL)
        {
            Logger.err(Strings.ukResourceNotFound + resourceImagePath);
            return null;
        }
        else
        {
            return (new ImageIcon(imageURL, toolTipDescription)).getImage( );
        }
    }
}
