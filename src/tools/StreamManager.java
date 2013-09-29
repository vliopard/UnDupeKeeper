package tools;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.mozilla.universalchardet.UniversalDetector;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class StreamManager
{
    private static String getEncoding(String file)
    {
        String encoding="WINDOWS-1252";
        try
        {
            InputStream fileName=new FileInputStream(file);
            byte[] buf=new byte[4096];
            UniversalDetector detector=new UniversalDetector(null);
            int nread;
            while((nread=fileName.read(buf))>0&&
                  !detector.isDone())
            {
                detector.handleData(buf,
                                    0,
                                    nread);
            }
            fileName.close();
            detector.dataEnd();
            encoding=detector.getDetectedCharset();
            detector.reset();
            if(encoding==null)
            {
                encoding="WINDOWS-1252";
            }
        }
        catch(IOException e)
        {
            encoding="UTF-8";
        }
        return encoding;
    }

    public static InputStreamReader InputStreamReader(String file) throws IOException
    {
        InputStream fileName=new FileInputStream(file);
        String encoding=getEncoding(file);
        if(encoding!=null)
        {
            return new InputStreamReader(fileName,
                                         encoding);
        }
        return new InputStreamReader(fileName);
    }

    public static Writer FileWriter(String fileName) throws IOException
    {
        File f=new File(fileName);
        String encoding=getEncoding(fileName);
        FileOutputStream fos=new FileOutputStream(f);
        OutputStreamWriter osw=new OutputStreamWriter(fos,
                                                      encoding);
        BufferedWriter bw=new BufferedWriter(osw);
        return bw;
    }
}
