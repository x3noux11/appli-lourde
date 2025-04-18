package fr.ram.creche.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CrecheApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        CrecheApp.primaryStage = primaryStage;
        primaryStage.setTitle("Gestion de Crèche RAM");
        
        try {
            // Charger la vue principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/ram/creche/ui/fxml/MainView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 700);
            
            // Charger les styles CSS
            String css = getClass().getResource("/fr/ram/creche/ui/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
