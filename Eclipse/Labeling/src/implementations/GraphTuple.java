package implementations;

public class GraphTuple{
	
	private String name;
	private int x;
	private int y;
	
	public GraphTuple(int e1, int e2)
	{
		x=e1;
		y=e2;
		name="Tuple";
	}
	public GraphTuple(String nm, int e1, int e2)
	{
		this(e1,e2);
		name=nm;
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
		return name+": ("+x+" | "+y+")";
	}

	//IMPORTANT: The name is currently not considered while comparing two GraphTuples 
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
