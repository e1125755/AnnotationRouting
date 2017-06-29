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
		return doOpoCalculations(super.findRoutes(graph,map));
	}

	/**
	 * Calculates Positions for all OPO-Leaders from each RouteInfo's route's end to the respective annotation.
	 * Since it is based on the biggest amount of parallel OPO-Leaders, all other clusters will not optimally use the available space.
	 * The method is made public, so it can easily be invoked from other routing algorithms that wish to use OPO-Leaders as well.
	 * NOTE: Maybe export into its own class? 
	 * @param list The List of RouteInfos that will be looked through. 
	 * @return The same list, with updated Values for the OPO-Leader's start and bend location. 
	 */
	public List<RouteInfo> doOpoCalculations(List<RouteInfo> list)
	{
		for(int i=list.size()-1;i>=0;i--)//Determine leader position relative to each other
		{
			RouteInfo info=list.get(i);
			if(info.isSuccessful())
			{
				GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> path=info.getPath();
				if(path.getEndVertex()!=info.getSource())//Detecting the paths direction and adjusting accordingly 
				{
					info.setOpoStart(path.getEndVertex().getY());
				}
				else
				{
					info.setOpoStart(path.getStartVertex().getY());
				}
				if(info.getOpoStart()!=info.getOpoEnd())
				{
					if((opoEnd==null)||(opoEnd>info.getOpoStart()))
					{
						opoNumber=1;
					}
					else
					{
						//TODO: implement cap? Might get silly with many parallel opo-leaders and little space to route through
						opoNumber++;
						if(opoNumber>biggestOpoNumber) biggestOpoNumber=opoNumber;
					}
					info.setOpoBendPosition(opoNumber);
					opoEnd=info.getOpoEnd();
				}
			}
		}

		Iterator<RouteInfo> it=list.iterator();

		while(it.hasNext())//Convert relative positions into absolute positions on the canvas
		{
			RouteInfo info=it.next();
			if(info.isSuccessful()&&info.getOpoBendPosition()!=null)
			{
				info.setOpoBendPosition(rightTextBorder+(biggestOpoNumber+1-info.getOpoBendPosition())*(leftAnnotationBorder-rightTextBorder)/(biggestOpoNumber+1));
			}
		}
		
		return list;
	}
}
