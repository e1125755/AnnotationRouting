package routingapp;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

/**
 * Small class designed to to hold information about a leader's path.
 * Mostly necessary to store data not covered by the GraphWalk or Annotation classes. 
 * @author Jakob Klinger
 *
 */
public class RouteInfo {

	private Annotation annotation;
	private GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> path;
	private GraphTuple source;
	private Integer opoStart, opoBend;
	
	public RouteInfo(Annotation ann, GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> wlk, GraphTuple src)
	{
		this.annotation=ann;
		this.path=wlk;
		this.source=src;
		this.opoStart=null;
		this.opoBend=null;
		src.setRouteInfo(this);
		ann.setRouteInfo(this);
	}

	/**
	 * Determines if a routing lead to a successful path. Since Backtracking is always used, this can be solved by measuring the path's length.
	 * Shouldn't lead to any false negatives, since it currently is impossible for source nodes to be at the rightmost position of a graph.
	 * @return whether or not this object contains a successful routing.
	 */
	public boolean isSuccessful()
	{
		return path.getLength()>1;
	}
	
	/**
	 * Helper method - only here to provide a clearer name and eliminate some calls to RouteInfo.getAnnotation() if only its position is needed.
	 * @return The Y-Position of the Annotation object - which is equal to the position where the vertical part of the OPO-Leader ends.
	 */
	public int getOpoEnd()
	{
		return annotation.getYpos();
	}
	
	public Integer getOpoStart() {
		return opoStart;
	}
	
	public Integer getOpoBendPosition()
	{
		return opoBend;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> getPath() {
		return path;
	}

	public GraphTuple getSource() {
		return source;
	}
	
	public void setOpoStart(int opoStart) {
		this.opoStart = opoStart;
	}
	
	public void setOpoBendPosition(int position)
	{
		this.opoBend=position;
	}
	
}
