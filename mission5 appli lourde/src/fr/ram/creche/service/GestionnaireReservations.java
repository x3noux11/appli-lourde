package fr.ram.creche.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import fr.ram.creche.dao.BaseDeDonnees;
import fr.ram.creche.model.ConfigurationCreche;
import fr.ram.creche.model.Enfant;
import fr.ram.creche.model.Reservation;
import fr.ram.creche.model.Reservation.TypeAccueil;

public class GestionnaireReservations {
    private static GestionnaireReservations instance;
    private BaseDeDonnees baseDeDonnees;
    private ConfigurationCreche configCreche;
    
    private GestionnaireReservations() {
        baseDeDonnees = BaseDeDonnees.getInstance();
        configCreche = ConfigurationCreche.getInstance();
    }
    
    public static GestionnaireReservations getInstance() {
        if (instance == null) {
            instance = new GestionnaireReservations();
        }
        return instance;
    }
    
    public List<Reservation> getReservationsParDate(LocalDate date) {
        return baseDeDonnees.getReservationsByDate(date);
    }
    
    public List<Reservation> getReservationsParEnfant(int enfantId) {
        return baseDeDonnees.getReservationsByEnfant(enfantId);
    }
    
    public List<Reservation> getReservationsParParent(int parentId) {
        return baseDeDonnees.getReservationsByParent(parentId);
    }
    
    public boolean placesDisponibles(LocalDate date, LocalTime heureDebut, LocalTime heureFin) {
        int capaciteMax = configCreche.getCapaciteMaximale();
        
        // Vu00e9rifier chaque tranche de 30 minutes dans l'intervalle
        LocalTime heureCourante = heureDebut;
        while (heureCourante.isBefore(heureFin)) {
            int placesOccupees = baseDeDonnees.getNombrePlacesOccupees(date, heureCourante);
            if (placesOccupees >= capaciteMax) {
                return false; // Plus de places disponibles pour cette tranche
            }
            heureCourante = heureCourante.plusMinutes(30);
        }
        
        return true; // Des places sont disponibles sur toute la pu00e9riode
    }
    
    public int getNombrePlacesDisponibles(LocalDate date, LocalTime heure) {
        int capaciteMax = configCreche.getCapaciteMaximale();
        int placesOccupees = baseDeDonnees.getNombrePlacesOccupees(date, heure);
        return Math.max(0, capaciteMax - placesOccupees);
    }
    
    public Reservation creerReservation(Enfant enfant, LocalDate date, LocalTime heureDebut, 
                                  LocalTime heureFin, TypeAccueil typeAccueil) {
        // Vu00e9rifier si des places sont disponibles
        if (!placesDisponibles(date, heureDebut, heureFin)) {
            return null; // Pas de places disponibles
        }
        
        // Calculer le tarif
        double dureeHeures = (double) (heureFin.getHour() * 60 + heureFin.getMinute() - 
                              heureDebut.getHour() * 60 - heureDebut.getMinute()) / 60.0;
        double tarif = dureeHeures * configCreche.getTarifHoraire();
        
        // Cru00e9er la ru00e9servation
        Reservation reservation = new Reservation();
        reservation.setEnfant(enfant);
        reservation.setDate(date);
        reservation.setHeureDebut(heureDebut);
        reservation.setHeureFin(heureFin);
        reservation.setTypeAccueil(typeAccueil);
        reservation.setConfirme(true);
        reservation.setTarif(tarif);
        
        // Enregistrer la ru00e9servation
        return baseDeDonnees.ajouterReservation(reservation);
    }
    
    public boolean annulerReservation(int reservationId) {
        return baseDeDonnees.supprimerReservation(reservationId);
    }
    
    public boolean modifierReservation(Reservation reservation, LocalDate nouvelleDate, 
                                     LocalTime nouvelleHeureDebut, LocalTime nouvelleHeureFin) {
        // Vu00e9rifier si la modification est possible (places disponibles)
        if (!placesDisponibles(nouvelleDate, nouvelleHeureDebut, nouvelleHeureFin)) {
            return false;
        }
        
        // Mettre u00e0 jour les informations
        reservation.setDate(nouvelleDate);
        reservation.setHeureDebut(nouvelleHeureDebut);
        reservation.setHeureFin(nouvelleHeureFin);
        
        // Recalculer le tarif
        double dureeHeures = (double) (nouvelleHeureFin.getHour() * 60 + nouvelleHeureFin.getMinute() - 
                              nouvelleHeureDebut.getHour() * 60 - nouvelleHeureDebut.getMinute()) / 60.0;
        double tarif = dureeHeures * configCreche.getTarifHoraire();
        reservation.setTarif(tarif);
        
        return true;
    }
}
