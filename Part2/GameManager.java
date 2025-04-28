import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {

    //constants
    public static final Color BACKGROUND_COLOR = Color.decode("#F0AE24");
    public static final Color ACCENT_COLOR = Color.decode("#B5702A");
    public static final Color ACCENT_BTN_COLOR = Color.decode("#6E4F2A");

    private static JFrame frame;
    private static JLayeredPane layeredPane;
    private static Racetrack racetrack;
    private static JScrollPane sidePanelParent;
    private static JPanel sidePanel;
    public static Font bigPixelFont, mediumPixelFont, smallPixelFont, verySmallPixelFont, extreemlySmallPixelFont;
    public static JButton mainButton1, mainButton2;

    public static boolean racing = false;
    public static String lastWinner = "none";

    public static void main(String[] args) {
        createGUI();
        setScreen(RACE_CONFIG);

        SwingUtilities.invokeLater(() -> {
            racetrack.addHorse("PIPI", 0.5);
            racetrack.addHorse("EL JEFE", 0.5);
            racetrack.addHorse("KOKOMO", 0.5);
            racetrack.onResized();
        });
    }

    public static void createGUI(){
        frame = new JFrame("Horse Race Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //create font
        Font pixelFont = new Font("Arial", Font.PLAIN, 30);

        try { pixelFont = Font.createFont(Font.TRUETYPE_FONT,new File("assets/pixelSans.ttf"));
        } catch (Exception ignoredException) {}

        bigPixelFont = pixelFont.deriveFont(65f);
        mediumPixelFont = pixelFont.deriveFont(45f);
        smallPixelFont = pixelFont.deriveFont(30f);
        verySmallPixelFont = pixelFont.deriveFont(20f);
        extreemlySmallPixelFont = pixelFont.deriveFont(15f);

        //set window size to 80% the monitor size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final float scale = 0.6f;
        int width = (int) (screenSize.width * scale);
        int height = (int) (screenSize.height * scale);
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(900,430));//smallest it can go before it starts to really break

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
        sidePanelParent = new JScrollPane();
        sidePanelParent.getVerticalScrollBar().setBackground(Color.GRAY);
        sidePanelParent.getVerticalScrollBar().setForeground(Color.DARK_GRAY);
        sidePanelParent.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidePanelParent.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(ACCENT_COLOR);
        sidePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidePanelParent.setViewportView(sidePanel); // put the old panel into the scroll


        //buttons
        mainButton1 = getNewStyledButton(""); //create buttons
        mainButton2 = getNewStyledButton("");

        for (JButton button : new JButton[] {mainButton1, mainButton2}) { //apply duplicate instructions
            button.setBackground(ACCENT_COLOR);
            button.setFont(mediumPixelFont);
        }
        
        layeredPane.add(racetrack, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(sidePanelParent, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(mainButton1, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(mainButton2, JLayeredPane.PALETTE_LAYER);

        frame.setContentPane(layeredPane);
        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter() {@Override public void componentResized(ComponentEvent e) {onResized();}});
    }

    public static void setBackgroundColor(Color color) {
        layeredPane.setBackground(color);
    }

    private static void onResized() {
        int height = layeredPane.getHeight();
        int width = layeredPane.getWidth();


        //padding of 10px included in all calculations
        sidePanelParent.setPreferredSize(new Dimension(280, height - 20));
        sidePanelParent.setBounds(width - 290, 10, 280, height - 20);

        int availableWidth = width - 300; //available space to fill
        int buttonY = height - 90;
        mainButton1.setBounds(10, buttonY, (availableWidth - 3 * 10) / 2, 80); // adjusted for padding
        mainButton2.setBounds(availableWidth / 2 + 10, buttonY, (availableWidth - 3 * 10) / 2, 80); // adjusted for padding


        racetrack.setBounds( 10, 10, (3*width/4) - 30, height - 20);
        racetrack.loadTrack();

        racetrack.onResized();


        racetrack.repaint();
        sidePanel.invalidate();
        mainButton1.invalidate();
        mainButton2.invalidate();
        frame.invalidate();
        frame.revalidate(); // Refresh layout after resizing

    }

    //useful to repaint the track statically so globally
    public static void invalidate() {
        racetrack.repaint();
    }


    public static void setWinner(Horse horse){
        lastWinner = horse.getName();
        racing = false;
    }

    private static Timer raceTimer;
    private static void startRaceGUI() {
        racing = true;
        racetrack.logStart();

        raceTimer = new Timer(100, e -> {
            if (racing) {
                racetrack.advanceEvent();
                if (racetrack.allRacersDown()) racing = false;
                updateRaceScreen();
                invalidate(); // triggers repaint
            } else {
                ((Timer) e.getSource()).stop(); // stop when race ends
                setScreen(RACE_END);
            }
        });

        raceTimer.start();
    }
    
    

    //screens
    public static int screen;
    public static final int RACE_CONFIG = 0, HORSE_CONFIG = 1, RACE = 2, RACE_END = 3;
    private static void setScreen(int phase) {
        screen = phase;
        if (phase == RACE_CONFIG) {
            setRaceConfigureScreen();
            repurposeButton(mainButton1,"", e -> {});
            repurposeButton(mainButton2, "NEXT", e -> setScreen(HORSE_CONFIG));
            racetrack.releaseHorses();
        } else if (phase == HORSE_CONFIG) {
            setHorseConfigureScreen();
            repurposeButton(mainButton1,"BACK", e -> setScreen(RACE_CONFIG));
            repurposeButton(mainButton2,"START", e -> setScreen(RACE));
            racetrack.resetTrack(1000); //animate horses taking starting position
        } else if (phase == RACE) {
            setRaceScreen();
            repurposeButton(mainButton1,"PLEASE", e -> {});
            repurposeButton(mainButton2,"WAIT", e -> {});
            startRaceGUI();
        } else if (phase == RACE_END) {
            setRaceEndScreen();
            repurposeButton(mainButton1,"", e -> {});
            repurposeButton(mainButton2,"RESTART", e -> setScreen(RACE_CONFIG));
            racetrack.releaseHorses();
        }
    }
    private static void repurposeButton(JButton button, String label, ActionListener function) {
        for (ActionListener al : button.getActionListeners()) {button.removeActionListener(al);}
        button.setText(label); button.addActionListener(function);
    }

    //Side panel screens
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
            if (racetrack.getLength() < 500) racetrack.increaseLength();
            invalidate();//plus function
            }, e -> {
            if (racetrack.getLength() > 20) racetrack.decreaseLength();
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
        addButtonList(sidePanel, Racetrack.trackNames, trackSets, verySmallPixelFont, 5);
        sidePanel.add(Box.createVerticalGlue());

        //weather selection
        addNewLabel(sidePanel, verySmallPixelFont, "Select Weather");

        ActionListener[] weatherSets = new ActionListener[Racetrack.weatherNames.length];
        for (int i = 0; i < weatherSets.length; i++) {
            int finalI = i;
            weatherSets[i] = e -> {racetrack.setWeather(finalI); invalidate();};
        }
        addButtonList(sidePanel, Racetrack.weatherNames, weatherSets, verySmallPixelFont, 5);


        sidePanel.repaint();
    }
    private static void setHorseConfigureScreen() {
        sidePanel.removeAll();
        SpringLayout layout = new SpringLayout();
        sidePanel.setLayout(layout);

        addNewLabel(sidePanel, smallPixelFont, "Customize");

        // Add a rigid area (just a gap for spacing)
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Configure layout constraints for components
        int yPos = 60;  // Vertical position for the next components
        System.out.println("HORSES SIZE: "+ racetrack.getHorses().size());
        for (Horse horse : racetrack.getHorses()) {
            JPanel horsePanel = createHorseCustomizePanel(sidePanel, horse);
            sidePanel.add(horsePanel);

            layout.putConstraint(SpringLayout.WEST, horsePanel, 10, SpringLayout.WEST, sidePanel);
            layout.putConstraint(SpringLayout.NORTH, horsePanel, yPos, SpringLayout.NORTH, sidePanel);
            layout.putConstraint(SpringLayout.EAST, horsePanel, -10, SpringLayout.EAST, sidePanel); // 10px padding from the right

            yPos += horsePanel.getPreferredSize().height + 10;  // Adjust based on your component height
        }

        sidePanel.setPreferredSize(new Dimension(sidePanel.getWidth(), yPos));

        sidePanel.revalidate();
        sidePanel.repaint();
    }
    private static void setRaceScreen() {
        sidePanel.removeAll();  // Clear current screen
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        addNewLabel(sidePanel, smallPixelFont, "RACE!");

        // Create a panel for each horse with their profile information
        for (Horse horse : racetrack.getHorses()) {
            JPanel horsePanel = createHorsePanelForRace(horse);
            sidePanel.add(horsePanel);
        }

        // Revalidate and repaint the side panel after adding horse panels
        sidePanel.revalidate();
        sidePanel.repaint();

        sidePanel.repaint();
    }
    public static void updateRaceScreen() {
        //this method relies on the fact that the horsePanels are all created the same and the elements are in a specific order
        Component[] sidePanelComponents = sidePanel.getComponents();

        racetrack.sortHorsesByProgress(); //now horses array is sorted based on position in race

        //race screen is a label, then however many horses
        for (int i = 1; i < sidePanelComponents.length; i++) {
            JPanel horsePanel = (JPanel) sidePanelComponents[i];
            Horse horse = racetrack.getHorses().get(i - 1);//i - 1 because the index in sidePanels is one ahead


            JPanel horseStats = (JPanel) horsePanel.getComponent(1); //in order name, speed, position

            String horseName = horse.getName();
            String horsePanelName = (   (JLabel) horseStats.getComponent(0)   ).getText();
            if (!horseName.equals(horsePanelName)) {
                (   (JLabel) horseStats.getComponent(0)   ).setText(" " +horseName);
                (   (JLabel) horseStats.getComponent(2)   ).setText(" Position: #"+i);

                JLabel horseImg = (JLabel) horsePanel.getComponent(0);

                BufferedImage ogImg = horse.getIcon();
                int size = Math.min(ogImg.getWidth(), ogImg.getHeight());
                BufferedImage cropped = ogImg.getSubimage(0, 0, size, size);
                Image scaledImg = cropped.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                horseImg.setIcon(new ImageIcon(scaledImg));
            }

            JLabel speedLabel = (JLabel) horseStats.getComponent(1);
            if (horse.hasFallen()) speedLabel.setText(" FALLEN :(");
            else speedLabel.setText(" Speed: "+racetrack.getSpeed(horse)+" m/s");


        }

        sidePanel.revalidate();
        sidePanel.repaint();
    }
    private static void setRaceEndScreen() {
        sidePanel.removeAll();

        addNewLabel(sidePanel, smallPixelFont, "RACE!");
        sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        String raceMsg = (lastWinner.equals("NOBODY")) ? "NO WINNER" : "WINNER:";

        addNewLabel(sidePanel, smallPixelFont, raceMsg);


        if (!lastWinner.equals("NOBODY")) {
            JPanel winnerPanel = null;
            Horse winner = null;
            for (Horse horse : racetrack.getHorses()) {
                if (horse.getName().equals(lastWinner)) {
                    winnerPanel = createHorsePanelForRace(horse);
                    winner = horse;
                    sidePanel.add(winnerPanel);
                }
            }

            if (winnerPanel != null && winner != null) {
                JPanel horseStats = (JPanel) winnerPanel.getComponent(1); //again still in order name, speed, position

                //this time, i will change speed to become average: --- m/s last known speed because its already average
                //and i will make position the time: -- s
                (   (JLabel) horseStats.getComponent(1)   ).setText(" Average: "+racetrack.getSpeed(winner)+" m/s");
                (   (JLabel) horseStats.getComponent(2)   ).setText(" Time: "+racetrack.getTime()+" s");
            }
        }





            /*String horseName = horse.getName();
            String horsePanelName = (   (JLabel) horseStats.getComponent(0)   ).getText();
            if (!horseName.equals(horsePanelName)) {


                JLabel horseImg = (JLabel) horsePanel.getComponent(0);

                BufferedImage ogImg = horse.getIcon();
                int size = Math.min(ogImg.getWidth(), ogImg.getHeight());
                BufferedImage cropped = ogImg.getSubimage(0, 0, size, size);
                Image scaledImg = cropped.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                horseImg.setIcon(new ImageIcon(scaledImg));
            }

            JLabel speedLabel = (JLabel) horseStats.getComponent(1);
            if (horse.hasFallen()) speedLabel.setText(" FALLEN :(");
            else speedLabel.setText(" Speed: "+racetrack.getSpeed(horse)+" m/s");*/




        sidePanel.revalidate();
        sidePanel.repaint();

        sidePanel.repaint();
    }

    //add premade custom components
    private static JButton getNewStyledButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
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
        JButton plusButton = getNewStyledButton("+");
        JButton minusButton = getNewStyledButton("-");

        for (JButton btn : new JButton[] {plusButton, minusButton}) {
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
    private static void addButtonList(JPanel parent, String[] labels, ActionListener[] functions, Font font, int yPad) {
        JPanel inlinePanel = new JPanel();
        inlinePanel.setLayout(new BoxLayout(inlinePanel, BoxLayout.Y_AXIS)); // Horizontally align elements
        inlinePanel.setBackground(ACCENT_COLOR);

        for (int i = 0; i < labels.length && i < functions.length; i++) { //ensure this cannot crash due to two diff array lengths
            JButton btn = getNewStyledButton(labels[i]);
            btn.setBackground(ACCENT_BTN_COLOR);
            btn.setFont(font);
            btn.addActionListener(functions[i]);

            btn.setAlignmentX(Component.LEFT_ALIGNMENT); // aligns with BoxLayout (left side)
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height)); // fills width, keeps height


            inlinePanel.add(btn);

            if (i < labels.length - 1 && i < functions.length - 1) { // skip adding padding after last button
                inlinePanel.add(Box.createRigidArea(new Dimension(0, yPad)));
            }
        }



        inlinePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50*labels.length));
        inlinePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        parent.add(inlinePanel);
    }
    private static JPanel createHorseCustomizePanel(JPanel parent, Horse horse) {
        JPanel horsePanel = new JPanel(new BorderLayout());
        horsePanel.setBackground(ACCENT_BTN_COLOR.darker());
        horsePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));  // Set max height

        // Horse image setup
        BufferedImage ogImg = horse.getIcon();
        int size = Math.min(ogImg.getWidth(), ogImg.getHeight());
        BufferedImage cropped = ogImg.getSubimage(0, 0, size, size);
        Image scaledImg = cropped.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
        horsePanel.add(imageLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(ACCENT_BTN_COLOR.darker());
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);  // Ensure left-alignment for the panel

        // Horse name
        JLabel name = new JLabel("  " + horse.getName());
        name.setFont(verySmallPixelFont);
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);  // Left-align the name
        rightPanel.add(name);

        // Buttons for breed, color, equipment
        JButton breedBtn = getNewStyledButton(horse.getBreed());
        JButton colorBtn = getNewStyledButton(horse.getColor());
        JButton equipmentBtn = getNewStyledButton(horse.getEquipment());

        breedBtn.addActionListener(e -> {horse.nextBreed(); breedBtn.setText(horse.getBreed()); invalidate();});
        colorBtn.addActionListener(e -> {horse.nextColor(); colorBtn.setText(horse.getColor()); invalidate();});
        equipmentBtn.addActionListener(e -> {horse.nextEquipment(); equipmentBtn.setText(horse.getEquipment());});

        for (JButton btn : new JButton[]{breedBtn, colorBtn, equipmentBtn}) {
            btn.setBackground(ACCENT_BTN_COLOR);
            btn.setFont(extreemlySmallPixelFont);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);  // Ensure buttons are left-aligned
            rightPanel.add(btn);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));  // Space between buttons
        }

        horsePanel.add(rightPanel, BorderLayout.CENTER);
        return horsePanel;
    }
    private static JPanel createHorsePanelForRace(Horse horse) {
        JPanel horsePanel = new JPanel(new BorderLayout());
        horsePanel.setBackground(ACCENT_COLOR);
        horsePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));  // Set max height

        // Horse image setup
        BufferedImage ogImg = horse.getIcon();  // Assuming the Horse class has a getIcon method
        int size = Math.min(ogImg.getWidth(), ogImg.getHeight());
        BufferedImage cropped = ogImg.getSubimage(0, 0, size, size);
        Image scaledImg = cropped.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
        horsePanel.add(imageLabel, BorderLayout.WEST);

        // Right panel for displaying the horse info
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(ACCENT_COLOR);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Horse name
        JLabel name = new JLabel("  " + horse.getName());
        name.setFont(verySmallPixelFont);
        name.setForeground(Color.WHITE);
        rightPanel.add(name);

        // Horse speed
        JLabel speed = new JLabel(" Speed: --- m/s");
        speed.setFont(extreemlySmallPixelFont);
        speed.setForeground(Color.WHITE);
        rightPanel.add(speed);

        // Horse position
        JLabel position = new JLabel(" Position: #--");
        position.setFont(extreemlySmallPixelFont);
        position.setForeground(Color.WHITE);
        rightPanel.add(position);


        // Add the right panel with info and buttons to the main horsePanel
        horsePanel.add(rightPanel, BorderLayout.CENTER);
        return horsePanel;
    }


}
