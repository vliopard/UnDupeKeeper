package main;

// TODO: RESEARCH: SAVE POSITION AND SCREEN SETTINGS
// TODO: RESEARCH: FIX FILESIZE ORDERING TO ASC/DESC NUMBER (NOT STRING ALPHABET)
// TODO: RESEARCH: ORDERING REPORT VIEWER FILES DESYNCHRONIZE SELECTED FOCUS FROM DETAIL
// TODO: METHOD AND VARIABLE NAMES REFACTORING
// TODO: JAVADOC
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
import settings.Settings;
import settings.Strings;
import tools.FileTableModel;
import tools.FileTreeCellRenderer;
import tools.Logger;
import tools.SettingsHandler;
import tools.TrayImage;
import tools.ReportGenerator;
import tools.Utils;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.URL;

/**
 * @author vliopard
 */
public class ReportViewer
{
    private static Desktop               desktop;
    private static FileSystemView        fileSystemView;
    private static File                  currentFile;
    private static JPanel                guiPanel;
    private static JTree                 mainTree;
    private static DefaultTreeModel      treeModel;
    private static JTable                fileTable;
    private static JProgressBar          progressBar    = new JProgressBar( );
    private static JLabel                statusBar;
    private static FileTableModel        fileTableModel;
    private static ListSelectionListener listSelectionListener;
    private static boolean               cellSizesSet   = false;
    private static int                   rowIconPadding = 1;
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
    private static int                   fileSize;
    private static SettingsHandler       settingsHandler;

    /**
     * @return
     */
    private static Container getGui( )
    {
        if (guiPanel == null)
        {
            guiPanel = new JPanel(new BorderLayout(1, 1));
            guiPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
            fileSystemView = FileSystemView.getFileSystemView( );
            desktop = Desktop.getDesktop( );
            JPanel detailView = new JPanel(new BorderLayout(1, 1));
            fileTable = new JTable( );
            fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fileTable.setAutoCreateRowSorter(true);
            fileTable.setShowVerticalLines(false);
            listSelectionListener = new ListSelectionListener( )
            {
                @Override
                public void valueChanged(ListSelectionEvent lse)
                {
                    int row = fileTable.getSelectionModel( ).getLeadSelectionIndex( );
                    setFileDetails(((FileTableModel) fileTable.getModel( )).getFile(row));
                }
            };
            fileTable.getSelectionModel( ).addListSelectionListener(listSelectionListener);
            JScrollPane tableScroll = new JScrollPane(fileTable);
            Dimension   d           = tableScroll.getPreferredSize( );
            tableScroll.setPreferredSize(new Dimension((int) d.getWidth( ), (int) d.getHeight( ) / 2));
            detailView.add(tableScroll, BorderLayout.CENTER);
            DefaultMutableTreeNode root = new DefaultMutableTreeNode( );
            treeModel = new DefaultTreeModel(root);
            TreeSelectionListener treeSelectionListener = new TreeSelectionListener( )
            {
                public void valueChanged(TreeSelectionEvent tse)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath( ).getLastPathComponent( );
                    showChildren(node);
                    setFileDetails(new File((String) node.getUserObject( )));
                }
            };
            root.add(ReportGenerator.getRootNode(settingsHandler.getDirectory( )));
            fileSize = ReportGenerator.size( );
            mainTree = new JTree(treeModel);
            mainTree.setRootVisible(false);
            mainTree.addTreeSelectionListener(treeSelectionListener);
            mainTree.setCellRenderer(new FileTreeCellRenderer( ));
            // tree.expandRow(0);
            JScrollPane treeScroll = new JScrollPane(mainTree);
            mainTree.setVisibleRowCount(10);
            mainTree.addTreeWillExpandListener(new TreeWillExpandListener( )
            {
                public void treeWillExpand(TreeExpansionEvent e)
                {
                    // Utils.displayBallon("Warning","This action may take a while...",MessageType.WARNING);
                }

                @Override
                public void treeWillCollapse(TreeExpansionEvent arg0) throws ExpandVetoException
                {
                }
            });
            mainTree.addTreeExpansionListener(new TreeExpansionListener( )
            {
                public void treeExpanded(TreeExpansionEvent e)
                {
                    // Utils.displayBallon("Tree Expansion","Done!",MessageType.INFO);
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent arg0)
                {
                }
            });
            Dimension preferredSize = treeScroll.getPreferredSize( );
            Dimension widePreferred = new Dimension(350, (int) preferredSize.getHeight( ));
            treeScroll.setPreferredSize(widePreferred);
            JPanel fileMainDetails = new JPanel(new BorderLayout(4, 2));
            fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));
            JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
            fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);
            JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
            fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);
            fileDetailsLabels.add(new JLabel(Strings.fbFile, JLabel.TRAILING));
            fileName = new JLabel( );
            fileDetailsValues.add(fileName);
            fileDetailsLabels.add(new JLabel(Strings.fbPathName, JLabel.TRAILING));
            path = new JTextField( );
            path.setEditable(false);
            fileDetailsValues.add(path);
            fileDetailsLabels.add(new JLabel(Strings.fbDate, JLabel.TRAILING));
            date = new JLabel( );
            fileDetailsValues.add(date);
            fileDetailsLabels.add(new JLabel(Strings.fbFileSize, JLabel.TRAILING));
            size = new JLabel( );
            fileDetailsValues.add(size);
            fileDetailsLabels.add(new JLabel(Strings.fbType, JLabel.TRAILING));
            JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
            isDirectory = new JRadioButton(Strings.fbDirectory);
            flags.add(isDirectory);
            isFile = new JRadioButton(Strings.fileName);
            flags.add(isFile);
            fileDetailsValues.add(flags);
            JToolBar toolBar = new JToolBar( );
            toolBar.setFloatable(false);
            locateFile = new JButton(Strings.btA);
            locateFile.setMnemonic(Strings.btAShort);
            locateFile.addActionListener(new ActionListener( )
            {
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        Logger.msg(Strings.btAMessage + currentFile.getParentFile( ));
                        desktop.open(currentFile.getParentFile( ));
                    }
                    catch (Throwable e)
                    {
                        showThrowable("MSG_025: " + Strings.btAError, Strings.btA, e);
                    }
                    guiPanel.repaint( );
                }
            });
            toolBar.add(locateFile);
            openFile = new JButton(Strings.btB);
            openFile.setMnemonic(Strings.btBShort);
            openFile.addActionListener(new ActionListener( )
            {
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        Logger.msg(Strings.btBMessage + currentFile);
                        desktop.open(currentFile);
                    }
                    catch (Throwable e)
                    {
                        showThrowable("MSG_026: " + Strings.btBError, Strings.btB, e);
                    }
                    guiPanel.repaint( );
                }
            });
            toolBar.add(openFile);
            editFile = new JButton(Strings.btC);
            editFile.setMnemonic(Strings.btCShort);
            editFile.addActionListener(new ActionListener( )
            {
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        Logger.msg(Strings.btCMessage + currentFile);
                        desktop.edit(currentFile);
                    }
                    catch (Throwable e)
                    {
                        showThrowable("MSG_027: " + Strings.btCError, Strings.btC, e);
                    }
                }
            });
            toolBar.add(editFile);
            deleteFile = new JButton(Strings.btD);
            deleteFile.setMnemonic(Strings.btDShort);
            deleteFile.addActionListener(new ActionListener( )
            {
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        Logger.msg(Strings.btDMessage + currentFile);
                        // TODO: RESEARCH: LOOK FOR A WAY TO DELETE FILE
                        // TODO: RESEARCH: AFTER DELETE FILE, REFRESH GUI FILE TREE
                        currentFile.setReadable(true);
                        currentFile.setWritable(true);
                        if (currentFile.delete( ))
                        {
                            showErrorMessage(Strings.btDError, Strings.btD);
                        }
                    }
                    catch (Throwable e)
                    {
                        showThrowable("MSG_028: " + Strings.btDError, Strings.btD, e);
                    }
                }
            });
            toolBar.add(deleteFile);
            locateFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
            openFile.setEnabled(desktop.isSupported(Desktop.Action.OPEN));
            editFile.setEnabled(desktop.isSupported(Desktop.Action.EDIT));
            deleteFile.setEnabled(true);
            flags.add(new JLabel(Strings.pipe + Strings.space + Strings.space + Strings.flags));
            readable = new JCheckBox(Strings.btRead);
            readable.setMnemonic(Strings.btReadShort);
            flags.add(readable);
            writable = new JCheckBox(Strings.btWrite);
            writable.setMnemonic(Strings.btWriteShort);
            flags.add(writable);
            executable = new JCheckBox(Strings.btExecute);
            executable.setMnemonic(Strings.btExecuteShort);
            flags.add(executable);
            int count = fileDetailsLabels.getComponentCount( );
            for (int i = 0; i < count; i++)
            {
                fileDetailsLabels.getComponent(i).setEnabled(false);
            }
            count = flags.getComponentCount( );
            for (int i = 0; i < count; i++)
            {
                flags.getComponent(i).setEnabled(false);
            }
            JPanel fileView = new JPanel(new BorderLayout(3, 3));
            fileView.add(toolBar, BorderLayout.NORTH);
            fileView.add(fileMainDetails, BorderLayout.CENTER);
            detailView.add(fileView, BorderLayout.SOUTH);
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, detailView);
            guiPanel.add(splitPane, BorderLayout.CENTER);
            JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
            // progressBar=new JProgressBar();
            simpleOutput.add(progressBar, BorderLayout.EAST);
            progressBar.setVisible(true);
            statusBar = new JLabel( );
            if (fileSize == 0)
            {
                statusBar.setText(Strings.noFileFound);
            }
            else
            {
                if (fileSize == 1)
                {
                    statusBar.setText(Strings.space + Utils.numberFormat(fileSize) + Strings.fileFound);
                }
                else
                {
                    statusBar.setText(Strings.space + Utils.numberFormat(fileSize) + Strings.filesFound);
                }
            }
            simpleOutput.add(statusBar, BorderLayout.WEST);
            guiPanel.add(simpleOutput, BorderLayout.SOUTH);
        }
        return guiPanel;
    }

    /**
     * 
     */
    private static void showRootFile( )
    {
        mainTree.setSelectionInterval(0, 0);
    }

    @SuppressWarnings("unused")
    private TreePath findTreePath(File find)
    {
        for (int ii = 0; ii < mainTree.getRowCount( ); ii++)
        {
            TreePath               treePath = mainTree.getPathForRow(ii);
            Object                 object   = treePath.getLastPathComponent( );
            DefaultMutableTreeNode node     = (DefaultMutableTreeNode) object;
            File                   nodeFile = (File) node.getUserObject( );
            if (nodeFile == find)
            {
                return treePath;
            }
        }
        return null;
    }

    private static void showErrorMessage(String errorMessage, String errorTitle)
    {
        JOptionPane.showMessageDialog(guiPanel, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
    }

    private static void showThrowable(String message, String title, Throwable throwable)
    {
        Logger.err("[" + title + "] " + message + Settings.Blank + throwable.getMessage( ));
        showErrorMessage(message, title);
        guiPanel.repaint( );
    }

    private static void setTableData(final File[ ] files)
    {
        SwingUtilities.invokeLater(new Runnable( )
        {
            public void run( )
            {
                if (fileTableModel == null)
                {
                    fileTableModel = new FileTableModel( );
                    fileTable.setModel(fileTableModel);
                }
                fileTable.getSelectionModel( ).removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                fileTable.getSelectionModel( ).addListSelectionListener(listSelectionListener);
                if ( ! cellSizesSet)
                {
                    Icon icon = fileSystemView.getSystemIcon(files[0]);
                    fileTable.setRowHeight(icon.getIconHeight( ) + rowIconPadding);
                    setColumnWidth(0, -1);
                    setColumnWidth(4, 60);
                    fileTable.getColumnModel( ).getColumn(4).setMaxWidth(255);
                    setColumnWidth(5, -1);
                    setColumnWidth(6, -1);
                    setColumnWidth(7, -1);
                    setColumnWidth(8, -1);
                    setColumnWidth(9, -1);
                    setColumnWidth(10, -1);
                    cellSizesSet = true;
                }
            }
        });
    }

    private static void setColumnWidth(int column, int width)
    {
        TableColumn tableColumn = fileTable.getColumnModel( ).getColumn(column);
        if (width < 0)
        {
            JLabel    label     = new JLabel((String) tableColumn.getHeaderValue( ));
            Dimension preferred = label.getPreferredSize( );
            width = (int) preferred.getWidth( ) + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    private static void showChildren(final DefaultMutableTreeNode node)
    {
        mainTree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        SwingWorker <Void, File> worker = new SwingWorker <Void, File>( )
        {
            @Override
            public Void doInBackground( )
            {
                File file = new File((String) node.getUserObject( ));
                if (file.getParentFile( ).isDirectory( ))
                {
                    File[ ] files = fileSystemView.getFiles(file.getParentFile( ), true);
                    if (node.isLeaf( ))
                    {
                        for (File child:files)
                        {
                            if (child.isDirectory( ))
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
            protected void process(List <File> chunks)
            {
                for (File child:chunks)
                {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }

            @Override
            protected void done( )
            {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(true);
                mainTree.setEnabled(true);
            }
        };
        worker.execute( );
    }

    /** Update the File details view with the details of this File. */
    private static void setFileDetails(File file)
    {
        currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);
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
        path.setText(file.getPath( ));
        date.setText(new Date(file.lastModified( )).toString( ));
        size.setText(Utils.numberFormat(file.length( )) + Strings.fileBytes);
        readable.setSelected(file.canRead( ));
        writable.setSelected(file.canWrite( ));
        executable.setSelected(file.canExecute( ));
        isDirectory.setSelected(file.isDirectory( ));
        isFile.setSelected(file.isFile( ));
        JFrame localFrame = (JFrame) guiPanel.getTopLevelAncestor( );
        if (localFrame != null)
        {
            localFrame.setTitle(Strings.fbTitleCheck +
                    Strings.space +
                    Strings.separator +
                    Strings.space +
                    fileSystemView.getSystemDisplayName(file));
        }
        guiPanel.repaint( );
    }

    public static JFrame show(SettingsHandler settings)
    {
        settingsHandler = settings;
        // SwingUtilities.invokeLater(new Runnable()
        // {
        // public void run()
        // {
        try
        {
            UIManager.setLookAndFeel(Settings.LookAndFeelPackages[settingsHandler.getLookAndFeel( )]);
        }
        catch (Exception e)
        {
            Logger.err("MSG_029: " + Strings.fbErrorLoadingLookAndFeel + e);
        }
        JFrame mainFrame = new JFrame(Strings.fbTitleCheck);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // ReportViewer FileBrowser=new ReportViewer();
        mainFrame.setContentPane(ReportViewer.getGui( ));
        try
        {
            ArrayList <Image> imageArray   = new ArrayList <Image>( );
            URL               smallIconUrl = UnDupeKeeper.class.getResource(Settings.iconList[8]);
            if (smallIconUrl == null)
            {
                Logger.err(Strings.ukResourceNotFound + Settings.iconList[8]);
                imageArray.add(TrayImage.createNewImage( ));
            }
            else
            {
                imageArray.add((new ImageIcon(smallIconUrl, "")).getImage( ));
            }
            URL bigIconUrl = UnDupeKeeper.class.getResource(Settings.iconList[7]);
            if (bigIconUrl == null)
            {
                Logger.err(Strings.ukResourceNotFound + Settings.iconList[7]);
                imageArray.add(TrayImage.createNewImage( ));
            }
            else
            {
                imageArray.add((new ImageIcon(bigIconUrl, "")).getImage( ));
            }
            mainFrame.setIconImages(imageArray);
        }
        catch (Exception e)
        {
            Logger.err("MSG_030: " + Strings.fbErrorLoadingIcons + e);
        }
        mainFrame.pack( );
        // f.setLocationByPlatform(true);
        mainFrame.setMinimumSize(mainFrame.getSize( ));
        mainFrame.setVisible(true);
        ReportViewer.showRootFile( );
        return mainFrame;
        // }
        // });
    }
}
