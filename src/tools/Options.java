package tools;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settings.Settings;
import settings.Strings;

/**
 * Options class is responsible for showing settings GUI.
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
     * Options Constructor - Starts all components of the GUI and display it.
     * 
     * @param settingsHandler
     *            A <code>SettingsHandler</code> object that contains all the
     *            user settings to be handled.
     */
    Options(final SettingsHandler settingsHandler)
    {
        this.setLocation(settingsHandler.getX(),
                         settingsHandler.getY());
        OptionsLayout customLayout=new OptionsLayout();
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
 * OptionsLayout class is the LayoutManager for Options class.
 * 
 * @author vliopard
 */
class OptionsLayout implements
                   LayoutManager
{
    private Insets[] values;

    /**
     * OptionsLayout Constructor - It is empty since no action should be taken
     * for this part of code.
     */
    public OptionsLayout()
    {
    }

    /**
     * Just an inherited method from LayoutManager, not used in this project
     * yet.
     */
    public void addLayoutComponent(String name,
                                   Component comp)
    {
    }

    /**
     * Just an inherited method from LayoutManager, not used in this project
     * yet.
     */
    public void removeLayoutComponent(Component comp)
    {
    }

    /**
     * This method sets the settings dialog default size.
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
     * This method sets the minimum settings dialog size.
     */
    public Dimension minimumLayoutSize(Container parent)
    {
        Dimension dim=new Dimension(0,
                                    0);
        return dim;
    }

    /**
     * This method sets an array of <code>Insets</code> containing the
     * components sizes of the settings screen.
     * 
     * @param i
     *            An <code>int</code> value that represents the order the
     *            component is placed.
     * @param left
     *            An <code>int</code> value that represents the left position of
     *            the component.
     * @param top
     *            An <code>int</code> value that represents the top position of
     *            the component.
     * @param right
     *            An <code>int</code> value that represents the right position
     *            of the component.
     * @param bottom
     *            An <code>int</code> value that represents the bottom position
     *            of the component.
     */
    private void setBouts(int i,
                          int left,
                          int top,
                          int right,
                          int bottom)
    {
        values[i].left=left;
        values[i].top=top;
        values[i].right=right;
        values[i].bottom=bottom;
    }

    /**
     * It creates the places where all component objects will be placed on
     * settings dialog.
     */
    public void layoutContainer(Container parent)
    {
        Insets insets=parent.getInsets();
        Insets[] values=new Insets[9];
        //@formatter:off
        setBouts(0,insets.left+8  ,insets.top+8  ,152,24);
        setBouts(1,insets.left+160,insets.top+8  ,152,24);
        setBouts(2,insets.left+8  ,insets.top+32 ,152,24);
        setBouts(3,insets.left+160,insets.top+32 ,152,24);
        setBouts(4,insets.left+8  ,insets.top+56 ,152,24);
        setBouts(5,insets.left+160,insets.top+56 ,152,24);
        setBouts(6,insets.left+8  ,insets.top+83 ,152,30);
        setBouts(7,insets.left+160,insets.top+83 ,152,30);
        setBouts(8,insets.left+8  ,insets.top+110,300,24);
        //@formatter:on
        for(int i=0; i<values.length; i++)
        {
            Component component=parent.getComponent(i);
            if(component.isVisible())
            {
                component.setBounds(values[i].left,
                                    values[i].top,
                                    values[i].right,
                                    values[i].bottom);
            }
        }
    }
}
