package routingapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
					"Manchmal Sätze, die alle Buchstaben des Alphabets enthalten - man nennt diese Sätze »Pangrams«. " +
					"Sehr bekannt ist dieser: The quick brown fox jumps over the lazy old dog. " + 
					"Oft werden in Typoblindtexte auch fremdsprachige Satzteile eingebaut (AVAIL® and Wefox™ are testing aussi la Kerning), " + 
					"um die Wirkung in anderen Sprachen zu testen. In Lateinisch sieht zum Beispiel fast jede Schrift gut aus. " + 
					"Quod erat demonstrandum. Seit 1975 fehlen in den meisten Testtexten die Zahlen, " +
					"weswegen nach TypoGb. 204 § ab dem Jahr 2034 Zahlen in 86 der Texte zur Pflicht werden. " +
					"Nichteinhaltung wird mit bis zu 245 € oder 368 $ bestraft." +
					"Genauso wichtig in sind mittlerweile auch Âçcèñtë,\\note{How do you pronounce that?} die in neueren Schriften aber fast immer enthalten sind. " +
					"Ein\\note{Hier waren mal Probleme} wichtiges aber schwierig zu integrierendes Feld sind OpenType-Funktionalitäten. " +
					"Je nach Software und Voreinstellungen können eingebaute\\note{Annotations might be longer than the remaining text if its text is excessively long and there's not enough space to route upwards tough} " +
					"Kapitälchen, Kerning oder Ligaturen (sehr pfiffig) nicht richtig dargestellt werden.\\note{test test}";/**/
	//Source: http://www.blindtextgenerator.de/

	private boolean showWordBoundaries=false;//Debug-value, draws rectangles around detected word boundaries in main text, if set to true

	private int width=550;
	private int height=600;
	private int leftTextBorder, rightTextBorder, leftAnnotationBorder, rightAnnotationBorder;
	private int upperBorder=10;
	private int spaceBetweenLines=6;

	private int nextAnnotationPos=0;
	
	private String routingtype="greedytop";//Change this to change the type of routing - see also GraphCreatorPanel.getRouter()
	
	private Font AnnotationFont;

	public Dimension getPreferredSize() {
		return new Dimension(width,height);//NOTE: Causes varying preferred sizes if called after window resizes -> Bad idea?
	}

	public void setFont(Font font) {
		super.setFont(font);
		this.AnnotationFont=new Font(font.getFontName(),font.getStyle(),((font.getSize()>2)?(font.getSize()-2):(font.getSize())));
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
		leftAnnotationBorder=(int) (width*0.76);
		rightAnnotationBorder=(int) (width*0.95);

		//resetting routing values, since routing needs to be re-done
		nextAnnotationPos=0;
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
	 * As it is easy to do at this point, the Graph is also constructed here. 
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

		//IMPORTANT: DO NOT use FontMetrics.getHeight() for the graph, FontMetrics.getAscent() plus FontMetrics.getDescent() might not equal FontMetrics.getHeight()!
		//It is assumed that all Nodes adjacent to each other share exactly one coordinate, which would be violated for vertically adjacent coordinates if Ascent+Descent!=Height
		int lineHeight=metrics.getAscent()+metrics.getDescent()+spaceBetweenLines;
		int x = leftTextBorder;
		int y = metrics.getAscent()+10;

		String words[]=text.split(" ");
		ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge> graph=new ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		TreeMap<Integer, GraphTuple> upperTuples=new TreeMap<Integer, GraphTuple>();
		TreeMap<Integer, GraphTuple> lowerTuples=new TreeMap<Integer, GraphTuple>();
		TreeMap<Integer,TreeMap<Integer,GraphTuple>> allLines=new TreeMap<Integer,TreeMap<Integer,GraphTuple>>();
		
		ArrayList<Integer> lineEnds=new ArrayList<Integer>();//Saves the position at which the text ends for each line.

		TreeMap<Integer,GraphTuple> annotatedTuples=new TreeMap<Integer,GraphTuple>();
		int annNumber=0;
		Routing router=getRouter(routingtype);

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
				
				lineEnds.add(x);
				
				//Create single tuple at text border, it will be propagated downwards
				if(allLines.isEmpty())
				{
					t1=new GraphTuple(rightTextBorder,y-metrics.getAscent()-spaceBetweenLines/2);
					if(graph.addVertex(t1)==true)
					{
						upperTuples.put(rightTextBorder, t1);
					}
				}
				
				//Creating additional tuples after end of current line
				Entry<Integer,GraphTuple> temp=upperTuples.higherEntry(x);
				while(temp!=null)
				{
					t2=new GraphTuple(temp.getValue().getX(),y+metrics.getDescent()+spaceBetweenLines/2);
					graph.addVertex(t2);
					lowerTuples.put(t2.getX(), t2);
					graph.addEdge(temp.getValue(),t2);
						
					temp=upperTuples.higherEntry(temp.getKey());
				}

				//Reset drawing coordinates and move lists of tuples adjacent to new line to correct labels
				
				allLines.put(y, upperTuples);
				upperTuples=lowerTuples;
				lowerTuples=new TreeMap<Integer,GraphTuple>();
				
				
				x=leftTextBorder;
				y+=lineHeight;
			}

			//detect possible annotations for current word - Only detects one annotation per word so far!
			if(words[i].contains("\\note"))
			{
				String temp[]=words[i].split("\\\\");
				//Create annotation
				String annText="";
				if(temp[1].contains("{")&&temp[1].contains("}"))//Currently assumes there's only one Argument 
				{
					annText=temp[1].substring(temp[1].indexOf('{')+1, temp[1].lastIndexOf('}'));
				}
				else
				{
					annText=temp[1].substring(temp[1].indexOf('{')+1);
					i++;
					while((i<words.length)&&(!words[i].contains("}")))
					{
						annText+=" "+words[i];
						i++;
					}
					if((i>=words.length)&&(!words[i-1].contains("}")))
					{
						annText="Malformed Command: "+temp[1];
						//TODO: Replace with proper error dialog, if necessary
					}
					else
					{
						assert(words[i].contains("}"));
						annText+=" "+words[i].substring(0,words[i].indexOf('}'));
					}
				}

				//Annotation title is currently stored in the GraphTuple's name attribute 
				GraphTuple annTuple=new GraphTuple("Annotated Tuple",x+metrics.stringWidth(temp[0])/2,y-metrics.getAscent()-spaceBetweenLines/2);
				Annotation ann=new Annotation(annText,AnnotationFont,annTuple);
				graph.addVertex(annTuple);
				upperTuples.put(annTuple.getX(), annTuple);
				annotatedTuples.put(annNumber,annTuple);
				annNumber++;

				g.drawString(temp[0], x, y);

				GraphTuple t1=new GraphTuple(x-metrics.stringWidth(" ")/2,y-metrics.getAscent()-spaceBetweenLines/2);
				graph.addVertex(t1);
				upperTuples.put(x-metrics.stringWidth(" ")/2, t1);

				GraphTuple t2=new GraphTuple(x-metrics.stringWidth(" ")/2,y+metrics.getDescent()+spaceBetweenLines/2);
				graph.addVertex(t2);
				lowerTuples.put(x-metrics.stringWidth(" ")/2, t2);

				graph.addEdge(t1,t2,new DefaultWeightedEdge());//Add Horizontal edge
				
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

				graph.addEdge(t1,t2,new DefaultWeightedEdge()); //Horizontal edges added here!

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
		
		lineEnds.add(x);
		
		//Creating additional tuples after end of current line (Last line only)
		Entry<Integer,GraphTuple> temp=upperTuples.higherEntry(x);
		while(temp!=null)
		{
			t2=new GraphTuple(temp.getValue().getX(),y+metrics.getDescent()+spaceBetweenLines/2);
			graph.addVertex(t2);
			lowerTuples.put(t2.getX(), t2);
			graph.addEdge(temp.getValue(),t2);
				
			temp=upperTuples.higherEntry(temp.getKey());
		}
		
		allLines.put(y, upperTuples);
		allLines.put((y+lineHeight),lowerTuples);
		
				
		//Adding more extra nodes after the end of shorter lines - this time coming from below
		
		Entry<Integer,TreeMap<Integer,GraphTuple>> prev=allLines.lowerEntry(y);
		int i=lineEnds.size()-1;
		while(prev!=null)
		{
			TreeMap<Integer,GraphTuple> prevLine=prev.getValue();
			
			temp=lowerTuples.higherEntry(lineEnds.get(i));
			while(temp!=null)
			{
				//Check if corresponding tuple already exists because of downward propagation
				if(!upperTuples.containsKey(temp.getKey()))
				{
					t2=temp.getValue();
					t1=new GraphTuple(t2.getX(),t2.getY()-lineHeight);
					graph.addVertex(t1);
					upperTuples.put(t1.getX(),t1);
					graph.addEdge(t1, t2);
					
				}
				temp=lowerTuples.higherEntry(temp.getKey());
			}
			graph=(ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>) finishTupleLine(lowerTuples,graph);
			
			i--;
			lowerTuples=upperTuples;
			upperTuples=prevLine;
			prev=allLines.lowerEntry(prev.getKey());
		}
		
		//Additional iteration for the top line
		temp=lowerTuples.higherEntry(lineEnds.get(0));
		while(temp!=null)
		{
			//Check if corresponding tuple already exists because of downward propagation
			if(!upperTuples.containsKey(temp.getKey()))
			{
				t2=temp.getValue();
				t1=new GraphTuple(t2.getX(),t2.getY()-lineHeight);
				graph.addVertex(t1);
				upperTuples.put(t1.getX(),t1);
				graph.addEdge(t1, t2);
				
			}
			temp=lowerTuples.higherEntry(temp.getKey());
		}
		
		graph=(ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>) finishTupleLine(upperTuples,graph);
		graph=(ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>) finishTupleLine(lowerTuples,graph);

		visualizeGraph(graph,g,Color.LIGHT_GRAY);
		
		//TODO: Program out findRoutes() and relpace this part with a single call.
		Entry<Integer,GraphTuple> currentEntry=annotatedTuples.firstEntry();
		while(currentEntry!=null)
		{
			router.updateNextAnnotationPos(nextAnnotationPos);
			GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> result=router.findRouteFor(graph, currentEntry.getValue());
			currentEntry.getValue().getAnnotation().setRoute(result);
			drawAnnotation(g, graph, result);
			currentEntry=annotatedTuples.higherEntry(currentEntry.getKey());
		}
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
	 * Creates an instance of the requested routing type, if supported.
	 * Currently recognized routing types are:
	 * 		greedytop	-	A greedy routing with backtracking, aiming to place the label as high up as possible.
	 * @param routingType The parameter determining the type of routing that will be used.
	 * @return
	 * @throws
	 */
	private Routing getRouter(String routingType)
	{
		if(routingType.equals("greedytop"))
		{
			return new GreedyTopRouting(rightTextBorder);
		}
		else//Argument not recognized.
		{
			throw new IllegalArgumentException("Unknown routing type: \""+routingType+"\"");
		}
	}

	/**
	 * Draws the path created by the routing and - if applicable - the annotation it is connected to.
	 * Afterwards it updates nextAnnotationPos, which is needed for subsequent routings.
	 * Edges are also marked as "used" here, was it is easy to distinguish between completed and aborted routings at this point.
	 * @param g The Graphics object - used to draw and retrieve font-metrics
	 * @param graph the Graph that the routes are based on
	 * @param path The route that was created by routeAnnotations()
	 */
	private void drawAnnotation(Graphics g, WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, GraphWalk<GraphTuple,? extends DefaultWeightedEdge>path) {


		if((path.getEndVertex().getX()>=rightTextBorder)||(path.getStartVertex().getX()>=rightTextBorder))//Draw Annotation (only if routing was successful)
		{
			g.setColor(Color.BLACK);

			
			//TODO: Replace "magic number" (3) with variable - Location, name yet unknown
			int x=leftAnnotationBorder+3,y;
			
			Annotation ann;
			
			
			String words[];
			//Identifying which end of the path connects to the annotated word, and extracting the annotation and its location from the respective ends. 
			if(path.getStartVertex().getX()<path.getEndVertex().getX())
			{
				y=path.getEndVertex().getY();
				ann=path.getStartVertex().getAnnotation();
				words=ann.getText().split(" ");
			}
			else
			{
				y=path.getStartVertex().getY();
				ann=path.getEndVertex().getAnnotation();
				words=ann.getText().split(" ");
			}
			
			Font oldfont=g.getFont();
			g.setFont(ann.getFont());
			FontMetrics metrics=ann.getFontMetrics();
			
			y+=metrics.getAscent();
			int startpos=y;

			for(int w=0;w<words.length;w++)
			{
				if((x+metrics.stringWidth(words[w]))>(rightAnnotationBorder-3))
				{
					x=leftAnnotationBorder+3;
					y+=metrics.getHeight()+spaceBetweenLines;
				}
				g.drawString(words[w], x, y);
				x+=metrics.stringWidth(words[w]+" ");
			}
			int annHeight=ann.calculateHeight(rightAnnotationBorder-leftAnnotationBorder-2*3, spaceBetweenLines);
			
			g.drawRect(leftAnnotationBorder, startpos-metrics.getAscent(), rightAnnotationBorder-leftAnnotationBorder, annHeight);
			y+=metrics.getHeight()+spaceBetweenLines;
			//x=leftAnnotationBorder+3;
			nextAnnotationPos=startpos+annHeight;
			g.setFont(oldfont);
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

