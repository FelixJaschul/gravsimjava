import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GravityVisualizer extends JPanel implements ActionListener, ComponentListener, MouseListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Color vectorColor = Color.LIGHT_GRAY;
    private Color planetColor = Color.LIGHT_GRAY;
    private Color backgroundColor = Color.WHITE;
    private Color highlightColor = new Color(163, 191, 213); // Default highlight color for menu

    private static final double G = 6.67430e-11; // 6.67430e-11 = standard
    private static final int PLANET_OFFSET = 100;

    private final List<Planet> planets;
    private int centerX;
    private int centerY;
    private Timer timer;
    private int DELAY = 4; // Default animation speed (milliseconds)
    private boolean isRunning = true;
    private JPopupMenu popupMenu;
    private boolean showVectors = true;
    private double timeScale = 1.0; // Time scale factor for simulation speed

    public GravityVisualizer() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(backgroundColor);

        centerX = WIDTH / 2;
        centerY = HEIGHT / 2;

        planets = new ArrayList<>();
        planets.add(new Planet(centerX - PLANET_OFFSET, centerY, 1e15, 0, 10, planetColor));
        planets.add(new Planet(centerX + PLANET_OFFSET, centerY, 1e15, 0, -10, planetColor));

        timer = new Timer(DELAY, this);
        timer.start();

        // Add component listener to detect resize events
        addComponentListener(this);

        // Add mouse listener for popup menu
        addMouseListener(this);

        // Create popup menu
        createPopupMenu();
    }

    private void createPopupMenu() {
        popupMenu = new JPopupMenu() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // White background
                g2d.setColor(new Color(255, 255, 255));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add a subtle border
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

                g2d.dispose();
            }

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

        // Settings submenu
        JMenu settingsMenu = createStyledMenu("Settings");

        // Colors submenu
        JMenu colorMenu = createStyledMenu("Colors");

        // Planet color options
        JMenuItem bluePlanetItem = createStyledMenuItem("Blue Planets");
        bluePlanetItem.addActionListener(e -> {
            planetColor = new Color(100, 149, 237); // Cornflower blue
            updatePlanetColors();
            repaint();
        });

        JMenuItem greenPlanetItem = createStyledMenuItem("Green Planets");
        greenPlanetItem.addActionListener(e -> {
            planetColor = new Color(60, 179, 113); // Medium sea green
            updatePlanetColors();
            repaint();
        });

        JMenuItem redPlanetItem = createStyledMenuItem("Red Planets");
        redPlanetItem.addActionListener(e -> {
            planetColor = new Color(205, 92, 92); // Indian red
            updatePlanetColors();
            repaint();
        });

        JMenuItem grayPlanetItem = createStyledMenuItem("Gray Planets");
        grayPlanetItem.addActionListener(e -> {
            planetColor = Color.LIGHT_GRAY;
            updatePlanetColors();
            repaint();
        });

        // Vector color options
        JMenu vectorColorMenu = createStyledMenu("Vector Colors");

        JMenuItem blueVectorItem = createStyledMenuItem("Blue Vectors");
        blueVectorItem.addActionListener(e -> {
            vectorColor = new Color(100, 149, 237, 150); // Cornflower blue with transparency
            repaint();
        });

        JMenuItem greenVectorItem = createStyledMenuItem("Green Vectors");
        greenVectorItem.addActionListener(e -> {
            vectorColor = new Color(60, 179, 113, 150); // Medium sea green with transparency
            repaint();
        });

        JMenuItem redVectorItem = createStyledMenuItem("Red Vectors");
        redVectorItem.addActionListener(e -> {
            vectorColor = new Color(205, 92, 92, 150); // Indian red with transparency
            repaint();
        });

        JMenuItem grayVectorItem = createStyledMenuItem("Gray Vectors");
        grayVectorItem.addActionListener(e -> {
            vectorColor = new Color(192, 192, 192, 150); // Light gray with transparency
            repaint();
        });

        // Background color options
        JMenu backgroundColorMenu = createStyledMenu("Background Colors");

        JMenuItem whiteBackgroundItem = createStyledMenuItem("White Background");
        whiteBackgroundItem.addActionListener(e -> {
            backgroundColor = Color.WHITE;
            setBackground(backgroundColor);
            repaint();
        });

        JMenuItem blackBackgroundItem = createStyledMenuItem("Black Background");
        blackBackgroundItem.addActionListener(e -> {
            backgroundColor = Color.BLACK;
            setBackground(backgroundColor);
            // Adjust vector color for visibility on dark background
            if (vectorColor.equals(Color.LIGHT_GRAY)) {
                vectorColor = new Color(255, 255, 255, 150);
            }
            repaint();
        });

        JMenuItem lightGrayBackgroundItem = createStyledMenuItem("Light Gray Background");
        lightGrayBackgroundItem.addActionListener(e -> {
            backgroundColor = new Color(240, 240, 240);
            setBackground(backgroundColor);
            repaint();
        });

        // Add color options to their respective menus
        colorMenu.add(bluePlanetItem);
        colorMenu.add(greenPlanetItem);
        colorMenu.add(redPlanetItem);
        colorMenu.add(grayPlanetItem);

        vectorColorMenu.add(blueVectorItem);
        vectorColorMenu.add(greenVectorItem);
        vectorColorMenu.add(redVectorItem);
        vectorColorMenu.add(grayVectorItem);

        backgroundColorMenu.add(whiteBackgroundItem);
        backgroundColorMenu.add(blackBackgroundItem);
        backgroundColorMenu.add(lightGrayBackgroundItem);

        // Add color menus to settings menu
        colorMenu.add(createStyledSeparator());
        colorMenu.add(vectorColorMenu);
        colorMenu.add(backgroundColorMenu);
        settingsMenu.add(colorMenu);

        // Simulation speed menu item
        JMenuItem speedItem = createStyledMenuItem("Simulation Speed");
        speedItem.addActionListener(e -> {
            // Create a custom input dialog for speed setting
            JDialog speedDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Simulation Speed", true);
            speedDialog.setLayout(new BorderLayout());
            speedDialog.setSize(350, 180);
            speedDialog.setMinimumSize(new Dimension(300, 150));
            speedDialog.setLocationRelativeTo(this);
            speedDialog.setResizable(true);

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

            // Create a label explaining what the speed is
            JLabel label = new JLabel("Enter time scale factor (0.1-10):");
            label.setFont(label.getFont().deriveFont(Font.BOLD));

            // Create a text field with the current speed as default
            JTextField speedField = new JTextField(String.valueOf(timeScale), 10);
            speedField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            speedField.setHorizontalAlignment(JTextField.CENTER);

            // Add a hint about what the speed affects
            JLabel hintLabel = new JLabel("Higher values = faster simulation");
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
                    double newSpeed = Double.parseDouble(speedField.getText().trim());

                    // Validate the input (ensure it's within reasonable bounds)
                    if (newSpeed >= 0.1 && newSpeed <= 10) {
                        // Update the time scale
                        timeScale = newSpeed;
                        speedDialog.dispose();
                    } else {
                        // Show error message for invalid range
                        JOptionPane.showMessageDialog(speedDialog,
                                "Please enter a value between 0.1 and 10.",
                                "Invalid Input",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    // Show error message for invalid input
                    JOptionPane.showMessageDialog(speedDialog,
                            "Please enter a valid number.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            // Add action listener to Cancel button
            cancelButton.addActionListener(event -> speedDialog.dispose());

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
            inputPanel.add(speedField, BorderLayout.CENTER);

            // Add panels to the content panel
            contentPanel.add(inputPanel, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Add content panel to dialog
            speedDialog.add(contentPanel, BorderLayout.CENTER);

            // Set default button and focus
            speedDialog.getRootPane().setDefaultButton(applyButton);
            speedField.requestFocusInWindow();

            // Select all text in the field for easy editing
            speedField.selectAll();

            // Show the dialog
            speedDialog.setVisible(true);
        });

        // Toggle vectors menu item
        JMenuItem toggleVectorsItem = createStyledMenuItem("Toggle Vectors");
        toggleVectorsItem.addActionListener(e -> {
            showVectors = !showVectors;
            repaint();
        });

        // Add planet menu item
        JMenuItem addPlanetItem = createStyledMenuItem("Add Planet");
        addPlanetItem.addActionListener(e -> {
            // Add a new planet with random properties
            double randomX = centerX + (Math.random() * 200 - 100);
            double randomY = centerY + (Math.random() * 200 - 100);
            double randomVelocityX = Math.random() * 20 - 10;
            double randomVelocityY = Math.random() * 20 - 10;
            planets.add(new Planet(randomX, randomY, 1e15, randomVelocityX, randomVelocityY, planetColor));
            repaint();
        });

        // Clear planets menu item
        JMenuItem clearPlanetsItem = createStyledMenuItem("Reset to Default");
        clearPlanetsItem.addActionListener(e -> {
            planets.clear();
            planets.add(new Planet(centerX - PLANET_OFFSET, centerY, 1e15, 0, 10, planetColor));
            planets.add(new Planet(centerX + PLANET_OFFSET, centerY, 1e15, 0, -10, planetColor));
            repaint();
        });

        // Add settings items to settings menu
        settingsMenu.add(speedItem);
        settingsMenu.add(toggleVectorsItem);
        settingsMenu.add(createStyledSeparator());
        settingsMenu.add(addPlanetItem);
        settingsMenu.add(clearPlanetsItem);

        // Start/Pause menu item
        JMenuItem startPauseItem = createStyledMenuItem(isRunning ? "Pause" : "Start");
        startPauseItem.addActionListener(e -> {
            toggleRunning();
            popupMenu.setVisible(false);
        });

        // Restart menu item
        JMenuItem restartItem = createStyledMenuItem("Restart");
        restartItem.addActionListener(e -> {
            resetSimulation();
            popupMenu.setVisible(false);
        });

        // Add all items to the popup menu
        popupMenu.add(settingsMenu);
        popupMenu.add(createStyledSeparator());
        popupMenu.add(startPauseItem);
        popupMenu.add(restartItem);
    }

    // Helper method to create styled menu items
    private JMenuItem createStyledMenuItem(String text) {
        Color gray = new Color(50, 50, 50);
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
                g2d.setColor(gray);
                FontMetrics fm = g2d.getFontMetrics();
                int x = 10;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(text, x, y);

                g2d.dispose();
            }
        };

        // Set preferred size for consistent menu item height
        item.setPreferredSize(new Dimension(150, 25));
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
                g2d.drawString(text, x, y);

                g2d.dispose();
            }
        };

        menu.setOpaque(false);
        return menu;
    }

    // Helper method to create styled separators
    private JSeparator createStyledSeparator() {
        JSeparator separator = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawLine(10, getHeight() / 2, getWidth() - 10, getHeight() / 2);
                g2d.dispose();
            }
        };
        separator.setPreferredSize(new Dimension(0, 10));
        return separator;
    }

    // Method to show popup menu
    private void showPopupMenu(MouseEvent e) {
        popupMenu.show(this, e.getX(), e.getY());
    }

    // Method to toggle simulation running state
    private void toggleRunning() {
        isRunning = !isRunning;
        if (isRunning) {
            timer.start();
        } else {
            timer.stop();
        }
        repaint();
    }

    // Method to reset the simulation
    private void resetSimulation() {
        planets.clear();
        planets.add(new Planet(centerX - PLANET_OFFSET, centerY, 1e15, 0, 10, planetColor));
        planets.add(new Planet(centerX + PLANET_OFFSET, centerY, 1e15, 0, -10, planetColor));

        if (!isRunning) {
            isRunning = true;
            timer.start();
        }

        repaint();
    }

    // Method to update all planet colors
    private void updatePlanetColors() {
        for (Planet planet : planets) {
            planet.color = planetColor;
        }
    }

    // Draw a small indicator circle when simulation is running
    private void drawRunningIndicator(Graphics2D g) {
        if (isRunning) {
            g.setColor(new Color(50, 205, 50)); // Lime Green
            g.fillOval(getWidth() - 20, 10, 10, 10);
        } else {
            g.setColor(new Color(205, 92, 92)); // Indian Red
            g.fillOval(getWidth() - 20, 10, 10, 10);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gravitational field vectors if enabled
        if (showVectors) {
            drawGravitationalField(g2d, planets);
        }

        // Draw planets
        for (Planet planet : planets) {
            planet.draw(g2d);
        }

        // Draw running indicator
        drawRunningIndicator(g2d);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isRunning) return;

        // Calculate and apply gravitational forces with time scaling
        double dt = 0.05 * timeScale;

        for (int i = 0; i < planets.size(); i++) {
            for (int j = i + 1; j < planets.size(); j++) {
                // Calculate gravitational acceleration
                double[] accelerations = calculateGravitationalForce(planets.get(i), planets.get(j));

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
        int gridSpacing = 15;
        double maxVectorLength = 50;
        double maxInfluenceDistance = 300;

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

    // MouseListener methods
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

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

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
}