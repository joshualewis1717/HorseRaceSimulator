import javax.swing.*;
import java.awt.*;

public class InfoPanel {

    private JPanel panel;
    private Runnable boundSetRunnable;

    public InfoPanel() {
        panel = new BeveledPanel();
    }

    //instead of storing all info in the object, just store a runnable on how to initialize it
    //allows for much more freedom
    public InfoPanel setBehaviour(Runnable boundSetRunnable) {
        this.boundSetRunnable = boundSetRunnable;
        invalidateBounds();
        return this;
    }

    public InfoPanel setColor(Color color) {
        panel.setBackground(color);
        return this;
    }

    public void invalidateBounds() {
        boundSetRunnable.run();
    }





    public JPanel getPanel() {return panel;}
}
