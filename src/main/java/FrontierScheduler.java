public class FrontierScheduler implements Frontier {

    private final Frontier frontier;

    public FrontierScheduler(Frontier frontier){
        this.frontier = frontier;
    }


    @Override
    public void addUrl(String url) {
        frontier.addUrl(url);
    }

    @Override
    public String getNextUrl() throws InterruptedException {
        return frontier.getNextUrl();
    }
}
