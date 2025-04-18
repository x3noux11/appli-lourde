package fr.ram.creche.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import fr.ram.creche.dao.BaseDeDonnees;
import fr.ram.creche.model.Enfant;
import fr.ram.creche.model.Parent;
import fr.ram.creche.model.Reservation;
import fr.ram.creche.model.Reservation.TypeAccueil;
import fr.ram.creche.service.GestionnaireReservations;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReservationsController implements Initializable {

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, TypeAccueil> colType;
    @FXML private TableColumn<Reservation, Enfant> colEnfant;
    @FXML private TableColumn<Reservation, Parent> colParent;
    @FXML private TableColumn<Reservation, LocalDate> colDate;
    @FXML private TableColumn<Reservation, String> colHoraires;
    @FXML private TableColumn<Reservation, Double> colTarif;
    @FXML private TableColumn<Reservation, Boolean> colStatut;
    @FXML private TableColumn<Reservation, Reservation> colActions;
    @FXML private ComboBox<String> cmbFiltre;
    @FXML private DatePicker dateFiltre;
    @FXML private TextField txtRecherche;
    @FXML private Label lblTotal;
    @FXML private Label lblTotalRegulier;
    @FXML private Label lblTotalOccasionnel;
    @FXML private Label lblRevenuTotal;
    
    private BaseDeDonnees baseDeDonnees;
    private GestionnaireReservations gestionnaireReservations;
    private ObservableList<Reservation> listeReservations;
    private FilteredList<Reservation> listeReservationsFiltree;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les services
        baseDeDonnees = BaseDeDonnees.getInstance();
        gestionnaireReservations = GestionnaireReservations.getInstance();
        
        // Initialiser les filtres
        initialiserFiltres();
        
        // Initialiser les colonnes du tableau
        initialiserColonnes();
        
        // Charger les données
        chargerDonnees();
    }
    
    private void initialiserFiltres() {
        // Options de filtre pour le type d'accueil
        cmbFiltre.setItems(FXCollections.observableArrayList(
                "Tous les types",
                TypeAccueil.REGULIER.getLibelle(),
                TypeAccueil.OCCASIONNEL.getLibelle()
        ));
        cmbFiltre.getSelectionModel().selectFirst();
        
        // Ecouteurs pour les filtres
        cmbFiltre.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        dateFiltre.valueProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
    }
    
    private void initialiserColonnes() {
        // Type d'accueil
        colType.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getTypeAccueil()));
        
        colType.setCellFactory(column -> new TableCell<Reservation, TypeAccueil>() {
            @Override
            protected void updateItem(TypeAccueil type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                } else {
                    setText(type.getLibelle());
                    // On peut ajouter un style selon le type
                    if (type == TypeAccueil.REGULIER) {
                        getStyleClass().add("regulier");
                    } else {
                        getStyleClass().add("occasionnel");
                    }
                }
            }
        });
        
        // Enfant
        colEnfant.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getEnfant()));
        
        colEnfant.setCellFactory(column -> new TableCell<Reservation, Enfant>() {
            @Override
            protected void updateItem(Enfant enfant, boolean empty) {
                super.updateItem(enfant, empty);
                if (empty || enfant == null) {
                    setText(null);
                } else {
                    setText(enfant.getPrenom() + " " + enfant.getNom());
                }
            }
        });
        
        // Parent
        colParent.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getEnfant().getParent()));
        
        colParent.setCellFactory(column -> new TableCell<Reservation, Parent>() {
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
        
        // Date
        colDate.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDate()));
        
        colDate.setCellFactory(column -> new TableCell<Reservation, LocalDate>() {
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
        
        // Horaires
        colHoraires.setCellValueFactory(cellData -> {
            Reservation res = cellData.getValue();
            String horaires = res.getHeureDebut().format(TIME_FORMATTER) + " - " + 
                            res.getHeureFin().format(TIME_FORMATTER);
            return new SimpleStringProperty(horaires);
        });
        
        // Tarif
        colTarif.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getTarif()));
        
        colTarif.setCellFactory(column -> new TableCell<Reservation, Double>() {
            @Override
            protected void updateItem(Double tarif, boolean empty) {
                super.updateItem(tarif, empty);
                if (empty || tarif == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", tarif));
                }
            }
        });
        
        // Statut
        colStatut.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().isConfirme()));
        
        colStatut.setCellFactory(column -> new TableCell<Reservation, Boolean>() {
            @Override
            protected void updateItem(Boolean confirme, boolean empty) {
                super.updateItem(confirme, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(confirme ? "Confirmée" : "En attente");
                    getStyleClass().removeAll("alert-success", "alert-warning");
                    getStyleClass().add(confirme ? "alert-success" : "alert-warning");
                }
            }
        });
        
        // Actions
        colActions.setCellFactory(column -> new TableCell<Reservation, Reservation>() {
            private final Button btnDetails = new Button("Détails");
            private final Button btnAnnuler = new Button("Annuler");
            private final HBox pane = new HBox(5, btnDetails, btnAnnuler);
            
            {
                btnDetails.getStyleClass().add("button");
                btnAnnuler.getStyleClass().add("button-danger");
                
                // Action pour le bouton Détails
                btnDetails.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    afficherDetailsReservation(reservation);
                });
                
                // Action pour le bouton Annuler
                btnAnnuler.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    annulerReservation(reservation);
                });
            }
            
            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }
    
    private void chargerDonnees() {
        List<Reservation> reservations = baseDeDonnees.getAllReservations();
        listeReservations = FXCollections.observableArrayList(reservations);
        listeReservationsFiltree = new FilteredList<>(listeReservations, p -> true);
        tableReservations.setItems(listeReservationsFiltree);
        
        // Mettre à jour les statistiques
        actualiserStatistiques();
    }
    
    private void appliquerFiltres() {
        listeReservationsFiltree.setPredicate(reservation -> {
            // Filtre par type
            String typeFiltre = cmbFiltre.getValue();
            if (typeFiltre != null && !typeFiltre.equals("Tous les types")) {
                if (typeFiltre.equals(TypeAccueil.REGULIER.getLibelle()) && 
                    reservation.getTypeAccueil() != TypeAccueil.REGULIER) {
                    return false;
                }
                if (typeFiltre.equals(TypeAccueil.OCCASIONNEL.getLibelle()) && 
                    reservation.getTypeAccueil() != TypeAccueil.OCCASIONNEL) {
                    return false;
                }
            }
            
            // Filtre par date
            LocalDate dateChoisie = dateFiltre.getValue();
            if (dateChoisie != null && !reservation.getDate().equals(dateChoisie)) {
                return false;
            }
            
            // Filtre par texte de recherche
            String texteRecherche = txtRecherche.getText();
            if (texteRecherche != null && !texteRecherche.isEmpty()) {
                String rechercheLowerCase = texteRecherche.toLowerCase();
                Enfant enfant = reservation.getEnfant();
                Parent parent = enfant.getParent();
                
                // Recherche dans les noms des enfants et parents
                boolean matchEnfant = (enfant.getPrenom() + " " + enfant.getNom()).toLowerCase().contains(rechercheLowerCase);
                boolean matchParent = (parent.getPrenom() + " " + parent.getNom()).toLowerCase().contains(rechercheLowerCase);
                
                if (!matchEnfant && !matchParent) {
                    return false;
                }
            }
            
            return true;
        });
        
        // Mettre à jour les statistiques avec les résultats filtrés
        actualiserStatistiques();
    }
    
    private void actualiserStatistiques() {
        // Total des réservations filtrées
        int total = listeReservationsFiltree.size();
        lblTotal.setText("Total réservations: " + total);
        
        // Réservations régulières
        long totalRegulier = listeReservationsFiltree.stream()
                .filter(r -> r.getTypeAccueil() == TypeAccueil.REGULIER)
                .count();
        lblTotalRegulier.setText("Réservations régulières: " + totalRegulier);
        
        // Réservations occasionnelles
        long totalOccasionnel = listeReservationsFiltree.stream()
                .filter(r -> r.getTypeAccueil() == TypeAccueil.OCCASIONNEL)
                .count();
        lblTotalOccasionnel.setText("Réservations occasionnelles: " + totalOccasionnel);
        
        // Revenu total
        double revenuTotal = listeReservationsFiltree.stream()
                .mapToDouble(Reservation::getTarif)
                .sum();
        lblRevenuTotal.setText(String.format("Revenu total: %.2f €", revenuTotal));
    }
    
    @FXML
    private void nouvelleReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/ram/creche/ui/fxml/NouvelleReservationView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle réservation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Actualiser les données après la création
            chargerDonnees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void afficherDetailsReservation(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/ram/creche/ui/fxml/DetailsReservationView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Passer la réservation au contrôleur
            DetailsReservationController controller = loader.getController();
            controller.setReservation(reservation);
            
            // Afficher la fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Détails de la réservation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Actualiser les données après fermeture de la fenêtre
            chargerDonnees();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void annulerReservation(Reservation reservation) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler la réservation");
        alert.setContentText("Êtes-vous sûr de vouloir annuler cette réservation ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (gestionnaireReservations.annulerReservation(reservation.getId())) {
                    // Rafraîchir la liste
                    chargerDonnees();
                } else {
                    javafx.scene.control.Alert erreur = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    erreur.setTitle("Erreur");
                    erreur.setHeaderText(null);
                    erreur.setContentText("Impossible d'annuler la réservation.");
                    erreur.showAndWait();
                }
            }
        });
    }
}
