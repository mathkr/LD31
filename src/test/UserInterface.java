package test;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.gui.MouseOverArea;
import test.resources.Resource;
import test.structures.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class UserInterface {
        public List<MyButton> buttons;
        public SideMenu menu;
        private static Stack<MyButton> overlayButtons;

        Structure structureToPlace;

        public Vector2i guiTopLeft = new Vector2i(0, 0);
        public int buttonSize = 40;
        public int buttonMargins = 10;

        public UserInterface(GameContainer gc) {
                menu = new SideMenu();
                buttons = new ArrayList<>();
                overlayButtons = new Stack<>();
                structureToPlace = null;

                // add Buttons
                try {
                        Vector2i currentButtonPosition = new Vector2i(guiTopLeft.x + buttonMargins,
                                guiTopLeft.y + buttonMargins);

                        addButton(
                                buttons,
                                "Copper mine",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.CopperMine,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                }
                        );

                        addButton(
                                buttons,
                                "Glass mine",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.GlassMine,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                }
                        );

                        addButton(
                                buttons,
                                "Silver mine",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.SilverMine,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                }
                        );

                        addButton(
                                buttons,
                                "Silicon mine",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.SiliconMine,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                }
                        );

                        addButton(
                                buttons,
                                "PSU T1",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.PSU_T1,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                }
                        );

                        addButton(
                                buttons,
                                "RAM T1",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.RAM_T1,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                }
                        );
                } catch (SlickException e) {
                        e.printStackTrace();
                }

                gc.getInput().addMouseListener(new MouseListener() {
                        @Override
                        public void mouseWheelMoved(int change) { }

                        @Override
                        public void mouseClicked(int button, int x, int y, int clickCount) {
                                if (button == Input.MOUSE_LEFT_BUTTON) {
                                        int worldX = (Game.appgc.getInput().getAbsoluteMouseX() - Game.renderer.xOffset) / Game.renderer.tilePixelDimensions.x;
                                        int worldY = (Game.appgc.getInput().getAbsoluteMouseY() - Game.renderer.yOffset) / Game.renderer.tilePixelDimensions.y;

                                        if (worldX >= 0 && worldX < Game.world.bounds.x && worldY >= 0 && worldY < Game.world.bounds.y) {
                                                // Hier haben wir auf ein gueltiges tile geclickt

                                                if (Game.gui.structureToPlace != null && Game.gui.structureToPlace.canBePlaced()) {
                                                        Game.gui.structureToPlace.actuallyPlace();
                                                        Game.gui.structureToPlace = null;
                                                }
                                        }
                                }

                                if (button == Input.MOUSE_RIGHT_BUTTON) {
                                        Game.gui.structureToPlace = null;
                                }
                        }

                        @Override
                        public void mousePressed(int button, int x, int y) { }

                        @Override
                        public void mouseReleased(int button, int x, int y) { }

                        @Override
                        public void mouseMoved(int oldx, int oldy, int newx, int newy) { }

                        @Override
                        public void mouseDragged(int oldx, int oldy, int newx, int newy) { }

                        @Override
                        public void setInput(Input input) { }

                        @Override
                        public boolean isAcceptingInput() {
                                return true;
                        }

                        @Override
                        public void inputEnded() { }

                        @Override
                        public void inputStarted() { }
                });
        }

        public void addButton(List<MyButton> buttonList, String description, GUIContext context,
                              Image image, Vector2i position, int length, ComponentListener listener)
        {
                final MyButton button = new MyButton(
                        description,
                        context,
                        image,
                        position.x,
                        position.y,
                        length,
                        length);
                button.addListener(listener);
                buttonList.add(button);
                position.x += length + buttonMargins;
        }

        public void render(GameContainer gc, Graphics g) {
                for (MyButton button : buttons) {
                        button.render(gc, g);
                }

                g.setColor(Color.white);
                g.drawString(Game.world.resources.toString(), 10, Game.WIN_HEIGHT - 40);

                while (!overlayButtons.empty()) {
                        MyButton button = overlayButtons.pop();
                        g.setColor(Color.white);
                        g.drawString(button.buttonDescription, button.getX() + 10, button.getY() + 45);
                }
                menu.render(g);
        }

        public void update(GameContainer gc) {
                if (structureToPlace != null) {
                        structureToPlace.position.x = Game.getWorldMouseX();
                        structureToPlace.position.y = Game.getWorldMouseY();
                }
        }

        public static class MyButton extends MouseOverArea {
                public String buttonDescription;

                public MyButton(String desc, GUIContext container, Image image, int x, int y, ComponentListener listener) {
                        super(container, image, x, y, listener);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, int x, int y) {
                        super(container, image, x, y);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, int x, int y, int width, int height, ComponentListener listener) {
                        super(container, image, x, y, width, height, listener);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, int x, int y, int width, int height) {
                        super(container, image, x, y, width, height);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, Shape shape) {
                        super(container, image, shape);
                        buttonDescription = desc;
                }

                @Override
                public void mouseClicked(int button, int x, int y, int clickCount) {
                        super.mouseClicked(button, x, y, clickCount);

                        if (    x > getX() && x < getX() + getWidth()
                             && y > getY() && y < getY() + getHeight())
                        {
                                // We have been clicked?
                                consumeEvent();
                        }
                }

                @Override
                public void render(GUIContext container, Graphics g) {
                        super.render(container, g);
                        if (isMouseOver()) {
                                overlayButtons.push(this);
                        }
                }
        }

        class SideMenu {
                private Image background;

                public SideMenu(){
                        try {
                                background = new Image("resources/debug_menu.png");
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }
                }

                public void render(Graphics g){
                        // Game.renderer.xOffset
                        Integer x = Game.renderer.windowDimensions.x ;
                        Integer y = Game.renderer.yOffset;
                        Integer xi = Game.WIN_WIDTH - Game.renderer.xOffset;
                        Integer yi = Game.renderer.windowDimensions.y -Game.renderer.yOffset;
                        g.drawImage(background,x,y,xi,yi,0,0, background.getWidth(),background.getHeight());

                        if(structureToPlace != null){
                                Image image = structureToPlace.image;
                                if(image!= null){
                                        g.setColor(Color.white);
                                        int width = image.getWidth();
                                        int height = image.getHeight();
                                        g.fillRect(xi - (width + 20), y, (width + 20), height + 20);
                                        g.drawImage(image, xi - (width + 10), y + 10 );
                                }

                                int h = y + 30 + (image != null ? image.getHeight() : 0);
                                g.setColor(Color.white);
                                g.drawLine(x,h, xi, h);

                                h += 10;
                                for (Map.Entry<Resource,Float> resource : structureToPlace.buildCost.resources.entrySet()) {
                                        g.drawString(resource.getKey() + " : " + resource.getValue(),x + 10, h  );
                                        h+= 20;
                                }



                        }



                }

        }
}
