package test;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.gui.MouseOverArea;
import org.newdawn.slick.util.FastTrig;
import test.resources.Resource;
import test.structures.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class UserInterface {
        enum SideMenuState {
                OFF,
                PLACING,
                SELECTING,
        }

        public List<MyButton> buttons;
        private static Stack<MyButton> overlayButtons;

        public SideMenu menu;
        public SideMenuState menuState;
        public Structure structureToPlace;
        public Structure selectedStructure;

        Color selectionColor = new Color(0xFF, 0xFF, 0xFF);
        float selectionColorTime = 0f;

        public int buttonMargins = 10;
        public int buttonSize = Renderer.HEADER_HEIGHT - buttonMargins;

        public UserInterface(GameContainer gc) {
                menu = new SideMenu();
                buttons = new ArrayList<>();
                overlayButtons = new Stack<>();

                structureToPlace = null;
                menuState = SideMenuState.OFF;
                selectedStructure = null;

                // add Buttons
                try {
                        Vector2i currentButtonPosition = new Vector2i(buttonMargins / 2, buttonMargins / 2);

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
                                        menuState = SideMenuState.PLACING;
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
                                        menuState = SideMenuState.PLACING;
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
                                        menuState = SideMenuState.PLACING;
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
                                        menuState = SideMenuState.PLACING;
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
                                        menuState = SideMenuState.PLACING;
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
                                        menuState = SideMenuState.PLACING;
                                }
                        );

                        addButton(
                                buttons,
                                "Memory T1",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.MEMORY_T1,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                        menuState = SideMenuState.PLACING;
                                }
                        );

                        addButton(
                                buttons,
                                "CPU T1",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.CPU_T1,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                        menuState = SideMenuState.PLACING;
                                }
                        );

                        addButton(
                                buttons,
                                "Copper road",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.CopperRoad,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                        menuState = SideMenuState.PLACING;
                                }
                        );

                        addButton(
                                buttons,
                                "Silver road",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.SilverRoad,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                        menuState = SideMenuState.PLACING;
                                }
                        );

                        addButton(
                                buttons,
                                "Glass road",
                                gc,
                                new Image("resources/debug_button.png"),
                                currentButtonPosition,
                                buttonSize,
                                (comp) -> {
                                        structureToPlace = StructureLoader.getInstance(StructureType.GlassRoad,
                                                Game.getWorldMouseX(),
                                                Game.getWorldMouseY()
                                        );
                                        menuState = SideMenuState.PLACING;
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
                                        int worldX = (Game.appgc.getInput().getAbsoluteMouseX() - Game.renderer.stagePosition.x) / Game.renderer.tileSize;
                                        int worldY = (Game.appgc.getInput().getAbsoluteMouseY() - Game.renderer.stagePosition.y) / Game.renderer.tileSize;
                                        Vector2i clickedPosition = new Vector2i(worldX, worldY);

                                        if (worldX >= 0 && worldX < Game.world.WORLD_DIMENSIONS.x && worldY >= 0 && worldY < Game.world.WORLD_DIMENSIONS.y) {
                                                // Hier haben wir auf ein gueltiges tile geclickt

                                                if (structureToPlace != null) {
                                                        // can it be placed? roads get handled in mouse dragged callback
                                                        if (structureToPlace.canBePlaced() && !structureToPlace.isRoad()) {
                                                                structureToPlace.actuallyPlace();

                                                                int particleX = x;
                                                                int particleW = structureToPlace.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                int particleY = y;
                                                                int particleH = structureToPlace.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                                Game.renderer.spawnParticlesInArea(
                                                                        particleX, particleY,
                                                                        particleW, particleH,
                                                                        40, 1, 20,
                                                                        Game.renderer.TERRAIN_DEFAULT_COLOR.brighter(1.5f), 1f);

                                                                structureToPlace = null;
                                                                menuState = SideMenuState.OFF;
                                                        }
                                                } else {
                                                        // See if we clicked on a structure
                                                        Structure clickedStructure = null;
                                                        int structureIndex = 0;
                                                        while (clickedStructure == null && structureIndex < Game.world.structures.size()) {
                                                                Structure current = Game.world.structures.get(structureIndex);

                                                                for (Vector2i occupiedTile : current.occupiedTiles) {
                                                                        if (Vector2i.add(current.position, occupiedTile).equals(clickedPosition)) {
                                                                                clickedStructure = current;
                                                                                break;
                                                                        }
                                                                }

                                                                ++structureIndex;
                                                        }

                                                        System.out.println("clicked: " + clickedPosition);

                                                        if (clickedStructure != null) {
                                                                // We clicked on a structure
                                                                selectedStructure = clickedStructure;
                                                                menuState = SideMenuState.SELECTING;
                                                                selectionColorTime = 0f;
                                                        } else {
                                                                // We clicked on nothing
                                                                selectedStructure = null;
                                                                menuState = SideMenuState.OFF;
                                                        }
                                                }
                                        }
                                }

                                if (button == Input.MOUSE_RIGHT_BUTTON) {
                                        if (menuState == SideMenuState.PLACING) {
                                                // Abbrechen
                                                structureToPlace = null;
                                                menuState = SideMenuState.OFF;
                                        } else if (menuState == SideMenuState.SELECTING) {
                                                // Abbrechen
                                                selectedStructure = null;
                                                menuState = SideMenuState.OFF;
                                        }
                                }
                        }

                        @Override
                        public void mousePressed(int button, int x, int y) { }

                        @Override
                        public void mouseReleased(int button, int x, int y) { }

                        @Override
                        public void mouseMoved(int oldx, int oldy, int newx, int newy) { }

                        @Override
                        public void mouseDragged(int oldx, int oldy, int newx, int newy) {
                                if (structureToPlace != null) {
                                        // Placing roads
                                        if (structureToPlace.canBePlaced() && structureToPlace.isRoad()) {
                                                structureToPlace.actuallyPlace();

                                                int particleX = newx;
                                                int particleW = structureToPlace.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                int particleY = newy;
                                                int particleH = structureToPlace.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                Game.renderer.spawnParticlesInArea(
                                                        particleX, particleY,
                                                        particleW, particleH,
                                                        40, 1, 20,
                                                        Game.renderer.TERRAIN_DEFAULT_COLOR.brighter(1.5f), 1f);

                                                structureToPlace = StructureLoader.getInstance(structureToPlace.type, structureToPlace.position.x,
                                                        structureToPlace.position.y);
                                        }
                                }
                        }

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
                if (buttonList != null) {
                        buttonList.add(button);
                }
                button.addListener(listener);
                position.x += length + buttonMargins;
        }

        public void render(GameContainer gc, Graphics g) {
                for (MyButton button : buttons) {
                        button.render(gc, g);
                }

                g.setColor(Color.white);

                String resources = String.format("Copper: %.0f/%.0f | Silver: %.0f/%.0f | Glass: %.0f/%.0f | Silicon: %.0f/%.0f | Energy: %.0f/%.0f ||| Electrons: %.0f/%.0f | Photons: %.0f/%.0f | Quants: %.0f/%.0f",
                        Game.world.resources.get(Resource.COPPER), Game.world.resourceCapacity.get(Resource.COPPER),
                        Game.world.resources.get(Resource.SILVER), Game.world.resourceCapacity.get(Resource.SILVER),
                        Game.world.resources.get(Resource.GLASS), Game.world.resourceCapacity.get(Resource.GLASS),
                        Game.world.resources.get(Resource.SILICON), Game.world.resourceCapacity.get(Resource.SILICON),
                        Game.world.resources.get(Resource.ENERGY), Game.world.resourceCapacity.get(Resource.ENERGY),
                        Game.world.resources.get(Resource.ELECTRON), Game.world.resourceCapacity.get(Resource.ELECTRON),
                        Game.world.resources.get(Resource.PHOTON), Game.world.resourceCapacity.get(Resource.PHOTON),
                        Game.world.resources.get(Resource.QUANTUM), Game.world.resourceCapacity.get(Resource.QUANTUM));

                g.drawString(resources, buttonMargins / 2, Game.renderer.stagePosition.y + Game.renderer.stageDimensions.y + 5);

                menu.render(g);

                while (!overlayButtons.empty()) {
                        MyButton button = overlayButtons.pop();
                        g.setColor(Color.white);
                        g.drawString(button.buttonDescription, button.getX() + 10, button.getY() + 45);
                }
        }

        public void update(GameContainer gc, float delta) {
                if (structureToPlace != null) {
                        int newPosX = Game.getWorldMouseX();
                        int newPosY = Game.getWorldMouseY();

                        newPosX = newPosX < 0 ? 0 : newPosX;
                        newPosX = newPosX >= World.WORLD_DIMENSIONS.x - structureToPlace.dimensions.x
                                ? (World.WORLD_DIMENSIONS.x - structureToPlace.dimensions.x) : newPosX;
                        
                        newPosY = newPosY < 0 ? 0 : newPosY;
                        newPosY = newPosY >= World.WORLD_DIMENSIONS.y - structureToPlace.dimensions.y
                                ? (World.WORLD_DIMENSIONS.y - structureToPlace.dimensions.y) : newPosY;

                        structureToPlace.position.x = newPosX;
                        structureToPlace.position.y = newPosY;
                }

                selectionColorTime += 5 * delta;
                float alpha = Math.abs((float) FastTrig.sin(selectionColorTime));
                selectionColor = new Color(0xFF, 0xFF, 0xFF, alpha);
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
                public MyButton removeButton;

                public SideMenu(){
                        try {
                                background = new Image("resources/debug_menu.png");

                                Image removeButtonImage = new Image("resources/debug_button_invert.png").getScaledCopy(80, 20);

                                removeButton = new MyButton(
                                        "Remove selected structure",
                                        Game.appgc,
                                        removeButtonImage,
                                        Game.renderer.stageDimensions.x + 10,
                                        Game.WIN_HEIGHT - 90,
                                        80,
                                        20);

                                removeButton.addListener(
                                        (comp) -> {
                                                if (menuState == SideMenuState.SELECTING) {
                                                        selectedStructure.remove();
                                                        selectedStructure = null;
                                                        menuState = SideMenuState.OFF;
                                                }
                                        });
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }
                }

                public void render(Graphics g) {
                        Structure structure = null;

                        switch (menuState) {
                                case OFF:
                                        return;
                                case PLACING:
                                        structure = structureToPlace;
                                        break;
                                case SELECTING:
                                        structure = selectedStructure;
                                        break;
                                default:
                                        System.err.println("Unknown menu state in side menu renderer.");
                                        System.exit(-1);
                        }

                        // Game.renderer.xOffset
                        Integer x = Game.renderer.stageDimensions.x;
                        Integer y = Renderer.HEADER_HEIGHT;
                        Integer xi = Game.WIN_WIDTH;
                        Integer yi = Game.WIN_HEIGHT - Renderer.FOOTER_HEIGHT;

                        g.drawImage(background,x,y,xi,yi,0,0, background.getWidth(),background.getHeight());

                        Image image = structure.image;
                        if(image != null){
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
                        for (Map.Entry<Resource,Float> resource : structure.buildCost.resources.entrySet()) {
                                g.drawString(resource.getKey() + " : " + resource.getValue(),x + 10, h  );
                                h+= 20;
                        }

                        if (menuState == SideMenuState.SELECTING) {
                                g.setColor(selectionColor);
                                int selectedX = Game.renderer.stagePosition.x + selectedStructure.position.x * Game.renderer.tileSize;
                                int selectedY = Game.renderer.stagePosition.y + selectedStructure.position.y * Game.renderer.tileSize;

                                g.drawRect(selectedX - 5, selectedY - 5,
                                        selectedStructure.dimensions.x * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10,
                                        selectedStructure.dimensions.y * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10);

                                removeButton.render(Game.appgc, g);
                        }
                }
        }
}
