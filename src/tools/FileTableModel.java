package tools;
import java.io.File;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import settings.Settings;
import settings.Strings;

/**
 * FileTableModel class provides a render set of tools to display files in a
 * table.
 * 
 * @author vliopard
 */
public class FileTableModel extends
        AbstractTableModel
{
    private static final long serialVersionUID  =-6234469100236698739L;
    private File[]            fileArray;
    private FileSystemView    fileSystemView    =FileSystemView.getFileSystemView();
    private String[]          fileDetailColumns =
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

    /**
     * FileTableModel Constructor - Initialize an empty FileTableModel object
     * for rendering a file table.
     */
    public FileTableModel()
    {
        this(new File[0]);
    }

    /**
     * FileTableModel Constructor - Initialize a FileTableModel object for
     * rendering a file table.
     * 
     * @param fileList
     *            A <code>File[]</code> array containing the file list to be
     *            displayed.
     */
    public FileTableModel(File[] fileList)
    {
        fileArray=fileList;
    }

    /**
     * This method gets an <code>Object</code> at the provided position.
     * 
     * @param row
     *            An <code>int</code> value of the <code>Object</code>'s row.
     * @param column
     *            An <code>int</code> value of the <code>Object</code>'s column.
     * @return Object The <code>Object</code> from provided position.
     */
    public Object getValueAt(int row,
                             int column)
    {
        File file=fileArray[row];
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
                    return Settings.Empty;
                }
                return fileSystemView.getSystemDisplayName(file)
                                     .substring(fileSystemView.getSystemDisplayName(file)
                                                              .lastIndexOf(Strings.dot)+1,
                                                fileSystemView.getSystemDisplayName(file)
                                                              .length());
            case 3:
                return file.getPath();
            case 4:
                return Utils.numberFormat(file.length());
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
        return Settings.Empty;
    }

    /**
     * This method returns the total column count.
     * 
     * @return An <code>int</code> value representing the total column count.
     */
    public int getColumnCount()
    {
        return fileDetailColumns.length;
    }

    /**
     * This method returns a class that represents the column position.
     * 
     * @param column
     *            An <code>int</code> value of the selected column.
     * @return The <code>Class</code> of the selected column.
     */
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

    /**
     * This method returns the <code>String</code> name of the selected column.
     * 
     * @param column
     *            An <code>int</code> value of the selected column.
     * @return A <code>String</code> value with the name of the column.
     */
    public String getColumnName(int column)
    {
        return fileDetailColumns[column];
    }

    /**
     * This method returns the total row count.
     * 
     * @return An <code>int</code> value representing the total row count.
     */
    public int getRowCount()
    {
        return fileArray.length;
    }

    /**
     * This method returns a <code>File</code> from a given selected row.
     * 
     * @param row
     *            An <code>int</code> value with the selected file row.
     * @return Returns a <code>File</code> from the selected row.
     */
    public File getFile(int row)
    {
        return fileArray[row];
    }

    /**
     * This method feed a file table with a list of files.
     * 
     * @param fileList
     *            A <code>File[]</code> array containing the files to be placed
     *            in a table list.
     */
    public void setFiles(File[] fileList)
    {
        fileArray=fileList;
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
