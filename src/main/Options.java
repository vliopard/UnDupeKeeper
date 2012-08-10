package main;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import settings.Settings;
import settings.Strings;
import tools.DataBase;
import tools.SettingsHandler;

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
    private JLabel            labelLanguage;
    private JLabel            labelEncryptionMethod;
    private JLabel            labelComparisonMethod;
    private JButton           buttonSave;
    private JButton           buttonCancel;
    private JTextField        directoryInputTextField;
    private JComboBox<String> comboBoxLookAndFeel;
    private JComboBox<String> comboBoxLanguage;
    private JComboBox<String> comboBoxEncryptionMethod;
    private JCheckBox         checkBoxComparisonMethod;

    /**
     * Options Constructor - Starts all components of the GUI and display it.
     * 
     * @param settingsHandler
     *            A <code>SettingsHandler</code> object that contains all the
     *            user settings to be handled.
     */
    public Options(final SettingsHandler settingsHandler)
    {
        this.setLocation(settingsHandler.getX(),
                         settingsHandler.getY());
        OptionsLayout customLayout=new OptionsLayout();
        getContentPane().setFont(new Font("Helvetica",
                                          Font.PLAIN,
                                          12));
        getContentPane().setLayout(customLayout);
        // DIRECTORY LABEL (0)
        labelDirectoryToWatch=new JLabel(Strings.ssDirectoryLabel);
        getContentPane().add(labelDirectoryToWatch);
        // DIRECTORY TEXT FIELD (1)
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
        // LOOK AND FEEL LABEL (2)
        labelLookAndFeel=new JLabel(Strings.ssLookAndFeelLabel);
        getContentPane().add(labelLookAndFeel);
        // LOOK AND FEEL COMBO BOX (3)
        comboBoxLookAndFeel=new JComboBox<String>();
        for(int i=0; i<9; i++)
        {
            comboBoxLookAndFeel.addItem(Settings.LookAndFeelNames[i]);
        }
        comboBoxLookAndFeel.setSelectedIndex(settingsHandler.getLookAndFeel());
        getContentPane().add(comboBoxLookAndFeel);
        // ENCRYPTION LABEL (4)
        labelEncryptionMethod=new JLabel(Strings.ssEncryptionMethodLabel);
        getContentPane().add(labelEncryptionMethod);
        // ENCRYPTION COMBO BOX (5)
        comboBoxEncryptionMethod=new JComboBox<String>();
        for(int i=0; i<9; i++)
        {
            comboBoxEncryptionMethod.addItem(Settings.CypherMethodList[i]);
        }
        comboBoxEncryptionMethod.setSelectedIndex(settingsHandler.getEncryptionMethod());
        getContentPane().add(comboBoxEncryptionMethod);
        // LANGUAGE LABEL (6)
        labelLanguage=new JLabel(Strings.ssLanguage);
        getContentPane().add(labelLanguage);
        // LANGUAGE COMBO BOX (7)
        comboBoxLanguage=new JComboBox<String>();
        for(int i=0; i<2; i++)
        {
            comboBoxLanguage.addItem(Settings.languageList[i]);
        }
        comboBoxLanguage.setSelectedIndex(settingsHandler.getLanguageIndex());
        getContentPane().add(comboBoxLanguage);
        // COMPARISON LABEL (8)
        labelComparisonMethod=new JLabel(Strings.opBinaryComparison);
        getContentPane().add(labelComparisonMethod);
        // COMPARISON CHECK BOX (9)
        checkBoxComparisonMethod=new JCheckBox();
        checkBoxComparisonMethod.setSelected(settingsHandler.getComparisonMethod());
        getContentPane().add(checkBoxComparisonMethod);
        // BUTTON SAVE (10)
        buttonSave=new JButton(Strings.ssSaveButton);
        buttonSave.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    settingsHandler.setDirectory(directoryInputTextField.getText());
                    settingsHandler.setLookAndFeel(comboBoxLookAndFeel.getSelectedIndex());
                    settingsHandler.setEncryptionMethod(comboBoxEncryptionMethod.getSelectedIndex());
                    settingsHandler.setComparisonMethod(checkBoxComparisonMethod.isSelected());
                    settingsHandler.setLanguage(comboBoxLanguage.getSelectedIndex());
                    screenPosition=getLocationOnScreen();
                    settingsHandler.setX(screenPosition.x);
                    settingsHandler.setY(screenPosition.y);
                    settingsHandler.setChanged(true);
                    dispose();
                }
            });
        getContentPane().add(buttonSave);
        // BUTTON CANCEL (11)
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
        // WARNING LABEL (12)
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
        dim.height=195+
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
    private void setBounds(int i,
                           int left,
                           int top,
                           int right,
                           int bottom)
    {
        values[i]=new Insets(top,
                             left,
                             bottom,
                             right);
    }

    /**
     * It creates the places where all component objects will be placed on
     * settings dialog.
     */
    public void layoutContainer(Container parent)
    {
        Insets insets=parent.getInsets();
        values=new Insets[13];
        //@formatter:off
        setBounds( 0,  8,  8,152,24);
        setBounds( 1,160,  8,152,24);
        setBounds( 2,  8, 32,152,24);
        setBounds( 3,160, 32,152,24);
        setBounds( 4,  8, 56,152,24);
        setBounds( 5,160, 56,152,24);
        setBounds( 6,  8, 83,152,24);
        setBounds( 7,160, 83,152,24);

        setBounds( 8,  8,110,152,24);
        setBounds( 9,160,110,152,24);       
        
        setBounds(10,  8,137,152,30);
        setBounds(11,160,137,152,30);
        setBounds(12,  8,164,300,24);
        //@formatter:on
        for(int i=0; i<values.length; i++)
        {
            Component component=parent.getComponent(i);
            if(component.isVisible())
            {
                component.setBounds(insets.left+
                                            values[i].left,
                                    insets.top+
                                            values[i].top,
                                    values[i].right,
                                    values[i].bottom);
            }
        }
    }
}
