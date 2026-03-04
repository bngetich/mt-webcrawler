import org.jsoup.nodes.Document;

import java.util.List;

public interface Parser {

    List<String> extractLinks(Document doc);

    String extractText(Document doc);
}