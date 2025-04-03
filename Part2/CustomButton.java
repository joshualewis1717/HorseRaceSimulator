import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CustomButton extends JButton { // Extend JButton instead of JPanel

    private Runnable boundSetRunnable;

    public CustomButton() {
        super(); // Call JButton constructor
    }

    public CustomButton setBehaviour(Runnable boundSetRunnable) {
        this.boundSetRunnable = boundSetRunnable;
        invalidate();
        return this;
    }

    @Override
    public void invalidate() {
        if (boundSetRunnable != null) boundSetRunnable.run();
        super.invalidate();
    }

    public CustomButton setAction(ActionListener action) {
        addActionListener(action);
        return this;
    }
}
