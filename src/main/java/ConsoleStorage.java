public class ConsoleStorage implements Storage {

    @Override
    public void save(String url, String text) {
        System.out.println("Saved: " + url);
    }
}