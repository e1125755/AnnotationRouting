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
	int opoPosition;
	
	public RouteInfo(Annotation ann, GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> wlk, GraphTuple src)
	{
		this.annotation=ann;
		this.path=wlk;
		this.source=src;
		src.setRouteInfo(this);
		ann.setRouteInfo(this);
	}

	public int getOpoPosition() {
		return opoPosition;
	}

	public void setOpoPosition(int opoPosition) {
		this.opoPosition = opoPosition;
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
}
