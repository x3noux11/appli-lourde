package fr.ram.creche.ui.controller;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import fr.ram.creche.model.Reservation;
import fr.ram.creche.service.GestionnaireReservations;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DetailsReservationController implements Initializable {

    @FXML private Label lblTypeAccueil;
    @FXML private Label lblEnfant;
    @FXML private Label lblParent;
    @FXML private Label lblDate;
    @FXML private Label lblHoraires;
    @FXML private Label lblTarif;
    @FXML private Label lblStatut;
    @FXML private Button btnAnnuler;
    @FXML private Button btnModifier;
    @FXML private Button btnFermer;
    
    private Reservation reservation;
    private GestionnaireReservations gestionnaireReservations;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gestionnaireReservations = GestionnaireReservations.getInstance();
    }
    
    /**
     * Définit la réservation à afficher et met à jour les champs
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        afficherDetailsReservation();
    }
    
    /**
     * Affiche les détails de la réservation dans les labels
     */
    private void afficherDetailsReservation() {
        if (reservation != null) {
            lblTypeAccueil.setText(reservation.getTypeAccueil().getLibelle());
            lblEnfant.setText(reservation.getEnfant().getPrenom() + " " + reservation.getEnfant().getNom());
            lblParent.setText(reservation.getEnfant().getParent().getPrenom() + " " + 
                              reservation.getEnfant().getParent().getNom());
            lblDate.setText(reservation.getDate().format(DATE_FORMATTER));
            lblHoraires.setText(reservation.getHeureDebut().format(TIME_FORMATTER) + " - " + 
                                reservation.getHeureFin().format(TIME_FORMATTER));
            lblTarif.setText(String.format("%.2f €", reservation.getTarif()));
            lblStatut.setText(reservation.isConfirme() ? "Confirmée" : "En attente");
            
            // Désactiver le bouton d'annulation pour les réservations régulières si désiré
            if (reservation.getTypeAccueil() == Reservation.TypeAccueil.REGULIER) {
                // Option: btnAnnuler.setDisable(true);
            }
        }
    }
    
    /**
     * Annule la réservation après confirmation
     */
    @FXML
    private void annulerReservation() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler la réservation");
        alert.setContentText("Êtes-vous sûr de vouloir annuler cette réservation ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (gestionnaireReservations.annulerReservation(reservation.getId())) {
                    fermer();
                } else {
                    afficherErreur("Erreur", "Impossible d'annuler la réservation.");
                }
            }
        });
    }
    
    /**
     * Ouvre la fenêtre de modification de réservation
     */
    @FXML
    private void modifierReservation() {
        // Cette fonctionnalité pourrait être implémentée dans une version future
        // Pour l'instant, on affiche un message
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Fonctionnalité à venir");
        alert.setContentText("La modification de réservation sera disponible dans une version future.");
        alert.showAndWait();
    }
    
    /**
     * Ferme la fenêtre de détails
     */
    @FXML
    private void fermer() {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
