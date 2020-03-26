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

    private JCheckBox remBG;

    public AutoDetectSettings()
    {
        Dimension d;

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        remBG = new JCheckBox("Subtract Static Background");
        remBG.setToolTipText(
            "Detect cards based on difference from a static background. "+
            "Use if you have a static, textured background with stable lighting."
        );

        JButton bSave = new JButton("Save");

        bSave.addActionListener(
            e -> {
                save();
                frame.setVisible(false);
            }
        );

        p.add(remBG);

        p.add(bSave);
        d = p.getPreferredSize();
        p.setPreferredSize(d);
        frame.setResizable(false);
        frame.add(p);
        frame.pack();
    }

    @Override
    public void init() {
        removeBG = (Boolean)SavedConfig.getProperty("autodetect.remove_background", false);
        remBG.setSelected(removeBG);
    }

    @Override
    public void save() {
        removeBG = remBG.isSelected();
        SavedConfig.putProperty("autodetect.remove_background", removeBG);
        SavedConfig.writeOut();
    }

    @Override
    public void showSettingsWindow() {
        init();
        frame.setVisible(false);
        frame.setVisible(true);
    }

    public boolean getRemoveBackground()
    {
        return removeBG;
    }
    
}