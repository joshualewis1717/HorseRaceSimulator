import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public class Horse {
    private int x, y; // Horse position
    private double rotationAngle = 0; //in radians
    public static final String srcImgPath = "assets/horse.png";
    private static final String srcIconPathFolder = "assets/horseIcons/";
    private BufferedImage imgAtlas; // Horse image
    private static final float TINT_STRENGTH = 0.1f;
    private int imgIndex = 0;//current image in the atlas (3 images)
    public BufferedImage horseIcon;
    private PathIterator trackIterator;

    private static final Random rand = new Random();
    public static final String[] breeds = {"Thoroughbred", "Clydesdale", "Mustang"};
    public static final double[] breedEffect = {0.15, -0.05, 0.05};
    public static final String[] colors = {"no color","Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "Orange", "Pink"};
    public static final Color[] colorsObj = {null, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK};
    public static final String[] equipmentList = {"no equipment", "goggles", "saddle", "horseshoes", "hood"};
    public static final double[] equipmentEffect = {0.0, 0.1, 0.2, 0.15, -0.1};

    private String name;
    private double confidence;
    private boolean hasFallen;
    private int motionCount;
    private int lane;
    private int breed;
    private int color;
    private int equipment;
    private Path2D track;

    private static int imgAssignCounter = 0;

    //frolic
    private javax.swing.Timer frolicTimer;
    private boolean frolicing = false;
    private Random r = new Random();
    private static int[] frolicArea = {800,600};//just a basic small size until its updated

    //anim
    private javax.swing.Timer animTimer;


    public Horse(String name, double confidence, int lane, int x, int y) {
        this.name = name;
        this.confidence = confidence;
        this.hasFallen = false;
        this.lane = lane;
        this.breed = rand.nextInt(breeds.length);
        this.color = 0;
        this.motionCount = 0;
        this.equipment = 0;
        this.x = x;
        this.y = y;
        try {
            imgAtlas = ImageIO.read(getClass().getResource(srcImgPath)); // Load image
            horseIcon = ImageIO.read(getClass().getResource(srcIconPathFolder + imgAssignCounter + ".png"));
        } catch (IOException e) { e.printStackTrace(); }
        imgAssignCounter++;
        if (imgAssignCounter > 4) imgAssignCounter = 0;
    }

    public void tintImg(Color color) {
        if (color == null) return; //for when there is no tint
        for (int y = 0; y < imgAtlas.getHeight(); y++) {
            for (int x = 0; x < imgAtlas.getWidth(); x++) {
                int pixel = imgAtlas.getRGB(x, y);

                int alpha = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel) & 0xFF;

                if (alpha < 255) continue;


                r = (int) (r * (1 - TINT_STRENGTH) + color.getRed() * TINT_STRENGTH);
                g = (int) (g * (1 - TINT_STRENGTH) + color.getGreen() * TINT_STRENGTH);
                b = (int) (b * (1 - TINT_STRENGTH) + color.getBlue() * TINT_STRENGTH);

                // Make sure RGB values are within the valid range (0-255)
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                int newPixel = (255 << 24) | (r << 16) | (g << 8) | b;
                imgAtlas.setRGB(x, y, newPixel);
            }
        }
    }
    public void reloadImg() {
        try {
            imgAtlas = ImageIO.read(getClass().getResource(srcImgPath)); // Load image atlas
            tintImg(colorsObj[color]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void draw(Graphics2D g2d) {
        if (imgAtlas != null) {
            AffineTransform originalTransform = g2d.getTransform();

            //find image from img atlas
            int offset = 0;
            if (imgIndex == 1) offset = 160;
            else if (imgIndex == 2) offset = 330;
            BufferedImage img = imgAtlas.getSubimage(0+offset, 0, 110, 567);

            //scale and rotate to match final position
            AffineTransform transform = new AffineTransform();
            float scale = 0.2f;
            transform.translate(x - 0.5*scale*img.getWidth(), y - 0.5*scale*img.getHeight());
            transform.scale(scale,scale);
            transform.rotate(rotationAngle, (double) img.getWidth() /2, (double) img.getHeight() /2);

            //draw image and reset the transform because the object is borrowed and used elsewhere
            g2d.drawImage(img, transform, null);
            g2d.setTransform(originalTransform);
        }
    }

    //frolic
    public static void setFrolicArea(int width, int height) {
        frolicArea[0] = width;
        frolicArea[1] = height;
    }
    public void marshal() {
        frolicing = false;
        if (animTimer != null) animTimer.stop();
    }
    public void frolic(){
        frolicing = true;

        frolicTimer = new javax.swing.Timer(3000 + r.nextInt(4000), e -> {
            if (frolicing) {
                doFrolic();
            } else {
                ((javax.swing.Timer) e.getSource()).stop(); // stop when race ends
            }
        });
        doFrolic();
        frolicTimer.start();
    }
    private void doFrolic() {
        if (imgIndex == 3) {
            imgIndex = 1;
        } else {
            if (r.nextFloat() < 0.6) { //either frolic or graze
                int range = 200; // how far the horse can wander from its current spot
                int newX = x + r.nextInt(range * 2 + 1) - range;
                int newY = y + r.nextInt(range * 2 + 1) - range;

                newX = Math.max(100, Math.min(newX, frolicArea[0] - 100));//clamp to frolic area
                newY = Math.max(100, Math.min(newY, frolicArea[1] - 100));

                animateTo(newX, newY, 2000);
            } else {
                imgIndex = 3;
            }
        }
    }
    public boolean isFrolicing() {return frolicing;}

    public String getBreed() {
        return breeds[breed];
    }
    public void nextBreed() {
        breed++;
        if (breed >= breeds.length) breed = 0;
    }
    public String getColor() {
        return colors[color];
    }
    public void nextColor() {
        color++;
        if (color >= colors.length) color = 0;
        reloadImg(); tintImg(colorsObj[color]);
    }
    public String getEquipment() {
        return equipmentList[equipment];
    }
    public void nextEquipment() {
        equipment++;
        if (equipment >= equipmentList.length) equipment = 0;
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

        animTimer = new Timer(32, e -> {
            animTime += step;

            // Stop the animation when the progress reaches 1
            if (animTime >= 1.0) {
                animTime = 1.0;
                ((Timer) e.getSource()).stop(); // Stop the timer
                x = targetX;
                y = targetY;
                GameManager.invalidate();
            } else {
                x = (int) (initialX + animTime * dx);
                y = (int) (initialY + animTime * dy);
                GameManager.invalidate();

                if ((int)(animTime * totalFrames) % 4 == 0) flipWalkingFrames(); //once every 4 frames
            }
        });

        animTimer.start();
    }

    private void flipWalkingFrames() {
        if (imgIndex == 0) imgIndex = 1;
        else imgIndex = 0;

    }

    public void setRotationAngle(int targetX, int targetY) {
        setRotationAngle(Math.atan2(targetY - y, targetX - x) + Math.PI / 2);
    }
    public void setRotationAngle(double rads) {
        rotationAngle = rads;
    }

    public BufferedImage getIcon() {return horseIcon;}


    public void goBackToStart(Racetrack racetrack, int duration) {
        track = racetrack.generateLane(lane,true);

        trackIterator = track.getPathIterator(null);
        double[] coords = new double[6];
        trackIterator.currentSegment(coords);
        animateTo((int) coords[0], (int) coords[1], duration);
        hasFallen = false;
        motionCount = 0;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {setRotationAngle(0);}, duration, TimeUnit.MILLISECONDS);
        executor.shutdown();
    }



    public void advanceEvent(double fallProbability, double weatherDebuff) {
        if  (!hasFallen)
        {
            if (trackIterator.isDone()) {
                GameManager.setWinner(this);
                this.confidence += 0.1;                      //<------------buffed confidence here
                return;
            }

            double updatedConfidence = confidence - weatherDebuff + equipmentEffect[equipment] + breedEffect[breed];
            if (updatedConfidence < 0.1) updatedConfidence = 0.1;
            else if (updatedConfidence > 1.0) updatedConfidence = 1.0;

            if (Math.random() < updatedConfidence) {
                double[] coords = new double[6];
                int segmentType = trackIterator.currentSegment(coords);

                if (segmentType != PathIterator.SEG_CLOSE && !trackIterator.isDone()) {
                    if (!((int) coords[0] == 0)) {//stop them teleporting to the origin when done for some reason
                        setRotationAngle((int) coords[0], (int) coords[1]);
                        x = (int) coords[0];
                        y = (int) coords[1];
                        flipWalkingFrames();
                    }
                }
                trackIterator.next();
                motionCount++;
            }

            if (Math.random() < (fallProbability*updatedConfidence*updatedConfidence))
            {
                fall();
            }
        }
    }

    public int getProgress() {
        return motionCount;
    }


    public void fall() {
        confidence -= 0.1;
        hasFallen = true;
        imgIndex = 2;//fallen img in atlas
        tintImg(Color.BLACK); //do it 4 times to you can actually see it
        tintImg(Color.BLACK);
        tintImg(Color.BLACK);
        tintImg(Color.BLACK);
    }

    public String getName() {
        return name;
    }

    public boolean hasFallen() {
        return hasFallen;
    }
}
