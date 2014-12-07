package test;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.Vector2f;
import test.structures.Structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {
        public static final int MENU_WIDTH = 200;
        public static final int HEADER_HEIGHT = 50;
        public static final int FOOTER_HEIGHT = 30;

        public Vector2i stageDimensions;
        public Vector2i stagePosition;

        public int tileSize;

        public Color TERRAIN_DEFAULT_COLOR = new Color(0x2e, 0x35, 0x52);
        public Color TERRAIN_COPPER_COLOR  = new Color(0xcd, 0x6d, 0x06);
        public Color TERRAIN_SILVER_COLOR  = new Color(0xcd, 0xcd, 0xcd);
        public Color TERRAIN_GLASS_COLOR   = new Color(0x83, 0xe3, 0xe4);

        public Image debugStructure;
        public WireImages wireImages;

        public static boolean debugGrid = true;

        public Map<String, Image> loadedImages;

        public List<Particle> particles;

        public Renderer() {
                loadedImages = new HashMap<>();
                particles = new ArrayList<>();

                wireImages = new WireImages();

                stageDimensions = new Vector2i(Game.WIN_WIDTH - MENU_WIDTH, (Game.WIN_HEIGHT - HEADER_HEIGHT) - FOOTER_HEIGHT);
                stagePosition = new Vector2i(0, HEADER_HEIGHT);
                tileSize = Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                Game.appgc.getGraphics().setBackground(Color.darkGray);
        }

        public Image getImage(String path) {
                if (loadedImages.containsKey(path)) {
                        // We already loaded and scaled the image
                        return loadedImages.get(path);
                } else {
                        try {
                                // We load and a scale the image, then put it into the map and return it
                                Image image = new Image(path, false, Image.FILTER_NEAREST);
                                Image scaled = image.getScaledCopy((float)Game.PIXEL_SCALE);
                                loadedImages.put(path, scaled);
                                return scaled;
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }
                        return null;
                }
        }

        public void renderStructureShadow(Structure structure, Graphics g) {
                Image image;

                if (structure.isRoad()) {
                        image = getWireImage(structure);
                } else {
                        image = structure.image;
                }

                if (image != null) {
                        int structureTileX = stagePosition.x + structure.position.x * tileSize;
                        int structureTileY = stagePosition.y + structure.position.y * tileSize;

                        Color shadowFilterColor = new Color(0, 0, 0, 100);
                        image.draw(structureTileX + 2, structureTileY + 2, shadowFilterColor);
                }
        }

        public Image getWireImage(Structure structure) {
                int x = structure.position.x;
                int y = structure.position.y;

                boolean w = false;
                boolean n = false;
                boolean e = false;
                boolean s = false;

                if (x > 0)
                        w = Game.world.structureGrid[x - 1][y] != null && Game.world.structureGrid[x - 1][y].isRoad();
                if (x < Game.world.WORLD_DIMENSIONS.x - 1)
                        e = Game.world.structureGrid[x + 1][y] != null && Game.world.structureGrid[x + 1][y].isRoad();
                if (y > 0)
                        n = Game.world.structureGrid[x][y - 1] != null && Game.world.structureGrid[x][y - 1].isRoad();
                if (y < Game.world.WORLD_DIMENSIONS.y - 1)
                        s = Game.world.structureGrid[x][y + 1] != null && Game.world.structureGrid[x][y + 1].isRoad();

                // 4 way
                if (w && n && e && s)
                        return wireImages.intersection4;

                // 3 way
                if (w && n && e)
                        return wireImages.intersection3N;
                if (n && e && s)
                        return wireImages.intersection3E;
                if (e && s && w)
                        return wireImages.intersection3S;
                if (s && w && n)
                        return wireImages.intersection3W;

                // corner
                if (w && n)
                        return wireImages.cornerWN;
                if (n && e)
                        return wireImages.cornerNE;
                if (e && s)
                        return wireImages.cornerES;
                if (s && w)
                        return wireImages.cornerSW;

                // straight
                if (w || e)
                        return wireImages.straightHoriz;
                if (n || s)
                        return wireImages.straightVerti;

                // alone or between buildings
                return wireImages.intersection4;
        }

        public void renderStructure(Structure structure, Graphics g) {
                Image image = structure.image;

                if (structure.isRoad()) {
                        Color filterColor = null;

                        switch (structure.type) {
                                case CopperRoad:
                                        filterColor = TERRAIN_COPPER_COLOR;
                                        break;
                                case SilverRoad:
                                        filterColor = TERRAIN_SILVER_COLOR;
                                        break;
                                case GlassRoad:
                                        filterColor = TERRAIN_GLASS_COLOR;
                                        break;
                                default:
                                        System.err.println("Road type unknown to renderer: " + structure.type);
                                        System.exit(-1);
                        }

                        int windowX = stagePosition.x + structure.position.x * tileSize;
                        int windowY = stagePosition.y + structure.position.y * tileSize;

                        Image wireImage = getWireImage(structure);
                        wireImage.draw(windowX, windowY, filterColor);
                } else if (image != null) {
                        int structureTileX = stagePosition.x + structure.position.x * tileSize;
                        int structureTileY = stagePosition.y + structure.position.y * tileSize;

                        image.draw(structureTileX, structureTileY);
                } else {
                        for (Vector2i occupiedTile : structure.occupiedTiles) {
                                int structureTileX = stagePosition.x + (structure.position.x + occupiedTile.x) * tileSize;
                                int structureTileY = stagePosition.y + (structure.position.y + occupiedTile.y) * tileSize;
                                g.drawImage(debugStructure, structureTileX, structureTileY);
                        }
                }
        }

        public void render(GameContainer gc, Graphics g) {
                if (debugStructure == null) {
                        try {
                                debugStructure = new Image("resources/debug_structure.png");
                                debugStructure = debugStructure.getScaledCopy(tileSize, tileSize);
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }
                }

                // Render terrain
                for (int x = 0; x < Game.world.WORLD_DIMENSIONS.x; ++x) {
                        for (int y = 0; y < Game.world.WORLD_DIMENSIONS.y; ++y) {
                                // Set color depending on terrainType
                                World.TerrainType terrainType = Game.world.terrain[x][y];
                                Color tileColor = null;
                                switch (terrainType) {
                                        case DEFAULT:
                                                tileColor = TERRAIN_DEFAULT_COLOR;
                                                break;
                                        case COPPER:
                                                tileColor = TERRAIN_COPPER_COLOR;
                                                break;
                                        case SILVER:
                                                tileColor = TERRAIN_SILVER_COLOR;
                                                break;
                                        case GLASS:
                                                tileColor = TERRAIN_GLASS_COLOR;
                                                break;
                                        default:
                                                System.err.println("Terrain type unknown to renderer. Fix it!");
                                                System.exit(-1);
                                }

                                // Draw tile
                                g.setColor(tileColor);
                                int drawX = stagePosition.x + x * tileSize;
                                int drawY = stagePosition.y + y * tileSize;
                                g.fillRect(drawX, drawY, tileSize, tileSize);
                        }
                }

                // Draw grid
                if (debugGrid) {
                        g.setColor(new Color(30, 30, 30, 30));

                        int y0 = stagePosition.y;
                        int y1 = stagePosition.y + stageDimensions.y;
                        for (int x = 0; x < Game.world.WORLD_DIMENSIONS.x; ++x) {
                                int drawX = stagePosition.x + x * tileSize;
                                g.drawLine(drawX, y0, drawX, y1);
                        }

                        int x0 = stagePosition.x;
                        int x1 = stagePosition.x + stageDimensions.x;
                        for (int y = 0; y < Game.world.WORLD_DIMENSIONS.y; ++y) {
                                int drawY = stagePosition.y + y * tileSize;
                                g.drawLine(x0, drawY, x1, drawY);
                        }
                }

                // render particles
                for (Particle particle : particles) {
                        particle.render(g);
                }

                // render structure shadows
                // In einem extra loop, damit niemals schatten ueber anderen structures gerendert werden
                // High Performance Code(C)! \s
                for (Structure structure : Game.world.structures) {
                        renderStructureShadow(structure, g);
                }

                // render structures
                for (Structure structure : Game.world.structures) {
                        renderStructure(structure, g);
                }

                Structure placeStructure = Game.gui.structureToPlace;
                if (placeStructure != null) {
                        renderStructure(placeStructure, g);
                        if (!placeStructure.canBePlaced()) {
                                for (Vector2i occupiedTile : placeStructure.occupiedTiles) {
                                        int structureTileX = stagePosition.x + (placeStructure.position.x + occupiedTile.x) * tileSize;
                                        int structureTileY = stagePosition.y + (placeStructure.position.y + occupiedTile.y) * tileSize;

                                        g.drawImage(debugStructure, structureTileX, structureTileY);
                                        g.setColor(new Color(255, 0, 0, 40));
                                        g.fillRect(structureTileX, structureTileY, tileSize, tileSize);
                                }
                        }
                }
        }

        public void update(float delta) {
                for (Particle particle : particles) {
                        particle.update(delta);
                }
                particles.removeIf((p) -> p.dead);
        }

        public void spawnParticlesAtPosition(int x, int y, float velocity, float variance, int num, Color color, float lifeTime) {
                for (int i = 0; i < num; ++i) {
                        float vel = (velocity + (velocity * variance)) - ((float)Math.random() * 2 * (velocity * variance));
                        float life = (lifeTime + (lifeTime * variance)) - ((float)Math.random() * 2 * (lifeTime * variance));
                        double theta = Math.random() * 360.0;

                        Particle particle = new Particle(x, y, life, color);
                        particle.velocity.scale(vel);
                        particle.velocity.setTheta(theta);
                        particles.add(particle);
                }
        }

        public void spawnParticlesInArea(int x, int y, int width, int height, float velocity, float variance, int num, Color color, float lifeTime) {
                for (int i = 0; i < num; ++i) {
                        int posx = x + (int)(Math.random() * width);
                        int posy = y + (int)(Math.random() * height);
                        float vel = (velocity + (velocity * variance)) - ((float)Math.random() * 2 * (velocity * variance));
                        float life = (lifeTime + (lifeTime * variance)) - ((float)Math.random() * 2 * (lifeTime * variance));
                        double theta = Math.random() * 360.0;

                        Particle particle = new Particle(posx, posy, life, color);
                        particle.velocity.scale(vel);
                        particle.velocity.setTheta(theta);
                        particles.add(particle);
                }
        }

        class WireImages {
                Image straightHoriz;
                Image straightVerti;

                Image cornerWN;
                Image cornerNE;
                Image cornerES;
                Image cornerSW;

                Image intersection3N;
                Image intersection3E;
                Image intersection3S;
                Image intersection3W;

                Image intersection4;

                WireImages() {
                        // Straight
                        Image straightBase = getImage("resources/wire/straight.png");

                        straightHoriz = straightBase.copy();

                        straightVerti = straightHoriz.copy();
                        straightVerti.rotate(90);

                        // Corners
                        Image cornerBase = getImage("resources/wire/corner.png");

                        cornerWN = cornerBase.copy();

                        cornerNE = cornerBase.copy();
                        cornerNE.rotate(90);

                        cornerES = cornerBase.copy();
                        cornerES.rotate(180);

                        cornerSW = cornerBase.copy();
                        cornerSW.rotate(270);

                        // 3-Way-Intersecions
                        Image intersection3Base = getImage("resources/wire/intersection_3.png");

                        intersection3N = intersection3Base.copy();

                        intersection3E = intersection3Base.copy();
                        intersection3E.rotate(90);

                        intersection3S = intersection3Base.copy();
                        intersection3S.rotate(180);

                        intersection3W = intersection3Base.copy();
                        intersection3W.rotate(270);

                        // 4-Way-Intersection
                        intersection4 = getImage("resources/wire/intersection_4.png");
                }
        }

        class Particle {
                Vector2f position;
                Vector2f velocity;
                float maxLifeTime;
                float lifeTime = 0;
                boolean dead;
                Color color;

                public Particle(int x, int y, float maxLifeTime, Color color) {
                        position = new Vector2f(x, y);
                        velocity = new Vector2f(1, 0);
                        dead = false;
                        this.maxLifeTime = maxLifeTime;
                        this.color = color;
                }

                public void update(float delta) {
                        lifeTime += delta;
                        dead = lifeTime >= maxLifeTime;

                        position = position.add(velocity.copy().scale(delta));
                }

                public void render(Graphics g) {
                        g.setColor(color);
                        g.fillRect(position.x, position.y, Game.PIXEL_SCALE, Game.PIXEL_SCALE);
                }
        }
}
