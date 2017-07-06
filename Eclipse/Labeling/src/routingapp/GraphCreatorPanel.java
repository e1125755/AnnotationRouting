package routingapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
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

	//DEBUG VALUES
	private boolean showWordBoundaries=false;//draws rectangles around detected word boundaries in main text, if set to true
	private boolean showGraphGrid=true;//Draws the whole routing Graph 
	//DEBUG VALUES END

	private int width=600;
	private int height=550;
	private int leftTextBorder, rightTextBorder, leftAnnotationBorder, rightAnnotationBorder;
	private int spaceBetweenLines=6;
	
	private int annotationBorderSize=3;//Distance from annotation content to it's border rectangle
	private int spaceBetweenAnnLines=4;
	private Font AnnotationFont;
	
	private int curveSize=3;

	private int nextAnnotationPos=0;
	private String routingtype;//Determines the type of routing - see also GraphCreatorPanel.getRouter() and GraphCreatorApplet.algNames[]

	/**
	 * NOTE: Current implementation causes varying preferred sizes if called after a window resize.
	 */
	public Dimension getPreferredSize() {
		return new Dimension(width,height);
	}

	public void setInfo(Font font, String algorithm) {
		super.setFont(font);
		this.AnnotationFont=new Font(font.getFontName(),font.getStyle(),((font.getSize()>2)?(font.getSize()-2):(font.getSize())));
		this.routingtype=algorithm;
		repaint();
	}

	/**
	 * Helper method - updates width, height and related values whenever called.
	 */
	private void updateMeasurements() {
		width=this.getWidth();
		height=this.getHeight();

		rightTextBorder=(int) (width*0.70);
		leftTextBorder=(int) (width*0.05);
		leftAnnotationBorder=(int) (width*0.80);
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
		updateMeasurements();
		
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
						throw new RuntimeException(annText);
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
				Annotation ann=new Annotation(annText,AnnotationFont,annTuple,spaceBetweenAnnLines,annotationBorderSize);
				annTuple.setAnnotation(ann);
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

		if(showGraphGrid)visualizeGraph(graph,g,Color.LIGHT_GRAY);
		
		//Calculate all Routes via Router.findRoutes();

		List<RouteInfo> results=router.findRoutes(graph, annotatedTuples);
		Iterator<RouteInfo> it=results.iterator();
		while(it.hasNext())
		{
			drawRouteInfo(g,graph,it.next());
		}

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
	 * 		Greedy/Topmost			-		A greedy routing with backtracking, aiming to place the label as high up as possible. Can only place labels separately.
	 * 		Greedy/Topmost (2-Pass)	-		Variation on the above, it will adjust all annotation's positions after finding a  place to move them closer to where the routing ends its path through the text. 
	 * @param routingType The parameter determining the type of routing that will be used.
	 * @return
	 * @throws
	 */
	private Routing getRouter(String routingType)
	{
		if(routingType.equals("Greedy/Topmost"))
		{
			return new GreedyTopRouting(rightTextBorder, leftAnnotationBorder, rightAnnotationBorder, spaceBetweenLines);
		}
		else if(routingType.equals("Greedy/Topmost (2-Pass)"))
		{
			return new GreedyTopTwoPassRouting(rightTextBorder, height, leftAnnotationBorder, rightAnnotationBorder, spaceBetweenLines);//Last argument should be replaced with space between annotations, if that becomes it's own thing
		}
		else if(routingType.equals("Greedy/Topmost (OPO-Leader)"))
		{
			return new GreedyTopOpo(rightTextBorder, leftAnnotationBorder, rightAnnotationBorder, spaceBetweenLines);
		}
		else//Argument not recognized.
		{
			throw new IllegalArgumentException("Unknown routing type: \""+routingType+"\"");
		}
	}

	/**
	 * Draws the path created by the routing and - if applicable - the annotation it is connected to.
	 * Afterwards it updates nextAnnotationPos, which might be needed for subsequent routings.
	 * @param g The Graphics object - used to draw and retrieve font-metrics
	 * @param graph the Graph that the routes are based on
	 * @param path The route that was created by routeAnnotations()
	 */
	private void drawRouteInfo(Graphics g, WeightedGraph<GraphTuple,DefaultWeightedEdge> graph, RouteInfo info) {


		int currentAnnotationPos=nextAnnotationPos;
		GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> path=info.getPath();
		
		if((path.getEndVertex().getX()>=rightTextBorder)||(path.getStartVertex().getX()>=rightTextBorder))
			//Draw Annotation (only if routing was successful)
		{
			g.setColor(Color.BLACK);

			int x=leftAnnotationBorder+annotationBorderSize;
			int y=nextAnnotationPos;
			Annotation ann=info.getAnnotation();
			String words[]=ann.getText().split(" ");
			
			Font oldfont=g.getFont();
			g.setFont(ann.getFont());
			FontMetrics metrics=ann.getFontMetrics();
			
			if(ann.getYpos()!=null)
			{
				y=ann.getYpos();
				currentAnnotationPos=y;
			}
				
			else ann.setYpos(y);
			
			y+=metrics.getAscent();
			int startpos=y;

			for(int w=0;w<words.length;w++)//Draw annotation text
			{
				if((x+metrics.stringWidth(words[w]))>(rightAnnotationBorder-annotationBorderSize))
				{
					x=leftAnnotationBorder+annotationBorderSize;
					y+=metrics.getHeight()+ann.getSpaceBetweenLines();
				}
				g.drawString(words[w], x, y);
				x+=metrics.stringWidth(words[w]+" ");
			}
			int annHeight=ann.calculateHeight(rightAnnotationBorder-leftAnnotationBorder);
			
			g.drawRect(leftAnnotationBorder, startpos-metrics.getAscent(), rightAnnotationBorder-leftAnnotationBorder, annHeight);
			
			nextAnnotationPos+=annHeight+ann.getSpaceBetweenLines();
			g.setFont(oldfont);
			g.setColor(Color.BLUE);

		}
		else
		{
			g.setColor(Color.RED);
		}
		//Draw complete or unfinished Route
		//NOTE: Only tested with paths that go up and to the right, although it should be capable of handling any kind of path.
		
		Graphics2D g2d=(Graphics2D)g;

		List<GraphTuple> nodeList=path.getVertexList();

		if(nodeList.size()>=2)
		{			
			Iterator<GraphTuple> it=nodeList.iterator();
			GraphTuple nextTuple=it.next();
			GraphTuple currentTuple=nextTuple;
			GraphTuple oldTuple=currentTuple;
			
			GeneralPath route=new GeneralPath();
			route.moveTo(nextTuple.getX(), nextTuple.getY());

			while(it.hasNext())
			{				
				oldTuple=currentTuple;
				currentTuple=nextTuple;
				nextTuple=it.next();

				if((oldTuple.getX()!=nextTuple.getX())&&(oldTuple.getY()!=nextTuple.getY()))//Curve detection - Draws rounded curves
				{
					if(oldTuple.getY()==currentTuple.getY())
					{
						route.lineTo(currentTuple.getX()+curveSize*Math.signum(oldTuple.getX()-currentTuple.getX()),currentTuple.getY());
						route.curveTo(route.getCurrentPoint().getX(), route.getCurrentPoint().getY(), currentTuple.getX(), currentTuple.getY(),currentTuple.getX(), currentTuple.getY()+curveSize*Math.signum(nextTuple.getY()-currentTuple.getY()));
					}
					else
					{
						route.lineTo(currentTuple.getX(),currentTuple.getY()+curveSize*Math.signum(oldTuple.getY()-currentTuple.getY()));
						route.curveTo(route.getCurrentPoint().getX(), route.getCurrentPoint().getY(), currentTuple.getX(), currentTuple.getY(),currentTuple.getX()+curveSize*Math.signum(nextTuple.getX()-currentTuple.getX()),currentTuple.getY());
					}
				}
				else
				{
					route.lineTo(currentTuple.getX(),currentTuple.getY());
				}
			}
			
			route.lineTo(nextTuple.getX(), nextTuple.getY());
			//TODO: Curve detection if coming from below!
			
			//Connection from Graph to the Annotation
			if(route.getCurrentPoint().getX()!=rightTextBorder)//Compensating for right-to-left routed paths
			{
					route.moveTo(nodeList.get(0).getX(), nodeList.get(0).getY());
			}
			
			if((info.getOpoStart()==null)||(info.getOpoStart()==info.getOpoEnd()))
			{
				route.lineTo(leftAnnotationBorder,currentAnnotationPos);
			}
			else
			{
				Annotation ann=info.getAnnotation();
				
				route.lineTo(info.getOpoBendPosition()-curveSize, route.getCurrentPoint().getY());
				route.curveTo(route.getCurrentPoint().getX(), route.getCurrentPoint().getY(), info.getOpoBendPosition(), route.getCurrentPoint().getY(), info.getOpoBendPosition(), route.getCurrentPoint().getY()-curveSize);
				route.lineTo(route.getCurrentPoint().getX(), ann.getYpos()+curveSize);
				route.curveTo(route.getCurrentPoint().getX(), route.getCurrentPoint().getY(), route.getCurrentPoint().getX(), ann.getYpos(), route.getCurrentPoint().getX()+curveSize, ann.getYpos());
				route.lineTo(leftAnnotationBorder, ann.getYpos());
			}
			
			g2d.draw(route);
		}
		else//Mark blocked off source node
		{
			assert(nodeList.size()==1);//Path should always at least contain the starting node
			GraphTuple temp=info.getSource();
			g.fillRect(temp.getX()-2,temp.getY()-2,4,4);
		}
		g.setColor(Color.BLACK);
	}
}

