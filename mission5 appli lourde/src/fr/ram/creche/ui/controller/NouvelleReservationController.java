package fr.ram.creche.ui.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import fr.ram.creche.dao.BaseDeDonnees;
import fr.ram.creche.model.ConfigurationCreche;
import fr.ram.creche.model.Enfant;
import fr.ram.creche.model.Reservation;
import fr.ram.creche.model.Reservation.TypeAccueil;
import fr.ram.creche.service.GestionnaireReservations;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class NouvelleReservationController implements Initializable {

    @FXML private ComboBox<TypeAccueil> cmbTypeAccueil;
    @FXML private ComboBox<Enfant> cmbEnfant;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> cmbHeureDebut;
    @FXML private ComboBox<LocalTime> cmbHeureFin;
    @FXML private HBox hboxInfoDisponibilite;
    @FXML private Label lblInfoDisponibilite;
    @FXML private HBox hboxInfoTarif;
    @FXML private Label lblInfoTarif;
    @FXML private Button btnEnregistrer;
    
    private BaseDeDonnees baseDeDonnees;
    private GestionnaireReservations gestionnaireReservations;
    private ConfigurationCreche configCreche;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        baseDeDonnees = BaseDeDonnees.getInstance();
        gestionnaireReservations = GestionnaireReservations.getInstance();
        configCreche = ConfigurationCreche.getInstance();
        
        // Initialisation des ComboBox
        initialiserTypeAccueil();
        initialiserComboEnfants();
        initialiserHeures();
        
        // Par défaut, date du jour
        datePicker.setValue(LocalDate.now());
        
        // Désactiver le bouton Enregistrer tant que tous les champs ne sont pas remplis
        btnEnregistrer.setDisable(true);
        
        // Ajouter des listeners pour mettre à jour les infos de disponibilité et tarif
        ajouterListeners();
    }
    
    private void initialiserTypeAccueil() {
        cmbTypeAccueil.setItems(FXCollections.observableArrayList(TypeAccueil.values()));
        cmbTypeAccueil.setConverter(new javafx.util.StringConverter<TypeAccueil>() {
            @Override
            public String toString(TypeAccueil type) {
                return type != null ? type.getLibelle() : "";
            }
            
            @Override
            public TypeAccueil fromString(String string) {
                return null; // Not needed for combo box
            }
        });
    }
    
    private void initialiserComboEnfants() {
        cmbEnfant.setItems(FXCollections.observableArrayList(baseDeDonnees.getAllEnfants()));
        cmbEnfant.setConverter(new javafx.util.StringConverter<Enfant>() {
            @Override
            public String toString(Enfant enfant) {
                return enfant != null ? enfant.getPrenom() + " " + enfant.getNom() : "";
            }
            
            @Override
            public Enfant fromString(String string) {
                return null; // Not needed for combo box
            }
        });
    }
    
    private void initialiserHeures() {
        // Heures de début possibles (toutes les demi-heures de 8h à 17h30)
        cmbHeureDebut.getItems().clear();
        for (int heure = 8; heure < 18; heure++) {
            cmbHeureDebut.getItems().add(LocalTime.of(heure, 0));
            if (heure < 17) {
                cmbHeureDebut.getItems().add(LocalTime.of(heure, 30));
            }
        }
        
        // Heures de fin possibles (toutes les demi-heures de 8h30 à 18h)
        cmbHeureFin.getItems().clear();
        for (int heure = 8; heure < 18; heure++) {
            if (heure > 8 || heure == 17) {
                cmbHeureFin.getItems().add(LocalTime.of(heure, 0));
            }
            if (heure < 18) {
                cmbHeureFin.getItems().add(LocalTime.of(heure, 30));
            }
        }
        cmbHeureFin.getItems().add(LocalTime.of(18, 0));
        
        // Formatage des heures
        javafx.util.StringConverter<LocalTime> timeConverter = new javafx.util.StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                if (time == null) return "";
                return String.format("%02d:%02d", time.getHour(), time.getMinute());
            }
            
            @Override
            public LocalTime fromString(String string) {
                return null; // Not needed for combo box
            }
        };
        
        cmbHeureDebut.setConverter(timeConverter);
        cmbHeureFin.setConverter(timeConverter);
    }
    
    private void ajouterListeners() {
        // Mettre à jour l'heure de fin minimale lorsque l'heure de début change
        cmbHeureDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // L'heure de fin doit être après l'heure de début
                cmbHeureFin.getItems().clear();
                LocalTime heureDebut = cmbHeureDebut.getValue();
                
                for (int heure = heureDebut.getHour(); heure <= 18; heure++) {
                    // Ajouter les heures pleines et demi-heures après l'heure de début
                    if (heure == heureDebut.getHour()) {
                        if (heureDebut.getMinute() == 0 && heure < 18) {
                            cmbHeureFin.getItems().add(LocalTime.of(heure, 30));
                        }
                    } else {
                        if (heure < 18) {
                            cmbHeureFin.getItems().add(LocalTime.of(heure, 0));
                            cmbHeureFin.getItems().add(LocalTime.of(heure, 30));
                        } else {
                            cmbHeureFin.getItems().add(LocalTime.of(heure, 0));
                        }
                    }
                }
                
                // Sélectionner une heure de fin par défaut (+1h)
                LocalTime defaultEndTime = heureDebut.plusHours(1);
                if (defaultEndTime.isBefore(LocalTime.of(18, 1))) {
                    cmbHeureFin.setValue(defaultEndTime);
                } else {
                    cmbHeureFin.setValue(LocalTime.of(18, 0));
                }
                
                verifierFormulaire();
            }
        });
        
        // Vérifier le formulaire quand les valeurs changent
        cmbTypeAccueil.valueProperty().addListener((obs, oldVal, newVal) -> verifierFormulaire());
        cmbEnfant.valueProperty().addListener((obs, oldVal, newVal) -> verifierFormulaire());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> verifierFormulaire());
        cmbHeureFin.valueProperty().addListener((obs, oldVal, newVal) -> verifierFormulaire());
    }
    
    private void verifierFormulaire() {
        boolean formulaireComplet = cmbTypeAccueil.getValue() != null &&
                                    cmbEnfant.getValue() != null &&
                                    datePicker.getValue() != null &&
                                    cmbHeureDebut.getValue() != null &&
                                    cmbHeureFin.getValue() != null;
        
        btnEnregistrer.setDisable(!formulaireComplet);
        
        if (formulaireComplet) {
            verifierDisponibilite();
            calculerTarif();
        }
    }
    
    private void verifierDisponibilite() {
        LocalDate date = datePicker.getValue();
        LocalTime heureDebut = cmbHeureDebut.getValue();
        LocalTime heureFin = cmbHeureFin.getValue();
        
        if (date != null && heureDebut != null && heureFin != null) {
            boolean placesDisponibles = gestionnaireReservations.placesDisponibles(date, heureDebut, heureFin);
            
            if (placesDisponibles) {
                lblInfoDisponibilite.setText("Places disponibles pour cet horaire");
                hboxInfoDisponibilite.getStyleClass().removeAll("alert-warning", "alert-error");
                hboxInfoDisponibilite.getStyleClass().add("alert-success");
                btnEnregistrer.setDisable(false);
            } else {
                lblInfoDisponibilite.setText("Aucune place disponible pour cet horaire");
                hboxInfoDisponibilite.getStyleClass().removeAll("alert-success", "alert-info");
                hboxInfoDisponibilite.getStyleClass().add("alert-error");
                btnEnregistrer.setDisable(true);
            }
        }
    }
    
    private void calculerTarif() {
        LocalTime heureDebut = cmbHeureDebut.getValue();
        LocalTime heureFin = cmbHeureFin.getValue();
        
        if (heureDebut != null && heureFin != null) {
            // Calculer le nombre d'heures
            double dureeHeures = (double) (heureFin.getHour() * 60 + heureFin.getMinute() -
                                 heureDebut.getHour() * 60 - heureDebut.getMinute()) / 60.0;
            
            // Calculer le tarif
            double tarif = dureeHeures * configCreche.getTarifHoraire();
            
            lblInfoTarif.setText(String.format("Tarif: %.2f €", tarif));
        }
    }
    
    @FXML
    private void enregistrer() {
        // Récupérer les valeurs du formulaire
        TypeAccueil typeAccueil = cmbTypeAccueil.getValue();
        Enfant enfant = cmbEnfant.getValue();
        LocalDate date = datePicker.getValue();
        LocalTime heureDebut = cmbHeureDebut.getValue();
        LocalTime heureFin = cmbHeureFin.getValue();
        
        // Vérifier que tous les champs sont remplis
        if (typeAccueil == null || enfant == null || date == null || 
                heureDebut == null || heureFin == null) {
            afficherErreur("Formulaire incomplet", "Veuillez remplir tous les champs du formulaire.");
            return;
        }
        
        // Vérifier la disponibilité
        if (!gestionnaireReservations.placesDisponibles(date, heureDebut, heureFin)) {
            afficherErreur("Aucune place disponible", 
                    "Il n'y a plus de places disponibles pour cet horaire. Veuillez choisir un autre créneau.");
            return;
        }
        
        // Créer la réservation
        Reservation reservation = gestionnaireReservations.creerReservation(
                enfant, date, heureDebut, heureFin, typeAccueil);
        
        if (reservation != null) {
            // Fermer la fenêtre
            Stage stage = (Stage) btnEnregistrer.getScene().getWindow();
            stage.close();
        } else {
            afficherErreur("Erreur", "Impossible de créer la réservation. Veuillez réessayer.");
        }
    }
    
    @FXML
    private void annuler() {
        Stage stage = (Stage) btnEnregistrer.getScene().getWindow();
        stage.close();
    }
    
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
