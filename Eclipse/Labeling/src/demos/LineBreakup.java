package demos;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/*
 * This applet displays a String with the user's selected 
 * fontname, style and size attributes.
*/

public class LineBreakup extends JApplet
    implements ChangeListener, ItemListener {

    LineTextTestPanel lineTextTestPanel;
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

        lineTextTestPanel = new LineTextTestPanel();
        lineTextTestPanel.setFont(new Font(fontChoice, styleChoice, sizeChoice));
        lineTextTestPanel.setBackground(Color.white);

        add(BorderLayout.NORTH, fontSelectorPanel);
        add(BorderLayout.CENTER, lineTextTestPanel);
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
        lineTextTestPanel.setFont(new Font(fontChoice, styleChoice, sizeChoice));
    }

    public void stateChanged(ChangeEvent e) {
        try {
            String size = sizes.getModel().getValue().toString();
            sizeChoice = Integer.parseInt(size);
            lineTextTestPanel.setFont(new Font(fontChoice,styleChoice,sizeChoice));
        } catch (NumberFormatException nfe) {
        }
    }

    public static void main(String s[]) {

        JFrame f = new JFrame("Linebreak test");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JApplet lineBreakup = new LineBreakup();
        f.add(lineBreakup, BorderLayout.CENTER);
        lineBreakup.init();
        f.pack();
        f.setVisible(true);
    }

}


class LineTextTestPanel extends JComponent {

    public Dimension getPreferredSize() {
        return new Dimension(500,200);
    }

    public void setFont(Font font) {
        super.setFont(font);
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);
        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        String text = "Many people believe that Vincent van Gogh painted his best works " +
            "during the two-year period he spent in Provence. Here is where he " +
            "painted The Starry Night--which some consider to be his greatest " +
            "work of all. However, as his artistic brilliance reached new " +
            "heights in Provence, his physical and mental health plummeted.";
        
        String words[]=text.split(" ");
        
        int rightCutoff=(int) (getWidth()*0.85);
        int leftCutoff=(int) (getWidth()*0.1);
        int lineHeight=metrics.getHeight()+2;
        int x = leftCutoff;
        int y = metrics.getAscent()+2;
        
        //g.drawRect(x-1,y-metrics.getAscent(),metrics.stringWidth(text)+1,metrics.getHeight());
        //g.setColor(Color.RED);
        
        for(int i=0;i<words.length; i++)
        {
        	if((metrics.stringWidth(words[i])+x)>rightCutoff)
        	{
        		x=leftCutoff;
        		y+=lineHeight;
        	}
        	g.drawString(words [i], x, y);
        	g.drawRect(x-1,y-metrics.getAscent(),metrics.stringWidth(words[i])+1,metrics.getHeight());
        	x+=metrics.stringWidth(words[i]+" ");
        }
    }
}
