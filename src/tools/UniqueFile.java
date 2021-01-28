package tools;

import main.Comparison;
import settings.Strings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UniqueFile
{

    private Path         fileUri      = null;
    private String       fileSha      = null;
    private ArrayList<Path> fileLinks = new ArrayList<Path>();

    public UniqueFile(String value)
    {
        fileUri = Paths.get(value);
        setSha();
    }

    public UniqueFile(Path value)
    {
        fileUri = value;
        setSha();
    }

    public String getSha()
    {
        return fileSha;
    }

    public void setSha()
    {
        fileSha = CheckSum.getChecksumElegant(fileUri);
    }

    public Path getFilePath()
    {
        return fileUri;
    }

    public String getFileStr()
    {
        return fileUri.toString();
    }
    
    public void clearUrl()
    {
        fileUri=Paths.get("");
    }

    public void setFileUri(String uri)
    {
        setFileUri(Paths.get(uri));
    }
    
    public void setFileUri(Path uri)
    {
        fileUri = uri;
        setSha();
        if (!fileUri.equals(uri))
        {
            if (!fileLinks.isEmpty())
            {
                for (int i = 0; i < fileLinks.size(); i++)
                {
                    renewLink(uri);
                }
            }
        }
    }

    public void renewLink(String uri)
    {
        renewLink(Paths.get(uri));
    }
    
    public void renewLink(Path uri)
    {
        
        if (FileOperations.isLink(uri))
        {
            FileOperations.deleteFile(uri);
        }
        Linker.createLink(uri, fileUri);
    }

    public ArrayList<Path> getFileLinks()
    {
        return fileLinks;
    }

    public void setFileLinks(ArrayList<Path> value)
    {
        fileLinks = value;
        for (int i = 0; i < value.size(); i++)
        {
            makeLink(value.get(i));
        }
    }

    public void addLink(String value)
    {
        addLink(Paths.get(value));
    }
    
    public void addLink(Path value)
    {
        fileLinks.add(value);
        makeLink(value);
    }

    public void unLink(String link)
    {
        unLink(Paths.get(link));
    }
    
    public void unLink(Path link)
    {
        if (fileLinks.contains(link))
        {
            fileLinks.remove(link);
        }
    }

    public void delLink(String link)
    {
        delLink(Paths.get(link));
    }

    public void delLink(Path link)
    {
        if (fileLinks.contains(link))
        {
            fileLinks.remove(link);
            FileOperations.deleteFile(link);
        }
    }

    public void removeLink(String link)
    {
        removeLink(Paths.get(link));
    }
    
    public void removeLink(Path link)
    {
        if (fileLinks.contains(link))
        {
            FileOperations.deleteFile(link);
        }
    }

    public void removeLinks()
    {
        for (int i = 0; i > fileLinks.size(); i++)
        {
            FileOperations.deleteFile(fileLinks.get(i));
        }
    }

    public void makeLinks()
    {
        for (int i = 0; i < fileLinks.size(); i++)
        {
            Linker.createLink(fileLinks.get(i), fileUri);
        }
    }

    public void makeLink(String uri)
    {
        makeLink(Paths.get(uri));
    }

    public void makeLink(Path uri)
    {
        if (Comparison.isFileEqual(uri, fileUri))
        {
            FileOperations.deleteFile(uri);
            Linker.createLink(uri, fileUri);
        }
        else
        {
            Logger.err(Strings.wkFatalError);
            Logger.msg("ERROR: FILE CONTENTS ARE NOT THE SAME");
        }
    }
    
    public void show() 
    {
        Logger.msg("Filename: [" + fileUri.toString() + "]");
        Logger.msg("Checksum: [" + fileSha + "]");
        if (fileLinks.size() > 0)
        {
            Logger.msg("Filelink:");
            for(int i = 0; i < fileLinks.size(); i++)
            {
                Logger.msg("\t" + fileLinks.get(i).toString());
            }
            Logger.msg("------------------------------------");
        }
    }
}