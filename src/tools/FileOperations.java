package tools;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import settings.Settings;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class FileOperations
{
    /**
     * Delete all files and directories in directory but do not delete the
     * directory itself.
     * 
     * @param strDir
     *            - string that specifies directory to delete
     * @return boolean - success flag
     */
    public static boolean deleteDirectoryContent(String strDir)
    {
        return ((strDir!=null)&&(strDir.length()>0))?deleteDirectoryContent(new File(strDir)):false;
    }

    /**
     * Delete all files and directories in directory but do not delete the
     * directory itself.
     * 
     * @param fDir
     *            - directory to delete
     * @return boolean - success flag
     */
    public static boolean deleteDirectoryContent(File fDir)
    {
        boolean bRetval=false;
        if((fDir!=null) && (fDir.isDirectory()))
        {
            File[] files=fDir.listFiles();
            if(files!=null)
            {
                bRetval=true;
                boolean dirDeleted;
                for(int index=0; index<files.length; index++)
                {
                    if(files[index].isDirectory())
                    {
                        // TODO: RESEARCH: Performance - Implement this as a
                        // queue where you add to the end and take from the
                        // beginning, it will be more efficient than the
                        // recursion
                        dirDeleted=deleteDirectoryContent(files[index]);
                        if(dirDeleted)
                        {
                            bRetval=bRetval&&files[index].delete();
                        }
                        else
                        {
                            bRetval=false;
                        }
                    }
                    else
                    {
                        bRetval=bRetval&&files[index].delete();
                    }
                }
            }
        }
        return bRetval;
    }

    /**
     * Deletes all files and sub directories under the specified directory
     * including the specified directory
     * 
     * @param strDir
     *            - string that specifies directory to be deleted
     * @return boolean - true if directory was successfully deleted
     */
    public static boolean deleteDir(String strDir)
    {
        return ((strDir!=null)&&(strDir.length()>0))?deleteDir(new File(strDir)):false;
    }

    /**
     * Deletes all files and sub directories under the specified directory
     * including the specified directory
     * 
     * @param fDir
     *            - directory to be deleted
     * @return boolean - true if directory was successfully deleted
     */
    public static boolean deleteDir(File fDir)
    {
        boolean bRetval=false;
        if((fDir!=null) && (fDir.exists()))
        {
            bRetval=deleteDirectoryContent(fDir);
            if(bRetval)
            {
                bRetval=bRetval && fDir.delete();
            }
        }
        return bRetval;
    }

    public static File file(String file)
    {
        return new File(file);
    }

    public static File file(Path file)
    {
        return file.toFile();
    }

    public static boolean exist(Path file)
    {
        return file(file).exists();
    }
    
    public static boolean isFile(String file)
    {
        return isFile(Paths.get(file));
    }

    public static boolean isFile(Path file)
    {
        if(null==file)
        {
            return false;
        }
        return (exist(file)) && (file(file).isFile());
    }

    public static Path path(String file)
    {
        return Paths.get(file);
    }

    public static boolean isLink(Path file)
    {
        if(null == file)
        {
            return false;
        }
        return(file.toFile().exists() && Files.isSymbolicLink(file));
    }

    public static boolean deleteFile(String filename)
    {
        return deleteFile(Paths.get(filename));
    }
    
    public static boolean deleteFile(Path filename)
    {
        if(filename.toFile().delete())
        {
            Logger.msg("File deleted successfully");
            return true;
        }
        Logger.msg("Failed to delete the file");
        return false;
    }

    /**
     * This method checks if a path is really a directory.
     * 
     * @param dirName
     *            A <code>Path</code> to a file location.
     * @return Return <code>false</code> when dirName is: <code>null</code>, not
     *         exists and is a Directory. Return <code>true</code> if it is an
     *         actual file.
     */
    public static boolean isDir(String dirName)
    {
        return isDir(Paths.get(dirName));
    }
    
    public static boolean isDir(Path dirName)
    {
        if(null==dirName)
        {
            return false;
        }
        return (exist(dirName)) && (file(dirName).isDirectory());
    }

    public static boolean isEmpty(String dirName)
    {
        if(isFile(dirName))
        {
            return(file(dirName).length()==0);
        }
        if(isDir(dirName))
        {
            return(file(dirName).list().length==0);
        }
        return true;
    }

    public static String getFilePath(String file)
    {
        return file.substring(0, file.lastIndexOf(Settings.Slash)+1);
    }

    public static String getFileName(String file)
    {
        if((file.lastIndexOf(Settings.Dot)>0)&&
           (file.lastIndexOf(Settings.Slash)>0)&&
           (file.lastIndexOf(Settings.Dot)>file.lastIndexOf(Settings.Slash)))
        {
            return file.substring(file.lastIndexOf(Settings.Slash) + 1, file.lastIndexOf(Settings.Dot));
        }
        if(file.lastIndexOf(Settings.Slash)>0)
        {
            return file.substring(file.lastIndexOf(Settings.Slash) + 1);
        }
        Logger.err("FATAL: " + file);
        return Settings.Empty;
    }

    public static String getFileExtension(String file)
    {
        if((file.lastIndexOf(Settings.Dot)>0) && (file.lastIndexOf(Settings.Dot)>file.lastIndexOf(Settings.Slash)))
        {
            return file.substring(file.lastIndexOf(Settings.Dot));
        }
        return Settings.Empty;
    }

    public static String getFile(String file)
    {
        return getFileName(file) + getFileExtension(file);
    }
    
    public static String getFile(Path file)
    {
        return file.getFileName().toString();
    }
}
