import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GravitySimulation extends JPanel implements ActionListener {
    // Screen dimensions
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // Colors
    private static final Color BLACK = Color.WHITE;
    private static final Color WHITE = Color.LIGHT_GRAY;
    private static final Color BLUE = Color.BLUE;
    private static final Color RED = Color.RED;
    private static final Color GRAY = Color.LIGHT_GRAY;

    // Gravitational constant
    private static final double G = 6.67430e-11;

    // Timer for animation
    private Timer timer;

    // List of planets
    private List<Planet> planets;

    public GravitySimulation() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BLACK);

        // Create planets
        planets = new ArrayList<>();
        planets.add(new Planet(WIDTH / 3, HEIGHT / 2, 1e15, 0, 10, BLUE));
        planets.add(new Planet(2 * WIDTH / 3, HEIGHT / 2, 1e15, 0, -10, RED));

        // Set up timer for animation (60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw gravitational field vectors
        drawGravitationalField(g2d, planets);

        // Draw planets
        for (Planet planet : planets) {
            planet.draw(g2d);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Calculate and apply gravitational forces
        for (int i = 0; i < planets.size(); i++) {
            for (int j = i + 1; j < planets.size(); j++) {
                // Calculate gravitational acceleration
                double[] accelerations = calculateGravitationalForce(planets.get(i), planets.get(j));

                // Update planet velocities
                planets.get(i).updatePosition(accelerations[0], accelerations[1], 0.1);
                planets.get(j).updatePosition(accelerations[2], accelerations[3], 0.1);
            }
        }

        // Repaint the panel
        repaint();
    }

    private double[] calculateGravitationalForce(Planet planet1, Planet planet2) {
        // Calculate distance between planets
        double dx = planet2.x - planet1.x;
        double dy = planet2.y - planet1.y;

        // Avoid division by zero
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 1) {
            distance = 1;
        }

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
        // Grid parameters
        int gridSpacing = 25;
        double maxVectorLength = 50; // Maximum length of vector
        double maxInfluenceDistance = 300; // Maximum distance to draw vectors

        g.setColor(GRAY);

        for (int x = 0; x < WIDTH; x += gridSpacing) {
            for (int y = 0; y < HEIGHT; y += gridSpacing) {
                double totalFx = 0;
                double totalFy = 0;

                // Calculate gravitational influence from each planet
                for (Planet planet : planets) {
                    double dx = planet.x - x;
                    double dy = planet.y - y;

                    double distance = Math.sqrt(dx * dx + dy * dy);

                    // Only consider planets within influence distance
                    if (distance > maxInfluenceDistance) {
                        continue;
                    }

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

    // Planet class
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
            // Update velocity
            velocityX += accelerationX * dt;
            velocityY += accelerationY * dt;

            // Update position
            x += velocityX * dt;
            y += velocityY * dt;
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            int drawRadius = Math.max((int) (radius / 2), 3);
            g.fillOval((int) x - drawRadius, (int) y - drawRadius, drawRadius * 2, drawRadius * 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gravity Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new GravitySimulation());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
