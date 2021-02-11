package tests;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import tools.DataBase;
import tools.FileOperations;
import tools.Storage;
import tools.UniqueFile;

public class DataBaseTests
{
    private HashMap <Storage, String>    linkMapTable = new HashMap <Storage, String>( );
    private HashMap <String, UniqueFile> hashMapTable = new HashMap <String, UniqueFile>( );
    private static Path                  dummyfile    = FileSetup.generateDummy( );

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
    public void DataBaseSave( )
    {
        DataBase.saveMap(hashMapTable);
    }

    @Test
    public void DataBase1Save( )
    {
        DataBase.saveMap1(linkMapTable);
    }

    @Test
    public void DataBaseLoad( )
    {
        hashMapTable = DataBase.loadMap( );
    }

    @Test
    public void DataBase1Load( )
    {
        linkMapTable = DataBase.loadMap1( );
    }
}
