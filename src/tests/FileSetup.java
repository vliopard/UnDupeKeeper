package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import settings.Settings;
import tools.CheckSum;
import tools.FileOperations;
import tools.Logger;
import tools.Storage;
import tools.UniqueFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SerializationUtils;

public class FileSetup
{
    public static Path generateDummy( )
    {
        try
        {
            FileOutputStream fos = new FileOutputStream("./dummyfile.tmp");
            fos.write(1);
            fos.close( );
            return Paths.get("./dummyfile.tmp");
        }
        catch (IOException e)
        {
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
                e.printStackTrace( );
            }
        }
    }

    public static String getChecksum(Path fn)
    {
        try
        {
            String         basename = FilenameUtils.getBaseName(fn.toString( ));
            File           fc       = new File(fn.getParent( ) + Settings.Slash + basename + ".cks");
            FileReader     fr       = new FileReader(fc);
            BufferedReader br       = new BufferedReader(fr);
            String         line     = br.readLine( );
            br.close( );
            return line;
        }
        catch (IOException e)
        {
            e.printStackTrace( );
        }
        return null;
    }

    public static void main(String[ ] args)
    {
        Path    dummyfile = FileSetup.generateDummy( );
        Storage sto       = new Storage(dummyfile.toFile( ));

        HashMap <Storage, String> linkMapTable = new HashMap <Storage, String>( );
        linkMapTable.put(sto, "SFDA4R3FA423");
        byte[ ]                   featureTransformerBytes = SerializationUtils.serialize(linkMapTable);

        @SuppressWarnings("unchecked")
        HashMap <Storage, String> linkMapTable1           = (HashMap <Storage, String>) SerializationUtils.deserialize(featureTransformerBytes);

        Iterator <Entry <Storage, String>> lm = linkMapTable1.entrySet( ).iterator( );
        while (lm.hasNext( ))
        {
            Map.Entry <Storage, String> pair = (Map.Entry <Storage, String>) lm.next( );
            Logger.msg("[" + pair.getValue( ) + "] [" + pair.getKey( ).getString( ) + "]");
        }

        HashMap <String, UniqueFile> hashMapTable = new HashMap <String, UniqueFile>( );
        hashMapTable.put("SFDA4R3FA423", new UniqueFile(dummyfile));
        byte[ ] featureTransformerBytes1 = SerializationUtils.serialize(hashMapTable);

        @SuppressWarnings("unchecked")
        HashMap <String, UniqueFile> hashMapTable1 = (HashMap <String, UniqueFile>) SerializationUtils.deserialize(featureTransformerBytes1);

        Iterator <Entry <String, UniqueFile>> lm1 = hashMapTable1.entrySet( ).iterator( );
        while (lm1.hasNext( ))
        {
            Map.Entry <String, UniqueFile> pair = (Map.Entry <String, UniqueFile>) lm1.next( );
            Logger.msg("[" + pair.getValue( ).getString( ) + "] [" + pair.getKey( ) + "]");
        }

        dummyfile.toFile( ).delete( );
    }
}
