import java.util.HashMap;
import java.util.Map;

public class FakeRobotsCache implements RobotsCache {
    private final Map<String, RobotsRules> rulesByHost = new HashMap<>();


    @Override
    public RobotsRules get(String host) {
        return rulesByHost.get(host);
    }

    @Override
    public void put(String host, RobotsRules rules, long ttlSeconds) {
        rulesByHost.put(host, rules);
    }
}
