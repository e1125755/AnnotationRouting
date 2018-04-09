package routingapp;

import java.util.Random;

/**
 * String-generating class. Generates correctly formatted texts for use in the routing application.
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
	 */
	
	double wordProb[]={	1,
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
						-1};//<--Probability is 0, but the RNG might still generate 0.0f 
	
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
	 * Currently unused, since generateNormalizedText produces the same results with a Standard Deviation of 0.
	 * @param annFreq Determines the interval in which annotations appear in the text
	 * @param textLength Length of the generated text in words
	 * @return The text generated by this function as a string.
	 */
	String generateUniformText(int annFreq, int textLength)
	{
		String text="";
		int annCounter=annFreq;
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
				//System.out.println("Rolled "+result);
			}
			//System.out.println(word);
			text+=word+" ";
			annCounter--;
			//System.out.println("AnnCounter="+annCounter);
			
			if(annCounter<=0)//generate Annotation
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
				text=text.substring(0,text.lastIndexOf(' '))+"} ";
				annCounter=annFreq;
			}
			
		}
		return text;
	}
	
	/**
	 * Generates a text with words made of random alphabetic characters and normally distributed annotations. 
	 * @param annMean The mean value of the annotation's distribution function
	 * @param annDevi The standard deviation of the annotation's distribution function
	 * @param textLength The length of the text in words
	 * @return The text generated by this function as a string.
	 */
	String generateNormalizedText(int annMean, int annDevi, int textLength)
	{
		String text="";
		short annCounter=(short)(rng.nextGaussian()*annDevi+annMean);
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
				//System.out.println("Rolled "+result);
			}
			//System.out.println(word);
			text+=word+" ";
			annCounter--;
			//System.out.println("AnnCounter="+annCounter);
			
			if(annCounter<=0)//generate Annotation
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
				text=text.substring(0,text.lastIndexOf(' '))+"} ";
				annCounter=(short)(rng.nextGaussian()*annDevi+annMean);
			}
			
		}
		return text;
	}
	
	public static void main(String args[])
	{
		System.out.println("This is for testing only!");
		long seed=((long)(Math.random()*Long.MAX_VALUE));
		System.out.println("Seed: "+seed+"\n");
		
		
		TextGenerator gen=new TextGenerator(seed);
		System.out.println(gen.generateUniformText((short)15, 200)+"\n");
		
		System.out.println(gen.generateNormalizedText((short)15, (short)2, 200));
	}
}