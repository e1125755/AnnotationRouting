package routingapp;

import java.util.List;
import java.util.TreeMap;

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
	 * Finds a possible location for a single annotation.
	 * @param graph The graph that will be used to find a route.
	 * @param source A node, designating the location of the annotated word. The annotation's information will be retrieved from the node's annotation object.
	 * @return A GraphWalk containing either the route to the Annotation's position, or an attempt at a solution.
	 */
	public GraphWalk<GraphTuple,? extends DefaultWeightedEdge> findRouteFor(WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, GraphTuple source);
	
	/**
	 * Finds possible Locations for a multiple annotations at once.
	 * @param graph The graph that will be used to find the routes.
	 * @param map A Treemap containing all Annotations that need to be routed, in order of appearance in the text.
	 * @return A List of RouteInfos, each containing all information about the routing, including whether or not it was successful.
	 */
	public List<RouteInfo> findRoutes(WeightedGraph<GraphTuple, DefaultWeightedEdge> graph,TreeMap<Integer,GraphTuple> map);

	/**
	 * Helper method - allows the main program to tell the routing algorithm the first position a new annotation could take.
	 * TODO: Eliminate this method and find a better solution - main program only calculates positions for the greedytop algorithm
	 * @param nextAnnotationPos the first position the next annotation could start at
	 */
	public void updateNextAnnotationPos(int nextAnnotationPos);
}
