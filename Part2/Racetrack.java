import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;

public class Racetrack extends JPanel {
    public static final int FIG8 = 0, OVAL = 1, CIRCLE = 2;

    private static int trackWidth = 100;

    private int trackType;
    private Path2D track;

    public Racetrack() {
        this.setOpaque(false);
        setTrack(FIG8);
    }

    public void setTrack(int trackType) {
        this.trackType = trackType;
        loadTrack();
    }

    public void loadTrack() {
        if (trackType == FIG8) track = infinityPath(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
        else if (trackType == OVAL) track = ovalPath(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
        else if (trackType == CIRCLE) track = circlePath(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
    }




    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Cast Graphics to Graphics2D for better control
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(GameManager.pixelFont);


        // 1. Render the track to an off-screen image
        BufferedImage buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bufferG2d = buffer.createGraphics();
        bufferG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferG2d.setColor(Color.decode(GameManager.ACCENT_COLOR));
        bufferG2d.setStroke(new BasicStroke(trackWidth));
        bufferG2d.draw(track); // Draw the track to the buffer
        drawLaneDivisions(bufferG2d, 6);
        //drawLanes(bufferG2d, 2);
        bufferG2d.dispose();


        // 3. Draw the pixelated image to the screen
        g2d.drawImage(pixelate(buffer, 5), 0, 0, null);

        drawLanes(g2d, 6);


        g2d.setColor(Color.decode("#A9731E"));
        g2d.drawString("FIG-8", 12, 72);
        g2d.setColor(Color.decode(GameManager.ACCENT_COLOR));
        g2d.drawString("FIG-8", 10, 70);
    }

    private void drawLanes(Graphics2D g2d, int lanes) {
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.red);
        for (int i = 0; i < lanes; i++) g2d.draw(generateLane(i,lanes, true));
    }

    private void drawLaneDivisions(Graphics2D g2d, int lanes) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.white);
        if (lanes > 1) {
            for (int i = 1; i < lanes; i++) { // Start at 1 to avoid first lane edge
                double midpointIndex = i - 0.5; // Place divisions between lanes
                g2d.draw(generateLane(midpointIndex, lanes, true));
            }
        }
    }



    public Path2D generateLane(double laneIndex, int totalLanes, boolean isCircular) {
        double laneWidth = trackWidth / (double) totalLanes;
        double laneOffset = (laneIndex - (totalLanes - 1) / 2.0) * laneWidth; // Center lanes correctly

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


    /*public Path2D generateLane(int laneIndex, int totalLanes) {
        double laneWidth = trackWidth / (double) totalLanes;
        double laneOffset = (laneIndex - (totalLanes - 1) / 2.0) * laneWidth; // Center lanes correctly

        Path2D lane = new Path2D.Double();
        PathIterator it = track.getPathIterator(null, 2.0);

        double[] coords = new double[6];
        double prevX = 0, prevY = 0;
        boolean first = true;

        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            double x = coords[0], y = coords[1];

            if (!first) {
                // Compute gradient
                double dx = x - prevX, dy = y - prevY;
                double length = Math.sqrt(dx * dx + dy * dy);
                double normalX = -dy / length, normalY = dx / length;

                // Offset for the lane
                double laneX = x + normalX * laneOffset;
                double laneY = y + normalY * laneOffset;

                if (type == PathIterator.SEG_MOVETO) lane.moveTo(laneX, laneY);
                else lane.lineTo(laneX, laneY);
            } else {
                lane.moveTo(x, y);
                first = false;
            }

            prevX = x;
            prevY = y;
            it.next();
        }

        return lane;
    }*/




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


}
