import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VisitedTracker {
    private final Set<String> visited = ConcurrentHashMap.newKeySet();

    public boolean markVisited(String url) {
        return visited.add(url);
    }
}
