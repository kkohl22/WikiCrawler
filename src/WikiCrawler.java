import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ken Kohl on 3/27/2017.
 */
public class WikiCrawler {
    private static int requestCount = 0;
    private static String seedUrl;
    private static String fileName;
    private static final String BASE_URL = "https://en.wikipedia.org";
    private static int max;
    private static String string1;
    private static int count = 0;


    /**
     * @param seedUrl  relative address of the seed url (within Wiki domain).
     * @param max      maximum number of pages to be crawled
     * @param fileName representing getName of a file-The graph will be written to the file.
     */
    WikiCrawler(String seedUrl, int max, String fileName) {
        this.seedUrl = seedUrl;
        this.max = max;
        this.fileName = fileName;
    }

    /**
     * This method gets a string (that represents contents of a .html
     * file) as parameter. This method should return an array list (of Strings) consisting of links from doc
     *
     * @param doc
     * @return ArrayList of links in the doc
     */
    public static ArrayList<String> extractLinks(String doc) {
        count++;
        if(count % 30 == 0) {
            System.out.println("Getting links on site " + count);
        }
        ArrayList<String> links = new ArrayList<>();
        String content = "";
        Boolean foundP = false;
        try {
            Scanner scanner = new Scanner(doc);
            Pattern p = Pattern.compile(("\"([^\"]*)\""));
            while (scanner.hasNext()) {
                content = scanner.next();
                if (content.contains("<p>") || content.contains("<P")) {
                    foundP = true;
                }
                if (foundP && content.contains("href=") && content.contains("wiki") && !(content.contains(":") || content.contains("#") || content.contains("//"))) {
                    Matcher matcher = p.matcher(content);
                    while (matcher.find()) {
                        String link = matcher.group();
                        link = link.substring(1, link.length() - 1);
                        if (!links.contains(link)) {
                            links.add(link);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return links;
    }

    /**
     * Contructs a graph over following pages.
     */
    public void crawl() {
        int count = 0;
        LinkedList<String> queue = new LinkedList<>();
        String root = seedUrl;
        ArrayList<String> seenLinks = new ArrayList<>();
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            seenLinks.add(root);
            queue.add(root);
            count++;
            if (count >= max) {
                return;
            }
            writer.println(max);
            writer.flush();
            // gets an Arraylist of all the nodes in the graph
            while (!queue.isEmpty()) {
                String current = queue.remove();
                ArrayList<String> links = extractLinks(extractWebsite(current)); //Gets all the links of the current node.
                // System.out.println(Arrays.toString(links.toArray()));
                for (String link : links) {
                    if (seenLinks.contains(link)) {
                        continue;
                    }
                    seenLinks.add(link);
                    queue.add(link);
                    count++;
                    if (count == max) {
                        break;
                    }
                }
                if (count == max) {
                    break;
                }
            }
            // Finds the interconnections of the graph
            Iterator<String> masterList = seenLinks.iterator();
            while (masterList.hasNext()) {
                ArrayList<String> written = new ArrayList<>();
                String next = masterList.next();
                ArrayList<String> childLinks = extractLinks(extractWebsite(next));
                for (String child : childLinks) {
                    String toWrite = next + " " + child;
                    if (!(next.equals(child) || written.contains(toWrite)) && seenLinks.contains(child)) {
                        writer.println(toWrite);
                        writer.flush();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     *  Method that takes in a web URL and extracts the HTML contents
     * @param address URL to process
     * @return String representing the HTML contents
     */
    private String extractWebsite(String address) {
        String website = BASE_URL + address;
        URLConnection connection = null;
        String content = "";
        requestCount = 0;
        try {
            URL url = new URL(website);
            InputStream is = url.openStream();
            requestCount++;
            if(requestCount % 50 == 0) {
                System.out.println(requestCount);
            }

          /*  if (requestCount >= 100) {
                requestCount = 0;
                TimeUnit.SECONDS.sleep(3);
            }*/
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();

            while (line != null) {
                content += line;
                line = br.readLine();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }


}
