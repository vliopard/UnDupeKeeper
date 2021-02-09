package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import settings.Settings;
import tools.CheckSum;
import tools.FileOperations;
import org.apache.commons.io.FilenameUtils;

import main.Comparison;

public class FileSetup
{
    public static void generateFile(String filename, long wantedSize)
    {
        String name  = filename + ".bin";
        String dupe  = filename + "_dupe.bin";
        String check = filename + ".cks";

        Path f1 = Paths.get(name);
        if ( ! FileOperations.exist(f1))
        {
            Random random = new Random( );
            File   file   = new File(f1.toString( ));
            long   start  = System.currentTimeMillis( );
            try
            {
                int              number  = 0;
                byte[ ]          bnumber = new byte[1000];
                FileOutputStream fos     = new FileOutputStream(file);
                while (true)
                {
                    for (int i = 0; i < 1000; i++)
                    {
                        number = random.nextInt(1000) + 1;
                        bnumber[i] = (byte) number;
                    }
                    fos.write(bnumber);

                    if (file.length( ) >= wantedSize)
                    {
                        fos.close( );
                        break;
                    }
                }
                long time = System.currentTimeMillis( ) - start;
                java.nio.file.Files.copy(f1, Paths.get(dupe));
                System.out.printf("Took %.1f seconds to create a file of %d bytes\n", time / 1e3, file.length( ));
                FileOutputStream focs = new FileOutputStream(new File(check));
                focs.write(CheckSum.getChecksumElegant(f1).getBytes( ));
                focs.close( );
            }
            catch (IOException e)
            {
                e.printStackTrace( );
            }
        }
    }

    public static String getChecksum(Path fn)
    {
        try
        {
            String          basename = FilenameUtils.getBaseName(fn.toString( ));
            File            fc       = new File(fn.getParent( ) + Settings.Slash + basename + ".cks");
            FileInputStream focs     = new FileInputStream(fc);
            String          retval   = "";
            int             b;
            while ((b = focs.read( )) != -1)
            {
                retval = retval + (char) b;
            }
            return retval;
        }
        catch (IOException e)
        {
            e.printStackTrace( );
        }
        return null;
    }
}
