import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class RedisFrontier implements Frontier {

    private static final String QUEUE_KEY = "crawler:frontier";
    private final JedisPool pool;

    public RedisFrontier() {
        this.pool = new JedisPool("localhost", 6379);
    }

    @Override
    public void addTask(CrawlTask task) {
        try(Jedis jedis = pool.getResource()){
            jedis.lpush(QUEUE_KEY, serialize(task));
        }
    }

    @Override
    public CrawlTask getNextTask() {
        try(Jedis jedis = pool.getResource()){
            // 0 = block forever until item exists
            /* redis allows blocking on multiple keys
             * so the response needs to tell you which
             * key the value came from
             * result = ["crawler:frontier", "https://example.com"]
             */
            List<String> result = jedis.brpop(0, QUEUE_KEY);
            return deserialize(result.get(1));
        }
    }

    private String serialize(CrawlTask task) {
        return task.getUrl() + "|" + task.getRetryCount();
    }

    private CrawlTask deserialize(String value) {
        String[] parts = value.split("\\|");
        return new CrawlTask(parts[0], Integer.parseInt(parts[1]));
    }
}
