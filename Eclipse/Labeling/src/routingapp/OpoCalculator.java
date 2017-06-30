package routingapp;

import java.util.Iterator;
import java.util.List;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

/**
 * This class holds the methods needed to calculate the OPO-Leaders that connect the annotation with the part of the route depicted by the GraphWalk.
 * As these calculations might be used by several routing algorithms, and aren't directly dependent on the rest of the routing, it is easy keep them separate.  
 * @author Jakob Klinger
 *
 */
public class OpoCalculator {

	Integer opoNumber, opoEnd=null, biggestOpoNumber=1;
	int rightTextBorder, leftAnnotationBorder;
	
	public OpoCalculator(int rightTextBorder, int leftAnnotationBorder)
	{
		this.rightTextBorder=rightTextBorder;
		this.leftAnnotationBorder=leftAnnotationBorder;
	}
	
	/**
	 * Calculates Positions for all OPO-Leaders from each RouteInfo's route's end to the respective annotation.
	 * Since it is based on the biggest amount of parallel OPO-Leaders, all other clusters will not optimally use the available space.
	 * @param list The List of RouteInfos that will be looked through. 
	 * @return The same list, with updated Values for the OPO-Leader's start and bend location. 
	 */
	public List<RouteInfo> simpleOpo(List<RouteInfo> list)
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
