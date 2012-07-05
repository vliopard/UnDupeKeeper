package gui;
// TODO: FIX FILE SIZE ORDERING
// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
// TODO: VIEW/REMOVE FILES (CONTEXT MENU?)
// TODO: SHUTDOWN FILE BROWSER
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
import tools.Logger;
import tools.SettingsHandler;
import tools.UnDupeChecker;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.URL;

public class FileBrowser
{
    // TODO: EXTERNALIZE STRING
    private static final String    APP_TITLE      ="UnDupeChecker";
    private Desktop                desktop;
    private FileSystemView         fileSystemView;
    private File                   currentFile;
    private JPanel                 gui;
    private JTree                  tree;
    private DefaultTreeModel       treeModel;
    private JTable                 table;
    private JProgressBar           progressBar;
    private JLabel                 progressBar1;
    private FileTableModel         fileTableModel;
    private ListSelectionListener  listSelectionListener;
    private boolean                cellSizesSet   =false;
    private int                    rowIconPadding =6;
    private JButton                openFile;
    private JButton                printFile;
    private JButton                editFile;
    private JLabel                 fileName;
    // private JLabel ext;
    private JTextField             path;
    private JLabel                 date;
    private JLabel                 size;
    private JCheckBox              readable;
    private JCheckBox              writable;
    private JCheckBox              executable;
    private JRadioButton           isDirectory;
    private JRadioButton           isFile;
    private int                    len;
    private static SettingsHandler settingsHandler;

    private String customFormat(String pattern,
                                double value)
    {
        DecimalFormat myFormatter=new DecimalFormat(pattern);
        return myFormatter.format(value);
    }

    public Container getGui()
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
            tree.expandRow(0);
            JScrollPane treeScroll=new JScrollPane(tree);
            tree.setVisibleRowCount(30);
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
            // TODO: EXTERNALIZE STRING
            fileDetailsLabels.add(new JLabel("File",
                                             JLabel.TRAILING));
            fileName=new JLabel();
            fileDetailsValues.add(fileName);
            // fileDetailsLabels.add(new JLabel("Ext",JLabel.TRAILING));
            // ext=new JLabel();
            // fileDetailsValues.add(ext);
            // TODO: EXTERNALIZE STRING
            fileDetailsLabels.add(new JLabel("Path/name",
                                             JLabel.TRAILING));
            path=new JTextField();
            path.setEditable(false);
            fileDetailsValues.add(path);
            // TODO: EXTERNALIZE STRING
            fileDetailsLabels.add(new JLabel("Last Modified",
                                             JLabel.TRAILING));
            date=new JLabel();
            fileDetailsValues.add(date);
            // TODO: EXTERNALIZE STRING
            fileDetailsLabels.add(new JLabel("File size",
                                             JLabel.TRAILING));
            size=new JLabel();
            fileDetailsValues.add(size);
            // TODO: EXTERNALIZE STRING
            fileDetailsLabels.add(new JLabel("Type",
                                             JLabel.TRAILING));
            JPanel flags=new JPanel(new FlowLayout(FlowLayout.LEADING,
                                                   4,
                                                   0));
            // TODO: EXTERNALIZE STRING
            isDirectory=new JRadioButton("Directory");
            flags.add(isDirectory);
            // TODO: EXTERNALIZE STRING
            isFile=new JRadioButton("File");
            flags.add(isFile);
            fileDetailsValues.add(flags);
            JToolBar toolBar=new JToolBar();
            toolBar.setFloatable(false);
            // TODO: EXTERNALIZE STRING
            JButton locateFile=new JButton("Locate");
            // TODO: EXTERNALIZE STRING
            locateFile.setMnemonic('l');
            locateFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            // TODO: EXTERNALIZE STRING
                            // TODO: HANDLE SYSTEM OUT MESSAGE
                            System.out.println("Locate: "+
                                               currentFile.getParentFile());
                            desktop.open(currentFile.getParentFile());
                        }
                        catch(Throwable t)
                        {
                            // TODO: HANDLE ERROR MESSAGE
                            showThrowable(t);
                        }
                        gui.repaint();
                    }
                });
            toolBar.add(locateFile);
            // TODO: EXTERNALIZE STRING
            openFile=new JButton("Open");
            // TODO: EXTERNALIZE STRING
            openFile.setMnemonic('o');
            openFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            // TODO: EXTERNALIZE STRING
                            // TODO: HANDLE SYSTEM OUT MESSAGE
                            System.out.println("Open: "+
                                               currentFile);
                            desktop.open(currentFile);
                        }
                        catch(Throwable t)
                        {
                            // TODO: HANDLE ERROR MESSAGE
                            showThrowable(t);
                        }
                        gui.repaint();
                    }
                });
            toolBar.add(openFile);
            // TODO: EXTERNALIZE STRING
            editFile=new JButton("Edit");
            // TODO: EXTERNALIZE STRING
            editFile.setMnemonic('e');
            editFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            desktop.edit(currentFile);
                        }
                        catch(Throwable t)
                        {
                            // TODO: HANDLE ERROR MESSAGE
                            showThrowable(t);
                        }
                    }
                });
            toolBar.add(editFile);
            // TODO: EXTERNALIZE STRING
            printFile=new JButton("Print");
            // TODO: EXTERNALIZE STRING
            printFile.setMnemonic('p');
            printFile.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        try
                        {
                            desktop.print(currentFile);
                        }
                        catch(Throwable t)
                        {
                            // TODO: HANDLE ERROR MESSAGE
                            showThrowable(t);
                        }
                    }
                });
            toolBar.add(printFile);
            openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
            editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
            printFile.setEnabled(desktop.isSupported(Desktop.Action.PRINT));
            // TODO: EXTERNALIZE STRING
            flags.add(new JLabel("::  Flags"));
            // TODO: EXTERNALIZE STRING
            readable=new JCheckBox("Read  ");
            // TODO: EXTERNALIZE STRING
            readable.setMnemonic('a');
            flags.add(readable);
            // TODO: EXTERNALIZE STRING
            writable=new JCheckBox("Write  ");
            // TODO: EXTERNALIZE STRING
            writable.setMnemonic('w');
            flags.add(writable);
            // TODO: EXTERNALIZE STRING
            executable=new JCheckBox("Execute");
            // TODO: EXTERNALIZE STRING
            executable.setMnemonic('x');
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
            progressBar=new JProgressBar();
            simpleOutput.add(progressBar,
                             BorderLayout.EAST);
            progressBar.setVisible(true);
            progressBar1=new JLabel();
            // TODO: EXTERNALIZE STRING
            if(len==0)
            {
                progressBar1.setText("No files found.");
            }
            else
            {
                if(len==1)
                {
                    progressBar1.setText(" "+
                                         customFormat("###,###,###",
                                                      len)+
                                         " file found.");
                }
                else
                {
                    progressBar1.setText(" "+
                                         customFormat("###,###,###",
                                                      len)+
                                         " files found.");
                }
            }
            simpleOutput.add(progressBar1,
                             BorderLayout.WEST);
            gui.add(simpleOutput,
                    BorderLayout.SOUTH);
        }
        return gui;
    }

    public void showRootFile()
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

    private void showErrorMessage(String errorMessage,
                                  String errorTitle)
    {
        JOptionPane.showMessageDialog(gui,
                                      errorMessage,
                                      errorTitle,
                                      JOptionPane.ERROR_MESSAGE);
    }

    private void showThrowable(Throwable t)
    {
        t.printStackTrace();
        showErrorMessage(t.toString(),
                         t.getMessage());
        gui.repaint();
    }

    private void setTableData(final File[] files)
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

    private void setColumnWidth(int column,
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

    private void showChildren(final DefaultMutableTreeNode node)
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
    private void setFileDetails(File file)
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
        // TODO: EXTERNALIZE STRING
        size.setText(customFormat("###,###.###",
                                  file.length())+
                     " bytes");
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());
        isFile.setSelected(file.isFile());
        JFrame f=(JFrame)gui.getTopLevelAncestor();
        if(f!=null)
        {
            f.setTitle(APP_TITLE+
                       " :: "+
                       fileSystemView.getSystemDisplayName(file));
            f.setTitle(APP_TITLE);
        }
        gui.repaint();
    }

    public static void show(SettingsHandler sh)
    {
        settingsHandler=sh;
        SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        UIManager.setLookAndFeel(Settings.LookAndFeelPackages[settingsHandler.getLookAndFeel()]);
                    }
                    catch(Exception weTried)
                    {
                        // TODO: HANDLE ERROR MESSAGE
                        err("WE TRIED: "+
                            weTried);
                    }
                    JFrame f=new JFrame(APP_TITLE);
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    FileBrowser FileBrowser=new FileBrowser();
                    f.setContentPane(FileBrowser.getGui());
                    try
                    {
                        URL urlSmall=UnDupeKeeper.class.getResource(Settings.iconList[7]);
                        URL urlBig=UnDupeKeeper.class.getResource(Settings.iconList[8]);
                        ArrayList<Image> images=new ArrayList<Image>();
                        images.add((new ImageIcon(urlBig,
                                                  "")).getImage());
                        images.add((new ImageIcon(urlSmall,
                                                  "")).getImage());
                        f.setIconImages(images);
                    }
                    catch(Exception weTried)
                    {
                        // TODO: HANDLE ERROR MESSAGE
                        err("WE TRIED: "+
                            weTried);
                    }
                    f.pack();
                    f.setLocationByPlatform(true);
                    f.setMinimumSize(f.getSize());
                    f.setVisible(true);
                    FileBrowser.showRootFile();
                }
            });
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
