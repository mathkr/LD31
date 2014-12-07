package test;

import org.newdawn.slick.*;
import test.structures.Structure;

import java.util.HashMap;
import java.util.Map;

public class Renderer {
        public Integer menuSize = 200;
        public Vector2i windowDimensions = new Vector2i(Game.WIN_WIDTH - menuSize, Game.WIN_HEIGHT);

        public Color TERRAIN_DEFAULT_COLOR = new Color(0x2e, 0x35, 0x52);
        public Color TERRAIN_COPPER_COLOR  = new Color(0xcd, 0x6d, 0x06);
        public Color TERRAIN_SILVER_COLOR  = new Color(0xcd, 0xcd, 0xcd);
        public Color TERRAIN_GLASS_COLOR   = new Color(0x83, 0xe3, 0xe4);

        public Image debugStructure;

        public static boolean debugGrid = true;

        public Vector2i tilePixelDimensions;

        public int xOffset;
        public int yOffset;

        public Map<String, Image> loadedImages;

        public Renderer() {
                xOffset = 0;
                yOffset = 0;
                loadedImages = new HashMap<>();

                try {
                        Image debug = new Image("resources/debug_structure.png");
                } catch (SlickException e) {
                        e.printStackTrace();
                }
        }

        public Image getImage(String path) {
                if (loadedImages.containsKey(path)) {
                        // We already loaded and scaled the image
                        return loadedImages.get(path);
                } else {
                        try {
                                // We load and a scale the image, then put it into the map and return it
                                Image image = new Image(path, false, Image.FILTER_NEAREST);
                                Image scaled = image.getScaledCopy((float)tilePixelDimensions.x / 8.0f);
                                loadedImages.put(path, scaled);
                                return scaled;
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }
                        return null;
                }
        }

        public void renderStructure(Structure structure, Graphics g) {
                Image image = structure.image;

                if (image != null) {
                        int structureTileX = xOffset + structure.position.x * tilePixelDimensions.x;
                        int structureTileY = yOffset + structure.position.y * tilePixelDimensions.y;

                        image.draw(structureTileX + 3, structureTileY + 3, new Color(0, 0, 0, 80));
                        image.draw(structureTileX, structureTileY);
                } else {
                        for (Vector2i occupiedTile : structure.occupiedTiles) {
                                int structureTileX = xOffset + (structure.position.x + occupiedTile.x) * tilePixelDimensions.x;
                                int structureTileY = yOffset + (structure.position.y + occupiedTile.y) * tilePixelDimensions.y;
                                g.drawImage(debugStructure, structureTileX, structureTileY);
                        }
                }
        }

        public void render(GameContainer gc, Graphics g) {
                tilePixelDimensions = new Vector2i(0, 0);
                xOffset = 0;
                yOffset = 0;

                float worldAspectRatio = (float)Game.world.bounds.x / (float)Game.world.bounds.y;
                float windowAspectRatio = (float)windowDimensions.x / (float)windowDimensions.y;
                if (worldAspectRatio > windowAspectRatio) {
                        // World dimensions are wider than high (landscape)
                        tilePixelDimensions.x = windowDimensions.x / Game.world.bounds.x;
                        tilePixelDimensions.y = tilePixelDimensions.x;
                } else {
                        // World dimensions are higher than wide (portrait)
                        tilePixelDimensions.y = windowDimensions.y / Game.world.bounds.y;
                        tilePixelDimensions.x = tilePixelDimensions.y;
                }

                xOffset = (windowDimensions.x - (Game.world.bounds.x * tilePixelDimensions.x)) / 2;
                yOffset = (windowDimensions.y - (Game.world.bounds.y * tilePixelDimensions.y)) / 2;

                if (debugStructure == null) {
                        try {
                                debugStructure = new Image("resources/debug_structure.png");
                                debugStructure = debugStructure.getScaledCopy(tilePixelDimensions.x, tilePixelDimensions.y);
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }
                }

                // Render terrain
                for (int x = 0; x < Game.world.bounds.x; ++x) {
                        for (int y = 0; y < Game.world.bounds.y; ++y) {
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
                                int drawX = xOffset + x * tilePixelDimensions.x;
                                int drawY = yOffset + y * tilePixelDimensions.y;
                                g.fillRect(drawX, drawY, tilePixelDimensions.x, tilePixelDimensions.y);
                        }
                }

                // Draw grid
                if (debugGrid) {
                        g.setColor(new Color(30, 30, 30, 30));

                        int y0 = yOffset;
                        int y1 = yOffset + tilePixelDimensions.y * Game.world.bounds.y;
                        for (int x = 0; x < Game.world.bounds.x; ++x) {
                                int drawX = xOffset + x * tilePixelDimensions.x;
                                g.drawLine(drawX, y0, drawX, y1);
                        }

                        int x0 = xOffset;
                        int x1 = xOffset + tilePixelDimensions.x * Game.world.bounds.x;
                        for (int y = 0; y < Game.world.bounds.y; ++y) {
                                int drawY = yOffset + y * tilePixelDimensions.y;
                                g.drawLine(x0, drawY, x1, drawY);
                        }
                }

                // render structures
                for (Structure structure : Game.world.structures) {
                        renderStructure(structure, g);
                }

                Structure placeStructure = Game.gui.structureToPlace;
                if (placeStructure != null) {
                        renderStructure(placeStructure, g);
                        for (Vector2i occupiedTile : placeStructure.occupiedTiles) {
                                int structureTileX = xOffset + (placeStructure.position.x + occupiedTile.x) * tilePixelDimensions.x;
                                int structureTileY = yOffset + (placeStructure.position.y + occupiedTile.y) * tilePixelDimensions.y;
                                if (!placeStructure.canBePlaced()) {
                                        g.drawImage(debugStructure, structureTileX, structureTileY);
                                        g.setColor(new Color(255, 0, 0, 40));
                                        g.fillRect(structureTileX, structureTileY, tilePixelDimensions.x, tilePixelDimensions.y);
                                }
                        }
                }
        }
}
