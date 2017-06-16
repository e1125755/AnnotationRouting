package routingapp;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

/**
 * Class to encapsulate annotations and store some additional information, such as the Node associated with the annotation. 
 * 
 * @author Jakob Klinger
 */
public class Annotation {
	
	private String text;
	private Font font;
	private Integer borderSize, ypos;
	private GraphTuple annotatedNode;
	private GraphWalk<GraphTuple,? extends DefaultWeightedEdge> route;
	
	/**
	 * Creates a new annotation object. The values are not meant to be edited later on.
	 * @param annText The text of the annotation.
	 * @param node The node this annotation belongs to.
	 */
	public Annotation(String annText, Font f, GraphTuple node, int borderSize)
	{
		this.text=annText;
		this.font=f;
		this.annotatedNode=node;
		this.annotatedNode.setAnnotation(this);
		this.borderSize=borderSize;
		this.route=null; 
		this.ypos=null;
	}
	
	/**
	 * Calculates the height of an annotation by using the same process that is used in the main program, and looking at the results.
	 * Theoretically, using LineBreakMeasurer would be possible, and allow better fits, but has the downside of being very difficult to work with for anything else.    
	 * @param width The space each annotation's line has available, including space for borders.
	 * @param spaceBetweenLines The amount of free space that has to be left between each line.
	 * @return The total height of the annotation's text, including the free space below the last line.
	 */
	public int calculateHeight(int width, int spaceBetweenLines)
	{
		width-=2*borderSize;
		FontMetrics met=this.getFontMetrics();
		
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
	/**
	 * Workaround method to create a FontMetrics Object from a Font, since the actual Constructor is protected. 
	 * @return A FontMetrics Object that uses this Object's Font for it's measurements. 
	 */
	public FontMetrics getFontMetrics()
	{
		Canvas c=new Canvas();
		return c.getFontMetrics(font);
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
	
	public int getBorderSize()
	{
		return borderSize;
	}
	public Integer getYpos()
	{
		return ypos;
	}
	
	public GraphWalk<GraphTuple,? extends DefaultWeightedEdge> getRoute() {
		return route;
	}

	/**
	 * Sets the route the annotation's leader takes throughout the graph. Only usable once, throws an exception if used with the route already set.
	 * @param route The GraphWalk describing the leader's route throughout the graph
	 */
	public void setRoute(GraphWalk<GraphTuple,? extends DefaultWeightedEdge> route) {
		
		if(this.route==null)this.route = route;
		else throw new UnsupportedOperationException("Error: Route already set for "+this.toString());
	}
	
	public void setYpos(int y)
	{
		ypos=y;
	}

	public String toString()
	{
		return "Annotation: "+text;
	}

}
