import java.awt.*;

// Planet class - needed for both type of Simulations
public class Planet {
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