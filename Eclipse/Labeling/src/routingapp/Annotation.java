package routingapp;
/**
 * Class to encapsulate annotations and store some additional information, such as the Node associated with the annotation. 
 * 
 * @author Jakob Klinger
 */
public class Annotation {
	
	private String text;
	private GraphTuple annotatedNode;
	
	/**
	 * Creates a new annotation object. The values are not meant to be edited later on.
	 * @param annText The text of the annotation.
	 * @param node The node this annotation belongs to.
	 */
	public Annotation(String annText, GraphTuple node)
	{
		this.text=annText;
		this.annotatedNode=node;
	}
	
	public String getText()
	{
		return text;
	}
	
	public GraphTuple getNode()
	{
		return annotatedNode;
	}
	

}
