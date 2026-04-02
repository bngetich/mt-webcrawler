import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface VisitedTracker {
    boolean markVisited(String url);
}
