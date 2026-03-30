import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
            return jedis.rpop(QUEUE_KEY);
        }
    }
}