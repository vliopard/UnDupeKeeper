package gui;
// TODO: FIX FILE SIZE ORDERING
// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
// TODO: SAVE POSITION AND SCREEN SETTINGS
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.filechooser.FileSystemView;
import main.UnDupeKeeper;
import settings.Settings;
import settings.Strings;
import tools.Logger;
import tools.SettingsHandler;
import tools.TrayImage;
import tools.UnDupeChecker;
import tools.Utils;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.URL;

public class ReportViewer
{
    private static Desktop               desktop;
    private static FileSystemView        fileSystemView;
    private static File                  currentFile;
    private static JPanel                gui;
    private static JTree                 tree;
    private static DefaultTreeModel      treeModel;
    private static JTable                table;
    private static JProgressBar          progressBar    =new JProgressBar();
    private static JLabel                progressBar1;
    private static FileTableModel        fileTableModel;
    private static ListSelectionListener listSelectionListener;
    private static boolean               cellSizesSet   =false;
    private static int                   rowIconPadding =1;
    private static JButton               locateFile;
    private static JButton               openFile;
    private static JButton               deleteFile;
    private static JButton               editFile;
    private static JLabel                fileName;
    private static JTextField            path;
    private static JLabel                date;
    private static JLabel                size;
    private static JCheckBox             readable;
    private static JCheckBox             writable;
    private static JCheckBox             executable;
    private static JRadioButton          isDirectory;
    private static JRadioButton          isFile;
    private static int                   len;
    private static SettingsHandler       settingsHandler;

    private static Container getGui()
    {
        if(gui==null)
        {
            gui=new JPanel(new BorderLayout(1,
                                            1));
            gui.setBorder(new EmptyBorder(1,
                                          1,
                                          1,
                                          1));
            fileSystemView=FileSystemView.getFileSystemView();
            desktop=Desktop.getDesktop();
            JPanel detailView=new JPanel(new BorderLayout(1,
                                                          1));
            table=new JTable();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoCreateRowSorter(true);
            table.setShowVerticalLines(false);
            listSelectionListener=new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent lse)
                    {
                        int row=table.getSelectionModel()
                                     .getLeadSelectionIndex();
                        setFileDetails(((FileTableModel)table.getModel()).getFile(row));
                    }
                };
            table.getSelectionModel()
                 .addListSelectionListener(listSelectionListener);
            JScrollPane tableScroll=new JScrollPane(table);
            Dimension d=tableScroll.getPreferredSize();
            tableScroll.setPreferredSize(new Dimension((int)d.getWidth(),
                                                       (int)d.getHeight()/2));
            detailView.add(tableScroll,
                           BorderLayout.CENTER);
            DefaultMutableTreeNode root=new DefaultMutableTreeNode();
            treeModel=new DefaultTreeModel(root);
            TreeSelectionListener treeSelectionListener=new TreeSelectionListener()
                {
                    public void valueChanged(TreeSelectionEvent tse)
                    {
                        DefaultMutableTreeNode node=(DefaultMutableTreeNode)tse.getPath()
                                                                               .getLastPathComponent();
                        showChildren(node);
                        setFileDetails(new File((String)node.getUserObject()));
                    }
                };
            root.add(UnDupeChecker.getRoot(settingsHandler.getDirectory()));
            len=UnDupeChecker.size();
            tree=new JTree(treeModel);
            tree.setRootVisible(false);
            tree.addTreeSelectionListener(treeSelectionListener);
            tree.setCellRenderer(new FileTreeCellRenderer());
            // tree.expandRow(0);
            JScrollPane treeScroll=new JScrollPane(tree);
            tree.setVisibleRowCount(10);
            tree.addTreeWillExpandListener(new TreeWillExpandListener()
                {
                    public void treeWillExpand(TreeExpansionEvent e)
                    {
                        // TODO: EXTERNALIZE STRING
                        Utils.displayBallon("Warning",
                                            "This action may take a while...");
                    }

                    @Override
                    public void treeWillCollapse(TreeExpansionEvent arg0) throws ExpandVetoException
                    {
                    }
                });
            tree.addTreeExpansionListener(new TreeExpansionListener()
                {
                    public void treeExpanded(TreeExpansionEvent e)
                    {
                        // TODO: EXTERNALIZE STRING
                        Utils.displayBallon("Warning",
                                            "Done");
                    }

                    @Override
                    public void treeCollapsed(TreeExpansionEvent arg0)
                    {
                    }
                });
            Dimension preferredSize=treeScroll.getPreferredSize();
            Dimension widePreferred=new Dimension(350,
                                                  (int)preferredSize.getHeight());
            treeScroll.setPreferredSize(widePreferred);
            JPanel fileMainDetails=new JPanel(new BorderLayout(4,
                                                               2));
            fileMainDetails.setBorder(new EmptyBorder(0,
                                                      6,
                                                      0,
                                                      6));
            JPanel fileDetailsLabels=new JPanel(new GridLayout(0,
                                                               1,
                                                               2,
                                                               2));
            fileMainDetails.add(fileDetailsLabels,
                                BorderLayout.WEST);
            JPanel fileDetailsValues=new JPanel(new GridLayout(0,
                                                               1,
                                                               2,
                                                               2));
            fileMainDetails.add(fileDetailsValues,
                                BorderLayout.CENTER);
            fileDetailsLabels.add(new JLabel(Strings.fbFile,
                                             JLabel.TRAILING));
            fileName=new JLabel();
            fileDetailsValues.add(fileName);
            fileDetailsLabels.add(new JLabel(Strings.fbPathName,
                                             JLabel.TRAILING));
            path=new JTextField();
            path.setEditable(false);
            fileDetailsValues.add(path);
            fileDetailsLabels.add(new JLabel(Strings.fbDate,
                                             JLabel.TRAILING));
            date=new JLabel();
            fileDetailsValues.add(date);
            fileDetailsLabels.add(new JLabel(Strings.fbFileSize,
                                             JLabel.TRAILING));
            size=new JLabel();
            fileDetailsValues.add(size);
            fileDetailsLabels.add(new JLabel(Strings.fbType,
                                             JLabel.TRAILING));
            JPanel flags=new JPanel(new FlowLayout(FlowLayout.LEADING,
                                                   4,
                                                   0));
            isDirectory=new JRadioButton(Strings.fbDirectory);
            flags.add(isDirectory);
            isFile=new JRadioButton(Strings.fileName);
            flags.add(isFile);
            fileDetailsValues.add(flags);
            JToolBar toolBar=new JToolBar();
            toolBar.setFloatable(false);
            locateFile=new JButton(Strings.btA);
            locateFile.setMnemonic(Strings.btAShort);
            locateFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            msg(Strings.btAMessage+
                                currentFile.getParentFile());
                            desktop.open(currentFile.getParentFile());
                        }
                        catch(Throwable t)
                        {
                            showThrowable(Strings.btAError,
                                          Strings.btA,
                                          t);
                        }
                        gui.repaint();
                    }
                });
            toolBar.add(locateFile);
            openFile=new JButton(Strings.btB);
            openFile.setMnemonic(Strings.btBShort);
            openFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            msg(Strings.btBMessage+
                                currentFile);
                            desktop.open(currentFile);
                        }
                        catch(Throwable t)
                        {
                            showThrowable(Strings.btBError,
                                          Strings.btB,
                                          t);
                        }
                        gui.repaint();
                    }
                });
            toolBar.add(openFile);
            editFile=new JButton(Strings.btC);
            editFile.setMnemonic(Strings.btCShort);
            editFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            msg(Strings.btCMessage+
                                currentFile);
                            desktop.edit(currentFile);
                        }
                        catch(Throwable t)
                        {
                            showThrowable(Strings.btCError,
                                          Strings.btC,
                                          t);
                        }
                    }
                });
            toolBar.add(editFile);
            deleteFile=new JButton(Strings.btD);
            deleteFile.setMnemonic(Strings.btDShort);
            deleteFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            msg(Strings.btDMessage+
                                currentFile);
                            // TODO: LOOK FOR A WAY TO DELETE FILE
                            // TODO: AFTER DELETE FILE, REFRESH GUI FILE TREE
                            currentFile.setReadable(true);
                            currentFile.setWritable(true);
                            if(currentFile.delete())
                            {
                                showErrorMessage(Strings.btDError,
                                                 Strings.btD);
                            }
                        }
                        catch(Throwable t)
                        {
                            showThrowable(Strings.btDError,
                                          Strings.btD,
                                          t);
                        }
                    }
                });
            toolBar.add(deleteFile);
            locateFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
            openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
            editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
            deleteFile.setEnabled(true);
            flags.add(new JLabel(Strings.pipe+
                                 Strings.space+
                                 Strings.space+
                                 Strings.flags));
            readable=new JCheckBox(Strings.btRead);
            readable.setMnemonic(Strings.btReadShort);
            flags.add(readable);
            writable=new JCheckBox(Strings.btWrite);
            writable.setMnemonic(Strings.btWriteShort);
            flags.add(writable);
            executable=new JCheckBox(Strings.btExecute);
            executable.setMnemonic(Strings.btExecuteShort);
            flags.add(executable);
            int count=fileDetailsLabels.getComponentCount();
            for(int ii=0; ii<count; ii++)
            {
                fileDetailsLabels.getComponent(ii)
                                 .setEnabled(false);
            }
            count=flags.getComponentCount();
            for(int ii=0; ii<count; ii++)
            {
                flags.getComponent(ii)
                     .setEnabled(false);
            }
            JPanel fileView=new JPanel(new BorderLayout(3,
                                                        3));
            fileView.add(toolBar,
                         BorderLayout.NORTH);
            fileView.add(fileMainDetails,
                         BorderLayout.CENTER);
            detailView.add(fileView,
                           BorderLayout.SOUTH);
            JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                treeScroll,
                                                detailView);
            gui.add(splitPane,
                    BorderLayout.CENTER);
            JPanel simpleOutput=new JPanel(new BorderLayout(3,
                                                            3));
            // progressBar=new JProgressBar();
            simpleOutput.add(progressBar,
                             BorderLayout.EAST);
            progressBar.setVisible(true);
            progressBar1=new JLabel();
            if(len==0)
            {
                progressBar1.setText(Strings.noFileFound);
            }
            else
            {
                if(len==1)
                {
                    progressBar1.setText(Strings.space+
                                         Utils.format(len)+
                                         Strings.fileFound);
                }
                else
                {
                    progressBar1.setText(Strings.space+
                                         Utils.format(len)+
                                         Strings.filesFound);
                }
            }
            simpleOutput.add(progressBar1,
                             BorderLayout.WEST);
            gui.add(simpleOutput,
                    BorderLayout.SOUTH);
        }
        return gui;
    }

    private static void showRootFile()
    {
        tree.setSelectionInterval(0,
                                  0);
    }

    @SuppressWarnings("unused")
    private TreePath findTreePath(File find)
    {
        for(int ii=0; ii<tree.getRowCount(); ii++)
        {
            TreePath treePath=tree.getPathForRow(ii);
            Object object=treePath.getLastPathComponent();
            DefaultMutableTreeNode node=(DefaultMutableTreeNode)object;
            File nodeFile=(File)node.getUserObject();
            if(nodeFile==find)
            {
                return treePath;
            }
        }
        return null;
    }

    private static void showErrorMessage(String errorMessage,
                                         String errorTitle)
    {
        JOptionPane.showMessageDialog(gui,
                                      errorMessage,
                                      errorTitle,
                                      JOptionPane.ERROR_MESSAGE);
    }

    private static void showThrowable(String a,
                                      String b,
                                      Throwable t)
    {
        err("["+
            b+
            "] "+
            a+
            " "+
            t.getMessage());
        showErrorMessage(a,
                         b);
        gui.repaint();
    }

    private static void setTableData(final File[] files)
    {
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if(fileTableModel==null)
                    {
                        fileTableModel=new FileTableModel();
                        table.setModel(fileTableModel);
                    }
                    table.getSelectionModel()
                         .removeListSelectionListener(listSelectionListener);
                    fileTableModel.setFiles(files);
                    table.getSelectionModel()
                         .addListSelectionListener(listSelectionListener);
                    if(!cellSizesSet)
                    {
                        Icon icon=fileSystemView.getSystemIcon(files[0]);
                        table.setRowHeight(icon.getIconHeight()+
                                           rowIconPadding);
                        setColumnWidth(0,
                                       -1);
                        setColumnWidth(4,
                                       60);
                        table.getColumnModel()
                             .getColumn(4)
                             .setMaxWidth(255);
                        setColumnWidth(5,
                                       -1);
                        setColumnWidth(6,
                                       -1);
                        setColumnWidth(7,
                                       -1);
                        setColumnWidth(8,
                                       -1);
                        setColumnWidth(9,
                                       -1);
                        setColumnWidth(10,
                                       -1);
                        cellSizesSet=true;
                    }
                }
            });
    }

    private static void setColumnWidth(int column,
                                       int width)
    {
        TableColumn tableColumn=table.getColumnModel()
                                     .getColumn(column);
        if(width<0)
        {
            JLabel label=new JLabel((String)tableColumn.getHeaderValue());
            Dimension preferred=label.getPreferredSize();
            width=(int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    private static void showChildren(final DefaultMutableTreeNode node)
    {
        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        SwingWorker<Void,File> worker=new SwingWorker<Void,File>()
            {
                @Override
                public Void doInBackground()
                {
                    File file=new File((String)node.getUserObject());
                    if(file.getParentFile()
                           .isDirectory())
                    {
                        File[] files=fileSystemView.getFiles(file.getParentFile(),
                                                             true);
                        if(node.isLeaf())
                        {
                            for(File child : files)
                            {
                                if(child.isDirectory())
                                {
                                    // // publish(child);
                                }
                            }
                        }
                        setTableData(files);
                    }
                    return null;
                }

                @Override
                protected void process(List<File> chunks)
                {
                    for(File child : chunks)
                    {
                        node.add(new DefaultMutableTreeNode(child));
                    }
                }

                @Override
                protected void done()
                {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisible(true);
                    tree.setEnabled(true);
                }
            };
        worker.execute();
    }

    /** Update the File details view with the details of this File. */
    private static void setFileDetails(File file)
    {
        currentFile=file;
        Icon icon=fileSystemView.getSystemIcon(file);
        fileName.setIcon(icon);
        // if(fileSystemView.getSystemDisplayName(file).lastIndexOf(".")<0)
        // {
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        // }
        // else
        // {
        // fileName.setText(fileSystemView.getSystemDisplayName(file).substring(0,fileSystemView.getSystemDisplayName(file).lastIndexOf(".")));
        // }
        // ext.setText(fileSystemView.getSystemDisplayName(file).substring(fileSystemView.getSystemDisplayName(file).lastIndexOf(".")+1,fileSystemView.getSystemDisplayName(file).length()));
        path.setText(file.getPath());
        date.setText(new Date(file.lastModified()).toString());
        size.setText(Utils.format(file.length())+
                     Strings.fileBytes);
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());
        isFile.setSelected(file.isFile());
        JFrame f=(JFrame)gui.getTopLevelAncestor();
        if(f!=null)
        {
            f.setTitle(Strings.fbTitleCheck+
                       Strings.space+
                       Strings.separator+
                       Strings.space+
                       fileSystemView.getSystemDisplayName(file));
        }
        gui.repaint();
    }

    public static JFrame show(SettingsHandler sh)
    {
        settingsHandler=sh;
        // SwingUtilities.invokeLater(new Runnable()
        // {
        // public void run()
        // {
        try
        {
            UIManager.setLookAndFeel(Settings.LookAndFeelPackages[settingsHandler.getLookAndFeel()]);
        }
        catch(Exception weTried)
        {
            err(Strings.fbErrorLoadingLookAndFeel+
                weTried);
        }
        JFrame f=new JFrame(Strings.fbTitleCheck);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // ReportViewer FileBrowser=new ReportViewer();
        f.setContentPane(ReportViewer.getGui());
        try
        {
            ArrayList<Image> images=new ArrayList<Image>();
            URL urlSmall=UnDupeKeeper.class.getResource(Settings.iconList[8]);
            if(urlSmall==null)
            {
                err(Strings.ukResourceNotFound+
                    Settings.iconList[8]);
                images.add(TrayImage.createNewImage());
            }
            else
            {
                images.add((new ImageIcon(urlSmall,
                                          "")).getImage());
            }
            URL urlBig=UnDupeKeeper.class.getResource(Settings.iconList[7]);
            if(urlBig==null)
            {
                err(Strings.ukResourceNotFound+
                    Settings.iconList[7]);
                images.add(TrayImage.createNewImage());
            }
            else
            {
                images.add((new ImageIcon(urlBig,
                                          "")).getImage());
            }
            f.setIconImages(images);
        }
        catch(Exception weTried)
        {
            err(Strings.fbErrorLoadingIcons+
                weTried);
        }
        f.pack();
        // f.setLocationByPlatform(true);
        f.setMinimumSize(f.getSize());
        f.setVisible(true);
        ReportViewer.showRootFile();
        return f;
        // }
        // });
    }

    /**
     * This method displays a message through the embedded log system.
     * 
     * @param message
     *            A <code>String</code> containing the message to display.
     */
    private static void msg(String message)
    {
        Logger.msg(message);
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
