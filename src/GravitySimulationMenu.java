import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GravitySimulationMenu extends JPanel implements ActionListener, ComponentListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final Color backgroundColor = Color.WHITE;
    private static final Color LIGHT_GRAY = Color.LIGHT_GRAY;

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
    private final Color highlightColor = LIGHT_GRAY;
    private int DELAY = 4;

    public GravitySimulationMenu() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(backgroundColor);

        centerX = WIDTH / 2;
        centerY = HEIGHT / 2;

        planets = new ArrayList<>();
        resetPlanets();

        timer = new Timer(DELAY, this);
        timer.start();

        addComponentListener(this);

        createPopupMenu();
        
        // Add mouse listener for right-click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        });
    }
    
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
                for (Component component : getComponents()) {
                    if (component instanceof JComponent) {
                        ((JComponent) component).setOpaque(false);
                    }
                }

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
            String input = JOptionPane.showInputDialog(this, 
                "Enter gravitational constant (current: " + G + ")", 
                G);
            if (input != null && !input.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(input);
                    if (newValue > 0) {
                        G = newValue;
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Time step
        JMenuItem timeStepItem = createStyledMenuItem("Time Step");
        timeStepItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter time step (current: " + timeStep + ")", 
                timeStep);
            if (input != null && !input.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(input);
                    if (newValue > 0) {
                        timeStep = newValue;
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Planet mass
        JMenuItem massItem = createStyledMenuItem("Planet Mass");
        massItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter planet mass (current: " + planetMass + ")", 
                planetMass);
            if (input != null && !input.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(input);
                    if (newValue > 0) {
                        planetMass = newValue;
                        resetPlanets();
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Initial velocity
        JMenuItem velocityItem = createStyledMenuItem("Initial Velocity");
        velocityItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter initial velocity (current: " + initialVelocity + ")", 
                initialVelocity);
            if (input != null && !input.isEmpty()) {
                try {
                    initialVelocity = Double.parseDouble(input);
                    resetPlanets();
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
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
            String input = JOptionPane.showInputDialog(this, 
                "Enter grid spacing (current: " + gridSpacing + ")", 
                gridSpacing);
            if (input != null && !input.isEmpty()) {
                try {
                    int newValue = Integer.parseInt(input);
                    if (newValue > 0) {
                        gridSpacing = newValue;
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Vector length
        JMenuItem vectorLengthItem = createStyledMenuItem("Max Vector Length");
        vectorLengthItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter maximum vector length (current: " + maxVectorLength + ")", 
                maxVectorLength);
            if (input != null && !input.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(input);
                    if (newValue > 0) {
                        maxVectorLength = newValue;
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Influence distance
        JMenuItem influenceItem = createStyledMenuItem("Max Influence Distance");
        influenceItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter maximum influence distance (current: " + maxInfluenceDistance + ")", 
                maxInfluenceDistance);
            if (input != null && !input.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(input);
                    if (newValue > 0) {
                        maxInfluenceDistance = newValue;
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Planet offset
        JMenuItem offsetItem = createStyledMenuItem("Planet Offset");
        offsetItem.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, 
                "Enter planet offset from center (current: " + PLANET_OFFSET + ")", 
                PLANET_OFFSET);
            if (input != null && !input.isEmpty()) {
                try {
                    int newValue = Integer.parseInt(input);
                    if (newValue > 0) {
                        PLANET_OFFSET = newValue;
                        resetPlanets();
                    }
                } catch (NumberFormatException ex) {
                    // Ignore invalid input
                }
            }
        });
        
        // Animation delay
        JMenuItem delayItem = createStyledMenuItem("Animation Delay");
        delayItem.addActionListener(e -> {
            // Create a custom input dialog for delay setting
            JDialog delayDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Animation Delay", true);
            delayDialog.setLayout(new BorderLayout());
            delayDialog.setSize(350, 180);
            delayDialog.setMinimumSize(new Dimension(300, 150));
            delayDialog.setLocationRelativeTo(this);

            // Create main content panel with a more flexible layout
            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

            // Create a styled panel for the input field
            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputPanel.setBackground(new Color(245, 245, 245));
            inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            // Create a label explaining what the delay is
            JLabel label = new JLabel("Enter delay in milliseconds (1-1000):");
            label.setFont(label.getFont().deriveFont(Font.BOLD));

            // Create a text field with the current delay as default
            JTextField delayField = new JTextField(String.valueOf(DELAY), 10);
            delayField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            delayField.setHorizontalAlignment(JTextField.CENTER);

            // Add a hint about what the delay affects
            JLabel hintLabel = new JLabel("Lower values = faster animation");
            hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
            hintLabel.setForeground(new Color(100, 100, 100));

            // Create a panel for the buttons with some spacing
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            buttonPanel.setOpaque(false);

            // Create Apply and Cancel buttons with consistent sizing
            JButton applyButton = new JButton("Apply");
            JButton cancelButton = new JButton("Cancel");

            // Make buttons the same size
            Dimension buttonSize = new Dimension(100, 30);
            applyButton.setPreferredSize(buttonSize);
            cancelButton.setPreferredSize(buttonSize);

            // Add action listener to Apply button
            applyButton.addActionListener(event -> {
                try {
                    // Parse the input value
                    int newDelay = Integer.parseInt(delayField.getText().trim());

                    // Validate the input (ensure it's within reasonable bounds)
                    if (newDelay >= 1 && newDelay <= 1000) {
                        // Update the delay
                        DELAY = newDelay;

                        // Update the timer delay
                        timer.setDelay(DELAY);

                        // Close the dialog
                        delayDialog.dispose();
                    } else {
                        // Show error message for invalid range
                        JOptionPane.showMessageDialog(delayDialog,
                            "Please enter a value between 1 and 1000.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    // Show error message for invalid input
                    JOptionPane.showMessageDialog(delayDialog,
                        "Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            // Add action listener to Cancel button
            cancelButton.addActionListener(event -> delayDialog.dispose());

            // Add glue to push buttons to the right
            buttonPanel.add(Box.createHorizontalGlue());
            buttonPanel.add(applyButton);
            buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Space between buttons
            buttonPanel.add(cancelButton);

            // Add components to the input panel
            JPanel labelPanel = new JPanel(new BorderLayout());
            labelPanel.setOpaque(false);
            labelPanel.add(label, BorderLayout.NORTH);
            labelPanel.add(Box.createRigidArea(new Dimension(0, 5)), BorderLayout.CENTER);
            labelPanel.add(hintLabel, BorderLayout.SOUTH);

            inputPanel.add(labelPanel, BorderLayout.NORTH);
            inputPanel.add(delayField, BorderLayout.CENTER);

            // Add panels to the content panel
            contentPanel.add(inputPanel, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Add content panel to dialog
            delayDialog.add(contentPanel, BorderLayout.CENTER);

            // Set default button and focus
            delayDialog.getRootPane().setDefaultButton(applyButton);
            delayField.requestFocusInWindow();

            // Select all text in the field for easy editing
            delayField.selectAll();

            // Show the dialog
            delayDialog.setVisible(true);
        });
        
        // Add visualization items to visualization menu
        visualizationMenu.add(gridItem);
        visualizationMenu.add(vectorLengthItem);
        visualizationMenu.add(influenceItem);
        visualizationMenu.add(offsetItem);
        visualizationMenu.add(delayItem);
        
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
                g2d.setColor(new Color(50, 50, 50));
                FontMetrics fm = g2d.getFontMetrics();
                int x = 10;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        // Set foreground color (not used in paintComponent but helps with UI manager)
        item.setForeground(new Color(50, 50, 50));

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
                g2d.setColor(new Color(50, 50, 50));
                FontMetrics fm = g2d.getFontMetrics();
                int x = 10;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        // Set foreground color
        menu.setForeground(new Color(50, 50, 50));

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

        // Apply Apple-like styling to the submenu
        subMenu.setUI(new javax.swing.plaf.basic.BasicPopupMenuUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // white background
                g2d.setColor(new Color(255, 255, 255, 240));
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
        // Recreate the popup menu to ensure it's updated with current state
        createPopupMenu();
        
        // Show the popup menu at the mouse position
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
        planets.add(new Planet(centerX - PLANET_OFFSET, centerY, planetMass, 0, initialVelocity, LIGHT_GRAY));
        planets.add(new Planet(centerX + PLANET_OFFSET, centerY, planetMass, 0, -initialVelocity, LIGHT_GRAY));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

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

    // Method to reposition planets around the center
    private void repositionPlanets() {
        if (planets.size() >= 2) {
            // Reposition the first two planets (you can extend this for more planets)
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
        g.setColor(LIGHT_GRAY);

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
            this.radius = (int) (Math.log(mass) * 2);
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
            JFrame frame = new JFrame("Gravity Simulation with Menu");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);
            frame.add(new GravitySimulationMenu());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}