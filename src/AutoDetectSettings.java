import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Dimension;

class AutoDetectSettings implements SettingsEntry
{
    private boolean removeBG;
    private JFrame frame = new JFrame("Auto-Detect Card Area Settings");
    private boolean initialized = false;

    @Override
    public void init() {
        if(!initialized)
        {
            Dimension d;
            removeBG = (boolean)SavedConfig.getProperty("autodetect.remove_background", false);

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(10, 10, 10, 10));
            JCheckBox rbg = new JCheckBox("Subtract Static Background");
            rbg.setToolTipText(
                "Detect cards based on difference from a static background. "+
                "Use if you have a static, textured background with stable lighting."
            );
            rbg.setSelected(removeBG);
            JButton bSave = new JButton("Save");

            bSave.addActionListener(
                e -> {
                    removeBG = rbg.isSelected();
                    save();
                    frame.setVisible(false);
                }
            );

            p.add(rbg);
            p.add(bSave);
            d = p.getPreferredSize();
            d.width = 200;
            p.setPreferredSize(d);
            frame.setResizable(false);
            frame.add(p);
            frame.pack();
            initialized = true;
        }
    }

    @Override
    public void save() {
        SavedConfig.putProperty("autodetect.remove_background", removeBG);
        SavedConfig.writeOut();
    }

    @Override
    public void showSettingsWindow() {
        frame.setVisible(false);
        frame.setVisible(true);
    }

    public boolean getRemoveBackground()
    {
        return removeBG;
    }
    
}