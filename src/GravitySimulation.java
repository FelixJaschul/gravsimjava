import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GravitySimulation extends JPanel implements ActionListener, ComponentListener {
    private static final int WIDTH = 800; // 800 = standard
    private static final int HEIGHT = 600; // 600 = standard

    private static final boolean color = true;

    private static final Color vectorColor = Color.LIGHT_GRAY;
    private static final Color circleColor = Color.LIGHT_GRAY;
    private static final Color backgroundColor = Color.WHITE;

    private static final double G = 6.67430e-11; // 6.67430e-11 = standard

    private static final int PLANET_OFFSET = 150;

    private final List<Planet> planets;
    private int centerX;
    private int centerY;

    public GravitySimulation() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(backgroundColor);

        centerX = WIDTH / 2;
        centerY = HEIGHT / 2;

        planets = new ArrayList<>();
        planets.add(new Planet(centerX - PLANET_OFFSET, centerY, 1e15, 0, 10, circleColor));
        planets.add(new Planet(centerX + PLANET_OFFSET, centerY, 1e15, 0, -10, circleColor));

        Timer timer = new Timer(4, this); // 16 = standard
        timer.start();

        // Add component listener to detect resize events
        addComponentListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw gravitational field vectors
        drawGravitationalField(g2d, planets);

        for (Planet planet : planets) planet.draw(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Calculate and apply gravitational forces
        for (int i = 0; i < planets.size(); i++) {
            for (int j = i + 1; j < planets.size(); j++) {
                // Calculate gravitational acceleration
                double[] accelerations = calculateGravitationalForce(planets.get(i), planets.get(j));

                double dt = 0.05;
                planets.get(i).updatePosition(accelerations[0], accelerations[1], dt);
                planets.get(j).updatePosition(accelerations[2], accelerations[3], dt);
            }
        }

        repaint();
    }

    // Method to reposition planets around the center
    private void repositionPlanets() {
        if (planets.size() >= 2) {
            // Reposition the first two planets (you can extend this for more planets)
            planets.getFirst().x = centerX - PLANET_OFFSET;
            planets.getFirst().y = centerY;
            planets.get(0).velocityX = 0; // Reset velocity
            planets.get(0).velocityY = 10; // Reset to initial velocity

            planets.get(1).x = centerX + PLANET_OFFSET;
            planets.get(1).y = centerY;
            planets.get(1).velocityX = 0; // Reset velocity
            planets.get(1).velocityY = -10; // Reset to initial velocity
        }
    }


    private double[] calculateGravitationalForce(Planet planet1, Planet planet2) {
        // Calculate distance between planets
        double dx = planet2.x - planet1.x;
        double dy = planet2.y - planet1.y;

        // Avoid division by zero
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 1) distance = 1;

        // Calculate gravitational force
        double force = G * planet1.mass * planet2.mass / (distance * distance);

        // Calculate acceleration components
        double angle = Math.atan2(dy, dx);
        double acceleration1X = force * Math.cos(angle) / planet1.mass;
        double acceleration1Y = force * Math.sin(angle) / planet1.mass;

        double acceleration2X = -force * Math.cos(angle) / planet2.mass;
        double acceleration2Y = -force * Math.sin(angle) / planet2.mass;

        return new double[] {acceleration1X, acceleration1Y, acceleration2X, acceleration2Y};
    }

    private void drawGravitationalField(Graphics2D g, List<Planet> planets) {
        int gridSpacing = 15; // 25 = standard
        double maxVectorLength = 50; // 50 = standard
        double maxInfluenceDistance = 300; // 300 == standard

        g.setColor(vectorColor);

        int width = getWidth();
        int height = getHeight();

        for (int x = 0; x < width; x += gridSpacing) {
            for (int y = 0; y < height; y += gridSpacing) {
                double totalFx = 0;
                double totalFy = 0;

                // Calculate gravitational influence from each planet
                for (Planet planet : planets) {
                    double dx = planet.x - x;
                    double dy = planet.y - y;

                    double distance = Math.sqrt(dx * dx + dy * dy);

                    // Only consider planets within influence distance
                    if (distance > maxInfluenceDistance) continue;

                    // Calculate gravitational force
                    double force = G * planet.mass / (distance * distance);

                    // Calculate direction
                    double angle = Math.atan2(dy, dx);
                    double fx = force * Math.cos(angle);
                    double fy = force * Math.sin(angle);

                    totalFx += fx;
                    totalFy += fy;
                }

                // Normalize and scale vector
                double magnitude = Math.sqrt(totalFx * totalFx + totalFy * totalFy);
                if (magnitude > 0) {
                    // Limit vector length
                    magnitude = Math.min(magnitude, maxVectorLength);

                    double nx = totalFx / Math.sqrt(totalFx * totalFx + totalFy * totalFy);
                    double ny = totalFy / Math.sqrt(totalFx * totalFx + totalFy * totalFy);

                    // Draw vector
                    int endX = (int) (x + nx * magnitude);
                    int endY = (int) (y + ny * magnitude);

                    g.drawLine(x, y, endX, endY);
                }
            }
        }
    }

    // ComponentListener methods
    @Override
    public void componentResized(ComponentEvent e) {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        repositionPlanets();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}

    private static class Planet {
        double x;
        double y;
        double mass;
        double velocityX;
        double velocityY;
        Color color;
        int radius;

        public Planet(double x, double y, double mass, double velocityX, double velocityY, Color color) {
            this.x = x;
            this.y = y;
            this.mass = mass;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.color = color;
            this.radius = (int) (Math.log(mass) * 2); // Adjust size based on mass
        }

        public void updatePosition(double accelerationX, double accelerationY, double dt) {
            velocityX += accelerationX * dt;
            velocityY += accelerationY * dt;

            x += velocityX * dt;
            y += velocityY * dt;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            int drawRadius = Math.max((radius / 2), 3);
            g.fillOval((int) x - drawRadius, (int) y - drawRadius, drawRadius * 2, drawRadius * 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gravity Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.add(new GravitySimulation());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
