public class ComponentFactory {

    public static Fetcher createFetcher() {
        return new JsoupFetcher();
    }

    public static Parser createParser() {
        return new HtmlParser();
    }

    public static Storage createStorage() {
        return new ConsoleStorage();
    }

    public static Frontier createFrontier() {
        return new InMemoryFrontier();
    }
}