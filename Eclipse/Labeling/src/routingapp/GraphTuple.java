package routingapp;
/**
 * Custom Graph node for the annotation routing program.
 * It contains its location as coordinates, and can be named.
 * Optionally, an Annotation can be set, which should in turn have this node listed as its associated node.
 * The routeinfo attribute contains all routing information from this site to its label. This implies that an annotation is set was well, although it is not enforced.
 * @author Jakob Klinger
 *
 */
public class GraphTuple{
	
	private String name;
	private int x;
	private int y;
	private Annotation annotation;
	private RouteInfo routeinfo;
	
	public GraphTuple(int xpos, int ypos)
	{
		x=xpos;
		y=ypos;
		name="Tuple";
		annotation=null;
		routeinfo=null;
	}
	public GraphTuple(String name, int xpos, int ypos)
	{
		this(xpos,ypos);
		this.name=name;
	}
	
	public void setAnnotation(Annotation ann)
	{
		//NOTE: Maybe disable/notify user if annotation is already set? 
		annotation=ann;
	}
	
	protected void setRouteInfo(RouteInfo info)
	{
		//NOTE: Maybe allow changes only once?
		this.routeinfo=info;
	}
	
	public Annotation getAnnotation()
	{
		return annotation;
	}
	
	public RouteInfo getRouteInfo()
	{
		return routeinfo;
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

	//IMPORTANT: This only looks at the coordinates while comparing two GraphTuples
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
