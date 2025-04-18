package fr.ram.creche.ui.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import fr.ram.creche.dao.BaseDeDonnees;
import fr.ram.creche.model.Parent;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ParentsController implements Initializable {

    @FXML private TableView<Parent> tableParents;
    @FXML private TableColumn<Parent, String> colPrenom;
    @FXML private TableColumn<Parent, String> colNom;
    @FXML private TableColumn<Parent, String> colTelephone;
    @FXML private TableColumn<Parent, String> colEmail;
    @FXML private TableColumn<Parent, Integer> colNombreEnfants;
    @FXML private TableColumn<Parent, Parent> colActions;
    @FXML private TextField txtRecherche;
    @FXML private Label lblNombreTotal;
    @FXML private Label lblMoyenneEnfants;
    
    private BaseDeDonnees baseDeDonnees;
    private ObservableList<Parent> listeParents;
    private FilteredList<Parent> listeParentsFiltree;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser la base de donnu00e9es
        baseDeDonnees = BaseDeDonnees.getInstance();
        
        // Initialiser les colonnes du tableau
        initialiserColonnes();
        
        // Charger les donnu00e9es
        chargerDonnees();
        
        // Initialiser la recherche
        initialiserRecherche();
    }
    
    private void initialiserColonnes() {
        colPrenom.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPrenom()));
        
        colNom.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNom()));
        
        colTelephone.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTelephone()));
        
        colEmail.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        
        colNombreEnfants.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getEnfants().size()).asObject());
        
        // Colonne d'actions
        colActions.setCellFactory(column -> new TableCell<Parent, Parent>() {
            private final Button btnDetails = new Button("Du00e9tails");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnDetails, btnSupprimer);
            
            {
                btnDetails.getStyleClass().add("button");
                btnSupprimer.getStyleClass().add("button-danger");
                
                // Action pour le bouton Du00e9tails
                btnDetails.setOnAction(event -> {
                    Parent parent = getTableView().getItems().get(getIndex());
                    afficherDetailsParent(parent);
                });
                
                // Action pour le bouton Supprimer
                btnSupprimer.setOnAction(event -> {
                    Parent parent = getTableView().getItems().get(getIndex());
                    supprimerParent(parent);
                });
            }
            
            @Override
            protected void updateItem(Parent parent, boolean empty) {
                super.updateItem(parent, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }
    
    private void chargerDonnees() {
        List<Parent> parents = baseDeDonnees.getAllParents();
        listeParents = FXCollections.observableArrayList(parents);
        listeParentsFiltree = new FilteredList<>(listeParents, p -> true);
        tableParents.setItems(listeParentsFiltree);
        
        // Mettre u00e0 jour les statistiques
        actualiserStatistiques();
    }
    
    private void initialiserRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            listeParentsFiltree.setPredicate(parent -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String searchKeyword = newValue.toLowerCase();
                
                if (parent.getPrenom().toLowerCase().contains(searchKeyword)) {
                    return true;
                }
                if (parent.getNom().toLowerCase().contains(searchKeyword)) {
                    return true;
                }
                if (parent.getEmail().toLowerCase().contains(searchKeyword)) {
                    return true;
                }
                if (parent.getTelephone().toLowerCase().contains(searchKeyword)) {
                    return true;
                }
                
                return false;
            });
            
            // Actualiser les statistiques apru00e8s filtrage
            actualiserStatistiques();
        });
    }
    
    private void actualiserStatistiques() {
        // Nombre total de parents dans la liste filtru00e9e
        int nombreTotal = listeParentsFiltree.size();
        lblNombreTotal.setText("Nombre total de parents: " + nombreTotal);
        
        // Moyenne d'enfants par parent
        if (nombreTotal > 0) {
            double sommeEnfants = listeParentsFiltree.stream()
                    .mapToInt(parent -> parent.getEnfants().size())
                    .sum();
            double moyenneEnfants = sommeEnfants / nombreTotal;
            lblMoyenneEnfants.setText(String.format("Moyenne d'enfants par parent: %.1f", moyenneEnfants));
        } else {
            lblMoyenneEnfants.setText("Moyenne d'enfants par parent: -");
        }
    }
    
    @FXML
    private void ajouterParent() {
        // Cette fonctionnalitu00e9 pourrait u00eatre implu00e9mentu00e9e dans une version future
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Fonctionnalitu00e9 u00e0 venir");
        alert.setContentText("L'ajout de parent sera disponible dans une version future.");
        alert.showAndWait();
    }
    
    private void afficherDetailsParent(Parent parent) {
        // Pour l'instant, on affiche un message d'information simple
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Du00e9tails du parent");
        alert.setHeaderText(parent.getPrenom() + " " + parent.getNom());
        
        StringBuilder sb = new StringBuilder();
        sb.append("Adresse: ").append(parent.getAdresse()).append("\n");
        sb.append("Tu00e9lu00e9phone: ").append(parent.getTelephone()).append("\n");
        sb.append("Email: ").append(parent.getEmail()).append("\n\n");
        
        sb.append("Enfants:\n");
        if (parent.getEnfants().isEmpty()) {
            sb.append("Aucun enfant enregistru00e9");
        } else {
            parent.getEnfants().forEach(enfant -> 
                sb.append("- ").append(enfant.getPrenom()).append(" ").append(enfant.getNom()).append("\n"));
        }
        
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
    
    private void supprimerParent(Parent parent) {
        // Vu00e9rifier si le parent a des enfants
        if (!parent.getEnfants().isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setHeaderText("Impossible de supprimer");
            alert.setContentText("Ce parent a des enfants enregistru00e9s. Veuillez d'abord supprimer ses enfants.");
            alert.showAndWait();
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer un parent");
        alert.setContentText("u00cates-vous su00fbr de vouloir supprimer " + parent.getPrenom() + " " + parent.getNom() + " ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean succes = baseDeDonnees.supprimerParent(parent.getId());
                if (succes) {
                    // Rafrau00eechir la liste
                    chargerDonnees();
                } else {
                    Alert erreur = new Alert(AlertType.ERROR);
                    erreur.setTitle("Erreur");
                    erreur.setHeaderText(null);
                    erreur.setContentText("Impossible de supprimer ce parent.");
                    erreur.showAndWait();
                }
            }
        });
    }
}
