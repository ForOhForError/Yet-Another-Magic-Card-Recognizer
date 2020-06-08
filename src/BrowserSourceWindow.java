import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.text.NumberFormatter;

class BrowserSourceWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    private BrowserSourceServer server;

    public BrowserSourceWindow()
    {
        super("Browser Source");
        server = new BrowserSourceServer();

        JPanel top = new JPanel();
        top.setBorder(new EmptyBorder(5, 5, 5, 5));
        top.setLayout(new FlowLayout());
		top.setPreferredSize(new Dimension(300,50));

        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(65535);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        JFormattedTextField portField = new JFormattedTextField(formatter);
        portField.setValue(7777);

        JButton start = new JButton("Start Server");
        start.addActionListener(e -> {
            server.start("localhost", (Integer)portField.getValue());
        });

        JButton stop = new JButton("Stop Server");
        stop.addActionListener(e -> {
            server.stop();
        });

        top.add(portField);
        top.add(start);
        top.add(stop);
        add(top);
        pack();
        setResizable(false);
    }

    public BrowserSourceServer getServer()
    {
        return server;
    }
}