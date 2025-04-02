import javax.swing.*;
import java.awt.*;

public class GameManager {

    private static JFrame frame;

    public static void main(String[] args) {
        createGUI();
    }

    public static void createGUI(){
        frame = new JFrame("Horse Race Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER));

        //set window size to 80% the monitor size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final float scale = 0.6f;
        int width = (int) (screenSize.width * scale);
        int height = (int) (screenSize.height * scale);
        frame.setSize(width, height);

        //center window
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        frame.setLocation(x, y);

        frame.setVisible(true);
    }

}
