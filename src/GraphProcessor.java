import java.security.InvalidAlgorithmParameterException;
import java.io.*;
import java.util.*;


/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * @author Brandon Fain
 * @author Owen Astrachan modified in Fall 2023
 * @author Frannie Goodman
 * @author Jackson Cooke
 *
 */
public class GraphProcessor {
    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */

    // include instance variables here
    int edges;
    int vertices;
    Map<Point, List<Point>> map;
    ArrayList<Point> points;
    HashSet<Point> visited;

    public GraphProcessor(){
        edges = 0;
        vertices = 0;
        map = new HashMap<>();
        points = new ArrayList<>();
        visited = new HashSet<>();

    }

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws IOException if file not found or error reading
     */

    public void initialize(FileInputStream file) throws IOException {
        try{
            Scanner scanner = new Scanner(file);
            vertices = scanner.nextInt();
            edges = scanner.nextInt();      
            scanner.nextLine();


            for (int i = 0; i < vertices; i++){
                String[] vert = scanner.nextLine().split(" ");
                double latitude = Double.parseDouble(vert[1]);
                double longitude = Double.parseDouble(vert[2]);
                points.add(new Point(latitude, longitude));
            }

            for (int i = 0; i < edges; i++){
                String line = scanner.nextLine().trim();  
                if (!line.isEmpty()) {
                    String[] edge = line.split(" ");
                    int u = Integer.parseInt(edge[0]);
                    int v = Integer.parseInt(edge[1]);

                    map.putIfAbsent(points.get(u), new ArrayList<>());
                    map.putIfAbsent(points.get(v), new ArrayList<>());
                    map.get(points.get(u)).add(points.get(v));
                    map.get(points.get(v)).add(points.get(u));
                }
            }

            scanner.close();
        }
        catch (Exception error){
            throw new IOException("Could not read .graph file");
        }
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return list of all vertices in graph
     */

    public List<Point> getVertices(){
        return null;
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return all edges in graph
     */
    public List<Point[]> getEdges(){
        return null;
    }

    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * @param p is a point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) {
        double min = Double.MAX_VALUE;
        Point closest = null;
        for (Point curr: map.keySet()){
            double distance = p.distance(curr);
            if (distance < min) min = distance; closest = curr;
        }

        return closest;
    }


    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points, 
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * @param start Beginning point. May or may not be in the graph.
     * @param end Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        double d = 0.0;
        for (int i = 0; i < route.size() - 1; i++){
            d += route.get(i).distance(route.get(i +1)); //distance between each point and the next
        }
        return d;
    }
    

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * @param p1 one point
     * @param p2 another point
     * @return true if and onlyu if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {
        Stack<Point> box = new Stack<>();
        visited.add(p1); box.add(p1);
        while(!box.isEmpty()){
            Point myP = box.pop();
            if(myP.equals(p2)) return true;
            for(Point p : map.get(myP)){
                if (! visited.contains(myP)) visited.add(myP); box.add(myP);
            }
        }

        return false;
    }

    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * @param start Beginning point.
     * @param end Destination point.
     * @return The shortest path [start, ..., end].
     * @throws IllegalArgumentException if there is no such route, 
     * either because start is not connected to end or because start equals end.
     */
    public List<Point> route(Point start, Point end) throws IllegalArgumentException {
        if (start.equals(end)) throw new IllegalArgumentException("start and end are the same");
        if (! connected(start, end)) throw new IllegalArgumentException("start and end aren't connected");

        Map<Point, Double> distMap = new HashMap<>();
        Map<Point, Point> pathMap = new HashMap<>();
        for (Point p: points) distMap.put(p, Double.MAX_VALUE);

        Comparator<Point> pointComparator = new Comparator<Point>(){
            @Override
            public int compare(Point x, Point y){
                return distMap.get(x).compareTo(distMap.get(y));
            }
        };

        PriorityQueue<Point> pq = new PriorityQueue<>(pointComparator);
        Point curr = start;
        distMap.put(start, 0.0);
        pq.add(curr);

        while(pq.size() > 0){
            curr = pq.remove();
            if (curr.equals(end)) break;
            for (Point p: map.get(curr)){
                double dist = distMap.get(curr) + curr.distance(p);
                if (dist < distMap.get(p)){
                    distMap.put(p, dist);
                    pathMap.put(p, curr);
                    pq.add(p);
                    

                }

            }
        }

        if(! curr.equals(end)) throw new IllegalArgumentException("no path exists");

        List<Point> ret = new ArrayList<>();
        curr = end;

        while(curr != null){
            ret.add(curr);
            curr = pathMap.get(curr);
        }
        Collections.reverse(ret);



        return null;
    }
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String name = "data/usa.graph";
        GraphProcessor gp = new GraphProcessor();
        gp.initialize(new FileInputStream(name));
        System.out.println("running GraphProcessor");
    }


    
}
