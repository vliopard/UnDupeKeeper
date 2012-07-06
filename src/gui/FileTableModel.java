package gui;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import settings.Strings;
import tools.Logger;
// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING

public class FileTableModel extends
        AbstractTableModel
{
    private static final long serialVersionUID =-6234469100236698739L;
    private File[]            files;
    private FileSystemView    fileSystemView   =FileSystemView.getFileSystemView();
    private String[]          columns          =
                                               {
            Strings.fbIcon,
            Strings.fbFileName,
            Strings.fbExtension,
            Strings.fbPath,
            Strings.fbSize,
            Strings.fbDateHead,
            Strings.fbRead,
            Strings.fbWrite,
            Strings.fbExecute,
            Strings.fbDir,
            Strings.fbIsFile,
                                               };

    private String customFormat(String pattern,
                                double value)
    {
        DecimalFormat myFormatter=new DecimalFormat(pattern);
        return myFormatter.format(value);
    }

    FileTableModel()
    {
        this(new File[0]);
    }

    FileTableModel(File[] files)
    {
        this.files=files;
    }

    public Object getValueAt(int row,
                             int column)
    {
        File file=files[row];
        switch(column)
        {
            case 0:
                return fileSystemView.getSystemIcon(file);
            case 1:
                if(fileSystemView.getSystemDisplayName(file)
                                 .lastIndexOf(Strings.dot)<0)
                {
                    return fileSystemView.getSystemDisplayName(file);
                }
                return fileSystemView.getSystemDisplayName(file)
                                     .substring(0,
                                                fileSystemView.getSystemDisplayName(file)
                                                              .lastIndexOf(Strings.dot));
            case 2:
                if(file.isDirectory())
                {
                    return Strings.fbDirSymbol;
                }
                if(fileSystemView.getSystemDisplayName(file)
                                 .lastIndexOf(Strings.dot)<0)
                {
                    return "";
                }
                return fileSystemView.getSystemDisplayName(file)
                                     .substring(fileSystemView.getSystemDisplayName(file)
                                                              .lastIndexOf(Strings.dot)+1,
                                                fileSystemView.getSystemDisplayName(file)
                                                              .length());
            case 3:
                return file.getPath();
            case 4:
                return customFormat(Strings.numberFormatMask,
                                    file.length());
            case 5:
                return file.lastModified();
            case 6:
                return file.canRead();
            case 7:
                return file.canWrite();
            case 8:
                return file.canExecute();
            case 9:
                return file.isDirectory();
            case 10:
                return file.isFile();
            default:
                err(Strings.fbInvalidColumnIndex);
        }
        return "";
    }

    public int getColumnCount()
    {
        return columns.length;
    }

    public Class<?> getColumnClass(int column)
    {
        switch(column)
        {
            case 0:
                return ImageIcon.class;
            case 4:
                return Long.class;
            case 5:
                return Date.class;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                return Boolean.class;
        }
        return String.class;
    }

    public String getColumnName(int column)
    {
        return columns[column];
    }

    public int getRowCount()
    {
        return files.length;
    }

    public File getFile(int row)
    {
        return files[row];
    }

    public void setFiles(File[] files)
    {
        this.files=files;
        fireTableDataChanged();
    }

    /**
     * This method displays an error message through the embedded log system.
     * 
     * @param errorMessage
     *            A <code>String</code> containing the error message to display.
     */
    private static void err(String errorMessage)
    {
        Logger.err(errorMessage);
    }
}
