import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ken Kohl on 3/28/2017.
 */
public class Playground {
    private static final String BASE_URL = "https://en.wikipedia.org";
    public static void main(String[] args) {
        System.out.println("Hello World! Its Wednesday!!");
        System.out.println("processing site...");

        long start = System.currentTimeMillis();
        WikiCrawler w = new WikiCrawler("/wiki/Computer_Science", 10000, "WikiCS.txt");
        w.crawl();
        long end = System.currentTimeMillis();
        System.out.println("Done");
        long totalTime = TimeUnit.MILLISECONDS.toSeconds((end-start));
        System.out.println("total time: " + totalTime);

        File file = new File("WikiCS.txt");
        String s = readFile(file);
        GraphProcessor graphProcessor = new GraphProcessor(s);
        System.out.println("Highest out vertex is: " + graphProcessor.highestOut());
        System.out.println("Number of components is: " +graphProcessor.numComponents());
        System.out.println("Largest component is: " + graphProcessor.largestComponent());
        ArrayList<String> scc = graphProcessor.componentVertices("/wiki/Computer_Science");
        System.out.println("LAgest is :" + Arrays.toString(scc.toArray()));
        System.out.println();
        System.out.println();
       // graphProcessor.printHashmap();
       // String[] thing = graphProcessor.computerOrder();

        //System.out.println(Arrays.toString(thing));
       // ArrayList<ArrayList<String>> sccs = graphProcessor.computeSCCs(13);
//        System.out.println("Largest is: " + graphProcessor.largestComponent());
//        System.out.println("num Sccs is: " + graphProcessor.numComponents());
//        System.out.println("a belongs to " + Arrays.toString(graphProcessor.componentVertices("a").toArray()));
//        System.out.println("Are a and b int the same component? " + graphProcessor.sameComponent("a", "b"));
//        System.out.println("Are d and h int the same component? " + graphProcessor.sameComponent("d", "h"));
//        System.out.println("Are f and c int the same component? " + graphProcessor.sameComponent("f", "c"));
//        System.out.println("Are f and d int the same component? " + graphProcessor.sameComponent("f", "d"));
        System.out.println();
//        ArrayList<String> path = graphProcessor.bfsPath("f", "d");
 //       System.out.print(Arrays.toString(path.toArray()));

        //System.out.println(Arrays.toString(sccs.toArray()));
        //graphProcessor.printSCCs();
        //graphProcessor.printHashmap();
        //ArrayList<String> order = graphProcessor.printThng("/wiki/Complexity_theory");
        //graphProcessor.printReverse();
        //graphProcessor.printHashmap();
        //graphProcessor.reverseGraph(s);
        //System.out.println(Arrays.toString(order.toArray()));
/*
        System.out.println("Expected out degree is 8" );
        System.out.println("Out degree of /wiki/Chaos_theory is " + graphProcessor.outDegree("/wiki/Chaos_theory"));
*/

    }

    private static ArrayList<String> callWebsite(String address, int numLinks) {
        String content = "";
        ArrayList<String> links = new ArrayList<>();
        String website = "https://en.wikipedia.org" + address;
        URLConnection connection = null;
        int count = 0;
        try {
            connection = new URL(website).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            Boolean foundP = false;
            while (scanner.hasNext()) {
                content = scanner.next();
                if (content.contains("<p")) {
                    foundP = true;
                }
                if (foundP && content.contains("href=") && !(content.contains(":") || content.contains("#"))) {
                    content.trim();
                    links.add(content.trim().substring(6, content.length()-1));
                    count++;
                    if(count == numLinks) {
                        return links;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("there are " + count + " links found");
        return null;
    }

    private static String readFile(File fileName) {
        String fileContents = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                fileContents += line + "\r\n";
            }
            reader.close();
            return  fileContents;
        }
        catch (Exception e) {
            System.out.println("that failed" + e);
            e.printStackTrace();
        }
        return null;
    }

}
