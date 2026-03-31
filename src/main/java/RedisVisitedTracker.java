import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisVisitedTracker implements VisitedTracker {

    private static final String VISITED_KEY = "crawler:visited";

    private final JedisPool pool;

    public RedisVisitedTracker(){
        this.pool = new JedisPool("localhost", 6379);
    }

    @Override
    public boolean markVisited(String url) {
        try(Jedis jedis = pool.getResource()){
           return jedis.sadd(VISITED_KEY, url) == 1;
        }
    }
}
