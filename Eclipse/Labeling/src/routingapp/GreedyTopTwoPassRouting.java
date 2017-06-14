package routingapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

public class GreedyTopTwoPassRouting extends GreedyTopRouting {
	
	private int leftAnnotationBorder, rightAnnotationBorder, annotationSpacing;
	//Inherited from parent: int rightTextBorder, nextAnnotationPos; GraphTuple lastAnnotatedWord.
	
	public GreedyTopTwoPassRouting(int textBorder, int leftAnnotationBorder, int rightAnnotationBorder, int AnnotationSpacing) {
		super(textBorder);
		this.leftAnnotationBorder = leftAnnotationBorder;
		this.rightAnnotationBorder = rightAnnotationBorder;
		this.annotationSpacing = annotationSpacing;
	}

	public GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> findRouteFor(
			WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, GraphTuple source)
	{
		return super.findRouteFor(graph, source);
	}
	
	public List<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>> findRoutes(
			WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, List<GraphTuple> list)
	{
		ArrayList<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>> allRoutes, successfulRoutes;
		allRoutes=new ArrayList<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>>();
		successfulRoutes=new ArrayList<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>>();
		
		Iterator<GraphTuple> it=list.iterator();
		
		while(it.hasNext())
		{
			GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> temp=findRouteFor(graph, it.next());
			if(temp.getLength()>1)//Since Backtracking is used, all failed attempts' routes have length 1 
			{
				successfulRoutes.add(temp);
				nextAnnotationPos+=annotationSpacing+temp.getStartVertex().getAnnotation().calculateHeight(rightAnnotationBorder-leftAnnotationBorder, annotationSpacing);
			}
			allRoutes.add(temp);
		}
		
		//TODO: Add second pass (to adjust annotation's positions) 
		
		return null;
	}
	
}
