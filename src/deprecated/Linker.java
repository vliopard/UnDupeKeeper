package deprecated;

import java.nio.file.Path;
import settings.Settings;
import tools.Utils;

public class Linker
{
    public static boolean createLink(Path current, Path target, int option)
    {
        String command = "";
        if (Settings.os.startsWith("windows"))
        {
            switch (option)
            {
                case 0:
                    command = "cmd /c mklink " + current + " " + target;
                break;

                case 1:
                    command = "cmd /c mklink /h " + current + " " + target;
            }
        }
        else
        {
            switch (option)
            {
                case 0:
                    command = "ln -s " + target + " " + current;
                break;

                case 1:
                    command = "ln " + target + " " + current;
            }
        }
        return Utils.runSystemCommand(command, 0);
    }
}
