import java.awt.*;

public class StringRect {

    public String string;
    public int x, y;
    public boolean background;

    public StringRect(String string, int x, int y, boolean background) {
        this.string = string;
        this.x = x;
        this.y = y;
        this.background = background;
    }

    public void draw(Graphics g) {
        if (background ) {
            g.setColor(Color.YELLOW);
            g.fillRect(x, y - g.getFontMetrics().getHeight(), g.getFontMetrics().stringWidth(string), g.getFontMetrics().getHeight());
            g.setColor(Color.black);
        }

        g.drawString(string, x, y);
    }
}
