public class ComponentFactory {

    public static Fetcher createFetcher() {
        return new AsyncHttpFetcher();
    }

    public static Parser createParser() {
        return new HtmlParser();
    }

    public static Storage createStorage() {
        return new ConsoleStorage();
    }

    public static Frontier createFrontier(HostDelayProvider hostDelayProvider) {
        String type = System.getProperty("frontier.type", "memory");

        if ("partitioned-redis".equalsIgnoreCase(type)) {
            int numPartitions = Integer.parseInt(
                    System.getProperty("frontier.partitions", "3")
            );
            int assignedPartition = Integer.parseInt(
                    System.getProperty("frontier.assignedPartition", "0")
            );

            return new PartitionedRedisFrontier(
                    numPartitions,
                    assignedPartition,
                    hostDelayProvider
            );
        }

        if ("redis".equalsIgnoreCase(type)) {
            return new RedisFrontier();
        }

        return new InMemoryFrontier();
    }

    public static VisitedTracker createVisitedTracker() {
        String type = System.getProperty("visited.type", "memory");

        if(type.equals("redis")){
            return new RedisVisitedTracker();
        }

        return new InMemoryVisitedTracker();
    }

    public static RobotsService createRobotsService() {
        return new RobotsService(new RedisRobotsCache());
    }
}