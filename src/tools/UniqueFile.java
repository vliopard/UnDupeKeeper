package tools;

import main.Comparison;
import settings.Strings;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;

public class UniqueFile implements Serializable
{
    private static final long   serialVersionUID = -2472447792902131726L;
    private Storage             fileUri          = null;
    private String              fileSha          = null;
    private ArrayList <Storage> fileLinks        = new ArrayList <Storage>( );

    public UniqueFile(String value)
    {
        fileUri = new Storage(value);
        setSha( );
    }

    public UniqueFile(File value)
    {
        fileUri = new Storage(value);
        setSha( );
    }

    public UniqueFile(Path value)
    {
        fileUri = new Storage(value);
        setSha( );
    }

    public UniqueFile(Storage value)
    {
        fileUri = value;
        setSha( );
    }

    public Storage getStorage( )
    {
        return fileUri;
    }

    public void setStorage(Storage file)
    {
        fileUri = file;
    }

    public String getSha( )
    {
        return fileSha;
    }

    public void setSha( )
    {
        fileSha = CheckSum.getChecksumElegant(fileUri);
    }

    public Path getPath( )
    {
        return fileUri.getPath( );
    }
    
    public boolean isEmpty()
    {
        return fileUri.isEmpty( );
    }

    public String getString( )
    {
        return fileUri.getString( );
    }

    public void clearUrl( )
    {
        fileUri = new Storage( );
    }

    public void setString(String uri)
    {
        setPath(new Storage(uri));
    }

    public void setPath(File uri)
    {
        setPath(new Storage(uri));
    }

    public void setPath(Path uri)
    {
        setPath(new Storage(uri));
    }

    public void setPath(Storage uri)
    {
        // TODO: THIS METHOD SHOULD BE REVISED - why remaking links?
        if ( !fileUri.getString( ).equals(uri.getString( )))
        {
            fileUri = uri;
            setSha( );
            if ( !fileLinks.isEmpty( ))
            {
                for (int i = 0; i < fileLinks.size( ); i++)
                {
                    remakeLink(uri);
                }
            }
        }
    }

    public void remakeLink(String uri)
    {
        remakeLink(new Storage(uri));
    }

    public void remakeLink(File uri)
    {
        remakeLink(new Storage(uri));
    }

    public void remakeLink(Path uri)
    {
        remakeLink(new Storage(uri));
    }

    public void remakeLink(Storage uri)
    {
        if (FileOperations.isLink(uri))
        {
            FileOperations.deleteFile(uri);
        }
        // TODO: CREATE TEST SUITE TO CHECK PERFORMANCE BETWEEN THEM
        //Linker.createLink(uri, fileUri);
        try
        {
            // TODO: TEST AND BENCHMARK: Files.createLink(getPath(), getPath())
            Files.createSymbolicLink(uri.getPath( ), fileUri.getPath( ));
            // TODO: BENCHMARK TO Linker.createLink(uri, fileUri);
        }
        catch (IOException e)
        {
            // TODO: index error message
            e.printStackTrace( );
        }
    }

    public ArrayList <Storage> getFileLinks( )
    {
        return fileLinks;
    }

    public void setFileLinks(ArrayList <Storage> value)
    {
        fileLinks = value;
        for (int i = 0; i < value.size( ); i++)
        {
            makeLink(value.get(i).getPath( ));
        }
    }

    public void addLink(String value)
    {
        addLink(Paths.get(value));
    }

    public void addLink(File value)
    {
        addLink(Paths.get(value.toString( )));
    }

    public void addLink(Path value)
    {
        fileLinks.add(new Storage(value));
        makeLink(value);
    }

    public void addLink(Storage value)
    {
        fileLinks.add(value);
        makeLink(value);
    }

    public void includeLink(Path value)
    {
        includeLink(new Storage(value));
    }

    public void includeLink(Storage value)
    {
        fileLinks.add(value);
    }

    public void renLink(Path oldname, Path newname)
    {
        renLink(new Storage(oldname), new Storage(newname));
    }

    public void renLink(Storage oldname, Storage newname)
    {
        fileLinks.remove(oldname);
        fileLinks.add(newname);
    }

    public void unLink(String link)
    {
        unLink(new Storage(link));
    }

    public void unLink(File link)
    {
        unLink(new Storage(link));
    }

    public void unLink(Path link)
    {
        unLink(new Storage(link));
    }

    public void unLink(Storage link)
    {
        if (fileLinks.contains(link))
        {
            fileLinks.remove(link);
        }
    }

    public void delLink(String link)
    {
        delLink(new Storage(link));
    }

    public void delLink(File link)
    {
        delLink(new Storage(link));
    }

    public void delLink(Path link)
    {
        delLink(new Storage(link));
    }

    public void delLink(Storage link)
    {
        if (fileLinks.contains(link))
        {
            fileLinks.remove(link);
            link.deleteFile( );
            link = null;
        }
    }

    public void removeLink(String link)
    {
        removeLink(Paths.get(link));
    }

    public void removeLink(File link)
    {
        removeLink(Paths.get(link.toString( )));
    }

    public void removeLink(Path link)
    {
        Storage uri = new Storage(link);
        if (fileLinks.contains(uri))
        {
            //FileOperations.deleteFile(link);
            uri.deleteFile( );
            uri = null;
        }
    }

    public void removeLinks( )
    {
        for (int i = 0; i < fileLinks.size( ); i++)
        {
            //FileOperations.deleteFile(fileLinks.get(i));
            fileLinks.get(i).deleteFile( );
        }
    }

    public void makeLinks( )
    {
        try
        {
            for (int i = 0; i < fileLinks.size( ); i++)
            {
                // TODO: TEST AND BENCHMARK: Files.createLink(getPath(), getPath())
                Files.createSymbolicLink(fileLinks.get(i).getPath( ), fileUri.getPath( ));
                // TODO: BENCHMARK TO Linker.createLink(uri, fileUri);
            }
        }
        catch (IOException e)
        {
            // TODO: index error message
            e.printStackTrace( );
        }
    }

    public void makeLink(String uri)
    {
        makeLink(new Storage(uri));
    }

    public void makeLink(File uri)
    {
        makeLink(new Storage(uri));
    }

    public void makeLink(Path uri)
    {
        makeLink(new Storage(uri));
    }

    public void makeLink(Storage uri)
    {
        try
        {
            // TODO: COMPARE BY CHECKSUM
            if (Comparison.compareBySize(uri.getPath( ), fileUri.getPath( )))
            {
                FileOperations.deleteFile(uri);
                // TODO: TEST AND BENCHMARK: Files.createLink(getPath(), getPath())
                Files.createSymbolicLink(uri.getPath( ), fileUri.getPath( ));
                // TODO: BENCHMARK TO Linker.createLink(uri, fileUri);
            }
            else
            {
                Logger.err(Strings.wkFatalError);
                Logger.msg("ERROR: FILE CONTENTS ARE NOT THE SAME");
            }
        }
        catch (IOException e)
        {
            // TODO: index error message
            e.printStackTrace( );
        }
    }

    public void show( )
    {
        Logger.msg("Checksum: [" + fileSha + "] Filename: [" + fileUri.getString( ) + "]");
        if (fileLinks.size( ) > 0)
        {
            for (int i = 0; i < fileLinks.size( ); i++)
            {
                Logger.msg("\t\tFilelink: " + fileLinks.get(i).getString( ));
            }
        }
    }
}