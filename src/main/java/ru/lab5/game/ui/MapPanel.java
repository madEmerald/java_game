package ru.lab5.game.ui;

import ru.lab5.game.model.*;
import ru.lab5.game.movement.MovementMode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Consumer;

/** Только представление: решения о движении принимает модель. */
public final class MapPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private WorldMap map;
    private List<GameCharacter> characters = List.of();
    private GameCharacter selected;
    private Consumer<Position> onCellClick = position -> { };

    public MapPanel() {
        setPreferredSize(new Dimension(760, 550));
        setBackground(new Color(20, 28, 37));
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) {
                Position cell = cellAt(event.getX(), event.getY());
                if (cell != null) onCellClick.accept(cell);
            }
        });
    }
    public void setScene(WorldMap map, List<GameCharacter> characters, GameCharacter selected) {
        this.map = map;
        this.characters = List.copyOf(characters);
        this.selected = selected;
        repaint();
    }
    public void onCellClick(Consumer<Position> handler) { onCellClick = handler; }
    public static Color terrainColor(Terrain terrain) {
        if (!(terrain instanceof TerrainType type)) return new Color(100, 110, 115);
        return switch (type) {
            case PLAIN -> new Color(85, 125, 78);
            case ROAD -> new Color(166, 140, 99);
            case FOREST -> new Color(41, 83, 65);
            case SWAMP -> new Color(80, 92, 60);
            case ICE -> new Color(108, 169, 183);
            case LAVA -> new Color(153, 63, 43);
            case GROVE -> new Color(70, 135, 110);
            case CHASM -> new Color(24, 31, 43);
        };
    }
    private int tileSize() {
        if (map == null) return 1;
        return Math.max(1, Math.min((getWidth() - 32) / map.width(), (getHeight() - 32) / map.height()));
    }
    private int left() { return (getWidth() - map.width() * tileSize()) / 2; }
    private int top() { return (getHeight() - map.height() * tileSize()) / 2; }
    private Position cellAt(int px, int py) {
        if (map == null || px < left() || py < top()) return null;
        Position cell = new Position((px - left()) / tileSize(), (py - top()) / tileSize());
        return map.contains(cell) ? cell : null;
    }
    @Override public String getToolTipText(MouseEvent event) {
        Position position = cellAt(event.getX(), event.getY());
        if (position == null) return null;
        Terrain terrain = map.terrainAt(position);
        return position + " " + terrain.displayName() + ": " + terrain.surfaceEffect().description();
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (map == null) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = tileSize();
        int ox = left(), oy = top();
        for (int y = 0; y < map.height(); y++) {
            for (int x = 0; x < map.width(); x++) {
                Position position = new Position(x, y);
                Terrain terrain = map.terrainAt(position);
                int px = ox + x * size, py = oy + y * size;
                g.setColor(terrainColor(terrain));
                g.fillRoundRect(px + 1, py + 1, size - 2, size - 2, 8, 8);
                g.setColor(new Color(255, 255, 255, 28));
                g.setStroke(new BasicStroke(2));
                g.drawLine(px + size / 5, py + size * 2 / 3, px + size * 4 / 5, py + size / 3);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, Math.max(12, size / 4)));
                g.setColor(new Color(255, 255, 255, 130));
                g.drawString(String.valueOf(terrain.symbol()), px + 7, py + Math.max(16, size / 4));
                if (selected != null && selected.position().isAdjacentTo(position)) {
                    g.setColor(new Color(240, 222, 157, 130));
                    g.setStroke(new BasicStroke(1));
                    g.drawRoundRect(px + 4, py + 4, size - 9, size - 9, 7, 7);
                }
                long occupants = characters.stream().filter(c -> c.position().equals(position)).count();
                if (occupants > 0) {
                    GameCharacter visible = selected != null && selected.position().equals(position)
                            ? selected : characters.stream().filter(c -> c.position().equals(position)).findFirst().orElseThrow();
                    boolean airborne = visible.movementMode() != MovementMode.WALK;
                    int diameter = Math.max(15, size / 2);
                    int cx = px + size / 2, cy = py + size / 2 + 3;
                    if (airborne) {
                        g.setColor(new Color(218, 237, 255));
                        g.fillPolygon(new int[]{cx - diameter, cx, cx + diameter},
                                new int[]{cy - 7, cy + 6, cy - 7}, 3);
                        g.setColor(new Color(0, 0, 0, 55));
                        g.fillOval(cx - diameter / 2, py + size - 12, diameter, 6);
                    }
                    g.setColor(visible.isAlive() ? new Color(242, 226, 177) : new Color(170, 102, 102));
                    g.fillOval(cx - diameter / 2, cy - diameter / 2, diameter, diameter);
                    g.setColor(new Color(32, 40, 50));
                    String marker = visible.species().marker();
                    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(12, size / 4)));
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(marker, cx - fm.stringWidth(marker) / 2, cy + fm.getAscent() / 2 - 2);
                    if (occupants > 1) {
                        g.setColor(Color.WHITE);
                        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                        g.drawString("+" + (occupants - 1), px + size - 22, py + size - 8);
                    }
                }
            }
        }
        if (selected != null && map.contains(selected.position())) {
            int px = ox + selected.position().x() * size;
            int py = oy + selected.position().y() * size;
            g.setColor(new Color(247, 220, 133));
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(px + 2, py + 2, size - 5, size - 5, 8, 8);
        }
        g.dispose();
    }
}
