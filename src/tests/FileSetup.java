package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import settings.Settings;
import tools.CheckSum;
import tools.FileOperations;

import org.apache.commons.io.FilenameUtils;

public class FileSetup
{
    public static Path generateDummy(String dummy)
    {
        try
        {
            //FileOutputStream fos = new FileOutputStream(dummy);
            //fos.write(1);
            //fos.close( );
            Path dummyPath = Paths.get(dummy);
            Files.createFile(dummyPath);
            Files.writeString(dummyPath, "dummyTestContent");
            return dummyPath;
        }
        catch (IOException e)
        {
            // TODO: INDEX ERROR MESSAGE
            e.printStackTrace( );
        }
        return null;
    }

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

                FileWriter myWriter = new FileWriter(check);
                myWriter.write(CheckSum.getChecksumElegant(f1));
                myWriter.close( );
            }
            catch (IOException e)
            {
                // TODO: INDEX ERROR MESSAGE
                e.printStackTrace( );
            }
        }
    }

    public static String getChecksum(Path fn)
    {
        try
        {
            BufferedReader br   = new BufferedReader(new FileReader(new File(fn.getParent( ) + Settings.Slash
                    + FilenameUtils.getBaseName(fn.toString( )) + ".cks")));
            String         line = br.readLine( );
            br.close( );
            return line;
        }
        catch (IOException e)
        {
            // TODO: INDEX ERROR MESSAGE
            e.printStackTrace( );
        }
        return null;
    }
}
