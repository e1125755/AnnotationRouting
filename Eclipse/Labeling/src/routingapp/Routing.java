package routingapp;

import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

/**
 * Interface for all future routing algorithms.
 * 
 * @author Jakob Klinger
 *
 */
public interface Routing {

	/**
	 * Finds a possible location for a single annotation
	 * @param source A node, designating the location of the annotated word. The annotation's text will be equal to the node's name.
	 * @return A GraphWalk containing either the route to the Annotation's position, or an attempt at a solution.
	 */
	public GraphWalk<GraphTuple,? extends DefaultWeightedEdge> findRouteFor(GraphTuple source);
	
	/**
	 * Finds possible Locations for a multiple annotations at once.
	 * @param list A List containing all Annotations that need to be routed.
	 * @return An Array of GraphWalks, each either leading to a found destination or an unfinished attempt.
	 */
	public GraphWalk<GraphTuple,? extends DefaultWeightedEdge>[] findRoutes(List<GraphTuple> list);
}
