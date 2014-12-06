package test;

import org.newdawn.slick.*;
import test.structures.Structure;

public class Renderer {
        public Vector2i windowDimensions = new Vector2i(Game.WIN_WIDTH, Game.WIN_HEIGHT);

        public Color TERRAIN_DEFAULT_COLOR = new Color(0x3a, 0x8b, 0x27);
        public Color TERRAIN_COPPER_COLOR  = new Color(0xcd, 0x6d, 0x06);
        public Color TERRAIN_SILVER_COLOR  = new Color(0xcd, 0xcd, 0xcd);
        public Color TERRAIN_GLASS_COLOR   = new Color(0x83, 0xe3, 0xe4);

        public Image debugStructure;

        public static boolean debugGrid = true;

        public Renderer() {
                try {
                        debugStructure = new Image("resources/debug_structure.png");
                } catch (SlickException e) {
                        e.printStackTrace();
                }
        }

        public void render(GameContainer gc, Graphics g) {
                Vector2i tilePixelDimensions = new Vector2i(0, 0);
                int xOffset = 0;
                int yOffset = 0;

                float worldAspectRatio = (float)Game.world.bounds.x / (float)Game.world.bounds.y;
                if (worldAspectRatio > 1.0f) {
                        // World dimensions are wider than high (landscape)
                        tilePixelDimensions.x = windowDimensions.x / Game.world.bounds.x;
                        tilePixelDimensions.y = tilePixelDimensions.x;
                        yOffset = (windowDimensions.y - (Game.world.bounds.y * tilePixelDimensions.y)) / 2;
                } else {
                        // World dimensions are higher than wide (portrait)
                        tilePixelDimensions.y = windowDimensions.y / Game.world.bounds.y;
                        tilePixelDimensions.x = tilePixelDimensions.y;
                        xOffset = (windowDimensions.x - (Game.world.bounds.x * tilePixelDimensions.x)) / 2;
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
                                //System.out.println("currentDrawCoordinates = " + drawX + ", " + drawY);
                                g.fillRect(drawX, drawY, tilePixelDimensions.x, tilePixelDimensions.y);
                        }
                }

                // Debug render structures
                Image scaledDebugStructure = debugStructure.getScaledCopy(tilePixelDimensions.x, tilePixelDimensions.y);
                for (Structure structure : Game.world.structures) {
                        for (Vector2i occupiedTile : structure.occupiedTiles) {
                                int structureTileX = xOffset + (structure.position.x + occupiedTile.x) * tilePixelDimensions.x;
                                int structureTileY = yOffset + (structure.position.y + occupiedTile.y) * tilePixelDimensions.y;
                                g.drawImage(scaledDebugStructure, structureTileX, structureTileY);
                        }
                }

                // Draw debug grid
                if (debugGrid) {
                        g.setColor(new Color(30, 30, 30, 100));

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
        }
}
