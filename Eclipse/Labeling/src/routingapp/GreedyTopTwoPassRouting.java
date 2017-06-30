package routingapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

public class GreedyTopTwoPassRouting extends GreedyTopRouting {
	
	protected int pageHeight;
	//Inherited from parent:	int rightTextBorder, leftAnnotationBorder, rightAnnotationBorder, annotationSpacing, nextAnnotationPos;
	//							GraphTuple lastAnnotatedWord.
	
	public GreedyTopTwoPassRouting(int textBorder, int pageHeight, int leftAnnotationBorder, int rightAnnotationBorder, int annotationSpacing) {
		super(textBorder,leftAnnotationBorder,rightAnnotationBorder,annotationSpacing);
		this.pageHeight=pageHeight;
	}

	public GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> findRouteFor(
			WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, GraphTuple source)
	{
		return super.findRouteFor(graph, source);
	}
	
	public List<RouteInfo> findRoutes(
			WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, TreeMap<Integer,GraphTuple> map)
	{
		ArrayList<RouteInfo> allRoutes, successfulRoutes;
		allRoutes=new ArrayList<RouteInfo>();
		successfulRoutes=new ArrayList<RouteInfo>();
		
		Entry<Integer,GraphTuple> currentEntry=map.firstEntry();
		
		while(currentEntry!=null)
		{
			GraphTuple currentTuple=currentEntry.getValue();
			GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> route=findRouteFor(graph, currentTuple);
			
			RouteInfo info=new RouteInfo(currentTuple.getAnnotation(),route,currentTuple);
			
			if(route.getLength()>1)//Since Backtracking is used, all failed attempts' routes have length 1 
			{
				successfulRoutes.add(info);
				currentTuple.getAnnotation().setYpos(nextAnnotationPos);
				nextAnnotationPos+=annotationSpacing+currentTuple.getAnnotation().calculateHeight(rightAnnotationBorder-leftAnnotationBorder);
			}
			allRoutes.add(info);
			currentEntry=map.higherEntry(currentEntry.getKey());
		}
		if(!successfulRoutes.isEmpty())// Second Pass - adjust Annotation's position to keep them closer to the GraphWalk's last node
		{
			nextAnnotationPos=pageHeight;
			GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> currentWalk;
			int i=successfulRoutes.size()-1;
			
			do
			{
				currentWalk=successfulRoutes.get(i).getPath();
				GraphTuple startVertex=currentWalk.getStartVertex();
				Annotation ann=startVertex.getAnnotation();
				int walkEnd=currentWalk.getEndVertex().getY();
				int annHeight=ann.calculateHeight(rightAnnotationBorder-leftAnnotationBorder);
				
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
		OpoCalculator calc=new OpoCalculator(rightTextBorder, leftAnnotationBorder);
		
		return calc.simpleOpo(allRoutes);
	}
}
