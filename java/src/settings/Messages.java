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
    private static String         BUNDLE_NAME     = Settings.LanguagePackage + "en_us";   //$NON-NLS-1$
    private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Messages Constructor - Not used yet. Just an empty constructor.
     */
    private Messages( )
    {
    }

    /**
     * This method gets a string message from a text file by providing its key.
     * 
     * @param key
     *                A <code>String</code> with the key that will be changed by the string message.
     * 
     * @return Returns A <code>String</code> with the selected message.
     */
    public static String getString(String key)
    {
        try
        {
            BUNDLE_NAME = Settings.LanguagePackage + DataBase.loadLanguage( );
            RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            Logger.err("MSG_038: " + Strings.resourseError + e);
            return '!' + key + '!';
        }
    }
}
