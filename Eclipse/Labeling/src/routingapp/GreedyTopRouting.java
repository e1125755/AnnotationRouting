package routingapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;

public class GreedyTopRouting implements Routing{

	protected int rightTextBorder;
	protected int nextAnnotationPos;
	protected GraphTuple lastAnnotatedWord;
	public GreedyTopRouting(int textBorder)
	{
		rightTextBorder=textBorder;
		nextAnnotationPos=0;
		lastAnnotatedWord=new GraphTuple("Dummy",0,0);
	}

	@Override
	public GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> findRouteFor(
			WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, GraphTuple source) {

		int index=0;
		GraphTuple currentNode=source;

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
						lastAnnotatedWord=source;
						
						//Adjust used Edge's weights
						Iterator<GraphTuple>iter=pathNodes.iterator();
						GraphTuple oldTuple=iter.next();
						while(iter.hasNext())
						{
							GraphTuple newTuple=iter.next();
							DefaultWeightedEdge temp=graph.getEdge(oldTuple, newTuple);
							if(temp!=null)graph.setEdgeWeight(temp, graph.getEdgeWeight(temp)-1);
							oldTuple=newTuple;
						}
					}
				}
			}//end while
		}

		return new GraphWalk<GraphTuple,DefaultWeightedEdge>(graph, pathNodes,pathNodes.size());
	}

	@Override
	public List<GraphWalk<GraphTuple, ? extends DefaultWeightedEdge>> findRoutes(
			WeightedGraph<GraphTuple, DefaultWeightedEdge> graph, TreeMap<Integer,GraphTuple> map) {
		throw new UnsupportedOperationException("Method not implemented yet!");
		//TODO: Implement method or remove it from Interface.
		//return null;
	}

	@Override
	public void updateNextAnnotationPos(int nextAnnotationPos) {
		this.nextAnnotationPos=nextAnnotationPos;
	}
	
	@Override
	public boolean supportsFindRoutes() {return false;}

}
