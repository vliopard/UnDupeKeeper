package tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import settings.Settings;

// TODO: Files.copy(file, file, null)
// TODO: Files.createLink(file, file)
// TODO: Files.createSymbolicLink(file, file, null)
// TODO: Files.deleteIfExists(file)
// TODO: Files.isRegularFile(file, null)
// TODO: Files.isDirectory(file, null)
// TODO: Files.isSameFile(file, file)
// TODO: Files.move(file, file, null)

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class FileOperations
{
    public static boolean exist(String file)
    {
        return exist(new Storage(file));
    }

    public static boolean exist(File file)
    {
        return exist(new Storage(file));
    }

    public static boolean exist(Path file)
    {
        return exist(new Storage(file));
    }

    public static boolean exist(Storage file)
    {
        return file.getFile( ).exists( );
    }

    public static boolean isFile(String file)
    {
        return isFile(new Storage(file));
    }

    public static boolean isFile(File file)
    {
        return isFile(new Storage(file));
    }

    public static boolean isFile(Path file)
    {
        return isFile(new Storage(file));
    }

    public static boolean isFile(Storage file)
    {
        if (null == file.getString( ))
        {
            return false;
        }
        // TODO: CHECK Files.isRegularFile();
        return (exist(file)) && (file.getFile( ).isFile( ));
    }

    public static boolean isLink(String file)
    {
        return isLink(new Storage(file));
    }

    public static boolean isLink(File file)
    {
        return isLink(new Storage(file));
    }

    public static boolean isLink(Path file)
    {
        return isLink(new Storage(file));
    }

    public static boolean isLink(Storage file)
    {
        if (null == file.getString( ))
        {
            return false;
        }
        return (file.getFile( ).exists( ) && Files.isSymbolicLink(file.getPath( )));
    }

    public static boolean deleteFile(String filename)
    {
        return deleteFile(Paths.get(filename));
    }

    public static boolean deleteFile(File filename)
    {
        return deleteFile(Paths.get(filename.toString( )));
    }

    public static boolean deleteFile(Path filename)
    {
        try
        {
            Files.deleteIfExists(filename);
            Logger.msg("File deleted successfully");
            //Files.delete(filename);
            // if (filename.toFile( ).delete( ))
            return true;
        }
        catch (IOException e)
        {
            // TODO: index error message
            e.printStackTrace( );
        }
        Logger.msg("Failed to delete the file");
        return false;
    }

    public static boolean deleteFile(Storage filename)
    {
        return deleteFile(filename.getPath( ));
    }

    //////////////////////////////////
    /////////REVIEW CODE BELOW////////
    //////////////////////////////////

    public static String getFilePath(String file)
    {
        return file.substring(0, file.lastIndexOf(Settings.Slash) + 1);
    }

    public static String getFileName(String file)
    {
        if ((file.lastIndexOf(Settings.Dot) > 0) &&
                (file.lastIndexOf(Settings.Slash) > 0) &&
                (file.lastIndexOf(Settings.Dot) > file.lastIndexOf(Settings.Slash)))
        {
            return file.substring(file.lastIndexOf(Settings.Slash) + 1, file.lastIndexOf(Settings.Dot));
        }
        if (file.lastIndexOf(Settings.Slash) > 0)
        {
            return file.substring(file.lastIndexOf(Settings.Slash) + 1);
        }
        Logger.err("FATAL: " + file);
        return Settings.Empty;
    }

    public static String getFileExtension(String file)
    {
        if ((file.lastIndexOf(Settings.Dot) > 0) && (file.lastIndexOf(Settings.Dot) > file.lastIndexOf(Settings.Slash)))
        {
            return file.substring(file.lastIndexOf(Settings.Dot));
        }
        return Settings.Empty;
    }

    public static File file(String file)
    {
        return new File(file);
    }

    public static File file(Path file)
    {
        return file.toFile( );
    }

    public static boolean isDir(String dirName)
    {
        return isDir(Paths.get(dirName));
    }

    public static boolean isDir(File dirName)
    {
        return isDir(Paths.get(dirName.toString( )));
    }

    public static boolean isDir(Path dirName)
    {
        if (null == dirName)
        {
            return false;
        }
        return (dirName.toFile( ).exists( )) && (dirName.toFile( ).isDirectory( ));
    }

    public static boolean isEmpty(String dirName)
    {
        if (isFile(dirName))
        {
            return (file(dirName).length( ) == 0);
        }
        if (isDir(dirName))
        {
            return (file(dirName).list( ).length == 0);
        }
        return true;
    }
}
