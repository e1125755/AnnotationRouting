package routingapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JComponent;

import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class GraphCreatorPanel extends JComponent {

	private String text=
			"Dies ist ein Typoblindtext.\\note{This is an annotation} An ihm kann man sehen, ob alle Buchstaben da sind und wie sie aussehen. " +
					"Manchmal benutzt man Worte wie Hamburgefonts, Rafgenduks\\note{This too} oder Handgloves, um\\note{This is an annotation with long words, like supercalifragilisticexpiralidocious} "+
					"Schriften\\note{Source: http://www.blindtextgenerator.de/} zu testen. " +
					"Manchmal S�tze, die alle Buchstaben des Alphabets enthalten - man nennt diese S�tze �Pangrams�. " +
					"Sehr bekannt ist dieser: The quick brown fox jumps over the lazy old dog. " + 
					"Oft werden in Typoblindtexte auch fremdsprachige Satzteile eingebaut (AVAIL� and Wefox� are testing aussi la Kerning), " + 
					"um die Wirkung in anderen Sprachen zu testen. In Lateinisch sieht zum Beispiel fast jede Schrift gut aus. " + 
					"Quod erat demonstrandum. Seit 1975 fehlen in den meisten Testtexten die Zahlen, " +
					"weswegen nach TypoGb. 204 � ab dem Jahr 2034 Zahlen in 86 der Texte zur Pflicht werden. " +
					"Nichteinhaltung wird mit bis zu 245 � oder 368 $ bestraft." +
					"Genauso wichtig in sind mittlerweile auch ��c��t�,\\note{How do you pronounce that?} die in neueren Schriften aber fast immer enthalten sind. " +
					"Ein\\note{Hier waren mal Probleme} wichtiges aber schwierig zu integrierendes Feld sind OpenType-Funktionalit�ten. " +
					"Je nach Software und Voreinstellungen k�nnen eingebaute\\note{Annotations might be longer than the remaining text if its text is excessively long and there's not enough space to route upwards tough} " +
					"Kapit�lchen, Kerning oder Ligaturen (sehr pfiffig) nicht richtig dargestellt werden.\\note{test test}";/**/
	//Source: http://www.blindtextgenerator.de/

	private boolean showWordBoundaries=false;//Debug-value, draws rectangles around detected word boundaries in main text, if set to true

	private int width=550;
	private int height=600;
	private int leftTextBorder, rightTextBorder, rightBorder;
	private int upperBorder=10;
	private int spaceBetweenLines=6;

	private GraphTuple lastAnnotatedWord=new GraphTuple("Dummy",0,0);//NOTE:This might not be enough information for the routing!
	private int nextAnnotationPos=0;

	public Dimension getPreferredSize() {
		return new Dimension(width,height);//NOTE: This might be a bad idea - may cause varying preferred sizes if called later on!
	}

	public void setFont(Font font) {
		super.setFont(font);
		repaint();
	}

	/**
	 * Helper method - updates width, height and related values whenever called.
	 */
	private void updateMeasurements(Graphics g) {
		width=this.getWidth();
		height=this.getHeight();

		rightTextBorder=(int) (width*0.75);
		leftTextBorder=(int) (width*0.1);
		rightBorder=(int) (width*0.95);

		//resetting routing values, since routing needs to be re-done
		nextAnnotationPos=0;
		lastAnnotatedWord=new GraphTuple("Dummy",0,0);
	}

	/**
	 * Helper method - connects all nodes listed in the supplied ordered map with their neighbours.
	 * @param line The nodes, ordered by desired neighbourhood
	 * @param graph The Graph containing the nodes
	 * @return the changed graph
	 */
	private Graph<GraphTuple,DefaultWeightedEdge> finishTupleLine(TreeMap<Integer, GraphTuple> line, Graph<GraphTuple,DefaultWeightedEdge> graph)
	{
		Entry<Integer, GraphTuple> e1 = line.firstEntry();
		Entry<Integer, GraphTuple> e2 = line.higherEntry(line.firstKey());
		while(e2!=null)
		{
			GraphTuple t1=e1.getValue();
			GraphTuple t2=e2.getValue();

			graph.addEdge(t1,t2,new DefaultWeightedEdge());

			e1=e2;
			e2=line.higherEntry(e1.getKey());
		}
		return graph;
	}

	/**
	 * Splits the text and displays it on-screen - this is done manually, as java.awt.font.LineBreakMeasurer doesn't allow to extract the text line-by-line.
	 * As it is easy to do, the Graph is also constructed here. 
	 * @param g The Graphics object used to display everything - usually provided by Java.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setFont(this.getFont());
		updateMeasurements(g);

		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);
		FontMetrics metrics = g.getFontMetrics();


		int lineHeight=metrics.getHeight()+spaceBetweenLines;
		int x = leftTextBorder;
		int y = metrics.getAscent()+10;

		String words[]=text.split(" ");
		ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge> graph=new ListenableUndirectedWeightedGraph(DefaultWeightedEdge.class);
		TreeMap<Integer, GraphTuple> upperTuples=new TreeMap<Integer, GraphTuple>();
		TreeMap<Integer, GraphTuple> lowerTuples=new TreeMap<Integer, GraphTuple>();
		GraphTuple lastTuple=null;

		TreeMap<Integer,GraphTuple> annotations=new TreeMap<Integer,GraphTuple>();
		int annNumber=0;


		//While it is possible to have line breaks in more places than after each word, this will be ignored for ease of implementation.
		for(int i=0;i<words.length; i++)
		{
			if((metrics.stringWidth(words[i].split("\\\\")[0])+x)>rightTextBorder)//Four backslashes, since both the String and the Regex use '\' as escape character
			{
				//Add one more node-pair after the last word
				x-=metrics.stringWidth(" ");
				GraphTuple t1=new GraphTuple(x,y-metrics.getAscent()-spaceBetweenLines/2);
				graph.addVertex(t1);
				upperTuples.put(x, t1);

				GraphTuple t2=new GraphTuple(x,y+metrics.getDescent()+spaceBetweenLines/2);
				graph.addVertex(t2);
				lowerTuples.put(x, t2);

				graph.addEdge(t1,t2,new DefaultWeightedEdge());

				//Add extra node at the page border
				t1=new GraphTuple(rightTextBorder,y-metrics.getAscent()-spaceBetweenLines/2);
				if(graph.addVertex(t1)==true)
				{
					upperTuples.put(rightTextBorder, t1);

				}
				else
				{
					t1=upperTuples.lastEntry().getValue();
				}
				if(lastTuple!=null)//connect new tuple with its counterpart in the previous line
				{
					if(!graph.containsEdge(lastTuple,t1))graph.addEdge(lastTuple, t1);
				}
				lastTuple=t1;

				//Connect tuples above finished line
				graph=(ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>) finishTupleLine(upperTuples,graph);

				//Reset drawing coordinates and move lists of tuples adjacent to new line to correct labels
				x=leftTextBorder;
				y+=lineHeight;

				upperTuples=lowerTuples;
				lowerTuples=new TreeMap<Integer,GraphTuple>();
			}

			//detect possible annotations for current word - Only detects one annotation per word so far!
			if(words[i].contains("\\note"))
			{
				String temp[]=words[i].split("\\\\");
				//Create annotation
				String ann="";
				if(temp[1].contains("{")&&temp[1].contains("}"))//Currently assumes there's only one Argument 
				{
					ann=temp[1].substring(temp[1].indexOf('{')+1, temp[1].lastIndexOf('}'));
				}
				else
				{
					ann=temp[1].substring(temp[1].indexOf('{')+1);
					i++;
					while((i<words.length)&&(!words[i].contains("}")))
					{
						ann+=" "+words[i];
						i++;
					}
					if((i>=words.length)&&(!words[i-1].contains("}")))
					{
						ann="Malformed Command: "+temp[1];
						//TODO: Replace with proper error dialog, if necessary
					}
					else
					{
						assert(words[i].contains("}"));
						ann+=" "+words[i].substring(0,words[i].indexOf('}'));
					}
				}

				//Annotation title is currently stored in the GraphTuple's name attribute 
				GraphTuple annTuple=new GraphTuple(ann,x+metrics.stringWidth(temp[0])/2,y-metrics.getAscent()-spaceBetweenLines/2);
				graph.addVertex(annTuple);
				upperTuples.put(annTuple.getX(), annTuple);
				annotations.put(annNumber,annTuple);
				annNumber++;

				g.drawString(temp[0], x, y);

				GraphTuple t1=new GraphTuple(x-metrics.stringWidth(" ")/2,y-metrics.getAscent()-spaceBetweenLines/2);
				graph.addVertex(t1);
				upperTuples.put(x-metrics.stringWidth(" ")/2, t1);

				GraphTuple t2=new GraphTuple(x-metrics.stringWidth(" ")/2,y+metrics.getDescent()+spaceBetweenLines/2);
				graph.addVertex(t2);
				lowerTuples.put(x-metrics.stringWidth(" ")/2, t2);

				graph.addEdge(t1,t2,new DefaultWeightedEdge());
				if(showWordBoundaries)g.drawRect(x-1,y-metrics.getAscent(),metrics.stringWidth(temp[0])+1,metrics.getHeight());
				x+=metrics.stringWidth(temp[0]+" ");
			}

			else //Draw regular word
			{
				//Draw word and add Nodes behind it
				g.drawString(words [i], x, y);

				GraphTuple t1=new GraphTuple(x-metrics.stringWidth(" ")/2,y-metrics.getAscent()-spaceBetweenLines/2);
				graph.addVertex(t1);
				upperTuples.put(x-metrics.stringWidth(" ")/2, t1);

				GraphTuple t2=new GraphTuple(x-metrics.stringWidth(" ")/2,y+metrics.getDescent()+spaceBetweenLines/2);
				graph.addVertex(t2);
				lowerTuples.put(x-metrics.stringWidth(" ")/2, t2);

				graph.addEdge(t1,t2,new DefaultWeightedEdge()); //Vertical edges added here!

				if(showWordBoundaries)g.drawRect(x-1,y-metrics.getAscent(),metrics.stringWidth(words[i])+1,metrics.getHeight());
				x+=metrics.stringWidth(words[i]+" ");
			}
		}

		//Add one final pair of tuples after the last word
		GraphTuple t1=new GraphTuple(x,y-metrics.getAscent()-spaceBetweenLines/2);
		graph.addVertex(t1);
		upperTuples.put(x, t1);

		GraphTuple t2=new GraphTuple(x,y+metrics.getDescent()+spaceBetweenLines/2);
		graph.addVertex(t2);
		lowerTuples.put(x, t2);

		graph.addEdge(t1,t2,new DefaultWeightedEdge());
		//System.out.println(graph.getEdgeWeight(graph.getEdge(t1,t2)));

		//Add pair of tuples at right border
		t1=new GraphTuple(rightTextBorder,y-metrics.getAscent()-spaceBetweenLines/2);
		if(graph.addVertex(t1))
		{
			upperTuples.put(rightTextBorder, t1);
		}
		else
		{
			t1=upperTuples.lastEntry().getValue();
		}

		t2=new GraphTuple(rightTextBorder,y+metrics.getDescent()+spaceBetweenLines/2);
		if(graph.addVertex(t2))
		{
			lowerTuples.put(rightTextBorder, t2);
		}
		else
		{
			t2=lowerTuples.lastEntry().getValue();
		}
		//Connect last tuples in the final three lines
		if(lastTuple!=null)
		{
			if(!graph.containsEdge(lastTuple,t1))graph.addEdge(lastTuple,t1);
		}
		if(!graph.containsEdge(t1,t2))graph.addEdge(t1,t2);



		//Connect tuples adjacent to the last line
		graph=(ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>) finishTupleLine(upperTuples,graph);
		graph=(ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>) finishTupleLine(lowerTuples,graph);

		visualizeGraph(graph,g,Color.LIGHT_GRAY);
		routeAnnotationsGreedy(graph,annotations,g);
		//drawAnnotations(g);
	}

	/**
	 * Helper/Debug method - draws a simple visualization of the supplied graph
	 * NOTE: The current implementation will draw all nodes multiple times, once for each of their edges.
	 * @param graph the graph
	 * @param g the Graphics object of the visualization's basis
	 * @param col the color that will be used to draw the graph - the previously used color will be restored afterwards. 
	 * 
	 */
	private void visualizeGraph(Graph<GraphTuple,DefaultWeightedEdge>graph, Graphics g, Color col)
	{
		Color oldColor=g.getColor();
		g.setColor(col);
		Set<DefaultWeightedEdge> edges=graph.edgeSet();
		Iterator<DefaultWeightedEdge> it=edges.iterator();

		while(it.hasNext())
		{
			DefaultWeightedEdge edge=it.next();
			GraphTuple t1=graph.getEdgeSource(edge);
			GraphTuple t2=graph.getEdgeTarget(edge);

			g.fillOval(t1.getX()-2, t1.getY()-2, 4, 4);//NOTE: This causes Nodes to be drawn multiple times, once for each of their edges
			g.fillOval(t2.getX()-2, t2.getY()-2, 4, 4);
			g.drawLine(t1.getX(), t1.getY(), t2.getX(), t2.getY());
		}
		g.setColor(oldColor);
	}

	/**
	 * Greedy routing algorithm - prioritizes going as far up as possible to maximize the space remaining for other annotations. 
	 * NOTE: This method currently depends on the annotations' text being stored in a separate array - if this changes, the method needs to be reworked.
	 *  
	 * @param graph The graph containing all possible routes
	 * @param annotations A TreeMap containing the starting nodes for each route, ordered by their place in the text
	 * @param g The Graphics-object used to draw on the canvas. Not actively used in this method, but needed in calls to other methods.
	 */
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
	}

	/**
	 * Draws the path created by the routing and - if applicable - the annotation it is connected to.
	 * Afterwards it updates nextAnnotationPos, which is needed for subsequent routings.
	 * Edges are also marked as "used" here, was it is easy to distinguish between completed and aborted routings at this point.
	 * @param g The Graphics object - used to draw and retrieve font-metrics
	 * @param graph the Graph that the routes are based on
	 * @param path The route that was created by routeAnnotations()
	 * @param annNumber the Number of the annotation that belongs to the routing
	 */
	private void drawAnnotation(Graphics g, WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, GraphWalk<GraphTuple,DefaultWeightedEdge>path, Integer annNumber) {


		if((path.getEndVertex().getX()>=rightTextBorder)||(path.getStartVertex().getX()>=rightTextBorder))//Draw Annotation (only if routing was successful)
		{
			g.setColor(Color.BLACK);
			FontMetrics metrics=g.getFontMetrics(); 

			int x=rightTextBorder+3;
			int y=(path.getEndVertex().getX()>=rightTextBorder) ? (path.getEndVertex().getY()) : (path.getStartVertex().getY());
			y+=metrics.getAscent();

			int startpos=y;
			String words[];
			//Retrieving annotation text
			if(path.getStartVertex().getX()<path.getEndVertex().getX())
			{
				words=path.getStartVertex().getName().split(" ");
			}
			else
			{
				words=path.getEndVertex().getName().split(" ");
			}

			for(int w=0;w<words.length;w++)
			{
				if((x+metrics.stringWidth(words[w]))>(rightBorder-3))
				{
					x=rightTextBorder+3;
					y+=metrics.getHeight()+spaceBetweenLines;
				}
				g.drawString(words[w], x, y);
				x+=metrics.stringWidth(words[w]+" ");
			}
			g.drawRect(rightTextBorder+1, startpos-metrics.getAscent(), rightBorder-rightTextBorder-1, y+spaceBetweenLines/2+metrics.getHeight()-startpos);
			y+=metrics.getHeight()+spaceBetweenLines;
			x=rightTextBorder+3;
			nextAnnotationPos=y;
			g.setColor(Color.BLUE);

		}
		else
		{
			g.setColor(Color.RED);
		}
		//Draw Route or its fragment

		List<GraphTuple> nodeList=path.getVertexList();

		if(nodeList.size()>=2)
		{
			Iterator<GraphTuple> it=nodeList.iterator();
			GraphTuple nextTuple=it.next();

			while(it.hasNext())
			{
				GraphTuple oldTuple=nextTuple;
				nextTuple=it.next();

				g.drawLine(oldTuple.getX(),oldTuple.getY(),nextTuple.getX(),nextTuple.getY());
				DefaultWeightedEdge temp=graph.getEdge(oldTuple, nextTuple);
				graph.setEdgeWeight(temp, graph.getEdgeWeight(temp)-1);
			}
		}
		else//Mark blocked off source node
		{
			assert(nodeList.size()==1);//Path should always at least contain the starting node
			GraphTuple temp=path.getStartVertex();
			g.fillRect(temp.getX()-2,temp.getY()-2,4,4);
		}
		g.setColor(Color.BLACK);
	}
}
