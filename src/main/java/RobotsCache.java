public interface RobotsCache {
    RobotsRules get(String host);

    void put(String host, RobotsRules rules, long ttlSeconds);
}
