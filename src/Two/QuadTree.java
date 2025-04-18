package Two;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class QuadTree {
    Rectangle2D.Double boundary;
    int capacity; // Maximum number of particles before subdivision
    List<Particle> particles;
    boolean divided;

    // Four children quadrants
    QuadTree northWest;
    QuadTree northEast;
    QuadTree southWest;
    QuadTree southEast;

    // For center of mass calculations
    double totalMass;
    double centerOfMassX;
    double centerOfMassY;

    // Constructor
    public QuadTree(Rectangle2D.Double boundary, int capacity) {
        this.boundary = boundary;
        this.capacity = capacity;
        this.particles = new ArrayList<>();
        this.divided = false;
        this.totalMass = 0;
        this.centerOfMassX = 0;
        this.centerOfMassY = 0;
    }

    // Insert a particle into the quadtree
    public boolean insert(Particle particle) {
        // If particle is not in this quadrant, don't insert
        if (!boundary.contains(particle.x, particle.y)) {
            return false;
        }

        // If there's space and not divided, add particle here
        if (particles.size() < capacity && !divided) {
            particles.add(particle);
            return true;
        }

        // Otherwise, subdivide if not already divided
        if (!divided) {
            subdivide();
        }

        // Try to insert into children
        if (northWest.insert(particle)) return true;
        if (northEast.insert(particle)) return true;
        if (southWest.insert(particle)) return true;
        if (southEast.insert(particle)) return true;

        // This should never happen if the boundary check is correct
        return false;
    }

    // Subdivide this quadrant into four
    private void subdivide() {
        double x = boundary.x;
        double y = boundary.y;
        double w = boundary.width;
        double h = boundary.height;

        Rectangle2D.Double nw = new Rectangle2D.Double(x, y, w/2, h/2);
        Rectangle2D.Double ne = new Rectangle2D.Double(x + w/2, y, w/2, h/2);
        Rectangle2D.Double sw = new Rectangle2D.Double(x, y + h/2, w/2, h/2);
        Rectangle2D.Double se = new Rectangle2D.Double(x + w/2, y + h/2, w/2, h/2);

        northWest = new QuadTree(nw, capacity);
        northEast = new QuadTree(ne, capacity);
        southWest = new QuadTree(sw, capacity);
        southEast = new QuadTree(se, capacity);

        // Move existing particles to children
        for (Particle p : particles) {
            // Insert into appropriate child
            if (northWest.insert(p)) continue;
            if (northEast.insert(p)) continue;
            if (southWest.insert(p)) continue;
            if (southEast.insert(p)) continue;
        }

        divided = true;
        particles.clear();  // Clear particles from this node as they're now in children
    }

    // Calculate center of mass for this node and its children
    public void calculateMass() {
        totalMass = 0;
        centerOfMassX = 0;
        centerOfMassY = 0;

        if (!divided) {
            // Leaf node with particles
            for (Particle p : particles) {
                totalMass += p.mass;
                centerOfMassX += p.x * p.mass;
                centerOfMassY += p.y * p.mass;
            }

            if (totalMass > 0) {
                centerOfMassX /= totalMass;
                centerOfMassY /= totalMass;
            }
        } else {
            // Internal node - calculate from children
            northWest.calculateMass();
            northEast.calculateMass();
            southWest.calculateMass();
            southEast.calculateMass();

            totalMass = northWest.totalMass + northEast.totalMass +
                    southWest.totalMass + southEast.totalMass;

            if (totalMass > 0) {
                centerOfMassX = (northWest.centerOfMassX * northWest.totalMass +
                        northEast.centerOfMassX * northEast.totalMass +
                        southWest.centerOfMassX * southWest.totalMass +
                        southEast.centerOfMassX * southEast.totalMass) / totalMass;

                centerOfMassY = (northWest.centerOfMassY * northWest.totalMass +
                        northEast.centerOfMassY * northEast.totalMass +
                        southWest.centerOfMassY * southWest.totalMass +
                        southEast.centerOfMassY * southEast.totalMass) / totalMass;
            }
        }
    }

    // Calculate force on a particle using Barnes-Hut approximation
    public void calculateForce(Particle particle, double G, double theta) {
        if (totalMass == 0 || particle == null) return;

        // Calculate distance between particle and center of mass
        double dx = centerOfMassX - particle.x;
        double dy = centerOfMassY - particle.y;
        double distanceSquared = dx*dx + dy*dy;
        double distance = Math.sqrt(distanceSquared);

        // If distance is zero or very small, skip (same particle or exact overlap)
        if (distance < 0.1) return;

        // Check if node is a leaf or if it's far enough for approximation
        // s/d < theta where s is the width of the region and d is the distance
        if (!divided || (boundary.width / distance < theta)) {
            // Use approximation - treat node as a single particle at its center of mass
            double force = G * particle.mass * totalMass / distanceSquared;
            double fx = force * dx / distance;
            double fy = force * dy / distance;

            particle.applyForce(fx, fy);
        } else {
            // Node is too close, check its children
            northWest.calculateForce(particle, G, theta);
            northEast.calculateForce(particle, G, theta);
            southWest.calculateForce(particle, G, theta);
            southEast.calculateForce(particle, G, theta);
        }
    }

    public void draw(java.awt.Graphics2D g) {
        // Draw this boundary
        g.drawRect((int) boundary.x, (int) boundary.y, (int) boundary.width, (int) boundary.height);

        // Draw children if divided
        if (divided) {
            northWest.draw(g);
            northEast.draw(g);
            southWest.draw(g);
            southEast.draw(g);
        }
    }
}

