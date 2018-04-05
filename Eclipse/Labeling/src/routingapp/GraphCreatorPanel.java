package routingapp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
	
	//DEBUG VALUES
	private boolean testMode=true;//Toggles whether the program is in testing mode. If true, visualization is turned off, and multiple texts will be generated and routed. Overrides all other debug values.
	private boolean showWordBoundaries=false;//Draws rectangles around detected word boundaries in main text, if set to true
	private boolean showGraphGrid=false;//Draws the whole routing Graph 
	private boolean hideLeaders=false; //Hides the leaders and unsuccessfully routed nodes, if some other feature needs to be inspected visually
	private boolean hideText=false;//Disables drawing the text on-screen.
	//DEBUG VALUES END

	private TextGenerator gen=new TextGenerator(0);//<--Temporary value, will be changed before usage.
	private int numberOfTests=100, textLength=200;
	private int annMean=7, annSTDDevi=30; //Values for mean and standard deviation for normally distributed annotations. annMean is also used for uniformly distributed annotations.
	private String textType="normal"/**//*uniform"/**/;//Changes which type of text is used in the tests. Currently recognized values are "normal" and "uniform".
	private String testResults;
	
	
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

	private int width=600;
	private int height=550;
	private int leftTextBorder, rightTextBorder, leftAnnotationBorder, rightAnnotationBorder;
	private int spaceBetweenLines=6;
	
	private int annotationBorderSize=3;//Distance from annotation content to it's border rectangle
	private int spaceBetweenAnnLines=5;
	private Font AnnotationFont;
	
	private int curveSize=6;

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
	 * Splits the text and displays it on-screen - this is done word by word, as java.awt.font.LineBreakMeasurer doesn't allow us to extract the text line-by-line.
	 * As it is easy to do at this point, the Graph is also constructed here. 
	 * @param g The Graphics object used to display everything - usually provided by Java.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		ZonedDateTime startingTime=ZonedDateTime.now();
		long routingStart = 0;
		
		g.setFont(this.getFont());
		FontMetrics metrics = g.getFontMetrics();
		
		if(testMode)//Turn visualisation down as far as possible
		{
			hideLeaders=true;
			hideText=true;
			showGraphGrid=false;
			showWordBoundaries=false;
			testResults=	"Test started at: "+startingTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)+"\n"+
							"Testing mode: "+textType+" distribution\n"+
							"Font: "+this.getFont().getFontName()+", "+this.getFont().getSize()+" Pt\n"+
							"Text length: "+textLength+" Words.\n"+
							"Mean: "+annMean+"\n"+
							"Standard Deviation: "+annSTDDevi+"\n";
							
		}
		
		for(int repeats=0;(repeats<1)||(testMode&&(repeats<numberOfTests));repeats++)//Completely negligible, unless testMode==true
		{
			updateMeasurements();
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);
			g.setColor(Color.black);
			
			/*gen.setSeed(2925130799913272320L);
			text=gen.generateNormalizedText(annMean, annSTDDevi, textLength);/**/
			
			if(testMode)
			{
				long seed=(long)(Math.random()*Long.MAX_VALUE);
				gen.setSeed(seed);
				
				testResults+="----\n";
				testResults+="Seed: "+seed+"\n";
				
				if(textType.equals("normal"))text=gen.generateNormalizedText(annMean, annSTDDevi, textLength);
				else if(textType.equals("uniform"))text=gen.generateUniformText(annMean, textLength);
				else
				{
					testResults+="Unknown distribution type. Test halted.";
					g.setColor(Color.red);
					String error[]=testResults.split("\\n");
					for(int i=0;i<error.length; i++)
					{
						g.drawString(error[i], 30, 30+i*metrics.getHeight());
					}
					break;
				}
				routingStart=System.nanoTime();
			}
			
			//IMPORTANT: DO NOT use FontMetrics.getHeight() for the graph, FontMetrics.getAscent() plus FontMetrics.getDescent() might not equal FontMetrics.getHeight()!
			//			 It is assumed that all Nodes adjacent to each other share exactly one coordinate, which would be violated for vertically adjacent coordinates if Ascent+Descent!=Height
			int lineHeight=metrics.getAscent()+metrics.getDescent()+spaceBetweenLines;
			int x = leftTextBorder;
			int y = metrics.getAscent()+10;
			
			String words[]=text.split(" ");
			ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge> graph=new ListenableUndirectedWeightedGraph<GraphTuple, DefaultWeightedEdge>(DefaultWeightedEdge.class);
			TreeMap<Integer, GraphTuple> upperTuples=new TreeMap<Integer, GraphTuple>();
			TreeMap<Integer, GraphTuple> lowerTuples=new TreeMap<Integer, GraphTuple>();
			TreeMap<Integer,TreeMap<Integer,GraphTuple>> allLines=new TreeMap<Integer,TreeMap<Integer,GraphTuple>>();
			
			ArrayList<Integer> lineEnds=new ArrayList<Integer>();//Stores the position at which the text ends for each line.
			
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
					
					GraphTuple annTuple=new GraphTuple("Annotated Tuple",x+metrics.stringWidth(temp[0])/2,y-metrics.getAscent()-spaceBetweenLines/2);
					Annotation ann=new Annotation(annText,AnnotationFont,annTuple,spaceBetweenAnnLines,annotationBorderSize);
					annTuple.setAnnotation(ann);
					graph.addVertex(annTuple);
					upperTuples.put(annTuple.getX(), annTuple);
					annotatedTuples.put(annNumber,annTuple);
					annNumber++;
					
					if(!hideText)g.drawString(temp[0], x, y);
					
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
					if(!hideText)g.drawString(words [i], x, y);
					
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
			if(!hideLeaders)//Draw Results
			{
				Iterator<RouteInfo> it=results.iterator();
				while(it.hasNext())
				{
					drawRouteInfo(g,graph,it.next());
				}
			}
			
			if(testMode)//Generate stats for testing mode
			{
				long routingEnd=System.nanoTime();
				testResults+="Time elapsed: "+(routingEnd-routingStart)+" ns\n";
				testResults+=evaluateResults(results);
			}
		}
		if(testMode)//Save results to log file.
		{
			try
			{
				String filename="Test_"+startingTime.format(DateTimeFormatter.ofPattern("uuuu-mm-dd_kk-mm-ss"))+".log";
				filename.replaceAll(":", "");
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
				String[] resultsByLine=testResults.split("\n");
				
				for(int i=0;i<resultsByLine.length;i++)
				{
					writer.write(resultsByLine[i]);
					writer.newLine();
				}
				writer.flush();
				writer.close();
				g.setColor(new Color(0x00A000));
				g.drawString("Results successfully logged under: "+filename, 30, 30);
			}
			catch(IOException e)
			{
				g.setColor(Color.RED);
				g.drawString("An Exception has occurred.", 30, 200);
				g.drawString(e.toString(),30,200+metrics.getHeight());
				System.out.println(e);
			}
		}
		
	}

	/**
	 * Helper/Debug method - draws a simple visualization of the generated graph
	 * NOTE: This implementation will draw all nodes multiple times, once for each of their edges.
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
	 * 		Greedy/Topmost				-		A greedy routing with backtracking, aiming to place the label as high up as possible. Can only place labels separately.
	 * 		Greedy/Topmost (OPO-Leader)	-		Same as above, except it  will use OPO-Leaders to connect to the label instead of S-Leaders.  
	 * 		Greedy/Topmost (2-Pass)		-		Variation on the above, it will adjust all annotation's positions after finding a  place to move them closer to where the routing ends its path through the text. 
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
		
		if(info.isSuccessful())//Draw Annotation (only if routing was successful)
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
		//NOTE: OPO-Leaders currently only support going up!
		
		Graphics2D g2d=(Graphics2D)g;

		List<GraphTuple> nodeList=path.getVertexList();

		if(nodeList.size()>=2)
		{			
			Iterator<GraphTuple> it=nodeList.iterator();
			GraphTuple nextTuple=it.next();
			GraphTuple currentTuple=nextTuple;
			GraphTuple oldTuple=currentTuple;
			
			GraphTuple temp=info.getSource();
			g.fillRect(temp.getX()-2,temp.getY()-2,4,4);
			
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
						route.quadTo(currentTuple.getX(), currentTuple.getY(),currentTuple.getX(), currentTuple.getY()+curveSize*Math.signum(nextTuple.getY()-currentTuple.getY()));
					}
					else
					{
						if((Math.abs(nextTuple.getX()-currentTuple.getX())>=2*curveSize)||(!it.hasNext()))
						{
							route.lineTo(currentTuple.getX(),currentTuple.getY()+curveSize*Math.signum(oldTuple.getY()-currentTuple.getY()));
							route.quadTo(currentTuple.getX(), currentTuple.getY(),currentTuple.getX()+curveSize*Math.signum(nextTuple.getX()-currentTuple.getX()),currentTuple.getY());
						}
						else //Compensation for too tightly packed nodes - this will call Iterator.next()!
						{
							GraphTuple previewTuple=it.next();
							
							if(previewTuple.getX()==nextTuple.getX())//<=>2 Curves close to each other
							{
								route.lineTo(currentTuple.getX(),currentTuple.getY()+curveSize/2*Math.signum(oldTuple.getY()-currentTuple.getY()));
								route.curveTo(currentTuple.getX(),currentTuple.getY()-curveSize/2*Math.signum(oldTuple.getY()-currentTuple.getY()), nextTuple.getX(), nextTuple.getY()+curveSize/2*Math.signum(previewTuple.getY()-nextTuple.getY()), nextTuple.getX(), nextTuple.getY()+curveSize/2*Math.signum(previewTuple.getY()-nextTuple.getY()));
							}
							else if(Math.abs(nextTuple.getX()-currentTuple.getX())<=curveSize)//nextTuple is too close to the curve - we will just go past it
							{
								route.lineTo(currentTuple.getX(),currentTuple.getY()+curveSize*Math.signum(oldTuple.getY()-currentTuple.getY()));
								route.quadTo(currentTuple.getX(), currentTuple.getY(),currentTuple.getX()+curveSize*Math.signum(nextTuple.getX()-currentTuple.getX()),currentTuple.getY());
							}
							else//Proceed regularly, since no double-curve  happened, and nextTuple is far enough away
							{
								route.lineTo(currentTuple.getX(),currentTuple.getY()+curveSize*Math.signum(oldTuple.getY()-currentTuple.getY()));
								route.quadTo(currentTuple.getX(), currentTuple.getY(),currentTuple.getX()+curveSize*Math.signum(nextTuple.getX()-currentTuple.getX()),currentTuple.getY());
								route.lineTo(nextTuple.getX(),nextTuple.getY());
							}
							
							oldTuple=currentTuple;
							currentTuple=nextTuple;
							nextTuple=previewTuple;
						}
					}
				}
				else
				{
					route.lineTo(currentTuple.getX(),currentTuple.getY());
				}
			}
			if((currentTuple.getY()!=nextTuple.getY())&&(nextTuple.getX()==rightTextBorder))//Adding curve if last routing segment before OPO/S-Leader came from below
			{
				route.lineTo(nextTuple.getX(), nextTuple.getY()+curveSize);
				route.quadTo(nextTuple.getX(), nextTuple.getY(), nextTuple.getX()+curveSize*Math.signum(currentTuple.getY()-nextTuple.getY()), nextTuple.getY());
			}
			else
			{
				route.lineTo(nextTuple.getX(), nextTuple.getY());
			}
			
			//Connection from Graph to the Annotation
			//NOTE: This assumes OPO-Leaders that go upwards!
			if(route.getCurrentPoint().getX()<rightTextBorder)//Compensating for right-to-left routed paths
			{
					route.moveTo(nodeList.get(0).getX(), nodeList.get(0).getY());
			}
			
			if((info.getOpoStart()==null)||(info.getOpoStart()==info.getOpoEnd()))
			{
				route.lineTo(leftAnnotationBorder,currentAnnotationPos);
			}
			else if(Math.abs(info.getOpoStart()-info.getOpoEnd())<2*curveSize) //Bends are too close to each other
			{
				Annotation ann=info.getAnnotation();
				
				route.lineTo(info.getOpoBendPosition()-curveSize/2, route.getCurrentPoint().getY());
				route.curveTo(info.getOpoBendPosition()+curveSize/2,route.getCurrentPoint().getY(), info.getOpoBendPosition()-curveSize/2,ann.getYpos(), info.getOpoBendPosition()+curveSize/2, ann.getYpos());
				route.lineTo(leftAnnotationBorder,ann.getYpos());
			}
			else
			{
				Annotation ann=info.getAnnotation();
				
				route.lineTo(info.getOpoBendPosition()-curveSize, route.getCurrentPoint().getY());
				route.quadTo(info.getOpoBendPosition(), route.getCurrentPoint().getY(), info.getOpoBendPosition(), route.getCurrentPoint().getY()-curveSize);
				route.lineTo(route.getCurrentPoint().getX(), ann.getYpos()+curveSize);
				route.quadTo(route.getCurrentPoint().getX(), ann.getYpos(), route.getCurrentPoint().getX()+curveSize, ann.getYpos());
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
	
	/**
	 * Testing Method - inspects the routing algorithm's results and returns a string containing further information.
	 * Currently measures number and percentage of successfully routed sites, amount and percentage of space used in Labeling Area, and number of P-Segments.  
	 * @param routingInfo Any List object containing all Routing information created by the algorithm.
	 * @return A human-readable String containing the values described above.
	 */
	private String evaluateResults(List<RouteInfo> routingInfo)
	{
		String results="";
		int psegments=0;
		int numSuccess=0, numTotal=0;
		int annSpaceUsed=0;
		
		Iterator<RouteInfo> it=routingInfo.iterator();
		
		while(it.hasNext())//Go through individual routings
		{
			RouteInfo info=it.next();
			numTotal++;
			if(info.isSuccessful())
			{
				numSuccess++;
				annSpaceUsed+=info.getAnnotation().calculateHeight(rightAnnotationBorder-leftAnnotationBorder);
				GraphWalk<GraphTuple, ? extends DefaultWeightedEdge> path=info.getPath();
				Iterator<GraphTuple> vertices=path.getVertexList().iterator();
				GraphTuple oldTuple=vertices.next();
				
				while(vertices.hasNext())//Go through path vertex by vertex to determine the locations of the P-Segments.
				{
					GraphTuple newTuple=vertices.next();
					if(newTuple.getY()!=oldTuple.getY()) psegments++;
					oldTuple=newTuple;
				}
			}
		}
		
		results+=	"Successful Routings: "+numSuccess+"/"+numTotal+"("+(Math.round(10000*(((double)numSuccess)/((double)numTotal)))/100)+"%)\n"+
					"Space used by Annotations: "+annSpaceUsed+"/"+height+"("+(Math.round(10000*(((double)annSpaceUsed)/((double)height)))/100)+"%)\n"+
					"P-Segments in Text Area: "+psegments+"\n";
		
		return results;
	}
}

