package routingapp;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

public class GreedyTopRouting implements Routing{

	public GreedyTopRouting()
	{
		//TODO: Determine which values/objects are needed for a proper routing 
	}
	
	@Override
	public GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> findRouteFor(
			GraphTuple source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>[] findRoutes(
			List<GraphTuple> list) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 * Greedy routing algorithm - prioritizes going as far up as possible to maximize the space remaining for other annotations. 
	 * NOTE: This method currently depends on the annotations' text being stored in a separate array - if this changes, the method needs to be reworked.
	 *  
	 * @param graph The graph containing all possible routes
	 * @param annotations A TreeMap containing the starting nodes for each route, ordered by their place in the text
	 * @param g The Graphics-object used to draw on the canvas. Not actively used in this method, but needed in calls to other methods.
	 *
	private void routeAnnotationsGreedy(WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, TreeMap<Integer,GraphTuple> annotations, Graphics g)
	{
		Entry<Integer,GraphTuple> currentEntry=annotations.firstEntry();

		while(currentEntry!=null)//Iterate over all nodes 
		{
			GraphTuple currentNode=currentEntry.getValue();
			int index=0;

			ArrayList<GraphTuple> pathNodes=new ArrayList<GraphTuple>();
			pathNodes.add(index, currentNode);
			index++;

			if(currentNode.getY()>=nextAnnotationPos)//Check whether we can route up/horizontally
			{
				boolean deadend=false;
				boolean backtrack=false;
				while(!deadend)
				{

					Set<DefaultWeightedEdge> edges=graph.edgesOf(currentNode);
					Iterator<DefaultWeightedEdge> it=edges.iterator();

					GraphTuple verticalCandidate=null;
					GraphTuple horizontalCandidate=null;

					while(it.hasNext())//Iterate over neighbours of current node
					{
						DefaultWeightedEdge edge=it.next();

						if(graph.getEdgeWeight(edge)>0.0)// Limit for paths per edge
						{
							GraphTuple temp=graph.getEdgeSource(edge);

							if(temp.equals(currentNode)) temp=graph.getEdgeTarget(edge);

							if((temp.getX()>currentNode.getX()))
							{
								horizontalCandidate=temp;
							}
							else if((!backtrack)&&(temp.getY()<currentNode.getY())&&(temp.getY()>nextAnnotationPos))
							{
								if((currentNode.getX()>lastAnnotatedWord.getX())||(currentNode.getY()>lastAnnotatedWord.getY()))
								{
									verticalCandidate=temp;
								}
							}
						}
					}
					if(verticalCandidate!=null)
					{
						DefaultWeightedEdge temp=graph.getEdge(currentNode, verticalCandidate);
						currentNode=verticalCandidate;
						pathNodes.add(index,currentNode);
						index++;
					}
					else if(horizontalCandidate!=null)
					{
						DefaultWeightedEdge temp=graph.getEdge(currentNode, horizontalCandidate);
						currentNode=horizontalCandidate;
						pathNodes.add(index,currentNode);
						index++;
						backtrack=false;
					}
					else//No suitable nodes found - dead end or upper border for annotation position encountered
					{
						if(currentNode.getX()<rightTextBorder) //True dead end encountered, initiate backtracking
						{
							GraphTuple tempNode=currentNode;
							backtrack=true;
							while((tempNode.getY()==currentNode.getY())&&(currentNode!=pathNodes.get(0)))	
							{
								index--;
								pathNodes.remove(index);
								currentNode=pathNodes.get(index-1);
							}
							if(currentNode==pathNodes.get(0)&&(tempNode.getY()==currentNode.getY()))// <=> No possible solution for this algorithm
							{
								deadend=true;
								//Add error message?
							}
						}
						else// Right side was already reached, nothing to do here
						{
							deadend=true;
						}
					}
				}//end while
			}

			drawAnnotation(g,graph,new GraphWalk<GraphTuple,DefaultWeightedEdge>(graph,pathNodes,pathNodes.size()),currentEntry.getKey());

			lastAnnotatedWord=currentEntry.getValue();
			currentEntry=annotations.higherEntry(currentEntry.getKey());
		}
	}*/
	 */
	
}