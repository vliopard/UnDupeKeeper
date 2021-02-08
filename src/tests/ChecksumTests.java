package tests;

import static org.junit.Assert.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import tools.CheckSum;

public class ChecksumTests
{
    String testFilePath = "c:\\vliopard\\workspace\\vliopard\\files\\";

    @Test
    public void _0000005_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000005.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0000020_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000020.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0000100_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000100.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0000500_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000500.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0001500_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0001500.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0005000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0005000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0010000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0010000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0025000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0025000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0040000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0040000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0150000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0150000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0300000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file0300000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _1000000_getChecksumSimple( )
    {
        Path   f1     = Paths.get(testFilePath + "file1000000.bin");
        String checks = CheckSum.getChecksumSimple(f1);
    }

    @Test
    public void _0000005_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000005.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0000020_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000020.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0000100_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000100.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0000500_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000500.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0001500_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0001500.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0005000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0005000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0010000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0010000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0025000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0025000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0040000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0040000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0150000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0150000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0300000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file0300000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _1000000_getChecksumFaster( )
    {
        Path   f1     = Paths.get(testFilePath + "file1000000.bin");
        String checks = CheckSum.getChecksumFaster(f1);
    }

    @Test
    public void _0000005_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000005.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0000020_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000020.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0000100_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000100.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0000500_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000500.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0001500_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0001500.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0005000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0005000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0010000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0010000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0025000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0025000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0040000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0040000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0150000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0150000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0300000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file0300000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _1000000_getChecksumElegant( )
    {
        Path   f1     = Paths.get(testFilePath + "file1000000.bin");
        String checks = CheckSum.getChecksumElegant(f1);
    }

    @Test
    public void _0000005_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000005.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0000020_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000020.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0000100_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000100.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0000500_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0000500.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0001500_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0001500.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0005000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0005000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0010000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0010000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0025000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0025000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0040000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0040000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0150000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0150000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _0300000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file0300000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

    @Test
    public void _1000000_getBestChecksum( )
    {
        Path   f1     = Paths.get(testFilePath + "file1000000.bin");
        String checks = CheckSum.getBestChecksum(f1);
    }

}
