package tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import settings.Settings;
import settings.Strings;

/**
 * ReportGenerator class is responsible for providing complete tree information
 * regarding to directories and its file contents.
 * 
 * @author vliopard
 */
public class ReportGenerator
{
    private static int totalItems;

    /**
     * This method returns the total number of files inside a directory tree,
     * including its sub directories.
     * 
     * @return Returns an <code>int</code> value with the total number of files
     *         inside a complete directory tree.
     */
    public static int size()
    {
        return totalItems;
    }

    /**
     * This method returns a complete file tree, obtained from a given
     * directory.
     * 
     * @param directoryPath
     *            A <code>String</code> containing a directory path to be mapped
     *            in terms of a tree.
     * @return Returns a <code>JTree</code> that contains a complete directory
     *         branch representation, or <code>null</code> in case of any error.
     */
    public static JTree getTree(String directoryPath)
    {
        return new JTree(getRootNode(directoryPath));
    }

    /**
     * This method returns the root node of a complete file tree, obtained from
     * a given directory.
     * 
     * @param directoryPath
     *            A <code>String</code> containing a directory path to be mapped
     *            in terms of a node tree.
     * @return Returns a <code>DefaultMutableTreeNode</code> which is the root
     *         node of a complete directory branch tree, or <code>null</code> in
     *         case of any error.
     */
    public static DefaultMutableTreeNode getRootNode(String directoryPath)
    {
        DefaultMutableTreeNode treeNode=new DefaultMutableTreeNode(Strings.fbTitleKeep);
        TreeMap<String,ArrayList<String>> treeMap=convertToNodes(generateFileList(new File(directoryPath).listFiles(),
                                                                                  Settings.UnDupeKeeperExtension));
        if(null==treeMap)
        {
            return null;
        }
        totalItems=treeMap.size();
        for(int i=0; i<totalItems; i++)
        {
            Entry<String,ArrayList<String>> treeMapEntry=treeMap.firstEntry();
            String treeMapKey=treeMapEntry.getKey();
            ArrayList<String> treeMapValue=treeMapEntry.getValue();
            treeMap.remove(treeMapKey);
            DefaultMutableTreeNode treeNodeChild=new DefaultMutableTreeNode(treeMapKey);
            treeNode.add(treeNodeChild);
            for(int j=0; j<treeMapValue.size(); j++)
            {
                DefaultMutableTreeNode treeNodeGrandChild=new DefaultMutableTreeNode(treeMapValue.get(j));
                treeNodeChild.add(treeNodeGrandChild);
            }
        }
        return treeNode;
    }

    /**
     * This method converts an <code>ArrayList</code> of <code>String</code>
     * into a <code>TreeMap</code> of nodes.
     * 
     * @param fileNameArray
     *            An <code>ArrayList</code> of <code>String</code> containing a
     *            list of all files inside a directory and its sub directories.
     * @return Returns a <code>TreeMap</code> of nodes containing files
     *         organized by their original parents or <code>null</code> in case
     *         of any error.
     */
    private static TreeMap<String,ArrayList<String>> convertToNodes(ArrayList<String> fileNameArray)
    {
        TreeMap<String,ArrayList<String>> treeMap=new TreeMap<String,ArrayList<String>>();
        BufferedReader bufferedReader=null;
        for(int i=0; i<fileNameArray.size(); i++)
        {
            try
            {
                FileReader fileReader=new FileReader(fileNameArray.get(i));
                bufferedReader=new BufferedReader(fileReader);
                String fileName=bufferedReader.readLine();
                if(null!=fileName)
                {
                    fileName=fileName.substring(0,
                                                fileName.indexOf(Settings.UnDupeKeeperSignature));
                }
                else
                {
                    // TODO: EXTERNALIZE STRING
                    err("MSG_038:"+"Invalid file content");
                    return null;
                }
                if(!treeMap.containsKey(fileName))
                {
                    ArrayList<String> childrenFileList=new ArrayList<String>();
                    childrenFileList.add(fileNameArray.get(i));
                    treeMap.put(fileName,
                                childrenFileList);
                }
                else
                {
                    ArrayList<String> childrenFileList=treeMap.get(fileName);
                    childrenFileList.add(fileNameArray.get(i));
                    treeMap.remove(fileName);
                    treeMap.put(fileName,
                                childrenFileList);
                }
            }
            catch(IOException e)
            {
                err("MSG_034: "
                        +Strings.fbReportError+
                    e);
                return null;
            }
        }
        if(bufferedReader!=null)
        {
            try
            {
                bufferedReader.close();
            }
            catch(IOException e)
            {
                err("MSG_035: "
                        +Strings.rgErrorClosingBuffer+
                    e);
            }
        }
        return treeMap;
    }

    /**
     * This method generates a file list containing all files inside a directory
     * and its sub directories.
     * 
     * @param fileArray
     *            A <code>File[]</code> array with the root directory listing.
     * @param fileExtension
     *            A <code>String</code> containing the extension to filter files
     *            from a same type.
     * @return Returns an <code>ArrayList</code> of <code>Strings</code>
     *         containing all files from the root directory and its sub
     *         directories.
     */
    public static ArrayList<String> generateFileList(File[] fileArray,
                                                     String fileExtension)
    {
        ArrayList<String> fileList=new ArrayList<String>();
        for(File file : fileArray)
        {
            if(file.isDirectory())
            {
                fileList.addAll(generateFileList(file.listFiles(),
                                                 fileExtension));
            }
            else
            {
                if((null==fileExtension)||
                   (fileExtension.equals(Settings.Empty))||
                   (file.getAbsolutePath().endsWith(fileExtension)))
                {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
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
