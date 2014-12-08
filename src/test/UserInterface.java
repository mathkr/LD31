package test;

import org.newdawn.slick.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.gui.MouseOverArea;
import org.newdawn.slick.util.FastTrig;
import test.resources.Resource;
import test.structures.*;

import java.util.*;

public class UserInterface {
        enum SideMenuState {
                OFF,
                PLACING,
                SELECTING,
        }

        public static Resource[] base = { Resource.COPPER, Resource.SILVER, Resource.GLASS, Resource.SILICON };
        public static Resource[] energy = { Resource.ENERGY };
        public static Resource[] population = { Resource.ELECTRON, Resource.PHOTON, Resource.QUANTUM };
        public static Resource[] data = { Resource.DATA, Resource.PRODUKT1, Resource.PRODUKT2, Resource.PRODUKT3 };

        public List<MyButton> buttons;
        private static Stack<MyButton> overlayButtons;

        public SideMenu menu;
        public SideMenuState menuState;
        public Structure structureToPlace;
        public Structure selectedStructure;

        Color selectionColor = new Color(0xFF, 0xFF, 0xFF);
        float selectionColorTime = 0f;

        public int buttonMargins = 20;
        public int buttonSize = Renderer.HEADER_HEIGHT - buttonMargins;

        public UserInterface(GameContainer gc) {
                menu = new SideMenu();
                buttons = new ArrayList<>();
                overlayButtons = new Stack<>();

                structureToPlace = null;
                menuState = SideMenuState.OFF;
                selectedStructure = null;

                // Add a button per StructureType
                Vector2i buttonPos = new Vector2i(buttonMargins / 2, buttonMargins / 2);
                for (StructureType structureType : StructureType.values()) {
                        String name = StructureLoader.getProperties(structureType).getProperty("name", "no name");
                        String desc = StructureLoader.getProperties(structureType).getProperty("desc", "no description");
                        String imgPath = StructureLoader.getProperties(structureType).getProperty("image");

                        Rectangle shape = new Rectangle(buttonPos.x, buttonPos.y, buttonSize, buttonSize);

                        Image image = null;

                        if (imgPath != null) {
                                image = Game.renderer.getImage("resources/" + imgPath).copy();

                                Color filterColor;
                                switch (structureType) {
                                        case CopperRoad:
                                                filterColor = Game.renderer.TERRAIN_COPPER_COLOR;
                                                break;
                                        case SilverRoad:
                                                filterColor = Game.renderer.TERRAIN_SILVER_COLOR;
                                                break;
                                        case GlassRoad:
                                                filterColor = Game.renderer.TERRAIN_GLASS_COLOR;
                                                break;
                                        default:
                                                filterColor = Color.white;
                                                break;
                                }

                                image.setImageColor(filterColor.r, filterColor.g, filterColor.b, filterColor.a);
                        }

                        MyButton button = new MyButton(name + ": " + desc, gc, image, shape);

                        button.addListener((component) -> {
                                        structureToPlace = StructureLoader.getInstance(structureType,
                                                0,
                                                0
                                        );
                                        menuState = SideMenuState.PLACING;
                                });

                        buttons.add(button);

                        buttonPos.x += buttonSize + buttonMargins;
                }

                gc.getInput().addMouseListener(new MouseListener() {
                        @Override
                        public void mouseWheelMoved(int change) { }

                        @Override
                        public void mouseClicked(int button, int x, int y, int clickCount) {
                        }

                        @Override
                        public void mousePressed(int button, int x, int y) { }

                        @Override
                        public void mouseReleased(int button, int x, int y) {
                                if (button == Input.MOUSE_LEFT_BUTTON) {
                                        int worldX = (Game.appgc.getInput().getAbsoluteMouseX() - Game.renderer.stagePosition.x) / Game.renderer.tileSize;
                                        int worldY = (Game.appgc.getInput().getAbsoluteMouseY() - Game.renderer.stagePosition.y) / Game.renderer.tileSize;
                                        Vector2i clickedPosition = new Vector2i(worldX, worldY);

                                        if (worldX >= 0 && worldX < Game.world.WORLD_DIMENSIONS.x && worldY >= 0 && worldY < Game.world.WORLD_DIMENSIONS.y) {
                                                // Hier haben wir auf ein gueltiges tile geclickt

                                                if (structureToPlace != null) {
                                                        // can it be placed? roads get handled in mouse dragged callback
                                                        if (structureToPlace.canBePlaced()) {
                                                                structureToPlace.actuallyPlace();

                                                                int particleX = x;
                                                                int particleW = structureToPlace.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                int particleY = y;
                                                                int particleH = structureToPlace.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                                Game.renderer.spawnParticlesInArea(
                                                                        particleX, particleY,
                                                                        particleW, particleH,
                                                                        40, 1, 20,
                                                                        Game.renderer.TERRAIN_DEFAULT_COLOR.brighter(0.7f), 1f);

                                                                if (!structureToPlace.isRoad()) { // Only stop placing if we arent placing a wire
                                                                        structureToPlace = null;
                                                                        menuState = SideMenuState.OFF;
                                                                } else { // If we are, make a new wire piece to place
                                                                        structureToPlace = StructureLoader.getInstance(structureToPlace.type, structureToPlace.position.x,
                                                                                structureToPlace.position.y);
                                                                }
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
                        public void mouseMoved(int oldx, int oldy, int newx, int newy) { }

                        @Override
                        public void mouseDragged(int oldx, int oldy, int newx, int newy) {
                                if (Game.appgc.getInput().isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
                                        if (structureToPlace != null) {
                                                if (structureToPlace.isRoad()
                                                                && oldx >  Game.renderer.stagePosition.x
                                                                && oldx <= Game.renderer.stagePosition.x + Game.renderer.stageDimensions.x
                                                                && oldy >  Game.renderer.stagePosition.y
                                                                && oldy <= Game.renderer.stagePosition.y + Game.renderer.stageDimensions.y)
                                                {
                                                        // Placing roads
                                                        int startx = Game.getWorldX(oldx);
                                                        int starty = Game.getWorldY(oldy);

                                                        int endx = Game.getWorldX(newx);
                                                        int endy = Game.getWorldY(newy);

                                                        int deltax = endx - startx;
                                                        int deltay = endy - starty;

                                                        int signx = (int)Math.signum(deltax);
                                                        int signy = (int)Math.signum(deltay);

                                                        boolean stepX = Math.abs(deltax) > Math.abs(deltay);

                                                        int currentx = startx;
                                                        int currenty = starty;

                                                        while (true) {
                                                                structureToPlace.position.x = currentx;
                                                                structureToPlace.position.y = currenty;

                                                                if (structureToPlace.canBePlaced()) {
                                                                        structureToPlace.actuallyPlace();

                                                                        int particleX = newx;
                                                                        int particleW = structureToPlace.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                                        int particleY = newy;
                                                                        int particleH = structureToPlace.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                                        Game.renderer.spawnParticlesInArea(
                                                                                particleX, particleY,
                                                                                particleW, particleH,
                                                                                40, 1, 20,
                                                                                Game.renderer.TERRAIN_DEFAULT_COLOR.brighter(0.7f), 1f);

                                                                        structureToPlace = StructureLoader.getInstance(structureToPlace.type, structureToPlace.position.x,
                                                                                structureToPlace.position.y);
                                                                }

                                                                if (currentx == endx && currenty == endy) {
                                                                        break;
                                                                }

                                                                if (currentx == endx) {
                                                                        stepX = false;
                                                                }

                                                                if (currenty == endy) {
                                                                        stepX = true;
                                                                }

                                                                if (stepX) {
                                                                        currentx += signx;
                                                                        stepX = !stepX;
                                                                } else {
                                                                        currenty += signy;
                                                                        stepX = !stepX;
                                                                }
                                                        }
                                                }
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

                int textY = (Game.WIN_HEIGHT - Renderer.FOOTER_HEIGHT) + buttonMargins / 2;
                int textX = buttonMargins / 2;

                StringBuilder baseSB = new StringBuilder();
                for (Resource resource : base) {
                        baseSB.append(resource.name() + ": " + (Math.round(Game.world.resources.get(resource))) + "/"
                                + (Math.round(Game.world.resourceCapacity.get(resource))) + " ");
                }

                StringBuilder energySB = new StringBuilder();
                for (Resource resource : energy) {
                        energySB.append(resource.name() + ": " + (Math.round(Game.world.resources.get(resource))) + "/"
                                + (Math.round(Game.world.resourceCapacity.get(resource))) + " ");
                }

                StringBuilder populationSB = new StringBuilder();
                for (Resource resource : population) {
                        populationSB.append(resource.name() + ": " + (Math.round(Game.world.resources.get(resource))) + "/"
                                + (Math.round(Game.world.resourceCapacity.get(resource))) + " ");
                }

                StringBuilder dataSB = new StringBuilder();
                for (Resource resource : data) {
                        dataSB.append(resource.name() + ": " + (Math.round(Game.world.resources.get(resource))) + "/"
                                + (Math.round(Game.world.resourceCapacity.get(resource))) + " ");
                }

                g.drawString(baseSB.toString(), textX, textY);

                textX = (Game.WIN_WIDTH - buttonMargins / 2) - Game.renderer.font.getWidth(energySB.toString());
                g.drawString(energySB.toString(), textX, textY);

                textX = buttonMargins / 2;
                textY += buttonMargins / 2 + Game.renderer.font.getLineHeight();
                g.drawString(populationSB.toString(), textX, textY);

                textX = (Game.WIN_WIDTH - buttonMargins / 2) - Game.renderer.font.getWidth(dataSB.toString());
                g.drawString(dataSB.toString(), textX, textY);

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
                public Rectangle shape;

                public MyButton(String desc, GUIContext container, Image image, int x, int y, int width, int height) {
                        super(container, image, x, y, width, height);
                        buttonDescription = desc;
                }

                public MyButton(String desc, GUIContext container, Image image, Rectangle shape) {
                        super(container, image, shape);
                        this.shape = shape;
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
                        g.setColor(Game.renderer.TERRAIN_DEFAULT_COLOR);
                        if (shape != null) {
                                g.fill(shape);
                        }

                        if (isMouseOver()) {
                                overlayButtons.push(this);
                        }

                        Rectangle clip = g.getClip();
                        g.setClip(getX(), getY(), getWidth(), getHeight());

                        super.render(container, g);

                        g.setClip(clip);

                        if (isMouseOver()) {
                                g.setColor(Game.renderer.TERRAIN_GLASS_COLOR);
                        } else {
                                g.setColor(Game.renderer.TERRAIN_SILVER_COLOR);
                        }

                        if (shape != null) {
                                g.drawRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
                        }
                }
        }

        public static String addLineBreaks(String string, int width) {
                int w = 0;
                int lastSpaceIndex = -1;

                StringBuilder sb = new StringBuilder(string);

                for (int i = 0; i < string.length(); ++i) {
                        if (string.charAt(i) == ' ') {
                                lastSpaceIndex = i;
                        }

                        w += Game.renderer.font.getWidth(Character.toString(string.charAt(i)));

                        if (w > width) {
                                if (lastSpaceIndex != -1) {
                                        sb.setCharAt(lastSpaceIndex, '\n');

                                        int newWidth = 0;
                                        for (int j = lastSpaceIndex + 1; j <= i; ++j) {
                                                newWidth += Game.renderer.font.getWidth(Character.toString(string.charAt(j)));
                                        }

                                        lastSpaceIndex = -1;
                                        w = newWidth;
                                } else {
                                        sb.insert(i, '\n');
                                        lastSpaceIndex = -1;
                                        w = 0;
                                }
                        }
                }

                return sb.toString();
        }

        class SideMenu {
                public MyButton removeButton;

                public Vector2i menuPos;
                public Vector2i menuDim;

                public SideMenu(){
                        try {
                                String remove = "Remove Structure";
                                Image removeButtonImage = new Image(Game.renderer.font.getWidth(remove) + 10, Game.renderer.font.getHeight(remove) + 10);
                                removeButtonImage.getGraphics().setFont(Game.renderer.font);
                                removeButtonImage.getGraphics().drawString(remove, 5, 5);
                                removeButtonImage.getGraphics().flush();

                                Rectangle removeRect = new Rectangle(Game.renderer.stageDimensions.x + 10, Game.WIN_HEIGHT - 90,
                                        removeButtonImage.getWidth(), removeButtonImage.getHeight());

                                removeButton = new MyButton(
                                        "Remove selected structure",
                                        Game.appgc,
                                        removeButtonImage,
                                        removeRect);

                                removeButton.addListener(
                                        (comp) -> {
                                                if (menuState == SideMenuState.SELECTING) {
                                                        int particleX = Game.renderer.stagePosition.x + selectedStructure.position.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                        int particleW = selectedStructure.dimensions.x * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                        int particleY = Game.renderer.stagePosition.y + selectedStructure.position.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;
                                                        int particleH = selectedStructure.dimensions.y * Game.PIXELS_PER_TILE * Game.PIXEL_SCALE;

                                                        selectedStructure.remove();
                                                        selectedStructure = null;
                                                        menuState = SideMenuState.OFF;

                                                        Game.renderer.spawnParticlesInArea(
                                                                particleX, particleY,
                                                                particleW, particleH,
                                                                40, 1, 20,
                                                                Color.darkGray, 1f);

                                                        Game.renderer.spawnParticlesInArea(
                                                                particleX, particleY,
                                                                particleW, particleH,
                                                                40, 1, 20,
                                                                Color.lightGray, 1f);
                                                }
                                        });
                        } catch (SlickException e) {
                                e.printStackTrace();
                        }

                        menuPos = new Vector2i(Game.WIN_WIDTH - Renderer.MENU_WIDTH, Renderer.HEADER_HEIGHT);
                        menuDim = new Vector2i(Renderer.MENU_WIDTH, Game.WIN_HEIGHT - (Renderer.HEADER_HEIGHT + Renderer.FOOTER_HEIGHT));
                }

                public void render(Graphics g) {
                        Structure structure = null;

                        int margin = 10;
                        int lineHeight = Game.renderer.font.getLineHeight() + margin;
                        int lineWidth = menuDim.x - 2 * margin;
                        int leftX = menuPos.x + margin;
                        int lineY = menuPos.y;

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

                        Properties props = StructureLoader.getProperties(structure.type);

                        int thumbSize = 50;
                        Image image = structure.image;
                        if (image != null) {
                                g.setColor(Game.renderer.TERRAIN_DEFAULT_COLOR);
                                g.fillRect(leftX, lineY, thumbSize, thumbSize);
                                g.setColor(Color.white);

                                // Draw the image clipped
                                Rectangle clip = g.getClip();
                                g.setClip(leftX, lineY, thumbSize, thumbSize);
                                if (image.getWidth() < thumbSize && image.getHeight() < thumbSize) {
                                        image.drawCentered(leftX + thumbSize / 2, lineY + thumbSize / 2);
                                } else {
                                        image.draw(leftX + margin, lineY + margin);
                                }
                                g.setClip(clip);

                                g.drawRect(leftX, lineY, thumbSize, thumbSize);
                        }
                        lineY += thumbSize + margin;

                        String name = addLineBreaks(props.getProperty("name"), lineWidth);
                        g.drawString(name, leftX, lineY);
                        lineY += lineHeight;

                        String description = addLineBreaks(props.getProperty("desc"), lineWidth);
                        g.drawString(description, leftX, lineY);
                        lineY += Game.renderer.font.getHeight(description) + margin;

                        g.drawLine(leftX, lineY, leftX + lineWidth, lineY);
                        lineY += margin;

                        if (menuState == SideMenuState.PLACING) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("Build costs:\n");
                                boolean hasCosts = false;
                                for (Resource resource : Resource.values()) {
                                        float value = structure.buildCost.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + value + "\n");
                                                hasCosts = true;
                                        }
                                }

                                if (hasCosts) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        { // producing
                                StringBuilder sb = new StringBuilder();
                                sb.append("Produces (units per sec):\n");
                                boolean produces = false;
                                for (Resource resource : Resource.values()) {
                                        float value = structure.productionOutPerSec.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + value + "\n");
                                                produces = true;
                                        }
                                }

                                if (produces) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        { // raising cap
                                StringBuilder sb = new StringBuilder();
                                sb.append("Raises capacity:\n");
                                boolean raises = false;
                                for (Resource resource : Resource.values()) {
                                        float value = structure.capacityIncrease.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + value + "\n");
                                                raises = true;
                                        }
                                }

                                if (raises) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        { // consumes
                                StringBuilder sb = new StringBuilder();
                                sb.append("Consumes (units per sec):\n");
                                boolean consumes = false;
                                for (Resource resource : Resource.values()) {
                                        float value = structure.productionInPerSec.get(resource);
                                        if (value != 0) {
                                                sb.append(resource.name() + ": " + value + "\n");
                                                consumes = true;
                                        }
                                }

                                if (consumes) {
                                        // Delete last newline char
                                        sb.deleteCharAt(sb.length() - 1);
                                        g.drawString(sb.toString(), leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                }
                        }

                        if (menuState == SideMenuState.SELECTING) {
                                { // refund
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("Refunds when demolished:\n");
                                        boolean refunds = false;
                                        for (Resource resource : Resource.values()) {
                                                float value = structure.refundResources.get(resource);
                                                if (value != 0) {
                                                        sb.append(resource.name() + ": " + value + "\n");
                                                        refunds = true;
                                                }
                                        }

                                        if (refunds) {
                                                // Delete last newline char
                                                sb.deleteCharAt(sb.length() - 1);
                                                g.drawString(sb.toString(), leftX, lineY);
                                                lineY += Game.renderer.font.getHeight(sb.toString()) + margin;
                                        }
                                }

                                g.drawLine(leftX, lineY, leftX + lineWidth, lineY);
                                lineY += margin;

                                // show failure states
                                List<String> failureReasons = new ArrayList<>();
                                switch (structure.state) {
                                        case NoRoadAccess:
                                                failureReasons.add("Pathfinding failure! Not connected to the CPU!");
                                                break;
                                        case NoInputResources:
                                                failureReasons.add("No input signal! Not enough input resources!");
                                                break;
                                        case NoSpareCapacity:
                                                failureReasons.add("Stack overflow error! No spare capacity!");
                                                break;
                                }

                                for (String failure : failureReasons) {
                                        String text = addLineBreaks(failure, lineWidth);
                                        g.drawString(text, leftX, lineY);
                                        lineY += Game.renderer.font.getHeight(text) + margin;
                                }

                                {
                                        if (structure.getRoadAccess() != RoadAccess.NONE) {
                                                String roadAccess = "Quality of wiring to CPU:\n" + structure.getRoadAccess().name();
                                                g.drawString(roadAccess, leftX, lineY);
                                                lineY += Game.renderer.font.getHeight(roadAccess);
                                        }
                                }

                                removeButton.setX(leftX);
                                removeButton.setY(lineY);
                                removeButton.render(Game.appgc, g);

                                { // rect around selected struct
                                        g.setColor(selectionColor);
                                        int selectedX = Game.renderer.stagePosition.x + selectedStructure.position.x * Game.renderer.tileSize;
                                        int selectedY = Game.renderer.stagePosition.y + selectedStructure.position.y * Game.renderer.tileSize;

                                        g.drawRect(selectedX - 5, selectedY - 5,
                                                selectedStructure.dimensions.x * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10,
                                                selectedStructure.dimensions.y * Game.PIXEL_SCALE * Game.PIXELS_PER_TILE + 10);
                                }
                        }
                }
        }
}
