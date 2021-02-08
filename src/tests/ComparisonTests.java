package tests;

import static org.junit.Assert.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import main.Comparison;

public class ComparisonTests
{
    String testFilePath = "c:\\vliopard\\workspace\\vliopard\\files\\";

    @Test
    public void _0000005_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0000020_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0000100_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0000500_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0001500_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0005000_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0010000_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0025000_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0040000_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0150000_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

    @Test
    public void _0300000_isArrayEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.isArrayEqual(f1, f2));
    }

//    @Test
//    public void _1000000_isArrayEqual( )
//    {
//        Path f1 = Paths.get(testFilePath + "file1000000.bin");
//        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");
//
//        assertTrue(Comparison.isArrayEqual(f1, f2));
//    }

    @Test
    public void _0000005_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0000020_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0000100_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0000500_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0001500_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0005000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0010000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0025000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0040000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0150000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0300000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _1000000_isBufferedEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.isBufferedEqual(f1, f2));
    }

    @Test
    public void _0000005_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0000020_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0000100_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0000500_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0001500_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0005000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0010000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0025000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0040000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0150000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0300000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _1000000_isFileEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.isFileEqual(f1, f2));
    }

    @Test
    public void _0000005_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0000020_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0000100_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0000500_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0001500_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0005000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0010000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0025000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0040000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0150000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0300000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _1000000_isBinaryIdentical( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.isBinaryIdentical(f1, f2));
    }

    @Test
    public void _0000005_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0000020_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0000100_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0000500_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0001500_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0005000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0010000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0025000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0040000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0150000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0300000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _1000000_runSysComp_0( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 0));
    }

    @Test
    public void _0000005_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0000020_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0000100_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0000500_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0001500_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0005000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0010000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0025000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0040000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0150000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0300000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _1000000_runSysComp_1( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.runSystemCompare(f1, f2, 1));
    }

    @Test
    public void _0000005_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0000020_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0000100_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0000500_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0001500_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0005000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0010000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0025000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0040000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0150000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0300000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _1000000_isFileBinaryEqual( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.isFileBinaryEqual(f1, f2));
    }

    @Test
    public void _0000005_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0000005.bin");
        Path f2 = Paths.get(testFilePath + "file0000005_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0000020_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0000020.bin");
        Path f2 = Paths.get(testFilePath + "file0000020_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0000100_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0000100.bin");
        Path f2 = Paths.get(testFilePath + "file0000100_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0000500_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0000500.bin");
        Path f2 = Paths.get(testFilePath + "file0000500_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0001500_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0001500.bin");
        Path f2 = Paths.get(testFilePath + "file0001500_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0005000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0005000.bin");
        Path f2 = Paths.get(testFilePath + "file0005000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0010000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0010000.bin");
        Path f2 = Paths.get(testFilePath + "file0010000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0025000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0025000.bin");
        Path f2 = Paths.get(testFilePath + "file0025000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0040000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0040000.bin");
        Path f2 = Paths.get(testFilePath + "file0040000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0150000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0150000.bin");
        Path f2 = Paths.get(testFilePath + "file0150000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _0300000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file0300000.bin");
        Path f2 = Paths.get(testFilePath + "file0300000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }

    @Test
    public void _1000000_compareBySize( )
    {
        Path f1 = Paths.get(testFilePath + "file1000000.bin");
        Path f2 = Paths.get(testFilePath + "file1000000_dupe.bin");

        assertTrue(Comparison.compareBySize(f1, f2));
    }
}
