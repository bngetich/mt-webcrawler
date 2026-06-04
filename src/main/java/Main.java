public class Main {
    public static void main(String[] args) {

        Frontier frontier = ComponentFactory.createFrontier();
        VisitedTracker visited = ComponentFactory.createVisitedTracker();

        // seed once
        frontier.addTask(new CrawlTask("https://example.com", 0));

        Crawler crawler = new Crawler(5, frontier, visited);

        crawler.start();
    }
}
