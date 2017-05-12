package routingapp;

import java.util.List;

import org.jgrapht.WeightedGraph;
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
	 * @param graph The graph that will be used to find a route.
	 * @param source A node, designating the location of the annotated word. The annotation's text will be equal to the node's name.
	 * @return A GraphWalk containing either the route to the Annotation's position, or an attempt at a solution.
	 */
	public GraphWalk<GraphTuple,? extends DefaultWeightedEdge> findRouteFor(WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, GraphTuple source);
	
	/**
	 * Finds possible Locations for a multiple annotations at once.
	 * @param graph The graph that will be used to find the routes.
	 * @param list A List containing all Annotations that need to be routed.
	 * @return An Array of GraphWalks, each either leading to a found destination or an unfinished attempt.
	 */
	public GraphWalk<GraphTuple,? extends DefaultWeightedEdge>[] findRoutes(WeightedGraph<GraphTuple,? extends DefaultWeightedEdge> graph,List<GraphTuple> list);

	/**
	 * Helper method - allows the main program to tell the routing algorithm the first position a new annotation could take.
	 * TODO: Eliminate this method and find a better solution.
	 * @param nextAnnotationPos the first position the next annotation could start at
	 */
	public void updateNextAnnotationPos(int nextAnnotationPos);
}
