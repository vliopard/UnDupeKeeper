package tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import settings.Strings;

// TODO: JAVADOC
// TODO: METHOD AND VARIABLE NAMES REFACTORING
public class ProgressBarDialog
{
    private JFrame       parentFrame;
    private JLabel       jl;
    private JProgressBar dpb;
    private JButton      jb;

    public ProgressBarDialog(String title, String message)
    {
        Dimension dim = Toolkit.getDefaultToolkit( ).getScreenSize( );
        parentFrame = new JFrame(title);
        parentFrame.setSize(500, 90);
        jl = new JLabel(message);
        jb = new JButton(Strings.done);
        jb.addActionListener(new ActionListener( )
        {
            public void actionPerformed(ActionEvent ae)
            {
                jb.setEnabled(false);
                parentFrame.setVisible(false);
                parentFrame.dispose( );
                synchronized (parentFrame)
                {
                    parentFrame.notify( );
                }
            }
        });
        jb.setEnabled(false);
        jl.setHorizontalTextPosition(JLabel.CENTER);
        jl.setHorizontalAlignment(JLabel.CENTER);
        jl.setSize(500, 50);
        dpb = new JProgressBar(0, 100);
        parentFrame.add(BorderLayout.NORTH, dpb);
        parentFrame.add(BorderLayout.SOUTH, jl);
        parentFrame.add(BorderLayout.SOUTH, jb);
        parentFrame.setLocation((dim.width - parentFrame.getSize( ).width)
                / 2, (dim.height - parentFrame.getSize( ).height) / 3);
        parentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        parentFrame.setVisible(true);
    }

    public void setTitle(String progressTitle)
    {
        parentFrame.setTitle(progressTitle);
    }

    public void setProgress(int progressValue)
    {
        dpb.setValue(progressValue);
    }

    public void setIndeterminate(boolean indeterminate)
    {
        dpb.setIndeterminate(indeterminate);
    }

    public void setMessage(String message)
    {
        jl.setText(message);
    }

    public String getMessage( )
    {
        return jl.getText( );
    }

    public void setDismiss( )
    {
        jb.setEnabled(true);
        jb.requestFocusInWindow( );
    }

    public void keep( )
    {
        synchronized (parentFrame)
        {
            try
            {
                // TODO: RESEARCH: _SOLVE FINDBUGS ISSUE
                parentFrame.wait( );
            }
            catch (InterruptedException e)
            {
                Logger.err("MSG_057: " + Strings.fatalError + e);
            }
        }
    }
}
