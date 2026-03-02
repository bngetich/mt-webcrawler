import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public List<String> extractLinks(Document doc) {
        List<String> links = new ArrayList<>();

        Elements elements = doc.select("a[href]");
        for (Element e : elements) {
            links.add(e.absUrl("href"));
        }

        return links;
    }

    public String extractText(Document doc) {
        return doc.text();
    }
}