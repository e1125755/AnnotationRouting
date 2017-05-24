package routingapp;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

/**
 * Class to encapsulate annotations and store some additional information, such as the Node associated with the annotation. 
 * 
 * @author Jakob Klinger
 */
public class Annotation {
	
	private String text;
	private GraphTuple annotatedNode;
	private Font font;
	
	/**
	 * Creates a new annotation object. The values are not meant to be edited later on.
	 * @param annText The text of the annotation.
	 * @param node The node this annotation belongs to.
	 */
	public Annotation(String annText, Font f, GraphTuple node)
	{
		this.text=annText;
		this.font=f;
		this.annotatedNode=node;
		this.annotatedNode.setAnnotation(this);
	}
	
	/**
	 * Calculates the height of an annotation by using the same process that is used in the main program, and looking at the results.
	 * Theoretically, using LineBreakMeasurer would be possible, and allow better fits, but has the downside of being very difficult to work with for anything else.    
	 * @param width The space each annotation's line has available.
	 * @param spaceBetweenLines The amount of free space that has to be left between each line.
	 * @return The total height of the annotation's text, including the free space below the last line.
	 */
	public int calculateHeight(int width, int spaceBetweenLines)
	{
		//Workaround to create a FontMetrics Object from a Font (Constructor is protected)
		Canvas c=new Canvas();
		FontMetrics met=c.getFontMetrics(font);
		
		int ret=met.getHeight();
		String[] words=text.split(" ");
		int x=0;
		
		for(int w=0;w<words.length;w++)
		{
			if((x+met.stringWidth(words[w]))>(width))
			{
				x=0;
				ret+=met.getHeight()+spaceBetweenLines;
			}
			x+=met.stringWidth(words[w]+" ");
		}
		
		
		return (ret+spaceBetweenLines);
	}
	
	public String getText()
	{
		return text;
	}
	
	public GraphTuple getNode()
	{
		return annotatedNode;
	}
	
	public Font getFont()
	{
		return font;
	}
	
	public String toString()
	{
		return "Annotation: "+text;
	}

}
