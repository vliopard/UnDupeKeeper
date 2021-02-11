package tools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Storage implements Serializable
{
    private static final long serialVersionUID = 9181206264329886715L;
    private String            path;

    public Storage( )
    {
        path = Paths.get("").toString( );
    }

    public Storage(String file)
    {
        set(file);
    }

    public Storage(File file)
    {
        set(file);
    }

    public Storage(Path file)
    {
        set(file);
    }

    public void set(String file)
    {
        path = Paths.get(file).toString( );
    }

    public void set(File file)
    {
        path = Paths.get(file.toString( )).toString( );
    }

    public void set(Path file)
    {
        path = file.toString( );
    }

    public String getString( )
    {
        return path;
    }

    public File getFile( )
    {
        return new File(path);
    }

    public Path getPath( )
    {
        return Paths.get(path);
    }

    public boolean deleteFile( )
    {
        try
        {
            Files.deleteIfExists(Paths.get(path));
            Logger.msg("File deleted successfully");
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
    
    @Override
    public String toString()
    {
        return path;
    }
}
