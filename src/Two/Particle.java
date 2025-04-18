package Two;

import java.awt.*;

public class Particle {
    double x, y;
    double vx, vy;  // Velocity
    double ax, ay;  // Acceleration
    double mass;
    int radius;

    public Particle(double x, double y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.mass = radius * radius * 0.1; // Mass proportional to radius squared
        this.vx = 0;
        this.vy = 0;
        this.ax = 0;
        this.ay = 0;
    }

    public Particle(double x, double y, int radius, double mass, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.mass = mass;
        this.vx = vx;
        this.vy = vy;
        this.ax = 0;
        this.ay = 0;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval((int) (x - radius), (int) (y - radius), 2 * radius, 2 * radius);
    }

    public void update(double dt) {
        // Update velocity based on acceleration
        vx += ax * dt;
        vy += ay * dt;

        // Update position based on velocity
        x += vx * dt;
        y += vy * dt;

        // Reset acceleration for next frame
        ax = 0;
        ay = 0;
    }

    // Apply force to this particle
    public void applyForce(double fx, double fy) {
        ax += fx / mass;
        ay += fy / mass;
    }

    // Set initial velocity (useful for orbital mechanics)
    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    // Set mass
    public void setMass(double mass) {
        this.mass = mass;
    }
}
