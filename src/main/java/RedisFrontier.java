public class RedisFrontier implements Frontier {

    @Override
    public void addUrl(String url) {
        System.out.println("Redis ADD: " + url);
    }

    @Override
    public String getNextUrl() {
        System.out.println("Redis GET");
        return null;
    }
}