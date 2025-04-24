import javax.swing.*;
import java.awt.*;

public class CustomPanel extends JPanel  {

    private Runnable boundSetRunnable;

    public CustomPanel() {
        super();
    }

    //instead of storing all info in the object, just store a runnable on how to initialize it
    //allows for much more freedom
    public CustomPanel setBehaviour(Runnable boundSetRunnable) {
        this.boundSetRunnable = boundSetRunnable;
        invalidateBounds();
        return this;
    }

    public void invalidateBounds() {
        boundSetRunnable.run();
    }
}
