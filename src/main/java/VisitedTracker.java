import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface VisitedTracker {
    final Set<String> visited = ConcurrentHashMap.newKeySet();

    boolean markVisited(String url);
}
