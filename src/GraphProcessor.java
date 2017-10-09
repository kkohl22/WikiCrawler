import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Ken Kohl on 3/29/2017.
 */
public class GraphProcessor {

    private static HashMap graph;
    private static ArrayList<ArrayList<String>> SCCs;
    private static int vertices = -1;
    private static ArrayList<String> allVertices = new ArrayList<>();
    private static HashMap<Integer, String> finishTimes = new HashMap<Integer, String>();
    private static String order[];
    private static int counter;

    GraphProcessor(String graphData) {
        graph = connectionsGraph(graphData);
        computeOrder();
        order = new String[graph.size()];
        SCCs = computeSCCs(vertices);
    }

    /*
     * Returns the wikipage with the highest number of out-going links
     */
    public static String highestOut() {
        String highest = "";
        String tmp;
        int current = 0;
        int next;
        Iterator it = graph.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Node node = (Node) pair.getValue();
            tmp = node.getName();
            next = outDegree(tmp);
            if (next > current) {
                current = next;
                highest = tmp + " " + current;
                //highest = tmp;
            }
            node.unmark();
        }
        return highest;
    }

    /**
     * Returns the out degree of v
     *
     * @param v Vertex we want the out degree of
     */
    public static int outDegree(String v) {
        Node node = (Node) graph.get(v);
        ArrayList<String> connections = node.getOutConnections();
        return connections.size();
    }

    /**
     * Decides if u and v belong to the same SCC
     *
     * @param u
     * @param v
     * @return TRUE if u and v belong to the same SCC. False otherwise
     */
    public static Boolean sameComponent(String u, String v) {
        for (ArrayList<String> scc : SCCs) {
            if (scc.contains(v) && scc.contains(u)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns and ArrayList of strings of all the SCCs of v.
     *
     * @param v
     * @return all the vertices that belong to the same Strongly Connected Component of v (including v).
     */
    public static ArrayList<String> componentVertices(String v) {
        for (ArrayList<String> scc : SCCs) {
            if (scc.contains(v)) {
                return scc;
            }
        }
        return null;
    }


    /**
     * Returns the size of the largest component.
     *
     * @return
     */
    public static int largestComponent() {
        int largest = 0;
        for (ArrayList<String> scc : SCCs) {
            if (scc.size() > largest) {
                largest = scc.size();
            }
        }
        return largest;
    }


    /**
     * @return the number of strongly connect components
     */
    public static int numComponents() {
        return SCCs.size();
    }


    /**
     * Returns the BFS path from u to v
     *
     * @param u
     * @param v
     * @return an arrayList of strings that represents the BFS path from u to v
     */
    public static ArrayList<String> bfsPath(String u, String v) {
        if(GraphProcessor.graph.containsKey(u) && GraphProcessor.graph.containsKey(v)) {

            ArrayList<String> bfsResult;
            Node start = (Node) graph.get(u);
            Node end = (Node) graph.get(v);
            if (u.equals(v)) {
                bfsResult = new ArrayList<String>();
            } else {
                bfsResult = breadthFirstSearch(start, end);
            }

            return bfsResult;

        }

        return new ArrayList<String>();
    }

    private static ArrayList<String> breadthFirstSearch(Node start, Node finish) {
        if(start.getName() == finish.getName()) return new ArrayList<String>();

        Node currentNode = start;
        Map<Node, Node> bfsTree = new HashMap<Node, Node>();
        Queue<Node> queue = new LinkedList<Node>();
        ArrayList<Node> visitedNodes = new ArrayList<Node>();

        // Add the current node as the starting point
        queue.add(currentNode);
        visitedNodes.add(currentNode);

        //Search.
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            if (currentNode.equals(finish)) {
                break;
            }
            else {
                for (String nextNode : currentNode.getOutConnections()) {
                    Node next = (Node) graph.get(nextNode);

                    if (!visitedNodes.contains(next)) {
                        bfsTree.put(next, currentNode);
                        queue.add(next);
                        visitedNodes.add(next);
                    }
                }
            }
        }

        //If all nodes are explored and the destination node hasn't been found.
        if (!currentNode.equals(finish)) {
            return new ArrayList<String>();
        }

        ArrayList<String> retval = new ArrayList<String>();
        for (Node node = finish; node != null; node = bfsTree.get(node)) {
            retval.add(0, node.getName());
        }

        return retval;
    }

    private static ArrayList<String> getDirections(Node start, Node finish) {
        HashMap nextNodeMap = new HashMap();
        Node currentNode = start;
        ArrayList<String> directions = new ArrayList<>();
        Map<Node, Node> backlinks = new HashMap<Node, Node>();


        //Queue
        Queue<Node> queue = new LinkedList<Node>();
        queue.add(currentNode);
        Set<Node> visitedNodes = new HashSet<Node>();
        visitedNodes.add(currentNode);

        //Search.
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            if (currentNode.equals(finish)) {
                break;
            } else {
                for (String nextNode : currentNode.getOutConnections()) {
                    Node n = (Node) graph.get(nextNode);

                    if (!visitedNodes.contains(n)) {
                        if (!backlinks.containsKey(currentNode)) {
                            backlinks.put(currentNode, n);
                        }
                        queue.add(n);
                        visitedNodes.add(n);

                        //Look up of next node instead of previous.
                        nextNodeMap.put(currentNode, n);
                    }
                }
            }
        }

        //If all nodes are explored and the destination node hasn't been found.
        if (!currentNode.equals(finish)) {
            directions.add("No possible Path!");
            return directions;
        }
        //Reconstruct path. No need to reverse.
        for (Node node = start; node != null; node = (Node) backlinks.get(node)) {
            directions.add(node.name);
        }
        return directions;
    }

    /**
     * Scans the string and creates a hashtable with the first string being the key that maps to an arraylist of out connections.
     *
     * @param s
     * @return
     */
    private HashMap connectionsGraph(String s) {

        HashMap cGraph = new HashMap();
        Scanner lineScanner = new Scanner(s);
        Scanner nodeScanner;
        String rightNode;
        String leftNode;
        while (lineScanner.hasNextLine()) {
            nodeScanner = new Scanner(lineScanner.nextLine());
            if (nodeScanner.hasNext()) {
                if (vertices == -1) {
                    String numVertices = nodeScanner.next();
                    vertices = Integer.parseInt(numVertices);
                    continue;
                }
                leftNode = nodeScanner.next();
                // addNewVertice(leftNode);
                if (!cGraph.containsKey(leftNode)) {
                    allVertices.add(leftNode);
                    // cGraph.put(leftNode, connections);
                    Node node = new Node(leftNode);
                    cGraph.put(leftNode, node);
                }
                if (nodeScanner.hasNext()) {
                    rightNode = nodeScanner.next();
                    if (!cGraph.containsKey(rightNode)) {
                        // cGraph.put(leftNode, connections);
                        allVertices.add(rightNode);
                        Node node = new Node(rightNode);
                        cGraph.put(rightNode, node);
                    }
                    // addNewVertice(rightNode);
                    //  ArrayList<String> connect = (ArrayList<String>) cGraph.get(leftNode);
                    Node connection = (Node) cGraph.get(leftNode);
                    connection.addConnection(rightNode);
                }
            }
        }
        return cGraph;
    }

    private static ArrayList<ArrayList<String>> computeSCCs(int numEdges) {
        ArrayList<ArrayList<String>> sccs = new ArrayList<ArrayList<String>>();

        unMarkAll(graph);
        int index = numEdges;
        while(index > 0) {
            if(finishTimes.containsKey(index)) {
                ArrayList<String> marked = depthFirstSearch(graph, finishTimes.get(index));
                if(marked.size() != 0) {
                    sccs.add(marked);
                }

                int sccLength = marked.size();
                for(int i = 0; i < sccLength; i++) {
                    Node node = (Node) graph.get(marked.get(i));
                    node.setSCC(marked);
/*                    Node node = (Node) graph.get(sccs.get(i));
                    ((Node) graph.get(sccs.get(i))).setSCC(marked);*/
                }
            }

            index--;
        }

        return sccs;
/*        ArrayList<ArrayList<String>> sccs = new ArrayList<ArrayList<String>>();

        unMarkAll(graph);

        int finishTimesLength = finishTimes.size();
        for (int i = finishTimesLength - 1; i >= 0; i--) {
            ArrayList<String> marked = depthFirstSearch(graph, finishTimes.get(i));

            if (marked.size() != 0) sccs.add(marked);

            int sccLength = marked.size();
            for (int j = 0; j < sccLength; j++) {
                Node node = (Node) graph.get(marked.get(j));
                node.setSCC(marked);
            }
        }
        return sccs;*/
    }

    private static void computeOrder() {
        //finishTimes = new ArrayList<String>();

        //HashMap<String, GraphNode> reverse = createReverseGraph();
        //GraphProcessor.reverse = reverse;
        HashMap reverse = reverseGraph();

        Iterator iter = reverse.entrySet().iterator();
        Iterator iterator = reverse.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Node connections = (Node) pair.getValue();

            if(!connections.isMarked()) {
                depthFirstSearch(reverse, connections.getName());
            }
        }
/*        HashMap reverse = reverseGraph();
        Iterator iterator = reverse.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Node connections = (Node) pair.getValue();
            if (!connections.isMarked()) {
                depthFirstSearch(reverse, connections.getName());
            }
        }*/
    }

    private static ArrayList<String> depthFirstSearch(HashMap hashMap, String v) {
        ArrayList<String> marked = new ArrayList<String>();
        Node node = (Node) hashMap.get(v);
        if(node.isMarked()) return marked;

        marked.add(v);
        node.mark();
        //hashMap.get(v).mark();

        ArrayList<String> edges = ((Node) hashMap.get(v)).getOutConnections();
        int edgesLength = edges.size();
        for(int i = 0; i < edgesLength; i++) {
            Node node1 = (Node) hashMap.get(edges.get(i));
            if(!node1.isMarked()) {
                marked.addAll(depthFirstSearch(hashMap, ((Node) hashMap.get(edges.get(i))).getName()));
            }
        }

        counter++;
        finishTimes.put(counter, v);

        return marked;


        /*

        ArrayList<String> marked = new ArrayList<>();
        Node n = (Node) graph.get(v);
        if (n.isMarked()) {
            return marked;
        }

        marked.add(v);
        n.mark();

        ArrayList<String> edges = n.getOutConnections();
        int edgesLength = edges.size();
        for (int i = 0; i < edgesLength; i++) {
            Node node = (Node) graph.get(edges.get(i));
            if (node.isMarked()) {
                marked.addAll(depthFirstSearch(graph, node.getName(), false));
            }
        }

        if (mark) {
            // System.out.println(counter);
            GraphProcessor.finishTimes.add(v);
            GraphProcessor.counter++;
        }

        return marked;*/
    }

    private static void unMarkAll(HashMap hshmp) {
        Iterator it = hshmp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Node node = (Node) pair.getValue();
            node.unmark();
        }
    }

    private static HashMap reverseGraph() {
        HashMap revHashMap = new HashMap();
        Iterator it = graph.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Node connections = (Node) pair.getValue();
            ArrayList<String> arrayList = connections.getOutConnections();
            for (String node : arrayList) {
                if (revHashMap.containsKey(node)) {
                    Node n = (Node) revHashMap.get(node);
                    if (!n.getOutConnections().contains(pair.getKey())) {
                        n.getOutConnections().add((String) pair.getKey());
                    }
                } else {
                    Node toAdd = new Node(node);
                    toAdd.getOutConnections().add((String) pair.getKey());
                    revHashMap.put(node, toAdd);
                }
            }

        }
        for (int i = 0; i < allVertices.size(); i++) {
            String nodeName = allVertices.get(i);
            if (!revHashMap.containsKey(nodeName)) {
                Node node = new Node(nodeName);
                revHashMap.put(nodeName, node);
            }
        }
        return revHashMap;
    }


    private static class Node {
        private ArrayList<String> outConnections;
        private boolean marked;
        private String name;
        private ArrayList<String> scc;

        public Node(String name) {
            outConnections = new ArrayList<>();
            marked = false;
            this.name = name;

        }

        public boolean isMarked() {
            return marked;
        }

        public boolean mark() {
            return marked = true;
        }

        public boolean unmark() {
            return marked = false;
        }

        public ArrayList<String> getOutConnections() {
            return outConnections;
        }

        public String getName() {
            return name;
        }

        public void setSCC(ArrayList<String> scc) {
            this.scc = scc;
        }

        public ArrayList<String> getSCC() {
            return scc;
        }


        public Boolean addConnection(String vertex) {
            if (!outConnections.contains(vertex)) {
                outConnections.add(vertex);
                return true;
            }
            return false;
        }
    }
}
