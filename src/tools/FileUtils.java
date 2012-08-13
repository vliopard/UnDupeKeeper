package tools;
import java.io.File;
import java.nio.file.Path;
import settings.Settings;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class FileUtils
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
        return ((strDir!=null)&&(strDir.length()>0))?deleteDirectoryContent(new File(strDir))
                                                    :false;
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
        if((fDir!=null)&&
           (fDir.isDirectory()))
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
                        // queue where
                        // you add to the end and take from the beginning, it
                        // will be more efficient than the recursion
                        dirDeleted=deleteDirectoryContent(files[index]);
                        if(dirDeleted)
                        {
                            bRetval=bRetval&&
                                    files[index].delete();
                        }
                        else
                        {
                            bRetval=false;
                        }
                    }
                    else
                    {
                        bRetval=bRetval&&
                                files[index].delete();
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
        return ((strDir!=null)&&(strDir.length()>0))?deleteDir(new File(strDir))
                                                    :false;
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
        if((fDir!=null)&&
           (fDir.exists()))
        {
            bRetval=deleteDirectoryContent(fDir);
            if(bRetval)
            {
                bRetval=bRetval&&
                        fDir.delete();
            }
        }
        return bRetval;
    }

    public static File file(String file)
    {
        return new File(file);
    }

    public static boolean exist(String file)
    {
        return file(file).exists();
    }

    public static boolean isFile(String file)
    {
        if(null==file)
        {
            return false;
        }
        return (exist(file))&&
               (file(file).isFile());
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
    public static boolean isDir(Path dirName)
    {
        if(null==dirName)
        {
            return false;
        }
        return isDir(dirName.toString());
    }

    public static boolean isDir(String dirName)
    {
        if(null==dirName)
        {
            return false;
        }
        return (exist(dirName))&&
               (file(dirName).isDirectory());
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
        return file.substring(0,
                              file.lastIndexOf("\\")+1);
    }

    public static String getFileName(String file)
    {
        if(file.lastIndexOf(".")<0)
        {
            return file.substring(file.lastIndexOf("\\")+1);
        }
        return file.substring(file.lastIndexOf("\\")+1,
                              file.lastIndexOf("."));
    }

    public static String getFileExtension(String file)
    {
        if(file.lastIndexOf(".")<0)
        {
            return Settings.Empty;
        }
        return file.substring(file.lastIndexOf("."));
    }

    public static String getFile(String file)
    {
        return getFileName(file)+
               getFileExtension(file);
    }
}
