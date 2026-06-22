import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RobotsServiceTest {
    private FakeRobotsCache cache;
    private RobotsService service;

    @BeforeEach
    void setUp() {
        cache = new FakeRobotsCache();
        service = new RobotsService(cache);
    }


    @Test
    void returnsCachedCrawlDelay() {
        RobotsRules rules = new RobotsRules(
                "example.com",
                List.of("/private"),
                5000L,
                System.currentTimeMillis()
        );

        cache.put("example.com", rules, 3600);

        long actualDelay = service.getDelayMillis(
                "https://example.com/page"
        );

        assertEquals(5000L, actualDelay);
    }

    @Test
    void returnsDefaultDelayWhenCrawlDelayIsMissing() {
        RobotsRules rules = new RobotsRules(
                "example.com",
                List.of(),
                null,
                System.currentTimeMillis()
        );

        cache.put("example.com", rules, 3600);

        long actualDelay = service.getDelayMillis(
                "https://example.com/page"
        );

        assertEquals(1000L, actualDelay);
    }

    @Test
    void rejectsDisallowedPath() {
        RobotsRules rules = new RobotsRules(
                "example.com",
                List.of("/private"),
                null,
                System.currentTimeMillis()
        );

        cache.put("example.com", rules, 3600);

        boolean allowed = service.isAllowed(
                "https://example.com/private/account"
        );

        assertFalse(allowed);
    }

    @Test
    void allowsPathThatDoesNotMatchDisallowRule() {
        RobotsRules rules = new RobotsRules(
                "example.com",
                List.of("/private"),
                null,
                System.currentTimeMillis()
        );

        cache.put("example.com", rules, 3600);

        boolean allowed = service.isAllowed(
                "https://example.com/public/article"
        );

        assertTrue(allowed);
    }

    @Test
    void returnsDefaultDelayForInvalidUrl() {
        long actualDelay = service.getDelayMillis(
                "https://exa mple.com"
        );

        assertEquals(1000L, actualDelay);
    }
}
