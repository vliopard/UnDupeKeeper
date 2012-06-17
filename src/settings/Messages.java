package settings;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import tools.DataBase;
import tools.Logger;

/**
 * Messages class is responsible for changing language.
 * 
 * @author vliopard
 */
public class Messages
{
    // TODO: TRANSLATE PT_BR MESSAGES
    private static String         BUNDLE_NAME     ="settings.en_us";                     //$NON-NLS-1$
    private static ResourceBundle RESOURCE_BUNDLE =ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Messages Constructor - Not used yet. Just an empty constructor.
     */
    private Messages()
    {
        msg("hey-------------------");
        BUNDLE_NAME="settings."+
                    DataBase.loadLanguage();
        RESOURCE_BUNDLE=ResourceBundle.getBundle(BUNDLE_NAME);
    }

    /**
     * This method sets the new language to use on system.
     * 
     * @param language
     *            A <code>String</code> containing the desired language.
     */
    public void setLanguage(String language)
    {
        BUNDLE_NAME="settings."+
                    language;
        RESOURCE_BUNDLE=ResourceBundle.getBundle(BUNDLE_NAME);
    }

    /**
     * This method gets a string message from a text file by proviting its key.
     * 
     * @param key
     *            A <code>String</code> with the key that will be changed by the
     *            string message.
     * @return Returns A <code>String</code> with the selected message.
     */
    public static String getString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch(MissingResourceException e)
        {
            return '!'+key+'!';
        }
    }

    /**
     * This method displays a message through the embedded log system.
     * 
     * @param message
     *            A <code>String</code> containing the message to display.
     */
    private static void msg(String message)
    {
        Logger.msg(message);
    }
}
