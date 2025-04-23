import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Racetrack extends JPanel {

    private static int trackWidth = 100;
    private static int length = 1500;
    private Path2D track;
    private ArrayList<Horse> horses; //lanes is the same as horses.size()

    //weather and track type
    public static final int FIG8 = 0, OVAL = 1, CIRCLE = 2;
    public static final int SUNNY = 0, CLOUDY = 1, RAINY = 2, MUDDY = 3, ICY = 4;
    public static final String[] trackNames = {"FIG-8", "OVAL", "CIRCLE"};
    public static final String[] weatherNames = {"SUNNY", "CLOUDY", "RAINY", "MUDDY", "ICY"};
    private int weatherType;
    private int trackType;


    public Racetrack(int track, int weather) {
        this.setOpaque(false);
        horses = new ArrayList<>();
        setTrack(track);
        setWeather(weather);
    }

    public int getLanes() {
        return horses.size();
    }

    public void increaseLength() {
        length+=100;
    }
    public void decreaseLength() {
        length-=100;
    }

    public void setTrack(int trackType) {
        this.trackType = trackType;
        loadTrack();
    }

    public void setWeather(int weather) {
        this.weatherType = weather;
        invalidate();
    }

    public void loadTrack() {
        if (trackType == FIG8) track = infinityPath(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
        else if (trackType == OVAL) track = ovalPath(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
        else if (trackType == CIRCLE) track = circlePath(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
    }


    public void addNewRandomHorse() {
        addHorse("placeHolder " + horses.size(), 0.5);
    }

    public void killLastHorse() {
        if (horses.size() > 1) horses.removeLast();
    }

    public void addHorse(String name, double confidence){
        Random r = new Random();
        SwingUtilities.invokeLater(() ->
                horses.add(new Horse(
                        name,
                        confidence,
                        horses.size(),
                        (int) (this.getWidth() * r.nextFloat()),
                        (int) (this.getHeight() * r.nextFloat()))
                )

        );
    }




    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Cast Graphics to Graphics2D for better control
        Graphics2D g2d = (Graphics2D) g;



        // 1. Render the track to an off-screen image
        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bufferG2d = buffer.createGraphics();
        bufferG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferG2d.setColor(GameManager.ACCENT_COLOR);
        bufferG2d.setStroke(new BasicStroke(trackWidth));
        bufferG2d.draw(track); // Draw the track to the buffer
        drawLaneDivisions(bufferG2d);
        bufferG2d.dispose();



        g2d.drawImage(pixelate(buffer, 5), 0, 0, null);
        //g2d.drawImage(buffer, 0, 0, null);
        //drawLanes(g2d);
        //draw horses
        for (Horse horse: horses) horse.draw(g2d);

        g2d.setFont(GameManager.bigPixelFont);
        g2d.setColor(Color.decode("#A9731E"));
        g2d.drawString(trackNames[trackType], 12, 72);
        g2d.setColor(GameManager.ACCENT_COLOR);
        g2d.drawString(trackNames[trackType], 10, 70);

        g2d.setFont(GameManager.smallPixelFont);
        g2d.setColor(Color.decode("#A9731E"));
        g2d.drawString(length + "m", 11, 101);
        g2d.setColor(GameManager.ACCENT_COLOR);
        g2d.drawString(length + "m", 10, 100);

        g2d.setFont(GameManager.verySmallPixelFont);
        g2d.setColor(Color.decode("#A9731E"));
        g2d.drawString(weatherNames[weatherType], 11, 121);
        g2d.setColor(GameManager.ACCENT_COLOR);
        g2d.drawString(weatherNames[weatherType], 10, 120);
    }

    private void drawLanes(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.red);
        for (int i = 0; i < horses.size(); i++) g2d.draw(generateLane(i, true));
    }

    private void drawLaneDivisions(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.white);
        if (horses.size() > 1) {
            for (int i = 1; i < horses.size(); i++) { // Start at 1 to avoid first lane edge
                double midpointIndex = i - 0.5; // Place divisions between lanes
                g2d.draw(generateLane(midpointIndex, true));
            }
        }
    }



    public Path2D generateLane(double laneIndex, boolean isCircular) {
        double laneWidth = trackWidth / (double) horses.size();
        double laneOffset = (laneIndex - (horses.size() - 1) / 2.0) * laneWidth; // Center lanes correctly

        Path2D lane = new Path2D.Double();
        PathIterator it = track.getPathIterator(null, 2.0);

        double[] coords = new double[6];
        double prevX = 0, prevY = 0;
        double firstX = 0, firstY = 0; // Store first point
        double lastOffsetX = 0, lastOffsetY = 0; // Store last calculated offset
        boolean firstCoord = true, firstDefinition = true;

        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            double x = coords[0], y = coords[1];

            if (!firstCoord) {
                // Compute gradient
                double dx = x - prevX, dy = y - prevY;
                double length = Math.sqrt(dx * dx + dy * dy);
                double normalX = -dy / length, normalY = dx / length;

                // Compute lane offset
                double laneX = x + normalX * laneOffset;
                double laneY = y + normalY * laneOffset;

                if (type == PathIterator.SEG_MOVETO) lane.moveTo(laneX, laneY);
                else if (firstDefinition) {lane.moveTo(laneX, laneY); firstDefinition = false;}
                else lane.lineTo(laneX, laneY);

                lastOffsetX = normalX * laneOffset; // Store last applied offset
                lastOffsetY = normalY * laneOffset;
            } else {
                firstX = x; // Store the original first point
                firstY = y;
                firstCoord = false;
            }

            prevX = x;
            prevY = y;
            it.next();
        }

        // Apply the last offset to the first point before closing
        lane.lineTo(firstX + lastOffsetX, firstY + lastOffsetY);
        if (isCircular) lane.closePath();

        return lane;
    }




    public static Path2D.Double infinityPath(int centerX, int centerY, int width, int height) {
        Path2D.Double infinity = new Path2D.Double();

        // Parametric equations for the lemniscate
        // x = a * cos(t) / (1 + sin²(t))
        // y = a * sin(t) * cos(t) / (1 + sin²(t))

        double a = Math.min(width, height) * 0.5; // Scale factor
        int segments = 100; // Smoothness of the curve

        for (int i = 0; i <= segments; i++) {
            double t = 2 * Math.PI * i / segments;
            double denominator = 1 + Math.pow(Math.sin(t), 2);
            double x = a * Math.cos(t) / denominator;
            double y = a * Math.sin(t) * Math.cos(t) / denominator;

            // Translate to center and flip Y-axis
            double px = centerX + x;
            double py = centerY - y;

            if (i == 0) {
                infinity.moveTo(px, py);
            } else {
                infinity.lineTo(px, py);
            }
        }

        infinity.closePath();
        return infinity;
    }

    public static Path2D.Double ovalPath(int centerX, int centerY, int width, int height) {
        Path2D.Double oval = new Path2D.Double();

        // Parametric equations for the ellipse (oval)
        // x = a * cos(t)
        // y = b * sin(t)

        double a = width * 0.4;  // Semi-major axis
        double b = height * 0.4; // Semi-minor axis
        int segments = 100;      // Smoothness of the curve

        for (int i = 0; i <= segments; i++) {
            double t = 2 * Math.PI * i / segments;
            double x = a * Math.cos(t);
            double y = b * Math.sin(t);

            // Translate to center and flip Y-axis
            double px = centerX + x;
            double py = centerY - y;

            if (i == 0) {
                oval.moveTo(px, py);
            } else {
                oval.lineTo(px, py);
            }
        }

        oval.closePath();
        return oval;
    }

    public static Path2D.Double circlePath(int centerX, int centerY, int width, int height) {
        Path2D.Double circle = new Path2D.Double();

        // Use the smaller of width or height as the diameter
        int radius = Math.min(width, height) / 3;

        int segments = 100; // Smoothness of the curve

        for (int i = 0; i <= segments; i++) {
            double t = 2 * Math.PI * i / segments;
            double x = radius * Math.cos(t);
            double y = radius * Math.sin(t);

            // Translate to center and flip Y-axis
            double px = centerX + x;
            double py = centerY - y;

            if (i == 0) {
                circle.moveTo(px, py);
            } else {
                circle.lineTo(px, py);
            }
        }

        circle.closePath();
        return circle;
    }




    private BufferedImage pixelate(BufferedImage original, int pixelSize) {
        int w = original.getWidth();
        int h = original.getHeight();
        BufferedImage pixelated = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y += pixelSize) {
            for (int x = 0; x < w; x += pixelSize) {
                Color avgColor = getAverageColor(original, x, y, pixelSize, w, h);
                fillBlock(pixelated, x, y, pixelSize, avgColor);
            }
        }
        return pixelated;
    }
    private Color getAverageColor(BufferedImage img, int x, int y, int size, int maxW, int maxH) {
        int r = 0, g = 0, b = 0, a = 0, count = 0;
        for (int dy = 0; dy < size && y + dy < maxH; dy++) {
            for (int dx = 0; dx < size && x + dx < maxW; dx++) {
                Color color = new Color(img.getRGB(x + dx, y + dy), true);
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
                a += color.getAlpha();
                count++;
            }
        }
        if (count == 0) return new Color(0, 0, 0, 0);
        return new Color(r / count, g / count, b / count, a / count);
    }

    private void fillBlock(BufferedImage img, int x, int y, int size, Color color) {
        for (int dy = 0; dy < size && y + dy < img.getHeight(); dy++) {
            for (int dx = 0; dx < size && x + dx < img.getWidth(); dx++) {
                img.setRGB(x + dx, y + dy, color.getRGB());
            }
        }
    }


    public void resetTrack(int minDuration) {
        for (Horse horse : horses) horse.goBackToStart(this, minDuration);

    }

    public void advanceEvent() {
        for (Horse horse : horses) horse.advanceEvent();
    }
}
