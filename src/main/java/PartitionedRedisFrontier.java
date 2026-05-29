import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.List;

public class PartitionedRedisFrontier implements Frontier {

    private final JedisPool pool;
    private final PartitionRouter router;
    private final int assignedPartition;

    public PartitionedRedisFrontier(int numPartitions, int assignedPartition) {
        this.pool = new JedisPool("localhost", 6379);
        this.router = new PartitionRouter(numPartitions);
        this.assignedPartition = assignedPartition;
    }

    @Override
    public void addUrl(String url) {
        String host = getHost(url);
        if (host == null) return;

        int partition = router.getPartition(host);

        // DEBUG
        System.out.println(
                host + " -> partition " + partition
        );

        String key = getPartitionKey(partition);

        try (Jedis jedis = pool.getResource()) {
            jedis.lpush(key, url);
        }
    }

    @Override
    public String getNextUrl() {
        String key = getPartitionKey(assignedPartition);

        try (Jedis jedis = pool.getResource()) {
            List<String> result = jedis.brpop(0, key);
            return result.get(1);
        }
    }

    private String getPartitionKey(int partition){
        return "crawler:frontier:partition:" + partition;
    }

    private String getHost(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }
}
