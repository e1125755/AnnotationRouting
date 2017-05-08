package demos;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JApplet;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class TupleGraph extends JApplet{

	private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );
    
    private JGraphModelAdapter m_jgAdapter;
    
    public void init(  ) {
        // create a JGraphT graph
        ListenableUndirectedWeightedGraph<Tuple<Integer>, Serializable> g = new ListenableUndirectedWeightedGraph<Tuple<Integer>, Serializable>( DefaultWeightedEdge.class);

        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( g );

        JGraph jgraph = new JGraph( m_jgAdapter );

        adjustDisplaySettings( jgraph );
        getContentPane().add( jgraph );
        resize( DEFAULT_SIZE );/**/
        
        Tuple<Integer> t1=new Tuple<Integer>("T1",130,40);
        Tuple<Integer> t2=new Tuple<Integer>("T2",60,200);
        Tuple<Integer> t3=new Tuple<Integer>("T3",310,230);
        Tuple<Integer> t4=new Tuple<Integer>("T4",380,70);

        // add some sample data (graph manipulated via JGraphT)
        g.addVertex( t1 );
        g.addVertex( t2 );
        g.addVertex( t3 );
        g.addVertex( t4 );

        //Form: addEdge(Vertex source, Vertex target, E e) Anm: E ist aus dem Konstruktor
        DefaultWeightedEdge e=new DefaultWeightedEdge();
        g.addEdge( t1, t2,e);
        g.setEdgeWeight(e,2.01);
        //System.out.println(g.getEdgeWeight(e));
        g.addEdge( t2, t3,0);
        g.addEdge( t3, t1, "§" );
        g.addEdge( t4, t3, "test" );
        
        List<Tuple<Integer>> vertexList=new ArrayList<Tuple<Integer>>();
        vertexList.add(t1);
        vertexList.add(t3);
        vertexList.add(t4);

        GraphWalk<Tuple<Integer>,Serializable> walk=new GraphWalk<Tuple<Integer>,Serializable>(g,vertexList,2.0);
        
        List<Tuple<Integer>> pathList=walk.getVertexList(); 
        
        System.out.println(pathList.contains(t2));
        
        //Copy-Paste from above
        /*m_jgAdapter=new JGraphModelAdapter<Tuple<Integer>, Serializable>(walk.getGraph());
        JGraph jgraph=new JGraph(m_jgAdapter);
        
        adjustDisplaySettings( jgraph );
        getContentPane().add( jgraph );
        resize( DEFAULT_SIZE );/**/
        
        
        Iterator<Tuple<Integer>> it=pathList.iterator();
        while(it.hasNext())
        {
        	
        	Tuple<Integer> temp=it.next();
        	System.out.println(temp);
        	
        	//positionVertexAt(temp);
        }
        
        Iterator<Serializable> edges=walk.getEdgeList().iterator();
        
        while(edges.hasNext())
        {
        	System.out.println(edges.next());
        }
        
        // position vertices nicely within JGraph component
        positionVertexAt( t1);
        positionVertexAt( t2);
        positionVertexAt( t3);
        positionVertexAt( t4);/**/

        // that's all there is to it!...
    }
    
    private void adjustDisplaySettings(JGraph jg) {
    	jg.setPreferredSize( DEFAULT_SIZE );

        Color  c        = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter( "bgcolor" );
        }
         catch( Exception e ) {}

        if( colorStr != null ) {
            c = Color.decode( colorStr );
        }

        jg.setBackground( c );
		
	}
    
    private void positionVertexAt( Tuple<Integer> vertex) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
        Map              attr = cell.getAttributes(  );
        Rectangle        b    = GraphConstants.getBounds( attr ).getBounds();

        GraphConstants.setBounds( attr, new Rectangle( vertex.getValue1(), vertex.getValue2(), b.width, b.height ) );

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
        m_jgAdapter.edit( cellAttr, null, null, null );
    }

	private class Tuple<E>
    {
    	private String name;
    	private E value1;
    	private E value2;
    	
    	public Tuple(E e1, E e2)
    	{
    		value1=e1;
    		value2=e2;
    	}
    	public Tuple(String nm, E e1, E e2)
    	{
    		this(e1,e2);
    		name=nm;
    	}
    	
    	public String getName() {
			return name;
		}
    	
		public E getValue1() {
			return value1;
		}
		
		public E getValue2() {
			return value2;
		}
		
		public String toString()
    	{
    		return name+": ("+value1.toString()+" | "+value2.toString()+")";
    	}
    }
}
