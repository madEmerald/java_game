package ru.lab5.game.ui;

import ru.lab5.game.model.*;
import ru.lab5.game.movement.MovementMode;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GameWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private final MapPanel mapPanel = new MapPanel();
    private final JComboBox<MapPreset> maps = new JComboBox<>(MapPreset.values());
    private final JComboBox<GameCharacter> characterBox = new JComboBox<>();
    private final JLabel resources = new JLabel();
    private final JLabel status = new JLabel(" ");
    private final JTextArea journal = new JTextArea(8, 80);
    private final JButton walk = new JButton("Ходить");
    private final JButton fly = new JButton("Лететь");
    private final JButton cast = new JButton("Поднять всю группу");
    private final JButton land = new JButton("Завершить заклинание");
    private final DefaultTableModel table = new DefaultTableModel(
            new Object[]{"Персонаж", "Режим", "Клетка", "HP", "Силы", "Время, с"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private WorldMap world;
    private List<GameCharacter> characters;
    private Party party;

    public GameWindow() {
        super("Лабораторная №5 · Strategy · Фэнтезийный отряд");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 810));
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setContentPane(root);
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("ФЭНТЕЗИЙНЫЙ ОТРЯД  /  STRATEGY");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        top.add(title, BorderLayout.NORTH);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.add(new JLabel("Карта:")); toolbar.add(maps);
        JButton reset = new JButton("Начать заново");
        toolbar.add(reset); toolbar.add(new JLabel("Персонаж:")); toolbar.add(characterBox);
        top.add(toolbar, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);
        root.add(mapPanel, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(315, 500));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.add(new JLabel("СПОСОБ ПЕРЕДВИЖЕНИЯ"));
        JPanel modes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modes.add(walk); modes.add(fly); sidebar.add(modes);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(new JLabel("Магия: весь отряд (6 персонажей)"));
        sidebar.add(resources);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(cast); sidebar.add(Box.createVerticalStrut(6)); sidebar.add(land);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(new JLabel("ШАГ В СОСЕДНЮЮ КЛЕТКУ"));
        JPanel arrows = new JPanel(new GridLayout(3, 3, 4, 4));
        arrows.setMaximumSize(new Dimension(210, 110));
        addEmpty(arrows); addDirection(arrows, "↑", 0, -1); addEmpty(arrows);
        addDirection(arrows, "←", -1, 0); addEmpty(arrows); addDirection(arrows, "→", 1, 0);
        addEmpty(arrows); addDirection(arrows, "↓", 0, 1); addEmpty(arrows);
        sidebar.add(arrows);
        sidebar.add(Box.createVerticalStrut(10));
        JLabel hint = new JLabel("<html>Можно нажать соседнюю клетку карты.<br>Жёлтая рамка — выбранный персонаж.<br>Крылья на значке — активный полёт.<br><br>В полёте игнорируются ВСЕ свойства<br>поверхности, включая лечение.<br>Мана закончилась? Полёт сохраняется,<br>но двигаться без маны нельзя.</html>");
        sidebar.add(hint); sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(new JLabel("ЛАНДШАФТЫ"));
        for (TerrainType terrain : TerrainType.values()) {
            JLabel row = new JLabel(terrain.symbol() + "  " + terrain.displayName() + " — "
                    + (terrain.surfaceEffect().healthDelta() == 0 ? "" : "HP "
                    + String.format(Locale.ROOT, "%+d", terrain.surfaceEffect().healthDelta()) + ", ")
                    + (terrain.surfaceEffect().walkable() ? "v×" + terrain.surfaceEffect().speedMultiplier() : "только полёт"));
            row.setForeground(MapPanel.terrainColor(terrain).darker());
            sidebar.add(row);
        }
        for (Component component : sidebar.getComponents()) {
            if (component instanceof JComponent swingComponent) swingComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        JScrollPane sidebarScroll = new JScrollPane(sidebar);
        sidebarScroll.setBorder(BorderFactory.createEmptyBorder());
        sidebarScroll.setPreferredSize(new Dimension(330, 500));
        root.add(sidebarScroll, BorderLayout.EAST);
        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        JTable characterTable = new JTable(table);
        characterTable.setRowHeight(22);
        JScrollPane tableScroll = new JScrollPane(characterTable);
        tableScroll.setPreferredSize(new Dimension(900, 160));
        bottom.add(tableScroll, BorderLayout.NORTH);
        journal.setEditable(false);
        journal.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        journal.setLineWrap(true); journal.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(journal);
        logScroll.setPreferredSize(new Dimension(900, 110));
        bottom.add(logScroll, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        bottom.add(status, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        maps.addActionListener(event -> resetWorld());
        reset.addActionListener(event -> resetWorld());
        characterBox.addActionListener(event -> refresh());
        walk.addActionListener(event -> act(() -> { selected().setMovementMode(MovementMode.WALK); log(selected() + ": ходьба"); }));
        fly.addActionListener(event -> act(() -> { selected().setMovementMode(MovementMode.FLY); log(selected() + ": полёт"); }));
        cast.addActionListener(event -> act(() -> { party.castFlight(world); log("Группа поднята магией; мана -" + Party.CAST_MANA); }));
        land.addActionListener(event -> act(() -> party.endFlight(world).forEach(this::log)));
        mapPanel.onCellClick(target -> act(() -> moveTo(target)));
        bindKey("UP", 0, -1); bindKey("DOWN", 0, 1); bindKey("LEFT", -1, 0); bindKey("RIGHT", 1, 0);
        resetWorld();
        pack(); setSize(1320, 1000); setLocationRelativeTo(null);
    }
    private static void addEmpty(JPanel panel) { panel.add(new JLabel()); }
    private void addDirection(JPanel panel, String label, int dx, int dy) {
        JButton button = new JButton(label);
        button.addActionListener(event -> moveBy(dx, dy)); panel.add(button);
    }
    private void bindKey(String key, int dx, int dy) {
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt " + key), key);
        root.getActionMap().put(key, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override public void actionPerformed(ActionEvent event) { moveBy(dx, dy); }
        });
    }
    private GameCharacter selected() { return (GameCharacter) characterBox.getSelectedItem(); }
    private void resetWorld() {
        MapPreset preset = (MapPreset) maps.getSelectedItem();
        if (preset == null) return;
        world = preset.createMap();
        characters = new ArrayList<>();
        String[] names = {"Гром", "Бруг", "Лиэль", "Астра", "Нокс", "Эхо"};
        for (Species species : Species.values()) characters.add(new GameCharacter(names[species.ordinal()], species, preset.start()));
        party = new Party(characters, 160);
        characterBox.setModel(new DefaultComboBoxModel<>(characters.toArray(GameCharacter[]::new)));
        journal.setText(""); log("Новая карта: " + preset + ". Все персонажи находятся в " + preset.start());
        log("Alt + стрелки — шаг. Без заклинания движется один персонаж, с заклинанием — весь отряд.");
        refresh();
    }
    private void moveBy(int dx, int dy) {
        if (selected() == null) return;
        Position current = selected().position();
        act(() -> moveTo(new Position(current.x() + dx, current.y() + dy)));
    }
    private void moveTo(Position target) {
        if (party.isFlying()) {
            PartyMoveResult result = party.moveTo(world, target);
            result.moves().forEach(move -> log(move.toString()));
            log("Мана группы -" + result.manaSpent());
        } else log(selected().moveTo(world, target).toString());
    }
    private void act(Runnable action) {
        try {
            action.run(); status.setForeground(new Color(35, 100, 65)); status.setText("Действие выполнено");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            status.setForeground(new Color(160, 45, 40)); status.setText(exception.getMessage());
            log("ОТКАЗ: " + exception.getMessage());
        }
        refresh();
    }
    private void log(String text) {
        journal.append(text + "\n"); journal.setCaretPosition(journal.getDocument().getLength());
    }
    private void refresh() {
        if (characters == null || party == null || selected() == null) return;
        GameCharacter actor = selected();
        mapPanel.setScene(world, characters, actor);
        resources.setText("Мана: " + party.mana() + "; шаг группы: " + party.nextMoveManaCost());
        walk.setEnabled(!party.isFlying() && actor.availableModes().contains(MovementMode.WALK));
        fly.setEnabled(!party.isFlying() && actor.availableModes().contains(MovementMode.FLY));
        cast.setEnabled(!party.isFlying()); land.setEnabled(party.isFlying());
        table.setRowCount(0);
        for (GameCharacter character : characters) {
            table.addRow(new Object[]{character.toString(), character.movementMode(), character.position(),
                    character.health(), String.format(Locale.ROOT, "%.2f", character.stamina()),
                    String.format(Locale.ROOT, "%.2f", character.elapsedSeconds())});
        }
    }
}
