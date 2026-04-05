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
    public void addUrl(String url) {
        try(Jedis jedis = pool.getResource()){
            jedis.lpush(QUEUE_KEY, url);
        }
    }

    @Override
    public String getNextUrl() {
        try(Jedis jedis = pool.getResource()){
            // 0 = block forever until item exists
            /* redis allows blocking on multiple keys
             * so the response needs to tell you which
             * key the value came from
             * result = ["crawler:frontier", "https://example.com"]
             */
            List<String> result = jedis.brpop(0, QUEUE_KEY);
            return result.get(1);
        }
    }
}