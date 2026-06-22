public class Main {
    public static void main(String[] args) {

        RobotsService robotsService = ComponentFactory.createRobotsService();

        Frontier frontier = ComponentFactory.createFrontier(robotsService);
        VisitedTracker visited = ComponentFactory.createVisitedTracker();

        // seed once
        frontier.addTask(new CrawlTask("https://example.com", 0));

        Crawler crawler = new Crawler(5, frontier, visited, robotsService);

        crawler.start();
    }
}
