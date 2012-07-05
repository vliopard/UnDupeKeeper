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

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class UnDupeChecker
{
    private static int sz;

    public static int size()
    {
        return sz;
    }

    public JTree getTree(String pt)
    {
        return new JTree(getRoot(pt));
    }

    public static DefaultMutableTreeNode getRoot(String pathm)
    {
        // TODO: EXTERNALIZE STRING
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("UnDupeKeeper");
        TreeMap<String,ArrayList<String>> hashMapTable=merge(showFiles(new File(pathm).listFiles(),
                                                                       Settings.UnDupeKeeperExtension));
        sz=hashMapTable.size();
        for(int i=0; i<sz; i++)
        {
            Entry<String,ArrayList<String>> e=hashMapTable.firstEntry();
            String k=e.getKey();
            ArrayList<String> v=e.getValue();
            hashMapTable.remove(k);
            DefaultMutableTreeNode child=new DefaultMutableTreeNode(k);
            root.add(child);
            for(int j=0; j<v.size(); j++)
            {
                DefaultMutableTreeNode grandChild=new DefaultMutableTreeNode(v.get(j));
                child.add(grandChild);
            }
        }
        return root;
    }

    private static TreeMap<String,ArrayList<String>> merge(ArrayList<String> myar)
    {
        TreeMap<String,ArrayList<String>> hm=new TreeMap<String,ArrayList<String>>();
        for(int i=0; i<myar.size(); i++)
        {
            try
            {
                FileReader f=new FileReader(myar.get(i));
                BufferedReader fis=new BufferedReader(f);
                String wor=fis.readLine();
                wor=wor.substring(0,
                                  wor.indexOf(Settings.UnDupeKeeperSignature));
                if(!hm.containsKey(wor))
                {
                    ArrayList<String> tmp=new ArrayList<String>();
                    tmp.add(myar.get(i));
                    hm.put(wor,
                           tmp);
                }
                else
                {
                    ArrayList<String> tmp=hm.get(wor);
                    tmp.add(myar.get(i));
                    hm.remove(wor);
                    hm.put(wor,
                           tmp);
                }
            }
            catch(IOException e)
            {
                // TODO: HANDLE ERROR MESSAGE
            }
        }
        return hm;
    }

    private static ArrayList<String> showFiles(File[] files,
                                               String filter)
    {
        ArrayList<String> fileList=new ArrayList<String>();
        for(File file : files)
        {
            if(file.isDirectory())
            {
                fileList.addAll(showFiles(file.listFiles(),
                                          filter));
            }
            else
            {
                if(file.getAbsolutePath()
                       .endsWith(filter))
                {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }
}
