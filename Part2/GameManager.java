import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GameManager {

    //constants
    public static final Color BACKGROUND_COLOR = Color.decode("#F0AE24");
    public static final Color ACCENT_COLOR = Color.decode("#B5702A");
    public static final Color ACCENT_BTN_COLOR = Color.decode("#6E4F2A");

    private static JFrame frame;
    private static JLayeredPane layeredPane;
    private static Racetrack racetrack;
    private static CustomPanel sidePanel;
    public static Font bigPixelFont, mediumPixelFont, smallPixelFont, verySmallPixelFont;
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
        Font pixelFont = new Font("Arial", Font.PLAIN, 30);

        try { pixelFont = Font.createFont(Font.TRUETYPE_FONT,new File("pixelSans.ttf"));
        } catch (Exception ignoredException) {}

        bigPixelFont = pixelFont.deriveFont(65f);
        mediumPixelFont = pixelFont.deriveFont(45f);
        smallPixelFont = pixelFont.deriveFont(30f);
        verySmallPixelFont = pixelFont.deriveFont(20f);

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
        layeredPane.setBackground(BACKGROUND_COLOR);
        layeredPane.setOpaque(true);

        // main racetrack
        racetrack = new Racetrack(0,0);//just always start with default weather and track type (0)
        racetrack.setBounds( 10, 10, 3*layeredPane.getWidth()/4, layeredPane.getHeight() - 20);

        //info panel
        sidePanel = new CustomPanel();
        sidePanel.setBackground(ACCENT_COLOR);
        sidePanel.setBehaviour(() -> {
            int width1 = layeredPane.getWidth()/4;
            int height1 = layeredPane.getHeight() - 20;
            sidePanel.setBounds(layeredPane.getWidth() - width1 - 10, 10,
                    width1, height1);
        });


        setRaceConfigureScreen();

        //buttons
        mainButton1 = new CustomButton("Menu"); //create buttons
        mainButton2 = new CustomButton("Prepare Race");

        for (CustomButton button : new CustomButton[] {mainButton1, mainButton2}) { //apply duplicate instructions
            button.setBackground(ACCENT_COLOR);
            button.setFont(mediumPixelFont);
        }

        mainButton1.setBehaviour(()->{
            int w = ( 3*layeredPane.getWidth()/8 ) - 20;
            int h = 80;
            mainButton1.setBounds(10, layeredPane.getHeight() - h - 10, w, h);
        });

        mainButton2.setBehaviour(()->{
            int w = ( 3*layeredPane.getWidth()/8 ) - 20;
            int h = 80;
            mainButton2.setBounds(20 + w, layeredPane.getHeight() - h - 10, w, h);
        });

        mainButton2.setAction(e -> resetTrack());





        layeredPane.add(racetrack, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(sidePanel, JLayeredPane.PALETTE_LAYER);
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
        setHorseConfigureScreen();
        repurposeButton(mainButton2,"start race", _ -> startRaceGUI());
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
                repurposeButton(mainButton2,"reset race", _ -> resetTrack());
                setRaceConfigureScreen();
            } else {
                racetrack.advanceEvent();
                invalidate(); // triggers repaint
            }
        });

        raceTimer.start();
    }

    private static void repurposeButton(CustomButton button, String label, ActionListener function) {
        for (ActionListener al : button.getActionListeners()) {button.removeActionListener(al);}
        button.setText(label); button.setAction(function);
    }


    private static void setHorseConfigureScreen() {
        sidePanel.removeAll();

        addNewLabel(sidePanel, smallPixelFont, "Customize");



        sidePanel.repaint();
    }


    private static void setRaceConfigureScreen() {
        sidePanel.removeAll();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addNewLabel(sidePanel, smallPixelFont, "Configure Race");


        //Lanes
        addPlusMinusControl(sidePanel,"Lanes",
                e -> { if (racetrack.getLanes() < 6) racetrack.addNewRandomHorse();  invalidate();},  //plus function
                e -> { if (racetrack.getLanes() > 2) racetrack.killLastHorse();      invalidate();}); //minus function
        sidePanel.add(Box.createVerticalGlue());

        //Length
        addPlusMinusControl(sidePanel,"Length", e -> {
            racetrack.increaseLength();
            invalidate();//plus function
            }, e -> {
            racetrack.decreaseLength();
            invalidate();//minus function
        });
        sidePanel.add(Box.createVerticalGlue());

        //track selection
        addNewLabel(sidePanel, verySmallPixelFont, "Select Track");

        ActionListener[] trackSets = new ActionListener[Racetrack.trackNames.length];
        for (int i = 0; i < trackSets.length; i++) {
            int finalI = i;
            trackSets[i] = e -> {racetrack.setTrack(finalI); invalidate();};
        }
        addButtonList(sidePanel, Racetrack.trackNames, trackSets);
        sidePanel.add(Box.createVerticalGlue());

        //weather selection
        addNewLabel(sidePanel, verySmallPixelFont, "Select Weather");

        ActionListener[] weatherSets = new ActionListener[Racetrack.weatherNames.length];
        for (int i = 0; i < weatherSets.length; i++) {
            int finalI = i;
            weatherSets[i] = e -> {racetrack.setWeather(finalI); invalidate();};
        }
        addButtonList(sidePanel, Racetrack.weatherNames, weatherSets);


        sidePanel.repaint();
    }

    //add premade custom components
    private static void addNewLabel(JPanel parent, Font font, String label) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(font);
        jLabel.setForeground(Color.WHITE);
        parent.add(jLabel);
    }
    private static void addPlusMinusControl(JPanel parent, String label, ActionListener plusFunction, ActionListener minusFunction){
        JPanel inlinePanel = new JPanel();
        inlinePanel.setLayout(new BoxLayout(inlinePanel, BoxLayout.X_AXIS)); // Horizontally align elements
        inlinePanel.setBackground(ACCENT_COLOR);
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(verySmallPixelFont);
        jLabel.setForeground(Color.WHITE);
        CustomButton plusButton = new CustomButton("+");
        CustomButton minusButton = new CustomButton("-");

        for (CustomButton btn : new CustomButton[] {plusButton, minusButton}) {
            btn.setBackground(ACCENT_BTN_COLOR);
            btn.setFont(smallPixelFont);
        }

        plusButton.addActionListener(plusFunction);
        minusButton.addActionListener(minusFunction);

        inlinePanel.add(jLabel);
        inlinePanel.add(Box.createHorizontalGlue()); //just pushes elements apart
        inlinePanel.add(plusButton);
        inlinePanel.add(Box.createRigidArea(new Dimension(30, 0)));
        inlinePanel.add(minusButton);
        inlinePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        inlinePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(inlinePanel);
    }
    private static void addButtonList(JPanel parent, String[] labels, ActionListener[] functions) {
        JPanel inlinePanel = new JPanel();
        inlinePanel.setLayout(new BoxLayout(inlinePanel, BoxLayout.Y_AXIS)); // Horizontally align elements
        inlinePanel.setBackground(ACCENT_COLOR);

        for (int i = 0; i < labels.length && i < functions.length; i++) { //ensure this cannot crash due to two diff array lengths
            CustomButton btn = new CustomButton(labels[i]);
            btn.setBackground(ACCENT_BTN_COLOR);
            btn.setFont(verySmallPixelFont);
            btn.addActionListener(functions[i]);
            btn.setBehaviour(() -> {
                int width = parent.getWidth()-20; // full width
                int height = btn.getPreferredSize().height; // keep height from font or layout
                btn.setBounds(0, btn.getY(), width, height); // apply
            });

            inlinePanel.add(btn);
            inlinePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        inlinePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50*labels.length));
        inlinePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        parent.add(inlinePanel);
    }
    private static void addHorseCustomizePanel(JPanel parent, Horse horse)

}
