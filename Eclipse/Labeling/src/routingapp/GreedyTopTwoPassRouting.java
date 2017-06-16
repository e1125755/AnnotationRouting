package routingapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

public class GreedyTopTwoPassRouting extends GreedyTopRouting {
	
	private int leftAnnotationBorder, rightAnnotationBorder, annotationSpacing;
	//Inherited from parent: int rightTextBorder, nextAnnotationPos; GraphTuple lastAnnotatedWord.
	
	public GreedyTopTwoPassRouting(int textBorder, int leftAnnotationBorder, int rightAnnotationBorder, int annotationSpacing) {
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
			WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, TreeMap<Integer,GraphTuple> map)
	{
		ArrayList<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>> allRoutes, successfulRoutes;
		allRoutes=new ArrayList<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>>();
		successfulRoutes=new ArrayList<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>>();
		
		Entry<Integer,GraphTuple> currentEntry=map.firstEntry();
		
		while(currentEntry!=null)
		{
			GraphTuple currentTuple=currentEntry.getValue();
			GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> temp=findRouteFor(graph, currentTuple);
			if(temp.getLength()>1)//Since Backtracking is used, all failed attempts' routes have length 1 
			{
				successfulRoutes.add(temp);
				currentTuple.getAnnotation().setYpos(nextAnnotationPos);
				currentTuple.getAnnotation().setRoute(temp);
				nextAnnotationPos+=annotationSpacing+currentTuple.getAnnotation().calculateHeight(rightAnnotationBorder-leftAnnotationBorder, annotationSpacing);
			}
			allRoutes.add(temp);
			currentEntry=map.higherEntry(currentEntry.getKey());
		}
		
		//TODO: Add second pass (to adjust annotation's positions) 
		
		return allRoutes;
	}
	
	public boolean supportsFindRoutes() {return true;}
}
