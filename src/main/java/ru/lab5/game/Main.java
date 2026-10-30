package ru.lab5.game;

import ru.lab5.game.ui.GameWindow;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        if ((args.length > 0 && args[0].equals("--demo")) || GraphicsEnvironment.isHeadless()) {
            Demo.main(args);
        } else {
            SwingUtilities.invokeLater(() -> new GameWindow().setVisible(true));
        }
    }
}
