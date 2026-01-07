package hr.bmestric.sevens.ui.service;

import javafx.stage.FileChooser;

import java.io.File;

public class GameFileDialogService {
    public FileChooser createSaveChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Sevens Save Files (*.dat)", "*.dat")
        );

        File gameDir = new File("game");
        if (!gameDir.exists()) {
            // best-effort, ignore result
            gameDir.mkdirs();
        }

        fileChooser.setInitialDirectory(gameDir);
        fileChooser.setInitialFileName("sevens_save.dat");
        return fileChooser;
    }

    public FileChooser createLoadChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Sevens Save Files (*.dat)", "*.dat")
        );

        File gameDir = new File("game");
        if (gameDir.exists()) {
            fileChooser.setInitialDirectory(gameDir);
        }

        return fileChooser;
    }
}
