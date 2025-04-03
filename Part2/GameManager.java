import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

public class GameManager {

    //constants
    public static final String BACKGROUND_COLOR = "#F0AE24";
    public static final String ACCENT_COLOR = "#B5702A";

    private static JFrame frame;
    private static JLayeredPane layeredPane;
    private static Racetrack racetrack;
    private static CustomPanel sidePanel;
    public static Font pixelFont;
    public static CustomButton mainButton1, mainButton2;

    public static void main(String[] args) {
        createGUI();
    }

    public static void createGUI(){
        frame = new JFrame("Horse Race Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //create font
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT,new File("pixelSans.ttf")).deriveFont(70f);
        } catch (Exception e) {
            pixelFont = new Font("Arial", Font.PLAIN, 30);
        }

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

        // Create layered pane
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(width, height));
        layeredPane.setBackground(Color.decode(BACKGROUND_COLOR));
        layeredPane.setOpaque(true);

        // main racetrack
        racetrack = new Racetrack();
        racetrack.setBounds( 10, 10, 3*layeredPane.getWidth()/4, layeredPane.getHeight() - 20);

        //info panel
        sidePanel = new CustomPanel().setColor(Color.decode(ACCENT_COLOR));
        sidePanel.setBehaviour(() -> {
            int width1 = layeredPane.getWidth()/4;
            int height1 = layeredPane.getHeight() - 20;
            sidePanel.getPanel().setBounds(layeredPane.getWidth() - width1 - 10, 10,
                    width1, height1);
        });

        //buttons
        mainButton1 = new CustomButton();
        mainButton1.setBackground(Color.decode(ACCENT_COLOR));
        mainButton1.setBehaviour(()->{
            int w = ( 3*layeredPane.getWidth()/8 ) - 20;
            int h = 80;
            mainButton1.setBounds(10, layeredPane.getHeight() - h - 10, w, h);
        });

        mainButton2 = new CustomButton();
        mainButton2.setBackground(Color.decode(ACCENT_COLOR));
        mainButton2.setBehaviour(()->{
            int w = ( 3*layeredPane.getWidth()/8 ) - 20;
            int h = 80;
            mainButton2.setBounds(20 + w, layeredPane.getHeight() - h - 10, w, h);
        });



        layeredPane.add(racetrack, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(sidePanel.getPanel(), JLayeredPane.PALETTE_LAYER);
        layeredPane.add(mainButton1, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(mainButton2, JLayeredPane.PALETTE_LAYER);
        onResized();//build all windows

        frame.setContentPane(layeredPane);
        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {onResized();}});
    }


    private static void onResized() {
        sidePanel.invalidateBounds();

        racetrack.setBounds( 10, 10, (3*layeredPane.getWidth()/4) - 30, layeredPane.getHeight() - 20);
        racetrack.loadTrack();

        mainButton1.invalidate();
        mainButton2.invalidate();
        //frame.revalidate(); // Refresh layout after resizing
    }

    //private static void setPage

}
