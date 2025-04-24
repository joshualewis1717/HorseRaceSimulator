import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class Horse {
    private int x, y; // Horse position
    private double rotationAngle = 0; //in radians
    public static final String srcImgPath = "assets/horse.png";
    private static final String srcIconPathFolder = "assets/horseIcons/";
    private BufferedImage imgAtlas; // Horse image
    public BufferedImage horseIcon;
    private PathIterator trackIterator;

    private static final Random rand = new Random();
    public static final String[] breeds = {"Thoroughbred", "Clydesdale", "Mustang"};
    public static final String[] colors = {"red", "green", "blue"};
    public static final String[] equipmentList = {"nothing"};

    private String name;
    private double confidence;
    private int lane;
    private int breed;
    private int color;
    private int equipment;
    private Path2D track;

    public Horse(String name, double confidence, int lane, int x, int y) {
        this.name = name;
        this.confidence = confidence;
        this.lane = lane;
        this.breed = rand.nextInt(breeds.length);
        this.color = rand.nextInt(colors.length);
        this.equipment = rand.nextInt(equipmentList.length);
        this.x = x;
        this.y = y;
        try {
            imgAtlas = ImageIO.read(getClass().getResource(srcImgPath)); // Load image
            horseIcon = ImageIO.read(getClass().getResource(srcIconPathFolder + rand.nextInt(5) + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void draw(Graphics2D g2d) {
        if (imgAtlas != null) {
            AffineTransform originalTransform = g2d.getTransform();


            BufferedImage img = imgAtlas.getSubimage(0, 0, 110, 567);

            AffineTransform transform = new AffineTransform();
            float scale = 0.2f;
            transform.translate(x - 0.5*scale*img.getWidth(), y - 0.5*scale*img.getHeight());
            transform.scale(scale,scale);
            transform.rotate(rotationAngle, (double) img.getWidth() /2, (double) img.getHeight() /2);

            g2d.drawImage(img, transform, null);
            g2d.setTransform(originalTransform);
        }
    }

    public String getBreed() {
        return breeds[breed];
    }
    public String getColor() {
        return colors[color];
    }
    public String getEquipment() {
        return equipmentList[equipment];
    }

    double animTime;
    public void animateTo(int targetX, int targetY, int duration) {
        final int totalFrames = duration / 32;
        final double step = 1.0 / totalFrames;
        animTime = 0;

        // Calculate the difference in x and y coordinates
        int dx = targetX - x;
        int dy = targetY - y;

        int initialX = x;
        int initialY = y;

        setRotationAngle(targetX, targetY);

        Timer timer = new Timer(32, e -> {
            animTime += step;

            // Stop the animation when the progress reaches 1
            if (animTime >= 1.0) {
                animTime = 1.0;
                ((Timer) e.getSource()).stop(); // Stop the timer
                x = targetX;
                y = targetY;
                setRotationAngle(0);
                GameManager.invalidate();
            } else {
                x = (int) (initialX + animTime * dx);
                y = (int) (initialY + animTime * dy);
                GameManager.invalidate();
            }
        });

        timer.start();
    }

    public void setRotationAngle(int targetX, int targetY) {
        setRotationAngle(Math.atan2(targetY - y, targetX - x) + Math.PI / 2);
    }
    public void setRotationAngle(double rads) {
        rotationAngle = rads;
    }

    public BufferedImage getIcon() {return horseIcon;}


    public void goBackToStart(Racetrack racetrack, int minDuration) {
        track = racetrack.generateLane(lane,true);

        trackIterator = track.getPathIterator(null);
        double[] coords = new double[6];
        trackIterator.currentSegment(coords);
        animateTo((int) coords[0], (int) coords[1], minDuration);
    }

    public void advanceEvent() {
        if (trackIterator.isDone()) {
            GameManager.setWinner(this);
            System.out.println("CALLED" + name);
            return;
        }

        if (Math.random() < confidence) {
            double[] coords = new double[6];
            int segmentType = trackIterator.currentSegment(coords);

            if (segmentType != PathIterator.SEG_CLOSE && !trackIterator.isDone()) {
                if (!((int) coords[0] == 0)) {//stop them teleporting to the origin when done for some reason
                    setRotationAngle((int) coords[0], (int) coords[1]);
                    x = (int) coords[0];
                    y = (int) coords[1];
                }
            }
            trackIterator.next();
        }
    }


    public String getName() {
        return name;
    }
}
