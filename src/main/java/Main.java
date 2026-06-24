public class Main {
    public static void main(String[] args) {
        String seedUrl = args.length > 0 ? args[0] : "https://example.com";

        RobotsService robotsService = ComponentFactory.createRobotsService();

        Frontier frontier = ComponentFactory.createFrontier(robotsService);
        VisitedTracker visited = ComponentFactory.createVisitedTracker();

        // seed once
        frontier.addTask(new CrawlTask(seedUrl, 0));

        Crawler crawler = new Crawler(5, frontier, visited, robotsService);

        crawler.start();
    }
}
