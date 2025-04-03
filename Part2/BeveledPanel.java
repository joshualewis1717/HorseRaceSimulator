import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BeveledPanel extends JPanel {

    private static Color fillColor = Color.decode(GameManager.ACCENT_COLOR);
    private static Color outlineColor = Color.decode("#A9731E");

    // Custom panel constructor
    public BeveledPanel() {
        setOpaque(false);  // Make the panel transparent so the bevel effect shows through
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();

        // Anti-aliasing for smoother bevel edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a rounded rectangle with bevel effect
        // Outer bevel (lighter edge)
        g2d.setColor(outlineColor);  // Light color for outer bevel
        g2d.fill(new RoundRectangle2D.Double(2, 2, width - 4, height - 4, 20, 20));

        // Inner bevel (darker edge)
        g2d.setColor(fillColor);  // Darker color for inner bevel
        g2d.fill(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 20, 20));
    }
}
