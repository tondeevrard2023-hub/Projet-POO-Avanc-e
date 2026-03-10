package managerui;

import javafx.scene.Scene;

public class Theme {
    private static Scene scene;
    private static String currentTheme;

    public static void setScene(Scene scene) {
        Theme.scene = scene;
    }

    public static void apply(String theme) {
        if (scene == null) return;

        scene.getStylesheets().removeIf(s -> s.contains("light") || s.contains("dark"));

        String cssFile = "/ressources/style/" + theme + ".css";
        try {
            scene.getStylesheets().add(Theme.class.getResource(cssFile).toExternalForm());
            currentTheme = theme;
        } catch (Exception e) {
            System.err.println("Impossible de charger le thème : " + cssFile);
            e.printStackTrace();
        }
        
    }

    public static String getCurrentTheme() {
        return currentTheme;
    }
}