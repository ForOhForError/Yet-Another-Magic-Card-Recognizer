import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.text.NumberFormatter;

import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

class BrowserSourceWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    private BrowserSourceServer server;

    public BrowserSourceWindow()
    {
        super("Browser Source");
        Dimension d;
        server = new BrowserSourceServer();

        JPanel top = new JPanel();
        top.setBorder(new EmptyBorder(5, 5, 5, 5));
        top.setLayout(new FlowLayout());
		top.setPreferredSize(new Dimension(300,100));

        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(65535);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        JFormattedTextField portField = new JFormattedTextField(formatter);
        portField.setValue(((Long)SavedConfig.getProperty("browser_source.port", 7777)).intValue());
        d = portField.getPreferredSize();
        d.width = 40;
        portField.setPreferredSize(d);

        JTextField addressField = new JTextField(10);
        addressField.setText((String)SavedConfig.getProperty("browser_source.address", "localhost"));

        JButton start = new JButton("Start Server");
        JButton stop = new JButton("Stop Server");
        d = start.getPreferredSize();
        d.width = 96;
        start.setPreferredSize(d);
        stop.setPreferredSize(d);

        JButton copy = new JButton("Copy Source Address to Clipboard");

        stop.setEnabled(false);


        start.addActionListener(e -> {
            Integer port = (Integer)portField.getValue();
            String addr = addressField.getText();
            SavedConfig.putProperty("browser_source.port", port);
            SavedConfig.putProperty("browser_source.address", addr);
            SavedConfig.writeOut();
            if(server.start(addr, port))
            {
                start.setEnabled(false);
                portField.setEnabled(false);
                addressField.setEnabled(false);
                stop.setEnabled(true);
            }
        });

        stop.addActionListener(e -> {
            server.stop();
            start.setEnabled(true);
            portField.setEnabled(true);
            addressField.setEnabled(true);
            stop.setEnabled(false);
        });

        copy.addActionListener(e -> {
            Integer port = (Integer)portField.getValue();
            String addr = addressField.getText();
            setClipboard(String.format("http://%s:%d/card-view.html", addr, port));
        });

        top.add(new JLabel("Browser source address:"));
        top.add(addressField);
        top.add(new JLabel(":"));
        top.add(portField);
        top.add(start);
        top.add(stop);
        top.add(copy);

        add(top);
        pack();
        setResizable(false);
    }

    public BrowserSourceServer getServer()
    {
        return server;
    }

    private void setClipboard(String text)
    {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}