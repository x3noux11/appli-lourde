package fr.ram.creche.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationCreche {
    private static ConfigurationCreche instance;
    
    private int capaciteMaximale;
    private Map<DayOfWeek, HorairesJour> horairesOuverture;
    private double tarifHoraire;
    
    private ConfigurationCreche() {
        horairesOuverture = new HashMap<>();
        // Initialisation des valeurs par défaut
        capaciteMaximale = 20;
        tarifHoraire = 5.0; // 5€ par heure
        
        // Horaires par défaut (du lundi au vendredi de 8h à 18h)
        LocalTime ouverture = LocalTime.of(8, 0);
        LocalTime fermeture = LocalTime.of(18, 0);
        
        horairesOuverture.put(DayOfWeek.MONDAY, new HorairesJour(ouverture, fermeture));
        horairesOuverture.put(DayOfWeek.TUESDAY, new HorairesJour(ouverture, fermeture));
        horairesOuverture.put(DayOfWeek.WEDNESDAY, new HorairesJour(ouverture, fermeture));
        horairesOuverture.put(DayOfWeek.THURSDAY, new HorairesJour(ouverture, fermeture));
        horairesOuverture.put(DayOfWeek.FRIDAY, new HorairesJour(ouverture, fermeture));
    }
    
    public static ConfigurationCreche getInstance() {
        if (instance == null) {
            instance = new ConfigurationCreche();
        }
        return instance;
    }
    
    public static class HorairesJour {
        private LocalTime heureOuverture;
        private LocalTime heureFermeture;
        
        public HorairesJour(LocalTime heureOuverture, LocalTime heureFermeture) {
            this.heureOuverture = heureOuverture;
            this.heureFermeture = heureFermeture;
        }

        public LocalTime getHeureOuverture() {
            return heureOuverture;
        }

        public void setHeureOuverture(LocalTime heureOuverture) {
            this.heureOuverture = heureOuverture;
        }

        public LocalTime getHeureFermeture() {
            return heureFermeture;
        }

        public void setHeureFermeture(LocalTime heureFermeture) {
            this.heureFermeture = heureFermeture;
        }
    }

    // Getters and Setters
    public int getCapaciteMaximale() {
        return capaciteMaximale;
    }

    public void setCapaciteMaximale(int capaciteMaximale) {
        this.capaciteMaximale = capaciteMaximale;
    }

    public Map<DayOfWeek, HorairesJour> getHorairesOuverture() {
        return horairesOuverture;
    }

    public void setHorairesOuverture(Map<DayOfWeek, HorairesJour> horairesOuverture) {
        this.horairesOuverture = horairesOuverture;
    }

    public double getTarifHoraire() {
        return tarifHoraire;
    }

    public void setTarifHoraire(double tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
    }
    
    public boolean estOuvert(DayOfWeek jour) {
        return horairesOuverture.containsKey(jour);
    }
    
    public HorairesJour getHorairesJour(DayOfWeek jour) {
        return horairesOuverture.get(jour);
    }
}
