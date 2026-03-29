public class Main {
    public static void main(String[] args) {

        Frontier frontier = ComponentFactory.createFrontier();
        VisitedTracker visited = new VisitedTracker();

        // seed once
        frontier.addUrl("https://example.com");

        // simulate multiple nodes
        Crawler crawler1 = new Crawler(5, frontier, visited);
        Crawler crawler2 = new Crawler(5, frontier, visited);
        Crawler crawler3 = new Crawler(5, frontier, visited);

        crawler1.start();
        crawler2.start();
        crawler3.start();
    }
}