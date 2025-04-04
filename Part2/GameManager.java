import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class GameManager {

    //constants
    public static final String BACKGROUND_COLOR = "#F0AE24";
    public static final String ACCENT_COLOR = "#B5702A";

    private static JFrame frame;
    private static JLayeredPane layeredPane;
    private static Racetrack racetrack;
    private static CustomPanel sidePanel;
    public static Font bigPixelFont, mediumPixelFont, smallPixelFont;
    public static CustomButton mainButton1, mainButton2;

    public static boolean racing = false;
    public static String lastWinner = "none";

    public static void main(String[] args) {
        createGUI();

        racetrack.addHorse("stfu", 0.5);
        racetrack.addHorse("idiot", 0.5);
        racetrack.addHorse("dumbass", 0.5);
    }

    public static void createGUI(){
        frame = new JFrame("Horse Race Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //create font
        try {
            Font pixelFont = Font.createFont(Font.TRUETYPE_FONT,new File("pixelSans.ttf"));
            bigPixelFont = pixelFont.deriveFont(70f);
            mediumPixelFont = pixelFont.deriveFont(50f);
            smallPixelFont = pixelFont.deriveFont(30f);
        } catch (Exception e) {
            Font pixelFont = new Font("Arial", Font.PLAIN, 30);
            bigPixelFont = pixelFont.deriveFont(70f);
            mediumPixelFont = pixelFont.deriveFont(50f);
            smallPixelFont = pixelFont.deriveFont(30f);
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
        racetrack = new Racetrack(Racetrack.FIG8);
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
        mainButton2.setText("reset track");
        mainButton2.setFont(mediumPixelFont);
        mainButton2.setAction(e -> resetTrack());





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
        frame.revalidate(); // Refresh layout after resizing
    }

    public static void invalidate() {
        racetrack.repaint();
    }

    private static void resetTrack() {
        racetrack.resetTrack(1000);
        mainButton2.setText("start race");

        for (ActionListener al : mainButton2.getActionListeners()) {mainButton2.removeActionListener(al);}
        mainButton2.setAction(e -> startRaceGUI());
    }

    public static void setWinner(Horse horse){
        lastWinner = horse.getName();
        racing = false;
    }

    private static Timer raceTimer;

    private static void startRaceGUI() {
        racing = true;

        raceTimer = new Timer(100, e -> {
            if (!racing) {
                ((Timer) e.getSource()).stop(); // stop when race ends
                System.out.println(lastWinner);
            } else {
                racetrack.advanceEvent();
                invalidate(); // triggers repaint
            }
        });

        raceTimer.start();
    }


}
