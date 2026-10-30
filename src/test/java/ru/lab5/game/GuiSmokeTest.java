package ru.lab5.game;

import ru.lab5.game.ui.GameWindow;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

/** Дополнительный тест окна; требует рабочий дисплей, не запускается build.sh. */
public final class GuiSmokeTest {
    private GuiSmokeTest() { }
    private static <T extends Component> T find(Container root, Class<T> type, java.util.function.Predicate<T> predicate) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component) && predicate.test(type.cast(component))) return type.cast(component);
            if (component instanceof Container container) {
                T nested = find(container, type, predicate);
                if (nested != null) return nested;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless()) throw new IllegalStateException("Для теста нужен дисплей");
        GameWindow[] reference = new GameWindow[1];
        SwingUtilities.invokeAndWait(() -> {
            GameWindow window = new GameWindow(); reference[0] = window; window.setLocation(35, 35); window.setVisible(true);
            JButton cast = find(window, JButton.class, button -> button.getText().equals("Поднять всю группу"));
            JButton right = find(window, JButton.class, button -> button.getText().equals("→"));
            JButton land = find(window, JButton.class, button -> button.getText().equals("Завершить заклинание"));
            if (cast == null || right == null || land == null) throw new AssertionError("Не найдены элементы управления");
            cast.doClick(); right.doClick(); land.doClick(); cast.doClick();
            JTable table = find(window, JTable.class, item -> true);
            if (table == null || table.getRowCount() != 6) throw new AssertionError("Неверная таблица");
            for (int row = 0; row < 6; row++) {
                if (!table.getValueAt(row, 1).toString().equals("Магический полёт")) throw new AssertionError("Не применена стратегия");
                if (!table.getValueAt(row, 2).toString().equals("(1, 3)")) throw new AssertionError("Группа не переместилась");
            }
            JLabel mana = find(window, JLabel.class, label -> label.getText() != null && label.getText().startsWith("Мана:"));
            if (mana == null || !mana.getText().contains("128")) throw new AssertionError("Неверное списание маны");
        });
        Thread.sleep(500);
        GameWindow window = reference[0];
        if (args.length > 0) {
            ImageIO.write(new Robot().createScreenCapture(new Rectangle(window.getLocationOnScreen(), window.getSize())),
                    "png", new File(args[0]));
        }
        SwingUtilities.invokeAndWait(window::dispose);
        System.out.println("PASS GUI: окно открыто; группа взлетела, переместилась, приземлилась и снова взлетела; ресурсы проверены.");
    }
}
