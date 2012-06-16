package tools;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settings.Settings;
import settings.Strings;

// TODO: JAVADOC
/**
 * 
 * @author vliopard
 */
public class Options extends
        JDialog
{
    private static final long serialVersionUID =2964455539250507658L;
    private Point             screenPosition;
    private JLabel            labelDirectoryToWatch;
    private JLabel            labelWarning;
    private JLabel            labelLookAndFeel;
    private JLabel            labelEncryptionMethod;
    private JButton           buttonSave;
    private JButton           buttonCancel;
    private JTextField        directoryInputTextField;
    private JComboBox<String> comboBoxLookAndFeel;
    private JComboBox<String> comboBoxEncryptionMethod;

    /**
     * 
     * @param settingsHandler
     */
    Options(final SettingsHandler settingsHandler)
    {
        this.setLocation(settingsHandler.getX(),
                         settingsHandler.getY());
        UnDupeKeeperSettingsLayout customLayout=new UnDupeKeeperSettingsLayout();
        getContentPane().setFont(new Font("Helvetica",
                                          Font.PLAIN,
                                          12));
        getContentPane().setLayout(customLayout);
        labelDirectoryToWatch=new JLabel(Strings.ssDirectoryLabel);
        getContentPane().add(labelDirectoryToWatch);
        directoryInputTextField=new JTextField(settingsHandler.getDirectory());
        directoryInputTextField.setEditable(false);
        getContentPane().add(directoryInputTextField);
        directoryInputTextField.addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    String newDirectory=DataBase.chooseDir();
                    if(null!=newDirectory)
                    {
                        directoryInputTextField.setText(newDirectory);
                    }
                }
            });
        directoryInputTextField.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    String newDirectory=DataBase.chooseDir();
                    if(null!=newDirectory)
                    {
                        directoryInputTextField.setText(newDirectory);
                    }
                }
            });
        labelLookAndFeel=new JLabel(Strings.ssLookAndFeelLabel);
        getContentPane().add(labelLookAndFeel);
        comboBoxLookAndFeel=new JComboBox<String>();
        for(int i=0; i<9; i++)
        {
            comboBoxLookAndFeel.addItem(Settings.LookAndFeelNames[i]);
        }
        comboBoxLookAndFeel.setSelectedIndex(settingsHandler.getLookAndFeel());
        getContentPane().add(comboBoxLookAndFeel);
        labelEncryptionMethod=new JLabel(Strings.ssEncryptionMethodLabel);
        getContentPane().add(labelEncryptionMethod);
        comboBoxEncryptionMethod=new JComboBox<String>();
        for(int i=0; i<9; i++)
        {
            comboBoxEncryptionMethod.addItem(Settings.CypherMethodList[i]);
        }
        comboBoxEncryptionMethod.setSelectedIndex(settingsHandler.getEncryptionMethod());
        getContentPane().add(comboBoxEncryptionMethod);
        buttonSave=new JButton(Strings.ssSaveButton);
        buttonSave.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    settingsHandler.setDirectory(directoryInputTextField.getText());
                    settingsHandler.setLookAndFeel(comboBoxLookAndFeel.getSelectedIndex());
                    settingsHandler.setEncryptionMethod(comboBoxEncryptionMethod.getSelectedIndex());
                    screenPosition=getLocationOnScreen();
                    settingsHandler.setX(screenPosition.x);
                    settingsHandler.setY(screenPosition.y);
                    settingsHandler.setChanged(true);
                    dispose();
                }
            });
        getContentPane().add(buttonSave);
        buttonCancel=new JButton(Strings.ssCancelButton);
        buttonCancel.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    screenPosition=getLocationOnScreen();
                    settingsHandler.setX(screenPosition.x);
                    settingsHandler.setY(screenPosition.y);
                    settingsHandler.setChanged(false);
                    dispose();
                }
            });
        getContentPane().add(buttonCancel);
        labelWarning=new JLabel(Strings.ssWarningLabel);
        labelWarning.setFont(new Font("Helvetica",
                                      Font.BOLD,
                                      12));
        getContentPane().add(labelWarning);
        setSize(getPreferredSize());
        addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    settingsHandler.setChanged(false);
                    dispose();
                }
            });
    }
}

/**
 * 
 * @author vliopard
 */
class UnDupeKeeperSettingsLayout implements
                                LayoutManager
{
    /**
     * 
     */
    public UnDupeKeeperSettingsLayout()
    {
    }

    /**
     * 
     */
    public void addLayoutComponent(String name,
                                   Component comp)
    {
    }

    /**
     * 
     */
    public void removeLayoutComponent(Component comp)
    {
    }

    /**
     * 
     */
    public Dimension preferredLayoutSize(Container parent)
    {
        Dimension dim=new Dimension(0,
                                    0);
        Insets insets=parent.getInsets();
        dim.width=320+
                  insets.left+
                  insets.right;
        dim.height=130+
                   insets.top+
                   insets.bottom;
        return dim;
    }

    /**
     * 
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        Dimension dim=new Dimension(0,
                                    0);
        return dim;
    }

    /**
     * 
     */
    public void layoutContainer(Container parent)
    {
        Insets insets=parent.getInsets();
        Component c;
        c=parent.getComponent(0);
        if(c.isVisible())
        {
            c.setBounds(insets.left+8,
                        insets.top+8,
                        152,
                        24);
        }
        c=parent.getComponent(1);
        if(c.isVisible())
        {
            c.setBounds(insets.left+160,
                        insets.top+8,
                        152,
                        24);
        }
        c=parent.getComponent(2);
        if(c.isVisible())
        {
            c.setBounds(insets.left+8,
                        insets.top+32,
                        152,
                        24);
        }
        c=parent.getComponent(3);
        if(c.isVisible())
        {
            c.setBounds(insets.left+160,
                        insets.top+32,
                        152,
                        24);
        }
        c=parent.getComponent(4);
        if(c.isVisible())
        {
            c.setBounds(insets.left+8,
                        insets.top+56,
                        152,
                        24);
        }
        c=parent.getComponent(5);
        if(c.isVisible())
        {
            c.setBounds(insets.left+160,
                        insets.top+56,
                        152,
                        24);
        }
        c=parent.getComponent(6);
        if(c.isVisible())
        {
            c.setBounds(insets.left+8,
                        insets.top+83,
                        152,
                        30);
        }
        c=parent.getComponent(7);
        if(c.isVisible())
        {
            c.setBounds(insets.left+160,
                        insets.top+83,
                        152,
                        30);
        }
        c=parent.getComponent(8);
        if(c.isVisible())
        {
            c.setBounds(insets.left+8,
                        insets.top+110,
                        300,
                        24);
        }
    }
}
