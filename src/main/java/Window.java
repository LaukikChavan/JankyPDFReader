import javax.speech.EngineException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Window extends JFrame implements Runnable {
    public static Window window = null;
    public boolean isRuning = true;
    public PageImages pageImages;
    public KL keyListener = new KL();


    public Window(String title, int width, int height) {
        this.setSize(width, height);
        this.setTitle(title);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.addKeyListener(keyListener);
        this.pageImages = new PageImages(keyListener);
    }

    public static Window getWindow() {
        if(Window.window == null) Window.window = new Window(Constants.TITLE, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        return Window.window;
    }

    public void update() {
        Image doubleBufferImage = createImage(getWidth(), getHeight());
        Graphics dbGraphics = doubleBufferImage.getGraphics();
        this.draw(dbGraphics);
        this.pageImages.update();
        getGraphics().drawImage(doubleBufferImage, 0, 0, this);

        if (keyListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            try {
                PageImages.nextPage();
                System.out.println("Next Page");
            } catch (EngineException e) {
                e.printStackTrace();
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        this.pageImages.draw(g);
    }

    @Override
    public void run() {
        try {
            while (this.isRuning) {
                update();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pageImages.close();
        this.dispose();
    }
}
