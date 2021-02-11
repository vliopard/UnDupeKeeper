package deprecated;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Scanner;

import settings.Settings;
import tools.Logger;

public class Linker
{
    public static boolean createLink(Path current, Path target)
    {
        String command = "";
        if (Settings.os.startsWith("windows"))
        {
            command = "cmd /c mklink " + current + " " + target;
        }
        else
        {
            command = "ln -s " + target + " " + current;
        }
        Process process;
        try
        {
            Logger.msg(command);
            process = Runtime.getRuntime( ).exec(command);
            int exitValue = process.waitFor( );
            if (exitValue != 0)
            {
                Logger.msg("Abnormal process termination 1");
                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream( )));
            String         line;
            while ((line = reader.readLine( )) != null)
            {
                Logger.msg(line);
            }
            reader.close( );

            Scanner scanner = new Scanner(process.getInputStream( ));
            scanner.useDelimiter("\r\n");
            while (scanner.hasNext( ))
            {
                Logger.msg(scanner.next( ));
            }
            scanner.close( );

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream( )));
            while ((line = errorReader.readLine( )) != null)
            {
                Logger.msg(line);
            }
            errorReader.close( );

            process.destroy( );
            if (process.exitValue( ) != 0)
            {
                Logger.msg("Abnormal process termination 2");
            }

        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace( );
            Logger.msg("Exception: " + e);
        }
        return true;
    }
}
