package fr.ram.creche.ui.controller;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import fr.ram.creche.model.ConfigurationCreche;
import fr.ram.creche.model.Reservation;
import fr.ram.creche.service.GestionnaireReservations;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlanningController implements Initializable {

    @FXML private GridPane calendarGrid;
    @FXML private GridPane placesGrid;
    @FXML private Label lblSemaineActuelle;
    @FXML private ComboBox<String> cmbTypeAffichage;
    @FXML private Button btnPrecedent;
    @FXML private Button btnSuivant;
    
    private LocalDate dateDebut; // Premier jour de la semaine affichée
    private LocalDate dateFin;   // Dernier jour de la semaine affichée
    private GestionnaireReservations gestionnaireReservations;
    private ConfigurationCreche configCreche;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE dd/MM");
    
    private enum TypeAffichage {
        SEMAINE("Vue semaine"),
        JOUR("Vue jour"),
        MOIS("Vue mois");
        
        private final String libelle;
        
        TypeAffichage(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation des services
        gestionnaireReservations = GestionnaireReservations.getInstance();
        configCreche = ConfigurationCreche.getInstance();
        
        // Initialisation du comboBox
        cmbTypeAffichage.setItems(FXCollections.observableArrayList(
                TypeAffichage.SEMAINE.getLibelle(),
                TypeAffichage.JOUR.getLibelle(),
                TypeAffichage.MOIS.getLibelle()
        ));
        cmbTypeAffichage.getSelectionModel().selectFirst();
        
        // Par défaut, afficher la semaine courante
        dateDebut = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        dateFin = dateDebut.plusDays(6);
        
        // Mettre à jour l'affichage
        actualiserAffichage();
    }
    
    /**
     * Met à jour l'affichage du planning selon la période sélectionnée
     */
    private void actualiserAffichage() {
        // Mise à jour du titre de la période
        lblSemaineActuelle.setText("Semaine du " + dateDebut.format(DATE_FORMATTER));
        
        // Effacer et reconstruire la grille
        calendarGrid.getChildren().clear();
        placesGrid.getChildren().clear();
        
        // Ajout des en-têtes de colonnes (heures)
        ajouterEnTetesHeures();
        
        // Ajout des lignes pour chaque jour
        for (int jour = 0; jour < 5; jour++) { // On affiche les 5 jours ouvrables
            LocalDate date = dateDebut.plusDays(jour);
            ajouterLigneJour(date, jour + 1); // row = jour + 1 car row 0 contient les en-têtes
        }
        
        // Mise à jour du résumé des places disponibles
        actualiserPlacesDisponibles();
    }
    
    /**
     * Ajoute les en-têtes des heures en haut de la grille
     */
    private void ajouterEnTetesHeures() {
        // Première cellule vide (coin supérieur gauche)
        Label lblEmpty = new Label("");
        calendarGrid.add(lblEmpty, 0, 0);
        
        // En-têtes pour les heures (de 8h à 18h par défaut)
        LocalTime heureOuverture = LocalTime.of(8, 0);
        LocalTime heureFermeture = LocalTime.of(18, 0);
        
        int colIndex = 1;
        for (int heure = heureOuverture.getHour(); heure < heureFermeture.getHour(); heure++) {
            Label lblHeure = new Label(heure + "h-" + (heure + 1) + "h");
            lblHeure.getStyleClass().add("calendar-day-header");
            lblHeure.setMaxWidth(Double.MAX_VALUE);
            lblHeure.setAlignment(Pos.CENTER);
            calendarGrid.add(lblHeure, colIndex++, 0);
        }
    }
    
    /**
     * Ajoute une ligne dans la grille pour représenter un jour avec ses réservations
     */
    private void ajouterLigneJour(LocalDate date, int rowIndex) {
        // En-tête du jour
        Label lblJour = new Label(date.format(DAY_FORMATTER));
        lblJour.getStyleClass().add("calendar-day-header");
        lblJour.setMaxWidth(Double.MAX_VALUE);
        lblJour.setAlignment(Pos.CENTER);
        calendarGrid.add(lblJour, 0, rowIndex);
        
        // Cellules pour chaque heure du jour
        LocalTime heureOuverture = LocalTime.of(8, 0);
        LocalTime heureFermeture = LocalTime.of(18, 0);
        
        int colIndex = 1;
        for (int heure = heureOuverture.getHour(); heure < heureFermeture.getHour(); heure++) {
            VBox celluleHeure = new VBox(5);
            celluleHeure.getStyleClass().add("calendar-day");
            
            // Si c'est aujourd'hui, ajouter la classe "today"
            if (date.equals(LocalDate.now())) {
                celluleHeure.getStyleClass().add("today");
            }
            
            // Ajouter les réservations pour cette heure
            List<Reservation> reservationsJour = gestionnaireReservations.getReservationsParDate(date);
            for (Reservation reservation : reservationsJour) {
                // Vérifier si la réservation est active pendant cette heure
                if (heureCouverte(reservation, heure)) {
                    HBox reservationBox = creerReservationBox(reservation);
                    celluleHeure.getChildren().add(reservationBox);
                }
            }
            
            calendarGrid.add(celluleHeure, colIndex++, rowIndex);
        }
    }
    
    /**
     * Vérifie si une réservation couvre une heure spécifique
     */
    private boolean heureCouverte(Reservation reservation, int heure) {
        return reservation.getHeureDebut().getHour() <= heure && 
               reservation.getHeureFin().getHour() > heure;
    }
    
    /**
     * Crée un composant pour afficher une réservation dans la cellule du calendrier
     */
    private HBox creerReservationBox(Reservation reservation) {
        HBox box = new HBox();
        box.getStyleClass().add("calendar-reservation");
        
        // Ajouter la classe CSS correspondant au type d'accueil
        if (reservation.getTypeAccueil() == Reservation.TypeAccueil.REGULIER) {
            box.getStyleClass().add("regulier");
        } else {
            box.getStyleClass().add("occasionnel");
        }
        
        // Texte de la réservation
        Label lblEnfant = new Label(reservation.getEnfant().getPrenom());
        box.getChildren().add(lblEnfant);
        box.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblEnfant, Priority.ALWAYS);
        
        // Action au clic sur la réservation
        box.setOnMouseClicked(e -> afficherDetailsReservation(reservation));
        
        return box;
    }
    
    /**
     * Met à jour le résumé des places disponibles
     */
    private void actualiserPlacesDisponibles() {
        int row = 0;
        int capaciteMax = configCreche.getCapaciteMaximale();
        
        // En-têtes des colonnes
        Label lblDate = new Label("Date");
        lblDate.getStyleClass().add("form-label");
        placesGrid.add(lblDate, 0, row);
        
        Label lblOccupees = new Label("Places occupées");
        lblOccupees.getStyleClass().add("form-label");
        placesGrid.add(lblOccupees, 1, row);
        
        Label lblDisponibles = new Label("Places disponibles");
        lblDisponibles.getStyleClass().add("form-label");
        placesGrid.add(lblDisponibles, 2, row);
        
        // Une ligne par jour
        for (int jour = 0; jour < 5; jour++) { // 5 jours ouvrables
            row++;
            LocalDate date = dateDebut.plusDays(jour);
            
            // Jour
            Label lblJour = new Label(date.format(DAY_FORMATTER));
            placesGrid.add(lblJour, 0, row);
            
            // Places à midi (exemple)
            LocalTime midi = LocalTime.of(12, 0);
            int placesOccupees = gestionnaireReservations.getNombrePlacesDisponibles(date, midi);
            int placesDisponibles = Math.max(0, capaciteMax - placesOccupees);
            
            Label lblPlacesOccupees = new Label(String.valueOf(placesOccupees));
            placesGrid.add(lblPlacesOccupees, 1, row);
            
            Label lblPlacesDisponibles = new Label(String.valueOf(placesDisponibles));
            placesGrid.add(lblPlacesDisponibles, 2, row);
        }
    }
    
    /**
     * Affiche les détails d'une réservation dans une fenêtre modale
     */
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
            
            // Actualiser l'affichage après fermeture de la fenêtre
            actualiserAffichage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Ouvre la fenêtre de création d'une nouvelle réservation
     */
    @FXML
    public void nouvelleReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/ram/creche/ui/fxml/NouvelleReservationView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle réservation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Actualiser l'affichage après fermeture de la fenêtre
            actualiserAffichage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche la semaine précédente
     */
    @FXML
    public void semainePrecedente() {
        dateDebut = dateDebut.minusWeeks(1);
        dateFin = dateFin.minusWeeks(1);
        actualiserAffichage();
    }
    
    /**
     * Affiche la semaine suivante
     */
    @FXML
    public void semaineSuivante() {
        dateDebut = dateDebut.plusWeeks(1);
        dateFin = dateFin.plusWeeks(1);
        actualiserAffichage();
    }
    
    /**
     * Revient à la semaine actuelle
     */
    @FXML
    public void allerAujourdhui() {
        dateDebut = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        dateFin = dateDebut.plusDays(6);
        actualiserAffichage();
    }
}
