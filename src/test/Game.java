package test;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;

public class Game extends BasicGame {

        public static final int PIXELS_PER_TILE = 8;
        public static final int PIXEL_SCALE = 1;
        public static int WIN_WIDTH;
        public static int WIN_HEIGHT;

        public static AppGameContainer appgc;

        public static World world;
        public static Renderer renderer;
        public static UserInterface gui;

        public Game(String gamename) {
                super(gamename);
        }

        @Override
        public void init(GameContainer gc) throws SlickException {
                world = new World();
                renderer = new Renderer();
                gui = new UserInterface(gc);
        }

        @Override
        public void update(GameContainer gc, int i) throws SlickException {
                float delta = i / 1000.0f;
                world.update(delta);
                renderer.update(delta);
                gui.update(gc, delta);
        }

        @Override
        public void render(GameContainer gc, Graphics g) throws SlickException {
                renderer.render(gc, g);
                gui.render(gc, g);
        }

        public static int getWorldMouseX() {
                int stageX = appgc.getInput().getAbsoluteMouseX() - renderer.stagePosition.x;
                return stageX / renderer.tileSize;
        }

        public static int getWorldMouseY() {
                int stageY = appgc.getInput().getAbsoluteMouseY() - renderer.stagePosition.y;
                return stageY / renderer.tileSize;
        }

        public static void main(String[] args) {
                try {
                        WIN_WIDTH = (World.WORLD_DIMENSIONS.x * PIXELS_PER_TILE * PIXEL_SCALE) + Renderer.MENU_WIDTH;
                        WIN_HEIGHT = (World.WORLD_DIMENSIONS.y * PIXELS_PER_TILE * PIXEL_SCALE)
                                + Renderer.HEADER_HEIGHT + Renderer.FOOTER_HEIGHT;

                        appgc = new AppGameContainer(new Game("Simple Slick Game"));
                        appgc.setDisplayMode(WIN_WIDTH, WIN_HEIGHT, false);
                        appgc.setTargetFrameRate(60);
                        appgc.setShowFPS(true);
                        appgc.start();
                } catch (SlickException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
}
