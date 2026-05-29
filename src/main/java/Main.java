public class Main {
    public static void main(String[] args) {

        Frontier frontier = ComponentFactory.createFrontier();
        VisitedTracker visited = ComponentFactory.createVisitedTracker();

        // seed once
        frontier.addUrl("https://example.com");

        Crawler crawler = new Crawler(5, frontier, visited);

        crawler.start();
    }
}