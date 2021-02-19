package tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import settings.Settings;
import settings.Strings;

// TODO: Files.copy(file, file, null)
// TODO: Files.createLink(file, file)
// TODO: Files.createSymbolicLink(file, file, null)
// TODO: Files.deleteIfExists(file)
// TODO: Files.isRegularFile(file, null)
// TODO: Files.isDirectory(file, null)
// TODO: Files.isSameFile(file, file)
// TODO: Files.move(file, file, null)

public class FileOperations
{
    public static boolean exist(String path)
    {
        return exist(file(path));
    }

    public static boolean exist(File path)
    {
        return path.exists( );
    }

    public static boolean exist(Path path)
    {
        return exist(path.toFile( ));
    }

    public static boolean exist(Storage path)
    {
        return exist(path.getFile( ));
    }

    public static boolean isFile(String path)
    {
        return isFile(file(path));
    }

    public static boolean isFile(File path)
    {
        if (null == path)
        {
            return false;
        }
        return (exist(path)) && (path.isFile( ));
    }

    public static boolean isFile(Path path)
    {
        return isFile(path.toFile( ));
    }

    public static boolean isFile(Storage path)
    {
        // TODO: CHECK Files.isRegularFile();
        return isFile(path.getFile( ));
    }

    public static boolean isLink(String path)
    {
        return isLink(path(path));
    }

    public static boolean isLink(File path)
    {
        return isLink(path(path));
    }

    public static boolean isLink(Path path)
    {
        if (null == path)
        {
            return false;
        }
        return (path.toFile( ).exists( ) && Files.isSymbolicLink(path));
    }

    public static boolean isLink(Storage path)
    {
        return isLink(path.getPath( ));
    }

    public static boolean deleteFile(String filename)
    {
        return deleteFile(path(filename));
    }

    public static boolean deleteFile(File filename)
    {
        // if (filename.delete( ))
        // {
        //     Logger.msg("File deleted successfully");
        //     return true;
        // }
        // Logger.msg("Failed to delete the file");
        // return false;
        return deleteFile(filename.toPath( ));
    }

    public static boolean deleteFile(Path filename)
    {
        try
        {
            // if (filename.toFile( ).delete( ))
            // Files.delete(filename);
            Files.deleteIfExists(filename);
            log(" File deleted successfully");
            return true;
        }
        catch (IOException e)
        {
            // TODO: REPLACE GENERIC EXCEPTION
            Logger.err("MSG_055: " + Strings.generic + e);
        }
        // TODO: REPLACE GENERIC EXCEPTION
        Logger.err("Failed to delete the file");
        return false;
    }

    public static boolean deleteFile(Storage filename)
    {
        return deleteFile(filename.getPath( ));
    }

    //////////////////////////////////
    // TODO: ////REVIEW CODE BELOW////
    //////////////////////////////////

    public static String getFilename(String file)
    {
        return getFilename(path(file));
    }

    public static String getFilename(File file)
    {
        return getFilename(file.toPath( ));
    }

    public static String getFilename(Path file)
    {
        return file.getFileName( ).toString( );
    }

    public static String getDirectory(String path)
    {
        return path.substring(0, path.lastIndexOf(Settings.Slash) + 1);
    }

    public static String getFileName(String path)
    {
        if ((path.lastIndexOf(Settings.Dot) > 0) && (path.lastIndexOf(Settings.Slash) > 0)
                && (path.lastIndexOf(Settings.Dot) > path.lastIndexOf(Settings.Slash)))
        {
            return path.substring(path.lastIndexOf(Settings.Slash) + 1, path.lastIndexOf(Settings.Dot));
        }
        if (path.lastIndexOf(Settings.Slash) > 0)
        {
            return path.substring(path.lastIndexOf(Settings.Slash) + 1);
        }
        Logger.err("FATAL: " + path);
        return Settings.Empty;
    }

    public static String getFileExtension(String path)
    {
        if ((path.lastIndexOf(Settings.Dot) > 0) && (path.lastIndexOf(Settings.Dot) > path.lastIndexOf(Settings.Slash)))
        {
            return path.substring(path.lastIndexOf(Settings.Dot));
        }
        return Settings.Empty;
    }

    public static String getFileNameAndExtension(String file)
    {
        return getFileName(file) + getFileExtension(file);
    }

    public static File file(String file)
    {
        return new File(file);
    }

    public static File file(Path file)
    {
        if (null == file)
        {
            return null;
        }
        return file.toFile( );
    }

    public static Path path(String path)
    {
        return Paths.get(path);
    }

    public static Path path(File path)
    {
        return path.toPath( );
    }

    public static boolean isDirectory(String directory)
    {
        return isDirectory(file(directory));
    }

    public static boolean isDirectory(File directory)
    {
        if (null == directory)
        {
            return false;
        }
        return (directory.exists( )) && (directory.isDirectory( ));
    }

    public static boolean isDirectory(Path directory)
    {
        return isDirectory(file(directory));
    }

    public static boolean isEmpty(String path)
    {
        return isEmpty(path(path));
    }

    public static boolean isEmpty(File path)
    {
        if (isFile(path))
        {
            return (path.length( ) == 0);
        }
        if (isDirectory(path))
        {
            return (path.list( ).length == 0);
        }
        return true;
    }

    public static boolean isEmpty(Path path)
    {
        return isEmpty(path.toFile( ));
    }

    public static void createDirectory(String directory)
    {
        createDirectory(Paths.get(directory));
    }

    public static void createDirectory(File directory)
    {
        createDirectory(Paths.get(directory.toString( )));
    }

    public static void createDirectory(Path directory)
    {
        Path dir = directory.getParent( );
        try
        {
            Logger.msg(dir.toString( ));
            if ( ! FileOperations.exist(dir) && ! FileOperations.isDirectory(dir))
            {
                Logger.msg("CREATING " + dir.toString( ));
                Files.createDirectories(dir);
            }
        }
        catch (IOException e)
        {
            // TODO: REPLACE GENERIC EXCEPTION
            Logger.err("MSG_056: " + Strings.generic + e);
        }
    }

    public static void createDirectory(Storage directory)
    {
        createDirectory(directory.getPath( ));
    }

    /**
     * Delete all files and directories in directory but do not delete the directory itself.
     *
     * @param directory
     *                      - string that specifies directory to delete
     *
     * @return boolean - success flag
     */
    public static boolean deleteDirectoryContent(String directory)
    {
        return ((directory != null) && (directory.length( ) > 0)) ? deleteDirectoryContent(file(directory)) : false;
    }

    /**
     * Delete all files and directories in directory but do not delete the directory itself.
     *
     * @param directory
     *                      - directory to delete
     *
     * @return boolean - success flag
     */
    public static boolean deleteDirectoryContent(File directory)
    {
        boolean returnValue = false;
        if ((directory != null) && (directory.isDirectory( )))
        {
            File[ ] files = directory.listFiles( );
            if (files != null)
            {
                returnValue = true;
                for (int index = 0; index < files.length; index++)
                {
                    if (files[index].isDirectory( ))
                    {
                        // TODO: RESEARCH: Performance - Implement this as a queue where you add to the end and take from the beginning, it will be more efficient than the recursion
                        if (deleteDirectoryContent(files[index]))
                        {
                            returnValue = returnValue && files[index].delete( );
                        }
                        else
                        {
                            returnValue = false;
                        }
                    }
                    else
                    {
                        returnValue = returnValue && files[index].delete( );
                    }
                }
            }
        }
        return returnValue;
    }

    public static boolean deleteDirectoryContent(Path directory)
    {
        return deleteDirectoryContent(file(directory));
    }

    /**
     * Deletes all files and sub directories under the specified directory including the specified directory
     *
     * @param directory
     *                      - string that specifies directory to be deleted
     *
     * @return boolean - true if directory was successfully deleted
     */
    public static boolean deleteDirectory(String directory)
    {
        return ((directory != null) && (directory.length( ) > 0)) ? deleteDirectory(file(directory)) : false;
    }

    /**
     * Deletes all files and sub directories under the specified directory including the specified directory
     *
     * @param directory
     *                      - directory to be deleted
     *
     * @return boolean - true if directory was successfully deleted
     */
    public static boolean deleteDirectory(File directory)
    {
        boolean returnValue = false;
        if ((directory != null) && (directory.exists( )))
        {
            returnValue = deleteDirectoryContent(directory);
            if (returnValue)
            {
                returnValue = returnValue && directory.delete( );
            }
        }
        return returnValue;
    }

    public static boolean deleteDirectory(Path directory)
    {
        return deleteDirectory(file(directory));
    }

    /**
     * This method displays a log message through the embedded log system.
     *
     * @param logMessage
     *                       A <code>String</code> containing the log message to display.
     */
    public static boolean createLink(Path current, Path target, int option)
    {
        String command = "";
        if (Settings.os.startsWith("windows"))
        {
            switch (option)
            {
                case 0:
                    command = "cmd /c mklink " + current + " " + target;
                break;

                case 1:
                    command = "cmd /c mklink /h " + current + " " + target;
            }
        }
        else
        {
            switch (option)
            {
                case 0:
                    command = "ln -s " + target + " " + current;
                break;

                case 1:
                    command = "ln " + target + " " + current;
            }
        }
        return Utils.runSystemCommand(command, 0);
    }

    private static void log(String logMessage)
    {
        Logger.log(Thread.currentThread( ), logMessage, Logger.OPERATIONS);
    }
}
