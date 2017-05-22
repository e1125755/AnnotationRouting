package routingapp;
/**
 * Custom Graph node for the annotation routing program.
 * It currently contains its location as coordinates, and can be named.
 * Optionally, an Annotation can be set, which should in turn have this node listed as its associated node. 
 * @author Jakob Klinger
 *
 */
public class GraphTuple{
	
	private String name;
	private int x;
	private int y;
	private Annotation annotation;
	
	public GraphTuple(int e1, int e2)
	{
		x=e1;
		y=e2;
		name="Tuple";
		annotation=null;
	}
	public GraphTuple(String nm, int e1, int e2)
	{
		this(e1,e2);
		name=nm;
	}
	
	public void setAnnotation(Annotation ann)
	{
		//NOTE: Maybe disable/notify user if annotation is already set? 
		annotation=ann;
	}
	
	public Annotation getAnnotation()
	{
		return annotation;
	}
	
	public String getName() {
		return name;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String toString()
	{
		String ret=name+": ("+x+" | "+y+")";
		if(annotation!=null) ret+=" - "+annotation;
		return ret;
	}

	//IMPORTANT: Name and annotation are currently not considered while comparing two GraphTuples 
	public boolean equals(Object o)
	{
		if((o==null)||(o.getClass()!=this.getClass())) return false;
		
		else
		{
			GraphTuple other=(GraphTuple)o;
			return (this.getX()==other.getX())&&(this.getY()==other.getY());
		}
	}
	
}
