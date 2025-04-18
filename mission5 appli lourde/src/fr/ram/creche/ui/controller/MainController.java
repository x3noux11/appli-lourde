package fr.ram.creche.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnPlanning;
    @FXML private Button btnEnfants;
    @FXML private Button btnParents;
    @FXML private Button btnReservations;
    @FXML private Button btnConfiguration;
    
    private Button selectedButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Au démarrage, afficher le planning
        selectedButton = btnPlanning;
        selectButton(selectedButton);
        afficherPlanning();
    }
    
    /**
     * Change le style du bouton sélectionné dans le menu latéral
     */
    private void selectButton(Button button) {
        // Réinitialiser tous les boutons
        btnPlanning.getStyleClass().remove("active");
        btnEnfants.getStyleClass().remove("active");
        btnParents.getStyleClass().remove("active");
        btnReservations.getStyleClass().remove("active");
        btnConfiguration.getStyleClass().remove("active");
        
        // Sélectionner le bouton actif
        button.getStyleClass().add("active");
        selectedButton = button;
    }
    
    /**
     * Charge une vue FXML dans le contentArea
     */
    private void chargerVue(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vue = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void afficherPlanning() {
        selectButton(btnPlanning);
        chargerVue("/fr/ram/creche/ui/fxml/PlanningView.fxml");
    }
    
    @FXML
    public void afficherEnfants() {
        selectButton(btnEnfants);
        chargerVue("/fr/ram/creche/ui/fxml/EnfantsView.fxml");
    }
    
    @FXML
    public void afficherParents() {
        selectButton(btnParents);
        chargerVue("/fr/ram/creche/ui/fxml/ParentsView.fxml");
    }
    
    @FXML
    public void afficherReservations() {
        selectButton(btnReservations);
        chargerVue("/fr/ram/creche/ui/fxml/ReservationsView.fxml");
    }
    
    @FXML
    public void afficherConfiguration() {
        selectButton(btnConfiguration);
        chargerVue("/fr/ram/creche/ui/fxml/ConfigurationView.fxml");
    }
}
