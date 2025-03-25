package socialnetwork;

import model.ModelParameters;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.SingleGraph;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.graphstream.algorithm.ConnectedComponents;

/**
 * Incorporates a social network graph from the GraphStream framework.
 * 
 * CREATE A WRAPPER JUST FOR STATIC NETWORK... REMOVE ALL THE SF/RANDOM WITH A
 * GENERATOR OFFLINE, CREATE DSG FILES FOR SF, RANDOM, REGULAR (1000 NODES) AND
 * A REAL-WORLD
 * 
 * @author mchica
 * 
 */

public class GraphStreamer {

	// ########################################################################
	// Static
	// ########################################################################

	// types of Social Networks
	public static enum NetworkType {
		SCALE_FREE_NETWORK, RANDOM_NETWORK, SW_NETWORK, WELL_MIXED_POP, EMAIL_NETWORK, PGP_NETWORK, REGULAR_NETWORK
	}

	// ########################################################################
	// Variables
	// ########################################################################

	private Graph graph;

	// treemap with the neighbours of the nodes
	//private TreeMap<Integer, IntArrayList> mapNeighbours;
	
	private UnifiedMap<Integer, IntArrayList> mapNeighbours;

	// ########################################################################
	// Constructors
	// ########################################################################

	/**
	 * Constructor for getting the DSG file (static SN)
	 * 
	 * @param nrNodes
	 * @param _graph
	 * 
	 */
	public GraphStreamer(int nrNodes, ModelParameters params) {

		graph = new SingleGraph("SNFromFile");
		
	}

	/**
	 * with this function we copy the initial network read from a file in order not
	 * to have edges removed and added
	 * 
	 * @param params
	 */
	public void setGraph(ModelParameters params) {

		graph.clear();

		Graphs.mergeIn(graph, params.getGraph());

		// while read we create a hash map with the neighbours
		//this.mapNeighbours = new TreeMap<Integer, IntArrayList>();
		this.mapNeighbours = new UnifiedMap <Integer, IntArrayList>();
	    
		for (int i = 0; i < graph.getNodeCount(); i++) {

			// get neighbours of the i-th node
			IntArrayList neighbors = getNeighborsOfNodeFromGS(i);
			this.mapNeighbours.put(i, neighbors);			
		}
	}

	// ########################################################################
	// Methods/Functions
	// ########################################################################

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	/**
	 * e.
	 * 
	 * @param ind - index of the node.
	 * @return - the neighbors in the ArrayList.
	 */
	public IntArrayList getNeighborsOfNode(int ind) {

		return this.mapNeighbours.get(ind);

	}

	/**
	 * TODO BOTTLENECK Gets the neighbors of the given node.
	 * 
	 * @param ind - index of the node.
	 * @return - the neighbors in the ArrayList.
	 */
	private IntArrayList getNeighborsOfNodeFromGS(int ind) {

		IntArrayList neighbors = new IntArrayList();

		graph.getNode(ind).neighborNodes().forEach(e -> {
			neighbors.add(e.getIndex());
		});

		// Sort the neighbors as their ids are returned in the arbitrary order.
		neighbors.sortThis();

		return neighbors;
	}

	/**
	 * set a value to the attribute of an edge given by two nodes (it creates it if
	 * it doesn't exist)
	 * 
	 * @param ind1  - index of the first node.
	 * @param ind2  - index of the second node.
	 * @param key   - the string key of the attribute of the edge
	 * @param value - the object value to be set for the attribute of the edge
	 */
	public void setAttributeEdge(int ind1, int ind2, String key, Object value) {

		Edge edgeNodes = (graph.getNode(ind1)).getEdgeBetween(ind2);

		if (edgeNodes == null) {
			System.err.println(
					"Error (setAttributeEdge) when getting an edge between node " + ind1 + " and node " + ind2);
		}

		edgeNodes.setAttribute(key, value);
	}

	/**
	 * set the same value to the attributes of the edges of all the neighbors of the
	 * given node (it creates it if it doesn't exist)
	 * 
	 * @param ind   - index of the first node.
	 * @param key   - the string key of the attribute of the edges
	 * @param value - the object value to be set for the attribute of the edges
	 */
	public void setAttributeEdgeAllNeighbours(int ind, String key, Object value) {

		// Iterator<Node> it = graph.getNode(ind).getNeighborNodeIterator();

		graph.getNode(ind).neighborNodes().forEach(e -> {
			Edge edgeNodes = (graph.getNode(ind)).getEdgeBetween(e.getIndex());

			if (edgeNodes == null) {
				System.err.println("Error (setAttributeEdgeAllNeighbours) when getting an " + "edge between node " + ind
						+ " and node " + e.getIndex());
			}

			// set the attribute value
			edgeNodes.setAttribute(key, value);

		});

	}

	/**
	 * get a value of the attribute of an edge given by two nodes (it creates it if
	 * it doesn't exist)
	 * 
	 * @param ind1 - index of the first node.
	 * @param ind2 - index of the second node.
	 * @param key  - the string key of the attribute of the edge
	 * @return the object value of the attribute of the edge
	 */
	public Object getAttributeEdge(int ind1, int ind2, String key) {

		Edge edgeNodes = (graph.getNode(ind1)).getEdgeBetween(ind2);

		if (edgeNodes == null) {
			System.err.println(
					"Error (getAttributeEdge) when getting an edge between node " + ind1 + " and node " + ind2);
		}

		return edgeNodes.getAttribute(key);
	}

	/**
	 * This function add a new edge with another node which has no previous edge
	 * with ind
	 * 
	 * @param ind - index of the source node.
	 * @return the id of the target node for the new edge. Return -1 if not created
	 *         (already has all the possible edges)
	 */
	public int addNewNeighborForNode(int ind, XoRoShiRo128PlusRandom random) {

		if (graph.getNode(ind).getOutDegree() == (graph.getNodeCount() - 1)) {
			// the node has edges with all the remaining nodes
			return -1;
		}

		// at random, get a new node to be connected. It cannot be already connected
		// or be the node itself
		int node2Connect = random.nextInt(graph.getNodeCount());

		while (graph.getNode(ind).hasEdgeBetween(node2Connect) || (node2Connect == ind)) {
			node2Connect = random.nextInt(graph.getNodeCount());
		}

		// till that point we have to nodes to link (it is not efficient but works...)

		String idEdge = String.valueOf(ind) + "_" + String.valueOf(node2Connect);
		graph.addEdge(idEdge, ind, node2Connect, false);

		return node2Connect;
	}

	/**
	 * Remove an edge between the source node given by ind and one of its neighbors
	 * at random
	 * 
	 * @param ind - index of the node.
	 * @return the other node of the removed edge. Returns -1 if not removed (no
	 *         edges before)
	 */
	public int removeNeighborForNode(int ind, XoRoShiRo128PlusRandom random) {

		if (graph.getNode(ind).getOutDegree() == 0) {
			// the node has no edges so we cannot remove anything
			return -1;
		}

		IntArrayList neighbors = getNeighborsOfNode(ind);

		int randomPos = random.nextInt(neighbors.size());

		int node2Disconnect = neighbors.get(randomPos);

		graph.removeEdge(ind, node2Disconnect);

		return node2Disconnect;
	}

	public void cleanGraphSteamer() {
		this.graph = null;
	}

	public double getAvgDegree() {
		return Toolkit.averageDegree(graph);
	}

	public double getDensity() {
		return Toolkit.density(graph);
	}

	public int getConnectedComponents() {
		ConnectedComponents cc = new ConnectedComponents();
		cc.init(this.graph);
		return cc.getConnectedComponentsCount();
	}

}
