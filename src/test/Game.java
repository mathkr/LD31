package test;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;

public class Game extends BasicGame
{
        public static final int WIN_WIDTH = 800;
        public static final int WIN_HEIGHT = 600;

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
                gui.update(gc);
        }

        @Override
        public void render(GameContainer gc, Graphics g) throws SlickException {
                renderer.render(gc, g);
                gui.render(gc, g);
        }

        public static int getWorldMouseX() {
                int stageX = appgc.getInput().getAbsoluteMouseX() - renderer.xOffset;
                return stageX / renderer.tilePixelDimensions.x;
        }

        public static int getWorldMouseY() {
                int stageY = appgc.getInput().getAbsoluteMouseY() - renderer.yOffset;
                return stageY / renderer.tilePixelDimensions.y;
        }

        @Override
        public void mouseClicked(int button, int x, int y, int clickCount) {
                super.mouseClicked(button, x, y, clickCount);

                if (button == Input.MOUSE_LEFT_BUTTON) {
                        int worldX = (appgc.getInput().getAbsoluteMouseX() - renderer.xOffset) / renderer.tilePixelDimensions.x;
                        int worldY = (appgc.getInput().getAbsoluteMouseY() - renderer.yOffset) / renderer.tilePixelDimensions.y;

                        if (worldX >= 0 && worldX < world.bounds.x && worldY >= 0 && worldY < world.bounds.y) {
                                // Hier haben wir auf ein gueltiges tile geclickt
                                if (gui.structureToPlace != null && gui.structureToPlace.canBePlaced()) {
                                        gui.structureToPlace.actuallyPlace();
                                        gui.structureToPlace = null;
                                }
                        }
                }

                if (button == Input.MOUSE_RIGHT_BUTTON) {
                        gui.structureToPlace = null;
                }
        }

        public static void main(String[] args) {
                try {
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
