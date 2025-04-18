package fr.ram.creche.ui.controller;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import fr.ram.creche.model.ConfigurationCreche;
import fr.ram.creche.model.ConfigurationCreche.HorairesJour;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class ConfigurationController implements Initializable {

    @FXML private Spinner<Integer> spinnerCapacite;
    @FXML private TextField fieldTarif;
    
    @FXML private CheckBox cbLundi;
    @FXML private ComboBox<LocalTime> cbLundiOuverture;
    @FXML private ComboBox<LocalTime> cbLundiFermeture;
    
    @FXML private CheckBox cbMardi;
    @FXML private ComboBox<LocalTime> cbMardiOuverture;
    @FXML private ComboBox<LocalTime> cbMardiFermeture;
    
    @FXML private CheckBox cbMercredi;
    @FXML private ComboBox<LocalTime> cbMercrediOuverture;
    @FXML private ComboBox<LocalTime> cbMercrediFermeture;
    
    @FXML private CheckBox cbJeudi;
    @FXML private ComboBox<LocalTime> cbJeudiOuverture;
    @FXML private ComboBox<LocalTime> cbJeudiFermeture;
    
    @FXML private CheckBox cbVendredi;
    @FXML private ComboBox<LocalTime> cbVendrediOuverture;
    @FXML private ComboBox<LocalTime> cbVendrediFermeture;
    
    @FXML private CheckBox cbSamedi;
    @FXML private ComboBox<LocalTime> cbSamediOuverture;
    @FXML private ComboBox<LocalTime> cbSamediFermeture;
    
    @FXML private CheckBox cbDimanche;
    @FXML private ComboBox<LocalTime> cbDimancheOuverture;
    @FXML private ComboBox<LocalTime> cbDimancheFermeture;
    
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private ConfigurationCreche configuration;
    private ObservableList<LocalTime> heures;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configuration = ConfigurationCreche.getInstance();
        
        // Initialiser le spinner de capacité
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, configuration.getCapaciteMaximale());
        spinnerCapacite.setValueFactory(valueFactory);
        
        // Initialiser le champ de tarif
        fieldTarif.setText(String.format("%.2f", configuration.getTarifHoraire()));
        
        // Créer la liste des heures possibles (de 7h à 20h par palier de 30 minutes)
        heures = FXCollections.observableArrayList();
        for (int heure = 7; heure <= 20; heure++) {
            heures.add(LocalTime.of(heure, 0));
            heures.add(LocalTime.of(heure, 30));
        }
        
        // Formatter pour afficher les heures
        StringConverter<LocalTime> timeConverter = new StringConverter<LocalTime>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            @Override
            public String toString(LocalTime time) {
                return formatter.format(time);
            }
            
            @Override
            public LocalTime fromString(String string) {
                return LocalTime.parse(string, formatter);
            }
        };
        
        // Initialiser les combobox pour chaque jour
        initJourControls(cbLundi, cbLundiOuverture, cbLundiFermeture, DayOfWeek.MONDAY, timeConverter);
        initJourControls(cbMardi, cbMardiOuverture, cbMardiFermeture, DayOfWeek.TUESDAY, timeConverter);
        initJourControls(cbMercredi, cbMercrediOuverture, cbMercrediFermeture, DayOfWeek.WEDNESDAY, timeConverter);
        initJourControls(cbJeudi, cbJeudiOuverture, cbJeudiFermeture, DayOfWeek.THURSDAY, timeConverter);
        initJourControls(cbVendredi, cbVendrediOuverture, cbVendrediFermeture, DayOfWeek.FRIDAY, timeConverter);
        initJourControls(cbSamedi, cbSamediOuverture, cbSamediFermeture, DayOfWeek.SATURDAY, timeConverter);
        initJourControls(cbDimanche, cbDimancheOuverture, cbDimancheFermeture, DayOfWeek.SUNDAY, timeConverter);
    }
    
    private void initJourControls(CheckBox checkBox, ComboBox<LocalTime> comboOuverture, 
                                 ComboBox<LocalTime> comboFermeture, DayOfWeek jour, 
                                 StringConverter<LocalTime> converter) {
        // Configurer les combobox
        comboOuverture.setItems(heures);
        comboFermeture.setItems(heures);
        comboOuverture.setConverter(converter);
        comboFermeture.setConverter(converter);
        
        // Si le jour est configuré dans la configuration
        if (configuration.estOuvert(jour)) {
            checkBox.setSelected(true);
            HorairesJour horaires = configuration.getHorairesJour(jour);
            
            // Trouver les heures les plus proches dans notre liste
            LocalTime heureOuverture = trouverHeureLaPlusProche(horaires.getHeureOuverture());
            LocalTime heureFermeture = trouverHeureLaPlusProche(horaires.getHeureFermeture());
            
            comboOuverture.setValue(heureOuverture);
            comboFermeture.setValue(heureFermeture);
        } else {
            checkBox.setSelected(false);
            // Valeurs par défaut
            comboOuverture.setValue(LocalTime.of(8, 0));
            comboFermeture.setValue(LocalTime.of(18, 0));
        }
        
        // Activer/désactiver les combobox en fonction de l'état de la checkbox
        comboOuverture.setDisable(!checkBox.isSelected());
        comboFermeture.setDisable(!checkBox.isSelected());
        
        // Événement quand la checkbox change
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            comboOuverture.setDisable(!newVal);
            comboFermeture.setDisable(!newVal);
        });
    }
    
    private LocalTime trouverHeureLaPlusProche(LocalTime heure) {
        // Si l'heure exacte existe, la retourner
        if (heures.contains(heure)) {
            return heure;
        }
        
        // Sinon, trouver la plus proche
        LocalTime heureLaPlusProche = heures.get(0);
        long differenceMinimale = Math.abs(heure.toSecondOfDay() - heureLaPlusProche.toSecondOfDay());
        
        for (LocalTime h : heures) {
            long difference = Math.abs(heure.toSecondOfDay() - h.toSecondOfDay());
            if (difference < differenceMinimale) {
                differenceMinimale = difference;
                heureLaPlusProche = h;
            }
        }
        
        return heureLaPlusProche;
    }
    
    @FXML
    public void sauvegarderConfiguration() {
        try {
            // Récupérer et valider les valeurs
            int capacite = spinnerCapacite.getValue();
            double tarif = Double.parseDouble(fieldTarif.getText().replace(',', '.'));
            
            if (tarif <= 0) {
                afficherErreur("Le tarif horaire doit être supérieur à 0.");
                return;
            }
            
            // Mettre à jour la configuration
            configuration.setCapaciteMaximale(capacite);
            configuration.setTarifHoraire(tarif);
            
            // Mettre à jour les horaires
            updateJourHoraires(cbLundi, cbLundiOuverture, cbLundiFermeture, DayOfWeek.MONDAY);
            updateJourHoraires(cbMardi, cbMardiOuverture, cbMardiFermeture, DayOfWeek.TUESDAY);
            updateJourHoraires(cbMercredi, cbMercrediOuverture, cbMercrediFermeture, DayOfWeek.WEDNESDAY);
            updateJourHoraires(cbJeudi, cbJeudiOuverture, cbJeudiFermeture, DayOfWeek.THURSDAY);
            updateJourHoraires(cbVendredi, cbVendrediOuverture, cbVendrediFermeture, DayOfWeek.FRIDAY);
            updateJourHoraires(cbSamedi, cbSamediOuverture, cbSamediFermeture, DayOfWeek.SATURDAY);
            updateJourHoraires(cbDimanche, cbDimancheOuverture, cbDimancheFermeture, DayOfWeek.SUNDAY);
            
            afficherSucces("Configuration enregistrée avec succès.");
        } catch (NumberFormatException e) {
            afficherErreur("Veuillez entrer un nombre valide pour le tarif horaire.");
        } catch (Exception e) {
            afficherErreur("Une erreur est survenue : " + e.getMessage());
        }
    }
    
    private void updateJourHoraires(CheckBox checkBox, ComboBox<LocalTime> comboOuverture, 
                                   ComboBox<LocalTime> comboFermeture, DayOfWeek jour) {
        if (checkBox.isSelected()) {
            LocalTime heureOuverture = comboOuverture.getValue();
            LocalTime heureFermeture = comboFermeture.getValue();
            
            if (heureOuverture == null || heureFermeture == null) {
                throw new IllegalArgumentException("Les horaires doivent être définis pour les jours sélectionnés.");
            }
            
            if (heureOuverture.isAfter(heureFermeture) || heureOuverture.equals(heureFermeture)) {
                throw new IllegalArgumentException("L'heure d'ouverture doit être antérieure à l'heure de fermeture.");
            }
            
            configuration.getHorairesOuverture().put(jour, new HorairesJour(heureOuverture, heureFermeture));
        } else {
            configuration.getHorairesOuverture().remove(jour);
        }
    }
    
    @FXML
    public void annuler() {
        // Recharger la vue avec les valeurs actuelles
        initialize(null, null);
    }
    
    private void afficherErreur(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void afficherSucces(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
