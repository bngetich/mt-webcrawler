public class Main {
    public static void main(String[] args) {
        Crawler crawler =
                new Crawler(5, "https://nytimes.com");

        crawler.start();
    }
}