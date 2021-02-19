package tests;

import static org.junit.Assert.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import settings.Settings;
import tools.CheckSum;

public class Test_Checksum
{
    String testFilePath = "";

    @BeforeClass
    public static void setUpBeforeClass( ) throws Exception
    {
        String testFilePath = "";
        if (Settings.os.indexOf("win") >= 0)
        {
            testFilePath = "c:\\vliopard\\workspace\\vliopard\\files\\";
        }
        else
        {
            testFilePath = "/home/vliopard/tests/";
        }
        FileSetup.generateFile(testFilePath + "file0000005", 5000);
        FileSetup.generateFile(testFilePath + "file0000050", 50000);
        FileSetup.generateFile(testFilePath + "file0000100", 100000);
        FileSetup.generateFile(testFilePath + "file0000500", 500000);
        FileSetup.generateFile(testFilePath + "file0001500", 1500000);
        FileSetup.generateFile(testFilePath + "file0005000", 5000000);
        FileSetup.generateFile(testFilePath + "file0010000", 10000000);
        FileSetup.generateFile(testFilePath + "file0050000", 50000000);
        FileSetup.generateFile(testFilePath + "file0150000", 150000000);
        FileSetup.generateFile(testFilePath + "file0300000", 300000000);
        FileSetup.generateFile(testFilePath + "file1000000", 1000000000);
    }

    @Before
    public void setUp( ) throws Exception
    {
        if (Settings.os.indexOf("win") >= 0)
        {
            testFilePath = "c:\\vliopard\\workspace\\vliopard\\files\\";
        }
        else
        {
            testFilePath = "/home/vliopard/tests/";
        }
    }

    @Test
    public void _0000005_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000050_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0000050.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000100_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000500_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));

    }

    @Test
    public void _0001500_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0005000_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0010000_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0050000_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0050000.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0150000_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0300000_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _1000000_getChecksumSimple( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        assertEquals(CheckSum.getChecksumSimple(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000005_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000050_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0000050.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000100_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000500_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0001500_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0005000_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0010000_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0050000_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0050000.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0150000_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0300000_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _1000000_getChecksumFaster( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        assertEquals(CheckSum.getChecksumFaster(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000005_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000050_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0000050.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000100_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000500_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0001500_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0005000_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0010000_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0050000_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0050000.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0150000_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0300000_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _1000000_getChecksumElegant( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        assertEquals(CheckSum.getChecksumElegant(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000005_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000050_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0000050.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000100_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0000500_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0001500_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0005000_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0010000_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0050000_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0050000.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0150000_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _0300000_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

    @Test
    public void _1000000_getBestChecksum( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        assertEquals(CheckSum.getBestChecksum(f1), FileSetup.getChecksum(f1));
    }

}
