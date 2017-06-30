package routingapp;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

public class GreedyTopOpo extends GreedyTopRouting {

	Integer opoNumber, opoEnd=null, biggestOpoNumber=1;
	//Inherited from parent:	int rightTextBorder, leftAnnotationBorder, rightAnnotationBorder, annotationSpacing, nextAnnotationPos;
	//							GraphTuple lastAnnotatedWord.

	public GreedyTopOpo(int textBorder, int leftAnnBorder, int rightAnnBorder,
			int annSpacing) {
		super(textBorder, leftAnnBorder, rightAnnBorder, annSpacing);
	}

	public List<RouteInfo> findRoutes(WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, TreeMap<Integer,GraphTuple> map)
	{
		List<RouteInfo> list=super.findRoutes(graph, map);
		OpoCalculator calc=new OpoCalculator(rightTextBorder, leftAnnotationBorder);
		
		return calc.simpleOpo(list);
	}
}
