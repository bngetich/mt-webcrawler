import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.List;

public class RedisRobotsCache implements RobotsCache {
    private final JedisPool pool;

    public RedisRobotsCache() {
        this.pool = new JedisPool("localhost", 6379);
    }

    @Override
    public RobotsRules get(String host) {
        try (Jedis jedis = pool.getResource()){
            String key = getKey(host);
            String value = jedis.get(key);

            if(value == null){
                return null;
            }

            return deserialize(value);
        }

    }

    @Override
    public void put(String host, RobotsRules rules, long ttlSeconds) {
        String value = serialize(rules);

        try (Jedis jedis = pool.getResource()) {
            jedis.setex(getKey(host), ttlSeconds, value);
        }
    }

    private String getKey(String host) {
        return "robots:" + host;
    }

    private String serialize(RobotsRules rules) {
        String crawlDelay = rules.getCrawlDelayMs() == null
                ? ""
                : String.valueOf(rules.getCrawlDelayMs());

        String disallowedPaths = String.join(",", rules.getDisallowedPaths());

        return rules.getHost()
                + "|"
                + crawlDelay
                + "|"
                + rules.getFetchedAtMillis()
                + "|"
                + disallowedPaths;
    }

    private RobotsRules deserialize(String value) {
        String[] parts = value.split("\\|", -1);

        String host = parts[0];

        Long crawlDelayMs = parts[1].isBlank()
                ? null
                : Long.parseLong(parts[1]);

        long fetchedAtMillis = Long.parseLong(parts[2]);

        List<String> disallowedPaths = parts[3].isBlank()
                ? List.of()
                : Arrays.asList(parts[3].split(","));

        return new RobotsRules(
                host,
                disallowedPaths,
                crawlDelayMs,
                fetchedAtMillis
        );
    }
}
