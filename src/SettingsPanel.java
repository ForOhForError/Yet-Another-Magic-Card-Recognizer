import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SettingsPanel extends JPanel{
	private static final long serialVersionUID = 1L;

	public static boolean RECOG_EVERY_FRAME = true;

	public static int RECOG_THRESH = 40;

	private JComboBox<RecognitionStrategy> stratSelect;
	private JComboBox<AreaRecognitionStrategy> areaStratSelect;

	public SettingsPanel()
	{
		super();
		Dimension d;

		setLayout(new FlowLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setPreferredSize(new Dimension(700,280));

		JCheckBox recog = new JCheckBox("Only trigger recognition manually",!RECOG_EVERY_FRAME);
		recog.setToolTipText("Only trigger recognition manually");
		recog.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				RECOG_EVERY_FRAME = !recog.isSelected();
			}
		});
		add(recog);

		JSlider thresh = new JSlider(JSlider.HORIZONTAL, 0, 100, RECOG_THRESH);
		JLabel score = new JLabel("Score Threshold: "+RECOG_THRESH);
		thresh.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				RECOG_THRESH = thresh.getValue();
				score.setText("Score Threshold: "+RECOG_THRESH);
			}
		});
		add(score);
		add(thresh);

		areaStratSelect = new JComboBox<AreaRecognitionStrategy>(StrategySelect.getAreaStrats());
		d = areaStratSelect.getPreferredSize();
		d.width = 230;
		areaStratSelect.setPreferredSize(d);
		areaStratSelect.setSelectedItem(SavedConfig.getAreaStrat());
		areaStratSelect.addActionListener(new ActionListener()
		{
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				JComboBox<AreaRecognitionStrategy> cb = 
					(JComboBox<AreaRecognitionStrategy>)e.getSource();
				RecogApp.INSTANCE.doSetAreaStrat(cb.getItemAt(cb.getSelectedIndex()));
			}
		});
		add(areaStratSelect);

		JButton areaStratSettings = new JButton("Settings");
		d = areaStratSettings.getPreferredSize();
		d.width = 30;
		areaStratSettings.setPreferredSize(d);
		areaStratSettings.addActionListener(e -> {
			SettingsEntry ent = RecogApp.INSTANCE.getAreaStrategy().getSettingsEntry();
			if(ent != null)
			{
				ent.showSettingsWindow();
			}
		});
		add(areaStratSettings);



		stratSelect = new JComboBox<RecognitionStrategy>(StrategySelect.getStrats());
		d = stratSelect.getPreferredSize();
		d.width = 280;
		stratSelect.setPreferredSize(d);
		stratSelect.setSelectedItem(SavedConfig.getStrat());
		stratSelect.addActionListener(new ActionListener()
		{
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				JComboBox<RecognitionStrategy> cb = 
					(JComboBox<RecognitionStrategy>)e.getSource();
				RecogApp.INSTANCE.doSetStrat(cb.getItemAt(cb.getSelectedIndex()));
			}
		});
		add(stratSelect);

		JButton selectCam = new JButton("Reselect webcam");
		d = selectCam.getPreferredSize();
		d.width = 130;
		selectCam.setPreferredSize(d);
		selectCam.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				RecogApp.INSTANCE.doSetWebcam();
			}
		});
		add(selectCam);

		JButton launchPopout = new JButton("Card Preview");
		launchPopout.setPreferredSize(d);
		launchPopout.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new PopoutCardWindow();
			}
		});
		add(launchPopout);
		
		JButton loadSelected = new JButton("Load Selected");
		loadSelected.setPreferredSize(d);
		loadSelected.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				RecogApp.INSTANCE.getLoader().loadSelected();
			}
		});
		add(loadSelected);
		
		JButton unloadAll = new JButton("Unload all");
		unloadAll.setPreferredSize(d);
		unloadAll.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				RecogApp.INSTANCE.getLoader().unloadAll();
			}
		});
		add(unloadAll);
		
		JButton launchSetGen = new JButton("Bulk Generate Sets");
		launchSetGen.setPreferredSize(d);
		launchSetGen.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SetGenerator.bulkGenSets();
			}
		});
		add(launchSetGen);
		
		JButton launchDeckGen = new JButton("Deck Generator");
		launchDeckGen.setPreferredSize(d);
		launchDeckGen.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new DeckGenerator();
			}
		});
		add(launchDeckGen);

		JButton launchCustomGen = new JButton("Create Custom Set");
		launchCustomGen.setPreferredSize(d);
		launchCustomGen.addActionListener(e -> {
			new CustomSetGenerator();
		});
		add(launchCustomGen);
		
		JButton toggleSetPanel = new JButton("Refresh Set Listing");
		toggleSetPanel.setPreferredSize(d);
		toggleSetPanel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				RecogApp.INSTANCE.getLoader().refresh();
			}
		});
		add(toggleSetPanel);

		JButton showCollectionManager = new JButton("Collection Manager");
		showCollectionManager.setPreferredSize(d);
		showCollectionManager.addActionListener(
			e -> RecogApp.INSTANCE.getCollectionWindow().setVisible(true)
		);
		add(showCollectionManager);

		JButton resetBackground = new JButton("Set Key Background");
		resetBackground.setPreferredSize(d);
		resetBackground.addActionListener(
			e -> RecogApp.INSTANCE.doSetBackground()
		);
		add(resetBackground);
	}

	public void resetStratSelector(RecognitionStrategy strat)
	{
		stratSelect.setSelectedItem(strat);
	}

	public void resetAreaStratSelector(AreaRecognitionStrategy strat)
	{
		areaStratSelect.setSelectedItem(strat);
	}
}
