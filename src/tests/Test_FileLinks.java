package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import deprecated.Linker;
import tools.FileOperations;
import tools.Logger;

public class Test_FileLinks
{

    @Test
    public void createOSSymbolicLink( )
    {
        Logger.msg("createOSSymbolicLink start");
        Path dummyfile = FileSetup.generateDummy("./ossymb.txt");
        Path symlink   = Paths.get("./ossymb.lnx");
        Linker.createLink(symlink, dummyfile, 0);
        assertTrue(Files.isRegularFile(dummyfile));
        assertTrue(Files.isSymbolicLink(symlink));
        FileOperations.deleteFile(symlink);
        FileOperations.deleteFile(dummyfile);
        Logger.msg("createOSSymbolicLink end");
    }

    @Test
    public void createJavaSymbolicLink( )
    {
        Logger.msg("createJavaSymbolicLink start");
        Path dummyfile = FileSetup.generateDummy("./jvsymb.txt");
        Path symlink   = Paths.get("./jvsymb.lnx");
        try
        {
            Files.createSymbolicLink(symlink, dummyfile);
            assertTrue(Files.isRegularFile(dummyfile));
            assertTrue(Files.isSymbolicLink(symlink));
            FileOperations.deleteFile(symlink);
            FileOperations.deleteFile(dummyfile);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace( );
        }
        Logger.msg("createJavaSymbolicLink end");
    }

    @Test
    public void createOSHardLink( )
    {
        Logger.msg("createOSHardLink start");
        Path dummyfile = FileSetup.generateDummy("./oshard.txt");
        Path symlink   = Paths.get("./oshard.lnx");
        Linker.createLink(symlink, dummyfile, 1);
        assertTrue(Files.isRegularFile(dummyfile));
        assertTrue(Files.isRegularFile(symlink));
        FileOperations.deleteFile(symlink);
        FileOperations.deleteFile(dummyfile);
        Logger.msg("createOSHardLink end");
    }

    @Test
    public void createJavaHardLink( )
    {
        Logger.msg("createJavaHardLink start");
        Path dummyfile = FileSetup.generateDummy("./jvhard.txt");
        Path symlink   = Paths.get("./jvhard.lnx");
        try
        {
            Files.createLink(symlink, dummyfile);
            assertTrue(Files.isRegularFile(dummyfile));
            assertTrue(Files.isRegularFile(symlink));
            FileOperations.deleteFile(symlink);
            FileOperations.deleteFile(dummyfile);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace( );
        }
        Logger.msg("createJavaHardLink end");
    }
}
