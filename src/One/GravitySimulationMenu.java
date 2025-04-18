package One;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GravitySimulationMenu extends JPanel implements ActionListener, ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final Color backgroundColor = Color.WHITE;
    private static final Color highlightColor = Color.LIGHT_GRAY;
    private static final Color textColor = Color.BLACK;

    // Adjustable parameters
    private double G = 6.67430e-11; // 6.67430e-11 = standard
    private int PLANET_OFFSET = 100;
    private int gridSpacing = 15;
    private double maxVectorLength = 50;
    private double maxInfluenceDistance = 300;
    private double timeStep = 0.05;
    private double initialVelocity = 10;
    private double planetMass = 1e15;
    
    private final List<Planet> planets;
    private int centerX;
    private int centerY;
    private final Timer timer;
    private boolean isRunning = true;
    private JPopupMenu popupMenu;

    public GravitySimulationMenu() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(backgroundColor);

        centerX = WIDTH / 2;
        centerY = HEIGHT / 2;

        planets = new ArrayList<>();
        resetPlanets();

        int DELAY = 4;
        timer = new Timer(DELAY, this);
        timer.start();

        addComponentListener(this);

        createPopupMenu();
        
        // Add mouse listener for right-click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupMenu(e);
            }
        });
    }

    private void repositionPlanets() {
        if (planets.size() >= 2) { // 2 :=// Planet Count
            planets.getFirst().x = centerX - PLANET_OFFSET;
            planets.getFirst().y = centerY;
            planets.get(0).velocityX = 0; // Reset velocity
            planets.get(0).velocityY = initialVelocity; // Reset to initial velocity

            planets.get(1).x = centerX + PLANET_OFFSET;
            planets.get(1).y = centerY;
            planets.get(1).velocityX = 0; // Reset velocity
            planets.get(1).velocityY = -initialVelocity; // Reset to initial velocity
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
        g.setColor(highlightColor);

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

    // SwingUI PopupMenu Implementation
    private void createPopupMenu() {
        // Creates Pop Up Menu for Settings, ...
        popupMenu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // white background
                g2d.setColor(new Color(255, 255, 255));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add a subtle border
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);

                g2d.dispose();
            }

            // Override to ensure the popup doesn't paint outside our rounded rectangle
            @Override
            public void show(Component invoker, int x, int y) {
                // Set the popup menu to be non-opaque
                setOpaque(false);

                // Make all components in the popup non-opaque
                for (Component component : getComponents())
                    if (component instanceof JComponent) ((JComponent) component).setOpaque(false);

                super.show(invoker, x, y);
            }
        };

        // Set border to create padding inside the rounded rectangle
        popupMenu.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Settings menu
        JMenu settingsMenu = createStyledMenu("Settings");

        // Physics submenu
        JMenu physicsMenu = createStyledMenu("Physics");

        // Gravitational constant
        JMenuItem gravityItem = createStyledMenuItem("Gravitational Constant");
        gravityItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter gravitational constant (current: " + G + ")", G);
            if (input != null && !input.isEmpty()) {
                double newValue = Double.parseDouble(input);
                if (newValue > 0) G = newValue;
            }
        });

        // Time step
        JMenuItem timeStepItem = createStyledMenuItem("Time Step");
        timeStepItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter time step (current: " + timeStep + ")", timeStep);
            if (input != null && !input.isEmpty()) {
                double newValue = Double.parseDouble(input);
                if (newValue > 0) timeStep = newValue;
            }
        });

        // Planet mass
        JMenuItem massItem = createStyledMenuItem("Planet Mass");
        massItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter planet mass (current: " + planetMass + ")", planetMass);
            if (input != null && !input.isEmpty()) {
                double newValue = Double.parseDouble(input);
                if (newValue > 0) {
                    planetMass = newValue;
                    resetPlanets();
                }
            }
        });

        // Initial velocity
        JMenuItem velocityItem = createStyledMenuItem("Initial Velocity");
        velocityItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter initial velocity (current: " + initialVelocity + ")", initialVelocity);
            if (input != null && !input.isEmpty()) {
                initialVelocity = Double.parseDouble(input);
                resetPlanets();
            }
        });

        // Add physics items to physics menu
        physicsMenu.add(gravityItem);
        physicsMenu.add(timeStepItem);
        physicsMenu.add(massItem);
        physicsMenu.add(velocityItem);

        // Visualization submenu
        JMenu visualizationMenu = createStyledMenu("Visualization");

        // Grid spacing
        JMenuItem gridItem = createStyledMenuItem("Grid Spacing");
        gridItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter grid spacing (current: " + gridSpacing + ")", gridSpacing);
            if (input != null && !input.isEmpty()) {
                int newValue = Integer.parseInt(input);
                if (newValue > 0) gridSpacing = newValue;
            }
        });

        // Vector length
        JMenuItem vectorLengthItem = createStyledMenuItem("Max Vector Length");
        vectorLengthItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter maximum vector length (current: " + maxVectorLength + ")", maxVectorLength);
            if (input != null && !input.isEmpty()) {
                double newValue = Double.parseDouble(input);
                if (newValue > 0) maxVectorLength = newValue;
            }
        });

        // Influence distance
        JMenuItem influenceItem = createStyledMenuItem("Max Influence Distance");
        influenceItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter maximum influence distance (current: " + maxInfluenceDistance + ")", maxInfluenceDistance);
            if (input != null && !input.isEmpty()) {
                double newValue = Double.parseDouble(input);
                if (newValue > 0) maxInfluenceDistance = newValue;
            }
        });

        // Planet offset
        JMenuItem offsetItem = createStyledMenuItem("Planet Offset");
        offsetItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter planet offset from center (current: " + PLANET_OFFSET + ")", PLANET_OFFSET);
            if (input != null && !input.isEmpty()) {
                int newValue = Integer.parseInt(input);
                if (newValue > 0) {
                    PLANET_OFFSET = newValue;
                    resetPlanets();
                }
            }
        });

        // Add visualization items to visualization menu
        visualizationMenu.add(gridItem);
        visualizationMenu.add(vectorLengthItem);
        visualizationMenu.add(influenceItem);
        visualizationMenu.add(offsetItem);

        // Add submenus to settings menu
        settingsMenu.add(physicsMenu);
        settingsMenu.add(visualizationMenu);

        // Custom separator with Apple-like styling
        JSeparator separator = createStyledSeparator();

        // Start/Pause menu item
        JMenuItem startPauseItem = createStyledMenuItem(isRunning ? "Pause" : "Start");
        startPauseItem.addActionListener(e -> {
            toggleRunning();
            popupMenu.setVisible(false);
        });

        // Restart menu item
        JMenuItem restartItem = createStyledMenuItem("Restart");
        restartItem.addActionListener(e -> {
            resetPlanets();
            popupMenu.setVisible(false);
        });

        // Add all items to the popup menu
        popupMenu.add(settingsMenu);
        popupMenu.add(separator);
        popupMenu.add(startPauseItem);
        popupMenu.add(restartItem);
    }

    // Helper method to create styled menu items
    private JMenuItem createStyledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Check if the item is selected (hovered)
                if (getModel().isArmed()) {
                    // Highlight color when hovered
                    g2d.setColor(highlightColor);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                // Draw text
                g2d.setColor(textColor);
                FontMetrics fm = g2d.getFontMetrics();
                int x = 10;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        item.setForeground(highlightColor);

        // Remove default background and border
        item.setOpaque(false);
        item.setBorderPainted(false);
        item.setContentAreaFilled(false);

        // Add some padding
        item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        return item;
    }

    // Helper method to create styled menus
    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Check if the menu is selected (hovered)
                if (getModel().isArmed() || getModel().isSelected()) {
                    // Highlight color when hovered
                    g2d.setColor(highlightColor);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                // Draw text
                g2d.setColor(textColor);
                FontMetrics fm = g2d.getFontMetrics();
                int x = 10;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        // Set foreground color
        menu.setForeground(highlightColor);

        // Remove default background and border
        menu.setOpaque(false);
        menu.setBorderPainted(false);
        menu.setContentAreaFilled(false);

        // Add some padding
        menu.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Style the popup menu that appears when clicking this menu
        JPopupMenu subMenu = menu.getPopupMenu();
        subMenu.setOpaque(false);
        subMenu.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        subMenu.setUI(new javax.swing.plaf.basic.BasicPopupMenuUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // white background
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, c.getWidth(), c.getHeight());

                // Add a subtle border
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRect(0, 0, c.getWidth()-1, c.getHeight()-1);

                g2d.dispose();

                // Paint the menu items
                super.paint(g, c);
            }
        });

        return menu;
    }

    // Helper method to create a styled separator
    private JSeparator createStyledSeparator() {
        JSeparator separator = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw a subtle line
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawLine(10, getHeight() / 2, getWidth() - 10, getHeight() / 2);

                g2d.dispose();
            }
        };

        // Set height for the separator
        separator.setPreferredSize(new Dimension(0, 10));
        separator.setOpaque(false);

        return separator;
    }

    private void showPopupMenu(MouseEvent e) {
        createPopupMenu();
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void toggleRunning() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        } else {
            timer.start();
            isRunning = true;
        }
        repaint();
    }

    private void resetPlanets() {
        planets.clear();
        // Position planets relative to center with current settings
        planets.add(new Planet(centerX - PLANET_OFFSET, centerY, planetMass, 0, initialVelocity, highlightColor));
        planets.add(new Planet(centerX + PLANET_OFFSET, centerY, planetMass, 0, -initialVelocity, highlightColor));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gravitational field vectors
        drawGravitationalField(g2d, planets);

        for (Planet planet : planets) planet.draw(g2d);

        // Draw a green circle if simulation is running
        if (isRunning) {
            int radius = 5;
            int x = 10;
            int y = 10;
            g.setColor(Color.GREEN);
            g.fillOval(x, y, radius * 2, radius * 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Calculate and apply gravitational forces
        for (int i = 0; i < planets.size(); i++) {
            for (int j = i + 1; j < planets.size(); j++) {
                // Calculate gravitational acceleration
                double[] accelerations = calculateGravitationalForce(planets.get(i), planets.get(j));

                planets.get(i).updatePosition(accelerations[0], accelerations[1], timeStep);
                planets.get(j).updatePosition(accelerations[2], accelerations[3], timeStep);
            }
        }

        repaint();
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gravity Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.add(new GravitySimulationMenu());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}