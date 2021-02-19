package tests;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.SerializationUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import tools.DataBase;
import tools.FileOperations;
import tools.Logger;
import tools.Storage;
import tools.UniqueFile;

public class Test_DataBase
{
    private HashMap <Storage, String>    linkMapTable = new HashMap <Storage, String>( );
    private HashMap <String, UniqueFile> hashMapTable = new HashMap <String, UniqueFile>( );
    private static Path                  dummyfile    = FileSetup.generateDummy("./dummyfile.tst");

    @Before
    public void setUp( ) throws Exception
    {
        linkMapTable.put(new Storage("./"), "SFDA4R3FA423");
        hashMapTable.put("SFDA4R3FA423", new UniqueFile(dummyfile));
    }

    @AfterClass
    public static void tearDownAfterClass( ) throws Exception
    {
        FileOperations.deleteFile(dummyfile);
    }

    @Test
    public void dataBaseHshSave( )
    {
        DataBase.saveHashMap(hashMapTable);
    }

    @Test
    public void dataBaseLnkSave( )
    {
        DataBase.saveLinkMap(linkMapTable);
    }

    @Test
    public void dataBaseHshLoad( )
    {
        hashMapTable = DataBase.loadHashMap( );
    }

    @Test
    public void dataBaseLnkLoad( )
    {
        linkMapTable = DataBase.loadLinkMap( );
    }

    @Test
    public void SerializeDatabase( )
    {
        Storage sto = new Storage(dummyfile.toFile( ));

        HashMap <Storage, String> linkMapTable = new HashMap <Storage, String>( );
        linkMapTable.put(sto, "SFDA4R3FA423");
        byte[ ] featureTransformerBytes = SerializationUtils.serialize(linkMapTable);

        @SuppressWarnings("unchecked")
        HashMap <Storage, String> linkMapTable1 = (HashMap <Storage, String>) SerializationUtils.deserialize(featureTransformerBytes);

        Iterator <Entry <Storage, String>> lm = linkMapTable1.entrySet( ).iterator( );
        while (lm.hasNext( ))
        {
            Map.Entry <Storage, String> pair = (Map.Entry <Storage, String>) lm.next( );
            Logger.msg("[" + pair.getValue( ) + "] [" + pair.getKey( ).getString( ) + "]");
        }
    }

    @Test
    public void DeserializeDatabase( )
    {
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
    }

}
