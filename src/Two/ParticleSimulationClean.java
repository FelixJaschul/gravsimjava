package Two;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// QuadTree: https://www.youtube.com/watch?v=OJxEcs0w_kE
//           https://www.youtube.com/watch?v=QQx_NmCIuCY
//           https://www.youtube.com/watch?v=z0YFFg_nBjw
//           https://www.youtube.com/watch?v=tOlKLJ4WmSE&t=304s
//           https://arborjs.org/docs/barnes-hut
//           https://github.com/womogenes/GravitySim

public class ParticleSimulationClean extends JPanel implements ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final Color PARTICLE_COLOR = Color.LIGHT_GRAY;
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final int PARTICLE_OFFSET = 100;

    // Physics constants
    private static final double G = 6.67430e-3;
    private static final double THETA = 0.1;    // Barnes-Hut threshold (smaller = more accurate)
    private static final double TIME_STEP = 0.1; // Time step for simulation

    // Visualization options
    private boolean showQuadTree = false;
    private boolean shouldMerge = false;

    private final List<Particle> particles;
    private int centerX;
    private int centerY;
    private QuadTree quadTree;
    private final Random random = new Random();

    public ParticleSimulationClean() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BACKGROUND_COLOR);

        centerX = WIDTH / 2;
        centerY = HEIGHT / 2;

        particles = new ArrayList<>();
        setupInitialParticles();

        Timer timer = new Timer(16, e -> {
            updateParticles();
            repaint();
        });
        timer.start();

        // Add component listener to detect resize events
        addComponentListener(this);

        // Add mouse listener to add particles on click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                addParticleAt(e.getX(), e.getY());
            }
        });

        // Add key listener to toggle quadtree visualization
        setFocusable(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyChar() == 'q' || e.getKeyChar() == 'Q') {
                    showQuadTree = !showQuadTree;
                    repaint();
                } else if (e.getKeyChar() == 'm' || e.getKeyChar() == 'M') {
                    shouldMerge = !shouldMerge;
                } else if (e.getKeyChar() == 'c' || e.getKeyChar() == 'C') {
                    particles.clear();
                } else if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
                    setupRandomParticles(50);
                }
            }
        });
    }

    private void setupInitialParticles() {
        // Create a binary star system
        Particle star1 = new Particle(centerX - PARTICLE_OFFSET, centerY, 15, 1000, 0, 1);
        Particle star2 = new Particle(centerX + PARTICLE_OFFSET, centerY, 15, 1000, 0, -1);

        particles.add(star1);
        particles.add(star2);

        // Add some planets
        for (int i = 0; i < 5; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = PARTICLE_OFFSET * 2 + random.nextDouble() * 100;
            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;

            // Calculate orbital velocity for circular orbit
            double speed = Math.sqrt(G * 2000 / distance) * 0.7;
            double vx = Math.sin(angle) * speed;
            double vy = -Math.cos(angle) * speed;

            Particle planet = new Particle(x, y, 5, 10, vx, vy);
            particles.add(planet);
        }
    }

    private void setupRandomParticles(int count) {
        particles.clear();

        // Add a massive (?) central body
        particles.add(new Particle(centerX, centerY, 20, 5000, 0, 0));

        // Add random particles
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 50 + random.nextDouble() * 300;
            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;

            // Calculate orbital velocity for circular orbit
            double speed = Math.sqrt(G * 5000 / distance) * (0.8 + random.nextDouble() * 0.4);
            double vx = Math.sin(angle) * speed;
            double vy = -Math.cos(angle) * speed;

            int radius = 2 + random.nextInt(6);
            double mass = radius * radius * 0.1;

            Particle p = new Particle(x, y, radius, mass, vx, vy);
            particles.add(p);
        }
    }

    private void addParticleAt(int x, int y) {
        // Calculate velocity for orbit around center
        double dx = x - centerX;
        double dy = y - centerY;
        double distance = Math.sqrt(dx*dx + dy*dy);

        // Skip if too close to center
        if (distance < 20) return;

        // Calculate orbital velocity perpendicular to radius
        double speed = Math.sqrt(G * 2000 / distance) * 0.7;
        double vx = -dy / distance * speed;
        double vy = dx / distance * speed;

        Particle p = new Particle(x, y, 5, 10, vx, vy);
        particles.add(p);
    }

    private void updateParticles() {
        // Find boundaries for the quadtree
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (Particle p : particles) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }

        // Add some margin
        double margin = 100;
        minX -= margin;
        minY -= margin;
        maxX += margin;
        maxY += margin;

        // Create quadtree with appropriate boundaries
        Rectangle2D.Double boundary = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        quadTree = new QuadTree(boundary, 4);  // Capacity of 4 particles per node

        // Insert all particles into the quadtree
        for (Particle p : particles) {
            quadTree.insert(p);
        }

        // Calculate center of mass for all nodes
        quadTree.calculateMass();

        // Calculate forces on each particle using Barnes-Hut approximation
        for (Particle p : particles) {
            quadTree.calculateForce(p, G, THETA);
        }

        // Update positions
        for (Particle p : particles) {
            p.update(TIME_STEP);

            // Simple boundary handling - bounce off edges
            if (p.x < 0 || p.x > getWidth()) {
                p.vx = -p.vx * 0.8;
                p.x = Math.max(0, Math.min(p.x, getWidth()));
            }
            if (p.y < 0 || p.y > getHeight()) {
                p.vy = -p.vy * 0.8;
                p.y = Math.max(0, Math.min(p.y, getHeight()));
            }
        }

        // Handle collisions (optional)
        handleCollisions();
    }

    private void handleCollisions() {
        // Simple collision detection
        if (shouldMerge) {
            mergeParticles(); // - merge particles that get too close
        } else {
            collideParticles(); // - collision detection
        }
    }

    private void collideParticles() {
        // Implement elastic collisions between particles
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);

            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);

                // Calculate distance between particles
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // Check if particles are colliding (overlapping)
                if (distance < p1.radius + p2.radius && !shouldMerge) {
                    // Normalize the collision vector
                    double nx = dx / distance;
                    double ny = dy / distance;

                    // Calculate relative velocity
                    double dvx = p2.vx - p1.vx;
                    double dvy = p2.vy - p1.vy;

                    // Calculate relative velocity along the normal
                    double velAlongNormal = dvx * nx + dvy * ny;

                    // Do not resolve if objects are moving away from each other
                    if (velAlongNormal > 0) continue;

                    // Calculate impulse scalar
                    double restitution = 0.8; // Coefficient of restitution (0 = inelastic, 1 = elastic)
                    double impulseScalar = -(1 + restitution) * velAlongNormal;
                    impulseScalar /= (1 / p1.mass) + (1 / p2.mass);

                    // Apply impulse
                    double impulseX = impulseScalar * nx;
                    double impulseY = impulseScalar * ny;

                    // Update velocities
                    p1.vx -= impulseX / p1.mass;
                    p1.vy -= impulseY / p1.mass;
                    p2.vx += impulseX / p2.mass;
                    p2.vy += impulseY / p2.mass;

                    // Resolve penetration (move particles apart)
                    double penetration = (p1.radius + p2.radius - distance) * 0.5;
                    double correctionX = nx * penetration;
                    double correctionY = ny * penetration;

                    p1.x -= correctionX * (p2.mass / (p1.mass + p2.mass));
                    p1.y -= correctionY * (p2.mass / (p1.mass + p2.mass));
                    p2.x += correctionX * (p1.mass / (p1.mass + p2.mass));
                    p2.y += correctionY * (p1.mass / (p1.mass + p2.mass));
                }
            }
        }
    }

    private void mergeParticles() {
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);

            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);

                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // If particles are too close, merge them
                if (distance < p1.radius + p2.radius) {
                    // Conservation of momentum
                    double totalMass = p1.mass + p2.mass;
                    double newVx = (p1.vx * p1.mass + p2.vx * p2.mass) / totalMass;
                    double newVy = (p1.vy * p1.mass + p2.vy * p2.mass) / totalMass;

                    // New position is center of mass
                    double newX = (p1.x * p1.mass + p2.x * p2.mass) / totalMass;
                    double newY = (p1.y * p1.mass + p2.y * p2.mass) / totalMass;

                    // New radius based on conservation of volume
                    int newRadius = (int) Math.ceil(Math.pow(
                            Math.pow(p1.radius, 3) + Math.pow(p2.radius, 3),
                            1.0 / 3.0));

                    // Create new merged particle
                    Particle merged = new Particle(newX, newY, newRadius, totalMass, newVx, newVy);

                    // Remove old particles and add new one
                    particles.remove(j);
                    particles.set(i, merged);

                    // Adjust index to avoid skipping particles
                    j--;
                }
            }
        }
    }

    private void repositionParticles() {
        if (particles.size() >= 2) {
            // Reposition the first two particles
            particles.get(0).x = centerX - PARTICLE_OFFSET;
            particles.get(0).y = centerY;
            particles.get(0).vx = 0;
            particles.get(0).vy = 1;

            particles.get(1).x = centerX + PARTICLE_OFFSET;
            particles.get(1).y = centerY;
            particles.get(1).vx = 0;
            particles.get(1).vy = -1;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw quadtree if enabled
        if (showQuadTree && quadTree != null) {
            g2d.setColor(Color.DARK_GRAY);
            quadTree.draw(g2d);
        }

        // Draw particles
        for (Particle particle : particles) {
            particle.draw(g2d);
        }

        // Draw instructions
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Click to add particles, Q to toggle quadtree, M to toggle merging of particles, C to reset, R for random system", 10, 20);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        // Update center coordinates based on new panel size
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        // Reposition particles based on new center
        repositionParticles();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Barnes-Hut N-Body Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.add(new ParticleSimulationClean());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}