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
	private Font font;
	private Integer borderSize, ypos;
	private GraphTuple annotatedNode;
	private RouteInfo routeinfo;
	
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
		this.borderSize=borderSize;
		this.routeinfo=null; 
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
	public RouteInfo getRouteInfo() {
		return routeinfo;
	}

	/**
	 * Sets the RouteInfo of this annotation. Can only be used once, therefore any subsequent invocation causes an Exception.
	 * @param info the RouteInfo object - it contains information about this annotation's routing.
	 */
	protected void setRouteInfo(RouteInfo info) {
		
		if(this.routeinfo==null)this.routeinfo = info;
		else throw new UnsupportedOperationException("Error: Route information already set for "+this.toString());
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
