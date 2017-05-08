package demos;

import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.xml.bind.DatatypeConverter;

import tutorials.FontSelector;

import java.util.Hashtable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class WordBoundaries extends JApplet implements ChangeListener, ItemListener{

    TextTestPanel textTestPanel;
    JComboBox fonts, styles;
    JSpinner sizes;
    String fontChoice = "Dialog";
    int styleChoice = 0;
    int sizeChoice = 12;
    
    private String text="Many people believe that Vincent van Gogh painted his best works " +
    					"during the two-year period he spent in Provence. Here is where he " +
    					"painted The Starry Night--which some consider to be his greatest " +
    					"work of all. However, as his artistic brilliance reached new " +
    					"heights in Provence, his physical and mental health plummeted. ";

    public void init() {
    	
    	text=text.replace("--", " - "); //Quick-fix for alternate style of dashes between two unrelated words - causes problems with detecting word boundaries here

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

        textTestPanel = new TextTestPanel(text);
        textTestPanel.setFont(new Font(fontChoice, styleChoice, sizeChoice));
        textTestPanel.setBackground(Color.white);

        add(BorderLayout.NORTH, fontSelectorPanel);
        add(BorderLayout.CENTER, textTestPanel);
    }

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) {
            return;
        }
        if (e.getSource() == fonts) {
            fontChoice = (String)fonts.getSelectedItem();
        } else {
            styleChoice = styles.getSelectedIndex();
        }
        textTestPanel.setFont(new Font(fontChoice, styleChoice, sizeChoice));
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		try {
            String size = sizes.getModel().getValue().toString();
            sizeChoice = Integer.parseInt(size);
            textTestPanel.setFont(new Font(fontChoice,styleChoice,sizeChoice));
        } catch (NumberFormatException nfe) {
        }
		
	}
	public static void main(String s[]) {

        JFrame f = new JFrame("Word Boundaries demo");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JApplet fontSelector = new WordBoundaries();
        f.add(fontSelector, BorderLayout.CENTER);
        fontSelector.init();
        f.pack();
        f.setVisible(true);
    }
}

class TextTestPanel extends JComponent
{
	private String text;
	private String[] words;
	private int paragraphStart, paragraphEnd;
	
	public TextTestPanel(String text)
	{
		this.text=text;
		this.words=text.split(" ");
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(500,300);
	}
	
	public void setFont(Font font)
	{
		super.setFont(font);
		repaint();
	}
	
	public void paintComponent(Graphics g)
	{
		//Setup
		super.paintComponent(g);
		g.setColor(Color.white);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.black);
        
        //AttributedString is needed for LineBreakMeasurer.
        //g.setFont(...) is called so that the size of single words can be determined later.
		g.setFont(this.getFont());
		FontMetrics met=g.getFontMetrics();
		AttributedString at=new AttributedString(text,this.getFont().getAttributes());
		
		
		Graphics2D g2d=(Graphics2D)g;
		
		AttributedCharacterIterator aci=at.getIterator();
		paragraphStart=aci.getBeginIndex();
		paragraphEnd=aci.getEndIndex();
		LineBreakMeasurer lineMeasurer=new LineBreakMeasurer(aci,g2d.getFontRenderContext());
		
		float lineWidth= this.getSize().width*0.7f;
		float drawPosY=0;//Maybe adjust this for different sizes of the upper text margin?
		
		lineMeasurer.setPosition(paragraphStart);
		int wordcount=0;
		int skippedHyphens=0;
		
		while(lineMeasurer.getPosition()<paragraphEnd)
		{
			TextLayout layout=lineMeasurer.nextLayout(lineWidth);
			
			assert(layout.isLeftToRight()); //Else: Our layout breaks
			float drawPosX=this.getSize().width*0.075f;
			drawPosY+=layout.getAscent()+2; //add higher numbers for more line spacing
			
			layout.draw(g2d,drawPosX,drawPosY);
			
			//Finding out the borders of single words
			Rectangle2D rect=layout.getBounds();
			
			
			//Retrieving lines of paragraphed text from TextLayout.toString() - VERY hack-y/fragile, as it depends on a certain layout of the toString()-string!
			String hexChars[]=layout.toString().split("\"")[1].split(" ");
			String line="";
			for(int i=0;i<hexChars.length;i++)
			{
				line+= new String(DatatypeConverter.parseHexBinary(hexChars[i]));
			}
			System.out.println(layout.toString());
			System.out.println(line);
			for(double x=(drawPosX+rect.getX());(wordcount<words.length)&&(x<drawPosX+rect.getX()+rect.getWidth()+0.5);)
            {
            	
            	AttributedCharacterIterator it=at.getIterator();
            	FontMetrics met2d=g2d.getFontMetrics();
            	
            	if((skippedHyphens>0)||((words[wordcount].contains("-"))&&((x+met2d.stringWidth(words[wordcount])>drawPosX+rect.getX()+rect.getWidth()))))//Detecting possible linebreaks within a hyphenated word
            	{
            		//System.out.println("Word: "+words[wordcount]);
            		String temp[]=words[wordcount].split("-");
            		if(temp.length>0)
            		{
            			//System.out.println(skippedHyphens+"/"+temp.length);
            			String halfWord="";
            			for(int i=skippedHyphens;i<temp.length;i++)
            			{
            				skippedHyphens=0;//Only necessary once, but old value is needed in loop header 
            				if((x+met2d.stringWidth(halfWord+temp[i]+"-")<drawPosX+rect.getX()+rect.getWidth()+0.5)||((i+1==temp.length)&&(x+met2d.stringWidth(halfWord+temp[i])<drawPosX+rect.getX()+rect.getWidth()+0.5))) 
            				{
            					halfWord+=temp[i];
            					if(i+1<temp.length)halfWord+="-";
            					System.out.println("."+halfWord);
            				}
            				else
            				{
            					g.drawRect((int) x, (int)drawPosY+(int)rect.getY()-1, met2d.stringWidth(halfWord), (int)rect.getHeight()+2);
            					
            					skippedHyphens=i;
            					//System.out.println(skippedHyphens);
            					x=drawPosX+rect.getX()+rect.getWidth()+100; //Forcing the outer loop to exit
            					break;
            				}
            			}
            			if(skippedHyphens==0)// <=> word(-fragment) fits completely inside the line 
            			{
            				//System.out.println(halfWord+"2");
            				wordcount++;
            				x+=met2d.getStringBounds(halfWord+" ",g2d).getWidth();
            			}
            		}
            		else //"Word" was only a single Hyphen
            		{
            			g.drawRect((int)x, (int)drawPosY+(int)rect.getY()-1, met2d.stringWidth(words[wordcount]), (int)rect.getHeight()+2);
            			wordcount++;
            			x=(int)(drawPosX+rect.getX()+rect.getWidth())+100;//Forcing the outer loop to exit
            		}
            	}
            	else
            	{
            		int width=(int)met2d.getStringBounds(words[wordcount],g2d).getWidth();
            	
            		g.drawRect((int)x, (int)drawPosY+(int)rect.getY()-1, width, (int)rect.getHeight()+2);
            	
            		x+=met2d.getStringBounds(words[wordcount]+" ",g2d).getWidth();
            		wordcount++;
            	}
            }
		}
		
		
	}
}