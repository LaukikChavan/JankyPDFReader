import org.apache.log4j.BasicConfigurator;

public class Main {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        Window window = Window.getWindow();
        Thread t1 = new Thread(window);
        t1.start();
    }
}
