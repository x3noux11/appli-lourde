package fr.ram.creche.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import fr.ram.creche.dao.BaseDeDonnees;
import fr.ram.creche.model.Enfant;
import fr.ram.creche.model.Parent;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EnfantsController implements Initializable {

    @FXML private TableView<Enfant> tableEnfants;
    @FXML private TableColumn<Enfant, String> colPrenom;
    @FXML private TableColumn<Enfant, String> colNom;
    @FXML private TableColumn<Enfant, LocalDate> colDateNaissance;
    @FXML private TableColumn<Enfant, Integer> colAge;
    @FXML private TableColumn<Enfant, Parent> colParent;
    @FXML private TableColumn<Enfant, String> colObservations;
    @FXML private TableColumn<Enfant, Enfant> colActions;
    @FXML private TextField txtRecherche;
    @FXML private Label lblNombreTotal;
    @FXML private Label lblMoyenneAge;
    
    private BaseDeDonnees baseDeDonnees;
    private ObservableList<Enfant> listeEnfants;
    private FilteredList<Enfant> listeEnfantsFiltree;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser la base de données
        baseDeDonnees = BaseDeDonnees.getInstance();
        
        // Initialiser les colonnes du tableau
        initialiserColonnes();
        
        // Charger les données
        chargerDonnees();
        
        // Initialiser la recherche
        initialiserRecherche();
    }
    
    private void initialiserColonnes() {
        colPrenom.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPrenom()));
        
        colNom.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNom()));
        
        colDateNaissance.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDateNaissance()));
        
        colDateNaissance.setCellFactory(column -> new TableCell<Enfant, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER));
                }
            }
        });
        
        colAge.setCellValueFactory(cellData -> {
            LocalDate dateNaissance = cellData.getValue().getDateNaissance();
            int age = Period.between(dateNaissance, LocalDate.now()).getYears();
            return new SimpleIntegerProperty(age).asObject();
        });
        
        colParent.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getParent()));
        
        colParent.setCellFactory(column -> new TableCell<Enfant, Parent>() {
            @Override
            protected void updateItem(Parent parent, boolean empty) {
                super.updateItem(parent, empty);
                if (empty || parent == null) {
                    setText(null);
                } else {
                    setText(parent.getPrenom() + " " + parent.getNom());
                }
            }
        });
        
        colObservations.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getObservations()));
        
        // Colonne d'actions
        colActions.setCellFactory(column -> new TableCell<Enfant, Enfant>() {
            private final Button btnDetails = new Button("Détails");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox pane = new HBox(5, btnDetails, btnSupprimer);
            
            {
                btnDetails.getStyleClass().add("button");
                btnSupprimer.getStyleClass().add("button-danger");
                
                // Action pour le bouton Détails
                btnDetails.setOnAction(event -> {
                    Enfant enfant = getTableView().getItems().get(getIndex());
                    afficherDetailsEnfant(enfant);
                });
                
                // Action pour le bouton Supprimer
                btnSupprimer.setOnAction(event -> {
                    Enfant enfant = getTableView().getItems().get(getIndex());
                    supprimerEnfant(enfant);
                });
            }
            
            @Override
            protected void updateItem(Enfant enfant, boolean empty) {
                super.updateItem(enfant, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }
    
    private void chargerDonnees() {
        List<Enfant> enfants = baseDeDonnees.getAllEnfants();
        listeEnfants = FXCollections.observableArrayList(enfants);
        listeEnfantsFiltree = new FilteredList<>(listeEnfants, p -> true);
        tableEnfants.setItems(listeEnfantsFiltree);
        
        // Mettre à jour les statistiques
        actualiserStatistiques();
    }
    
    private void initialiserRecherche() {
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            listeEnfantsFiltree.setPredicate(enfant -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String searchKeyword = newValue.toLowerCase();
                
                if (enfant.getPrenom().toLowerCase().contains(searchKeyword)) {
                    return true;
                }
                if (enfant.getNom().toLowerCase().contains(searchKeyword)) {
                    return true;
                }
                if (enfant.getParent() != null && 
                    (enfant.getParent().getNom().toLowerCase().contains(searchKeyword) ||
                     enfant.getParent().getPrenom().toLowerCase().contains(searchKeyword))) {
                    return true;
                }
                
                return false;
            });
            
            // Actualiser les statistiques après filtrage
            actualiserStatistiques();
        });
    }
    
    private void actualiserStatistiques() {
        // Nombre total d'enfants dans la liste filtrée
        int nombreTotal = listeEnfantsFiltree.size();
        lblNombreTotal.setText("Nombre total d'enfants : " + nombreTotal);
        
        // Âge moyen
        if (nombreTotal > 0) {
            double sommesAges = listeEnfantsFiltree.stream()
                    .mapToInt(enfant -> Period.between(enfant.getDateNaissance(), LocalDate.now()).getYears())
                    .sum();
            double moyenneAge = sommesAges / nombreTotal;
            lblMoyenneAge.setText(String.format("Âge moyen : %.1f ans", moyenneAge));
        } else {
            lblMoyenneAge.setText("Âge moyen : - ans");
        }
    }
    
    @FXML
    private void ajouterEnfant() {
        // Cette fonctionnalité pourrait être implémentée dans une version future
        // Pour l'instant, on affiche un message
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("L'ajout d'enfant sera disponible dans une version future.");
        alert.showAndWait();
    }
    
    private void afficherDetailsEnfant(Enfant enfant) {
        // Cette fonctionnalité pourrait être implémentée dans une version future
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Détails de l'enfant");
        alert.setHeaderText(enfant.getPrenom() + " " + enfant.getNom());
        
        StringBuilder sb = new StringBuilder();
        sb.append("Date de naissance : ").append(enfant.getDateNaissance().format(DATE_FORMATTER)).append("\n");
        sb.append("Âge : ").append(Period.between(enfant.getDateNaissance(), LocalDate.now()).getYears()).append(" ans\n");
        sb.append("Parent : ").append(enfant.getParent().getPrenom()).append(" ").append(enfant.getParent().getNom()).append("\n");
        
        if (enfant.getObservations() != null && !enfant.getObservations().isEmpty()) {
            sb.append("Observations : ").append(enfant.getObservations());
        }
        
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
    
    private void supprimerEnfant(Enfant enfant) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer un enfant");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + enfant.getPrenom() + " " + enfant.getNom() + " ?\n" +
                             "Cette action supprimera également toutes les réservations associées à cet enfant.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean succes = baseDeDonnees.supprimerEnfant(enfant.getId());
                if (succes) {
                    // Rafraîchir la liste
                    chargerDonnees();
                } else {
                    Alert erreur = new Alert(AlertType.ERROR);
                    erreur.setTitle("Erreur");
                    erreur.setHeaderText(null);
                    erreur.setContentText("Impossible de supprimer cet enfant.");
                    erreur.showAndWait();
                }
            }
        });
    }
}
