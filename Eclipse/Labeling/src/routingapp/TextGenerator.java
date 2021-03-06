package routingapp;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * String-generating class. Generates correctly formatted texts for use in the routing application.
 * NOTE: Generating words is done the same way in all methods - it should be put into a separate method.
 * 
 * @author Jakob Klinger
 *
 */
public class TextGenerator {

	/*Probability that a word has at least this many characters:
	 * 
	 * 
	 * For all analyzed words:
	 * 1	1
	 * 2	0.659639992
	 * 3	0.20638369
	 * 4	0.095874714
	 * 5	0.06834893
	 * 6	0.055361917
	 * 7	0.047579259
	 * 8	0.040930577
	 * 9	0.035344251 
	 * 10	0.030187643
	 * 11	0.024911669
	 * 12	0.020996467
	 * 13	0.017296123
	 * 14	0.013034759
	 * 15	0.009859626
	 * 16	0.006684492
	 * >16	0.003342246
	 * 
	 * For Content words:
	 * 1	1
	 * 2	0.907959418
	 * 3	0.803367849
	 * 4	0.652755988
	 * 5	0.537914444
	 * 6	0.465327895
	 * 7	0.404873967
	 * 8	0.353205732
	 * 9	0.305093609
	 * 10	0.263047798
	 * 11	0.218073423
	 * 12	0.183662797
	 * 13	0.15186696
	 * 14	0.114213994
	 * 15	0.086392637
	 * 16	0.058571279
	 * >16	0.02928564
	 * 
	 * Data taken from https://www.sciencedirect.com/science/article/pii/S0019995858902298 (Table III) and converted to percentages (See Tab3Calc.xslx for calculations.)
	 * 
	 * Custom "longer words" probability:
	 * 1	1
	 * 2	0.957959418
	 * 3	0.903367849
	 * 4	0.852755988
	 * 5	0.737914444
	 * 6	0.665327895
	 * 7	0.604873967
	 * 8	0.553205732
	 * 9	0.405093609
	 * 10	0.363047798
	 * 11	0.318073423
	 * 12	0.283662797
	 * 13	0.21186696
	 * 14	0.154213994
	 * 15	0.126392637
	 * 16	0.088571279
	 * >16	0.02928564
	 */
	
	double wordProb[]= /*{1,//Longer words
	 					0.957959418,
	 					0.903367849,
	 					0.852755988,
	 					0.737914444,
	 					0.665327895,
	 					0.604873967,
	 					0.553205732,
	 					0.405093609,
	 					0.363047798,
	 					0.318073423,
	 					0.283662797,
	 					0.21186696,
	 					0.154213994,
	 					0.126392637,
	 					0.088571279,
	 					0.02928564,
						-1};
				/*/	  {	1,//Content words
						0.907959418,
						0.803367849,
						0.652755988,
						0.537914444,
						0.465327895,
						0.404873967,
						0.353205732,
						0.305093609,
						0.263047798,
						0.218073423,
						0.183662797,
						0.15186696,
						0.114213994,
						0.086392637,
						0.058571279,
						0.02928564,
						-1};/**///<--Actual Probability is 0, but the RNG might still generate 0.0f 
	
	Random rng;
	
	/**
	 * Creates a new instance of the text generator.
	 * @param seed The seed used for the random number generator.
	 */
	public TextGenerator(long seed)
	{
		rng=new Random(seed);
	}
	
	/**
	 * Sets a new seed for the random number generator.
	 * @param seed The seed used for the random number generator.
	 */
	public void setSeed(long seed)
	{
		rng.setSeed(seed);
	}
	
	/**
	 * Generates a text made up of random alphabetic characters with spaces and uniformly distributed annotations.
	 * @param annNum Determines the number of annotations that appear in the text - at most one per word. 
	 * @param textLength Length of the generated text in words
	 * @return The text generated by this function as a string.
	 */
	String generateUniformText(int annNum, int textLength)
	{
		String text="";
		if(annNum>textLength)annNum=textLength; //There can't be more than one annotation per word
		ArrayList<Integer> annLocations=new ArrayList<Integer>(annNum);
		for(int i=0;i<annNum;i++)
		{
			int rand=rng.nextInt(textLength);
			while(annLocations.contains(rand))
			{
				rand=rng.nextInt(textLength);
			}
			annLocations.add(rand);
		}
		for(int i=0; i<textLength;i++)
		{
			String word="";
			float result=rng.nextFloat();
			//System.out.println("Rolled "+result);
			for(int j=0;(result<=wordProb[j]);j++)
			{
				if((j==0)&&(rng.nextFloat()<0.125f))
				{
					word+=(char)('A'+rng.nextInt(25));
				}
				else
				{
					word+=(char)('a'+rng.nextInt(25));
				}
				result=rng.nextFloat();
			}
			text+=word;
			
			if(annLocations.contains(i))//generate Annotation
			{
				text+="\\note{";
				
				do{
					word="";
					for(int j=0;(rng.nextFloat()<=wordProb[j]);j++)
					{
						if((j==0)&&(rng.nextFloat()<0.125f))
						{
							word+=(char)('A'+rng.nextInt(25));
						}
						else
						{
							word+=(char)('a'+rng.nextInt(25));
						}
					}
					text+=word+" ";
				}while(rng.nextFloat()>0.2);
				text=text.substring(0,text.lastIndexOf(' ')-1)+"}";
				annLocations.remove((Integer)i);//Might not be necessary
			}
			text+=" ";
			
		}
		return text;
	}

	
	/**
	 * Generates a text with words made of random alphabetic characters and normally distributed annotations, focused on a region of the text.
	 * To do so, this method must use the same process of dividing the text into separate lines as GraphCreatorPanel.java.
	 * Any changes to that process must be applied here as well, or unintended results will happen!  
	 * @param textLength The length of the text in words
	 * @param annNum How many annotations the text will contain. 
	 * @param textWidth Space available to the text, measured in pixels - needed to split the text into lines to define text regions.
	 * @param metrics The FintMetrics object generated by where the text will be displayed. Used to measure the dimensions of individual words. 
	 * @param annMode 	Defines the region the annotations are clustered in.
	 * 					Currently recognized values are "left", "right", "top" and "bottom", as well as combinations, such as "top-right".
	 * 					For unknown values, the annotations will be clustered in the center of the text.
	 *  
	 * @return The text generated by this function as a string.
	 */
	String generateNormalizedText(int annNum, int textLength, int textWidth, FontMetrics metrics, String annMode)
	{
		String text="";
		double vertMean=0.5, horizMean=0.5;//Default values - remain unchanged if annMode==center
		double vertStDev=0.33/2, horizStDev=0.33/2;
		
		if(annMode.contains("top"))
		{
			vertMean=0.25;
		}
		else if(annMode.contains("bottom"))
		{
			vertMean=0.75;
		}
		if(annMode.contains("left"))
		{
			horizMean=0.25;
		}
		else if(annMode.contains("right"))
		{
			horizMean=0.75;
		}
		
		int xPos=0;
		
		for(int i=0; i<textLength;i++)//Generate text only - this needs to be done first, or we won't know which word is in which text region.
		{
			String word="";
			float result=rng.nextFloat();
			for(int j=0;(result<=wordProb[j]);j++)
			{
				if((j==0)&&(rng.nextFloat()<0.125f))
				{
					word+=(char)('A'+rng.nextInt(25));
				}
				else
				{
					word+=(char)('a'+rng.nextInt(25));
				}
				result=rng.nextFloat();
			}
			if(xPos+metrics.stringWidth(word+" ")>textWidth)
			{
				text+="\n";
				xPos=0;
			}
			text+=word+" ";
			xPos+=metrics.stringWidth(word+" ");
			
		}
		
		String textByLine[]=text.split("\n");
		
		//Generate annotations
		//NOTE: Using GraphTuples for positions mostly because Java has no in-built Tuple class. Coordinates used here are based on lines and character count, whereas 
		TreeSet<GraphTuple> locations=new TreeSet<GraphTuple>(new Comparator<GraphTuple>(){

			@Override
			public int compare(GraphTuple o1, GraphTuple o2) {
				int ret;//Tuples will be ordered by their order in the text.
				
				if(o1.equals(o2)) ret=0;
				else if( (o1.getY()<o2.getY()) || ( (o1.getY()==o2.getY()) && (o1.getX()<o2.getX()) ) ) ret=(-1);
				else ret=1;
				return ret;
			}});
		
		while(locations.size()<annNum)//Generate annotation positions
		{
			int line=(int)((rng.nextGaussian()*vertStDev+vertMean)*textByLine.length);//Determine line number of annotation
			line=Math.max(line, 0);//Compensating for out-of-bounds results
			line=Math.min(line, textByLine.length-1);
			
			int posInLine=(int)((rng.nextGaussian()*horizStDev+horizMean)*textByLine[line].length());//Determine vertical position, measured in characters - note that this favors longer words.
			posInLine=Math.max(posInLine, 0);//Compensating for out-of-bounds results
			posInLine=Math.min(posInLine, textByLine[line].length()-1);
			posInLine=posInLine+textByLine[line].substring(posInLine, textByLine[line].length()).indexOf(" ");//Find position of selected word's end
			if(posInLine==-1) posInLine=textByLine[line].length();
			
			GraphTuple newPos=new GraphTuple(posInLine, line);
			if(!locations.contains(newPos))locations.add(newPos);
		}
		
		//Place annotations in text - using reverse reading order because inserting any other way would throw off the positions for annotations further down in the same line. 
		Iterator<GraphTuple> it=locations.descendingIterator();
		
		while(it.hasNext())
		{
			//Generate annotation text
			String annText="\\note{";
			do{
				String word="";
				for(int j=0;(rng.nextFloat()<=wordProb[j]);j++)
				{
					if((j==0)&&(rng.nextFloat()<0.125f))
					{
						word+=(char)('A'+rng.nextInt(25));
					}
					else
					{
						word+=(char)('a'+rng.nextInt(25));
					}
				}
				annText+=word+" ";
			}while(rng.nextFloat()>0.2);
			annText=annText.substring(0,annText.lastIndexOf(' '))+"}";
			
			GraphTuple annPos=it.next();
			String before=textByLine[annPos.getY()].substring(0, annPos.getX());
			String after=textByLine[annPos.getY()].substring(annPos.getX(),textByLine[annPos.getY()].length());
			textByLine[annPos.getY()]=before+annText+after;
		}
		
		//System.out.println(String.join("\n",textByLine));
		
		return String.join("", textByLine);
	}
}
