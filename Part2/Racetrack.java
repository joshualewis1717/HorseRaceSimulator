import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class Racetrack extends JPanel {
    //general class fields
    private static int trackWidth = 100;
    private static int length = 150;
    private Path2D track;
    private ArrayList<Horse> horses; //lanes is the same as horses.size()

    //weather and track type
    public static final int FIG8 = 0, OVAL = 1, CIRCLE = 2;
    public static final int SUNNY = 0, CLOUDY = 1, RAINY = 2, MUDDY = 3, ICY = 4;
    public static final String[] trackNames = {"FIG-8", "OVAL", "CIRCLE"};
    public static final String[] weatherNames = {"SUNNY", "CLOUDY", "RAINY", "MUDDY", "ICY"};
    private int weatherType;
    private int trackType;

    //speed metrics
    private LocalDateTime startTime;

    //constructor
    //
    public Racetrack(int track, int weather) {
        this.setOpaque(false);
        horses = new ArrayList<>();
        setTrack(track);
        setWeather(weather);
    }

    //accessor methods
    //
    public int getLanes() {
        return horses.size();
    }
    public int getLength() {
        return length;
    }
    public ArrayList<Horse> getHorses() {
        return horses;
    }

    //event driven mutators - triggered from GameManager
    //
    public void increaseLength() {
        length+=10;
        loadTrack();
    }
    public void decreaseLength() {
        length-=10;
        loadTrack();
    }
    public void setTrack(int trackType) {
        this.trackType = trackType;
        loadTrack();
    }
    public void setWeather(int weather) {
        this.weatherType = weather;
        if (weather == SUNNY || weather == MUDDY) GameManager.setBackgroundColor(GameManager.BACKGROUND_COLOR);
        if (weather == CLOUDY || weather == ICY) GameManager.setBackgroundColor(Color.decode("#cd9b1f"));
        if (weather == RAINY) GameManager.setBackgroundColor(Color.decode("#bb8c17"));
        invalidate();
    }


    //dynamic creation and deletion of horses when user changes number of lanes
    //
    public void addNewRandomHorse() {
        addHorse("placeHolder " + horses.size(), 0.5);
    }
    public void killLastHorse() {
        if (horses.size() > 1) {
            Horse horse = horses.removeLast();
            horse.marshal();//just cancels any animations and timers, and facilitates java's garbage collection
        }
    }
    public void addHorse(String name, double confidence){
        Random r = new Random();
        SwingUtilities.invokeLater(() -> {
            Horse horse = new Horse(
                    name,
                    confidence,
                    horses.size(),
                    (int) (20 + this.getWidth() * r.nextFloat() * 0.9f),//not rlly accurate padding but it works
                    (int) (20 + this.getHeight() * r.nextFloat() * 0.9f));
            horse.frolic();
            horses.add(horse);
        });
    }

    //horse management
    //
    public void advanceEvent() {
        for (Horse horse : horses) {
            horse.advanceEvent(getFallProbabilityMultiplier(), getWeatherDebuff());
        }
    }
    public void resetTrack(int duration) {
        marshalHorses();
        for (Horse horse : horses) horse.goBackToStart(this, duration);
    }
    public boolean allRacersDown() {
        for (Horse horse : horses) if (!horse.hasFallen()) return false;//for all horses, if anybody still moving its good
        GameManager.lastWinner = "NOBODY";
        return true;//otherwise they all down
    }
    public void sortHorsesByProgress() { //uses built in arraylist sort and uses the number of advance events as the comparator
        horses.sort((h1, h2) -> Integer.compare(h2.getProgress(), h1.getProgress()));
    }

    //horse frolicing
    //
    public void releaseHorses() {
        for (Horse horse: horses) if (!horse.isFrolicing()) horse.frolic();
        for (Horse horse: horses) horse.reloadImg(); //remove any dark fallen tint
    }
    public void marshalHorses() {
        for (Horse horse: horses) horse.marshal();
    }
    public void onResized() {
        Horse.setFrolicArea(getWidth(), getHeight());
    }

    //user customization debuffs/buffs - used in advanceEvent
    //
    public double getWeatherDebuff() {
        switch (weatherType) {
            case SUNNY: return 0.0; // speed
            case CLOUDY: return 0.05; // slight slow
            case RAINY: case MUDDY: case ICY: return 0.1; // max debuff
            default: return 0.0; // fallback
        }
    }
    private double getFallProbabilityMultiplier() {
        switch (weatherType) {
            case SUNNY: return 0.005; // almost zero
            case CLOUDY: return 0.01; // rare fall
            case RAINY: return 0.05; // ok chance
            case MUDDY: return 0.08; // big risk
            case ICY: return 0.1; // guaranteed somebody's dying
            default: return 0.005; // fallback
        }
    }

    //speed metrics
    //
    public void logStart() {
        startTime = LocalDateTime.now();
    }
    public long getTime() {
        return Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }
    public String getSpeed(Horse horse) {
        float speed = ((float) horse.getProgress()) / getTime();
        return String.format("%.2f", speed);
    }

    //main racetrack render function - creates the final image
    //
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //generate a buffered image of the track
        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bufferG2d = buffer.createGraphics(); //create a new graphics object only used fot this buffer
        bufferG2d.setColor(GameManager.ACCENT_COLOR);
        bufferG2d.setStroke(new BasicStroke(trackWidth));
        bufferG2d.draw(track); //draw the track to an offscreen buffer
        drawLaneDivisions(bufferG2d); //draw the lane divisions too
        bufferG2d.dispose(); //new graphics obj not-needed anymore


        g2d.drawImage(pixelate(buffer), 0, 0, null); //pixelate track and draw it to screen as an image
        for (Horse horse: horses) horse.draw(g2d); //overlay horses

        Color textColor = Color.decode("#A9731E");

        g2d.setColor(textColor);
        g2d.setFont(GameManager.bigPixelFont);
        g2d.drawString(trackNames[trackType], 12, 72);
        g2d.setColor(GameManager.ACCENT_COLOR);
        g2d.drawString(trackNames[trackType], 10, 70);

        g2d.setColor(textColor);
        g2d.setFont(GameManager.smallPixelFont);
        g2d.drawString(length + "m", 11, 101);
        g2d.setColor(GameManager.ACCENT_COLOR);
        g2d.drawString(length + "m", 10, 100);

        g2d.setColor(textColor);
        g2d.setFont(GameManager.verySmallPixelFont);
        g2d.drawString(weatherNames[weatherType], 11, 121);
        g2d.setColor(GameManager.ACCENT_COLOR);
        g2d.drawString(weatherNames[weatherType], 10, 120);
    }

    //track generators - use parametric curves to draw a path
    //
    public void loadTrack() {
        if (trackType == FIG8) track = infinityPath(getWidth(), getHeight());
        else if (trackType == OVAL) track = ovalPath(getWidth(), getHeight());
        else if (trackType == CIRCLE) track = circlePath( getWidth(), getHeight());
    }
    public static Path2D.Double infinityPath(int width, int height) {
        // Parametric equations for the lemniscate (infinity)
        // x = a * cos(t) / (1 + sin²(t))
        // y = a * sin(t) * cos(t) / (1 + sin²(t))

        Path2D.Double infinity = new Path2D.Double();
        int centerX = width / 2;
        int centerY = height / 2;
        double a = Math.min(width, height) * 0.5; // Scale factor

        for (int i = 0; i <= length; i++) {
            double t = 2 * Math.PI * i / length;
            double denominator = 1 + Math.pow(Math.sin(t), 2);
            double x = a * Math.cos(t) / denominator;
            double y = a * Math.sin(t) * Math.cos(t) / denominator;

            plot(infinity, i, centerX + x, centerY - y); //translate to center and flip Y-axis
        }

        infinity.closePath();
        return infinity;
    }
    public static Path2D.Double ovalPath(int width, int height) {
        // Parametric equations for the ellipse (oval)
        // x = a * cos(t)
        // y = b * sin(t)

        Path2D.Double oval = new Path2D.Double();
        int centerX = width / 2;
        int centerY = height / 2;
        double a = width * 0.4;  // Semi-major axis
        double b = height * 0.25; // Semi-minor axis

        for (int i = 0; i <= length; i++) {
            double t = 2 * Math.PI * i / length;
            double x = a * Math.cos(t);
            double y = b * Math.sin(t);

            plot(oval, i, centerX + x, centerY - y); //translate to center and flip Y-axis
        }

        oval.closePath();
        return oval;
    }
    public static Path2D.Double circlePath(int width, int height) {
        Path2D.Double circle = new Path2D.Double();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 4;

        for (int i = 0; i <= length; i++) {
            double t = 2 * Math.PI * i / length;
            double x = radius * Math.cos(t);
            double y = radius * Math.sin(t);

            plot(circle, i, centerX + x, centerY - y); //translate to center and flip Y-axis
        }

        circle.closePath();
        return circle;
    }
    private static void plot(Path2D.Double track, int i, double px, double py) {
        if (i == 0) track.moveTo(px, py); //if statement stops a line from (0,0) to start coord
        else track.lineTo(px, py);
    }

    //pathing features - reliant on the base paths created above
    //
    private void drawLaneDivisions(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.white);
        if (horses.size() > 1) {
            for (int i = 1; i < horses.size(); i++) { // Start at 1 to avoid first lane edge
                double midpointIndex = i - 0.5; // Place divisions between lanes
                g2d.draw(generateLane(midpointIndex));
            }
        }
    }
    public Path2D generateLane(double laneIndex) {
        //basically works out the normal at each point using the last one, then uses the track width
        //to move along that normal, and plot a lane inside the trackWidth
        double laneWidth = trackWidth / (double) horses.size();
        double laneOffset = (laneIndex - (horses.size() - 1) / 2.0) * laneWidth; //center lanes correctly

        Path2D lane = new Path2D.Double();
        PathIterator it = track.getPathIterator(null, 2.0);

        double[] coords = new double[6];
        double prevX = 0, prevY = 0;
        double firstX = 0, firstY = 0; //store first point
        double lastOffsetX = 0, lastOffsetY = 0; //store last calculated offset
        boolean firstCoord = true, firstDefinition = true;

        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            double x = coords[0], y = coords[1];

            if (!firstCoord) {
                //compute gradient
                double dx = x - prevX, dy = y - prevY;
                double length = Math.sqrt(dx * dx + dy * dy);
                double normalX = -dy / length, normalY = dx / length;

                //compute lane offset
                double laneX = x + normalX * laneOffset;
                double laneY = y + normalY * laneOffset;

                if (type == PathIterator.SEG_MOVETO) lane.moveTo(laneX, laneY);
                else if (firstDefinition) {lane.moveTo(laneX, laneY); firstDefinition = false;}
                else lane.lineTo(laneX, laneY);

                lastOffsetX = normalX * laneOffset;
                lastOffsetY = normalY * laneOffset;
            } else {
                firstX = x;
                firstY = y;
                firstCoord = false;
            }

            prevX = x;
            prevY = y;
            it.next();
        }

        //apply the last offset to the first point before closing
        lane.lineTo(firstX + lastOffsetX, firstY + lastOffsetY);
        lane.closePath();

        return lane;
    }

    //image pixelate effect - to maintain the feel of the game by pixelating the racetrack
    //
    private BufferedImage pixelate(BufferedImage original) {
        int w = original.getWidth();
        int h = original.getHeight();
        BufferedImage pixelated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        //5 is the pixelation
        for (int y = 0; y < h; y += 5) {
            for (int x = 0; x < w; x += 5) {
                Color avgColor = getAverageColor(original, x, y, 5, w, h); //get average color in the block
                fillBlock(pixelated, x, y, 5, avgColor);                   //fill the block with said color
            }
        }
        return pixelated;
    }
    private Color getAverageColor(BufferedImage img, int x, int y, int size, int maxW, int maxH) {
        int r = 0, g = 0, b = 0, a = 0, count = 0;
        for (int dy = 0; dy < size && y + dy < maxH; dy++) {
            for (int dx = 0; dx < size && x + dx < maxW; dx++) {//for every pixel inside the chunk given
                Color color = new Color(img.getRGB(x + dx, y + dy), true); //sample that pixel
                r += color.getRed(); //tally up rgb and a channels
                g += color.getGreen();
                b += color.getBlue();
                a += color.getAlpha();
                count++; //count how many pixels sampled to average later
            }
        }
        if (count == 0) return new Color(0, 0, 0, 0); //if no pixels sampled just return a clear color
        return new Color(r / count, g / count, b / count, a / count); //otherwise return mean value
    }
    private void fillBlock(BufferedImage img, int x, int y, int size, Color color) {
        for (int dy = 0; dy < size && y + dy < img.getHeight(); dy++) {
            for (int dx = 0; dx < size && x + dx < img.getWidth(); dx++) {//same as before for every pixel in the specified range
                img.setRGB(x + dx, y + dy, color.getRGB()); //only this time just fill it with the average colour from before
            }
        }
    }
}
