package demos;

import implementations.GraphCreatorPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GraphCreatorApplet extends JApplet
implements ChangeListener, ItemListener {

	GraphCreatorPanel graphCreatorPanel;
	JComboBox fonts, styles;
	JSpinner sizes;
	String fontChoice = "Dialog";
	int styleChoice = 0;
	int sizeChoice = 12;

	public void init() {

		try {
			String cn = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(cn);
		} catch (Exception cnf) {
		}

		JPanel fontSelectorPanel = new JPanel();

		fontSelectorPanel.add(new JLabel("Font family:"));

		GraphicsEnvironment gEnv =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
		fonts = new JComboBox(gEnv.getAvailableFontFamilyNames());
		fonts.setSelectedItem(fontChoice);
		fonts.setMaximumRowCount(5);
		fonts.addItemListener(this);
		fontSelectorPanel.add(fonts);

		fontSelectorPanel.add(new JLabel("Style:"));

		String[] styleNames = {"Plain", "Bold", "Italic", "Bold Italic"};
		styles = new JComboBox(styleNames);
		styles.addItemListener(this);
		fontSelectorPanel.add(styles);

		fontSelectorPanel.add(new JLabel("Size:"));

		sizes = new JSpinner(new SpinnerNumberModel(12, 6, 24, 1));
		sizes.addChangeListener(this);
		fontSelectorPanel.add(sizes);

		graphCreatorPanel = new GraphCreatorPanel();
		graphCreatorPanel.setFont(new Font(fontChoice, styleChoice, sizeChoice));
		graphCreatorPanel.setBackground(Color.white);

		add(BorderLayout.NORTH, fontSelectorPanel);
		add(BorderLayout.CENTER, graphCreatorPanel);
	}

	/*
	 * Detect a state change in any of the settings and create a new
	 * Font with the corresponding settings. Set it on the test component.
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) {
			return;
		}
		if (e.getSource() == fonts) {
			fontChoice = (String)fonts.getSelectedItem();
		} else {
			styleChoice = styles.getSelectedIndex();
		}
		graphCreatorPanel.setFont(new Font(fontChoice, styleChoice, sizeChoice));
	}

	public void stateChanged(ChangeEvent e) {
		try {
			String size = sizes.getModel().getValue().toString();
			sizeChoice = Integer.parseInt(size);
			graphCreatorPanel.setFont(new Font(fontChoice,styleChoice,sizeChoice));
		} catch (NumberFormatException nfe) {
		}
	}

	public static void main(String s[]) {

		JFrame f = new JFrame("Graph generation test");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		JApplet graphCreatorApplet = new GraphCreatorApplet();
		f.add(graphCreatorApplet, BorderLayout.CENTER);
		graphCreatorApplet.init();
		f.pack();
		f.setVisible(true);
	}

}