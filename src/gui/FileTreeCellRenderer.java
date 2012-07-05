package gui;
import java.awt.Component;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class FileTreeCellRenderer extends
        DefaultTreeCellRenderer
{
    private static final long serialVersionUID =-4760945787076736344L;
    private FileSystemView    fileSystemView;
    private JLabel            label;

    FileTreeCellRenderer()
    {
        label=new JLabel();
        label.setOpaque(true);
        fileSystemView=FileSystemView.getFileSystemView();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus)
    {
        DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
        File file=new File((String)node.getUserObject());
        label.setIcon(fileSystemView.getSystemIcon(file));
        label.setText(fileSystemView.getSystemDisplayName(file));
        label.setToolTipText(file.getPath());
        if(selected)
        {
            label.setBackground(backgroundSelectionColor);
            label.setForeground(textSelectionColor);
        }
        else
        {
            label.setBackground(backgroundNonSelectionColor);
            label.setForeground(textNonSelectionColor);
        }
        return label;
    }
}
