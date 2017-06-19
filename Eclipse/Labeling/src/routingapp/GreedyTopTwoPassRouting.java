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
	
	private int pageHeight, leftAnnotationBorder, rightAnnotationBorder, annotationSpacing;
	//Inherited from parent: int rightTextBorder, nextAnnotationPos; GraphTuple lastAnnotatedWord.
	
	public GreedyTopTwoPassRouting(int textBorder, int pageHeight, int leftAnnotationBorder, int rightAnnotationBorder, int annotationSpacing) {
		super(textBorder);
		this.pageHeight=pageHeight;
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
		if(!successfulRoutes.isEmpty())// Second Pass - adjust Annotation's position to keep them closer to the GraphWalk's last node
		{
			nextAnnotationPos=pageHeight;
			GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> currentWalk;
			int i=successfulRoutes.size()-1;
			
			do
			{
				currentWalk=successfulRoutes.get(i);
				GraphTuple startVertex=currentWalk.getStartVertex();
				Annotation ann=startVertex.getAnnotation();
				int walkEnd=currentWalk.getEndVertex().getY();
				int annHeight=ann.calculateHeight(rightAnnotationBorder-leftAnnotationBorder, annotationSpacing);
				
				if(nextAnnotationPos-annHeight>=walkEnd)
				{
					ann.setYpos(walkEnd);
					nextAnnotationPos=walkEnd-annotationSpacing;
				}
				else
				{
					ann.setYpos(nextAnnotationPos-annHeight);
					nextAnnotationPos=nextAnnotationPos-annHeight-annotationSpacing;
				}

				i--;
			}
			while(i>=0);

		}
		return allRoutes;
	}
	
	public boolean supportsFindRoutes() {return true;}
}
