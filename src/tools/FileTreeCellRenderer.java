package tools;
import java.awt.Component;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * FileTreeCellRenderer class provides a render set of tools to display files in
 * a tree.
 * 
 * @author vliopard
 */
public class FileTreeCellRenderer extends
        DefaultTreeCellRenderer
{
    private static final long serialVersionUID =-4760945787076736344L;
    private FileSystemView    fileSystemView;
    private JLabel            label;

    /**
     * FileTreeCellRenderer Constructor - Initialize a FileTreeCellRenderer
     * object for rendering a tree.
     */
    public FileTreeCellRenderer()
    {
        label=new JLabel();
        label.setOpaque(true);
        fileSystemView=FileSystemView.getFileSystemView();
    }

    /**
     * Sets the value of the current tree cell to value. If selected is true,
     * the cell will be drawn as if selected. If expanded is true the node is
     * currently expanded and if leaf is true the node represets a leaf and if
     * hasFocus is true the node currently has focus. tree is the JTree the
     * receiver is being configured for. Returns the Component that the renderer
     * uses to draw the value.
     * 
     * @param tree
     *            A <code>JTree</code> that contains the file tree.
     * @param value
     *            An <code>Object</code> that contains the focused file.
     * @param selected
     *            A <code>boolean</code> that indicates if node is selected.
     * @param expanded
     *            A <code>boolean</code> that indicates if node is expanded.
     * @param leaf
     *            A <code>boolean</code> that indicates if node is a leaf.
     * @param row
     *            An <code>int</code> containing the row number.
     * @param hasFocus
     *            A <code>boolean</code> that indicates if note has focus.
     * @return Returns the <code>Component</code> that the renderer uses to draw
     *         the value.
     */
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
