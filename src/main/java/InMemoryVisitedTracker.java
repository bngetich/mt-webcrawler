import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryVisitedTracker implements VisitedTracker {

    private final Set<String> visited = ConcurrentHashMap.newKeySet();


    @Override
    public boolean markVisited(String url) {
        return visited.add(url);
    }
}
