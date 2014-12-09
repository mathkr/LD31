package test;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;

public class Game extends BasicGame {

        public static final int PIXELS_PER_TILE = 8;
        public static final int PIXEL_SCALE = 2;
        public static int WIN_WIDTH;
        public static int WIN_HEIGHT;

        public static AppGameContainer appgc;

        public static World world;
        public static Renderer renderer;
        public static SoundLib sound;
        public static UserInterface gui;

        private final float startTime = 3;
        private float startElapsed = 0;
        private boolean gameHasStarted = false;
        private Image title;

        public Game(String gamename) {
                super(gamename);
        }

        @Override
        public void init(GameContainer gc) throws SlickException {
                world = new World();
                renderer = new Renderer();
                sound = new SoundLib();
                gui = new UserInterface(gc);

                title = new Image("resources/title.png", false, Image.FILTER_NEAREST).getScaledCopy(Game.PIXEL_SCALE);
        }

        @Override
        public void update(GameContainer gc, int i) throws SlickException {
                float delta = i / 1000.0f;
                if (gameHasStarted) {
                        world.update(delta);
                        renderer.update(delta);
                        gui.update(gc, delta);
                } else {
                        startElapsed += delta;
                        if (startElapsed >= startTime) {
                                gameHasStarted = true;
                        }
                }
        }

        @Override
        public void render(GameContainer gc, Graphics g) throws SlickException {
                if (gameHasStarted) {
                        g.setFont(renderer.font);
                        g.setLineWidth(2f);
                        renderer.render(gc, g);
                        gui.render(gc, g);
                } else {
                        title.drawCentered(WIN_WIDTH / 2, WIN_HEIGHT / 2);
                }
        }

        public static int getWorldMouseX() {
                int worldX = (appgc.getInput().getAbsoluteMouseX() - renderer.stagePosition.x) / renderer.tileSize;

                worldX = worldX < 0 ? 0 : worldX;
                worldX = worldX >= World.WORLD_DIMENSIONS.x ? World.WORLD_DIMENSIONS.x - 1 : worldX;

                return worldX;
        }

        public static int getWorldMouseY() {
                int worldY = (appgc.getInput().getAbsoluteMouseY() - renderer.stagePosition.y) / renderer.tileSize;

                worldY = worldY < 0 ? 0 : worldY;
                worldY = worldY >= World.WORLD_DIMENSIONS.y ? World.WORLD_DIMENSIONS.y - 1 : worldY;

                return worldY;
        }

        public static int getWorldX(int windowX) {
                int worldX =  (windowX - renderer.stagePosition.x) / renderer.tileSize;

                worldX = worldX < 0 ? 0 : worldX;
                worldX = worldX >= World.WORLD_DIMENSIONS.x ? World.WORLD_DIMENSIONS.x - 1 : worldX;

                return worldX;
        }

        public static int getWorldY(int windowY) {
                int worldY =  (windowY - renderer.stagePosition.y) / renderer.tileSize;

                worldY = worldY < 0 ? 0 : worldY;
                worldY = worldY >= World.WORLD_DIMENSIONS.y ? World.WORLD_DIMENSIONS.y - 1 : worldY;

                return worldY;
        }

        public static void main(String[] args) {
                try {
                        WIN_WIDTH = (World.WORLD_DIMENSIONS.x * PIXELS_PER_TILE * PIXEL_SCALE) + Renderer.MENU_WIDTH;
                        WIN_HEIGHT = (World.WORLD_DIMENSIONS.y * PIXELS_PER_TILE * PIXEL_SCALE)
                                + Renderer.HEADER_HEIGHT + Renderer.FOOTER_HEIGHT;

                        appgc = new AppGameContainer(new Game("Simple Slick Game"));
                        appgc.setDisplayMode(WIN_WIDTH, WIN_HEIGHT, false);
                        appgc.setTargetFrameRate(60);
                        appgc.setShowFPS(false);
                        appgc.start();
                } catch (SlickException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}
