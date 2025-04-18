package fr.ram.creche.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.ram.creche.model.Enfant;
import fr.ram.creche.model.Parent;
import fr.ram.creche.model.Reservation;
import fr.ram.creche.model.Reservation.TypeAccueil;

public class BaseDeDonnees {
    private static BaseDeDonnees instance;
    
    private Map<Integer, Parent> parents;
    private Map<Integer, Enfant> enfants;
    private Map<Integer, Reservation> reservations;
    
    private int nextParentId = 1;
    private int nextEnfantId = 1;
    private int nextReservationId = 1;
    
    private BaseDeDonnees() {
        parents = new HashMap<>();
        enfants = new HashMap<>();
        reservations = new HashMap<>();
        
        // Ajouter des données de test
        initialiserDonneesTest();
    }
    
    public static BaseDeDonnees getInstance() {
        if (instance == null) {
            instance = new BaseDeDonnees();
        }
        return instance;
    }
    
    private void initialiserDonneesTest() {
        // Création de quelques parents
        Parent parent1 = new Parent(nextParentId++, "Dupont", "Jean", "15 rue des Lilas", "0612345678", "jean.dupont@email.com", "motdepasse1");
        Parent parent2 = new Parent(nextParentId++, "Martin", "Sophie", "22 avenue des Roses", "0687654321", "sophie.martin@email.com", "motdepasse2");
        
        // Ajout des parents à la BD
        parents.put(parent1.getId(), parent1);
        parents.put(parent2.getId(), parent2);
        
        // Création d'enfants
        Enfant enfant1 = new Enfant(nextEnfantId++, "Dupont", "Lucas", LocalDate.of(2020, 5, 15), "Allergie aux arachides", parent1);
        Enfant enfant2 = new Enfant(nextEnfantId++, "Dupont", "Emma", LocalDate.of(2022, 3, 10), "", parent1);
        Enfant enfant3 = new Enfant(nextEnfantId++, "Martin", "Léa", LocalDate.of(2021, 8, 20), "", parent2);
        
        // Ajout des enfants à la BD et aux parents
        enfants.put(enfant1.getId(), enfant1);
        enfants.put(enfant2.getId(), enfant2);
        enfants.put(enfant3.getId(), enfant3);
        
        parent1.ajouterEnfant(enfant1);
        parent1.ajouterEnfant(enfant2);
        parent2.ajouterEnfant(enfant3);
        
        // Création de réservations
        LocalDate today = LocalDate.now();
        
        // Réservations régulières
        for (int i = 0; i < 4; i++) {
            LocalDate date = today.plusDays(i);
            Reservation res1 = new Reservation(nextReservationId++, enfant1, date, 
                    LocalTime.of(8, 30), LocalTime.of(17, 0), TypeAccueil.REGULIER, true, 42.5);
            reservations.put(res1.getId(), res1);
            
            Reservation res2 = new Reservation(nextReservationId++, enfant3, date, 
                    LocalTime.of(9, 0), LocalTime.of(16, 30), TypeAccueil.REGULIER, true, 37.5);
            reservations.put(res2.getId(), res2);
        }
        
        // Réservations occasionnelles
        Reservation res3 = new Reservation(nextReservationId++, enfant2, today.plusDays(2), 
                LocalTime.of(10, 0), LocalTime.of(15, 0), TypeAccueil.OCCASIONNEL, true, 25.0);
        reservations.put(res3.getId(), res3);
    }
    
    // Méthodes pour les parents
    public List<Parent> getAllParents() {
        return new ArrayList<>(parents.values());
    }
    
    public Parent getParentById(int id) {
        return parents.get(id);
    }
    
    public Parent ajouterParent(Parent parent) {
        if (parent.getId() == 0) {
            parent.setId(nextParentId++);
        }
        parents.put(parent.getId(), parent);
        return parent;
    }
    
    public boolean supprimerParent(int id) {
        return parents.remove(id) != null;
    }
    
    // Méthodes pour les enfants
    public List<Enfant> getAllEnfants() {
        return new ArrayList<>(enfants.values());
    }
    
    public Enfant getEnfantById(int id) {
        return enfants.get(id);
    }
    
    public Enfant ajouterEnfant(Enfant enfant) {
        if (enfant.getId() == 0) {
            enfant.setId(nextEnfantId++);
        }
        enfants.put(enfant.getId(), enfant);
        if (enfant.getParent() != null) {
            enfant.getParent().ajouterEnfant(enfant);
        }
        return enfant;
    }
    
    public boolean supprimerEnfant(int id) {
        Enfant enfant = enfants.get(id);
        if (enfant != null) {
            // Supprimer toutes les réservations associées à cet enfant
            List<Reservation> reservationsEnfant = getReservationsByEnfant(id);
            for (Reservation r : reservationsEnfant) {
                reservations.remove(r.getId());
            }
            
            // Supprimer l'enfant de la liste du parent
            if (enfant.getParent() != null) {
                enfant.getParent().getEnfants().remove(enfant);
            }
            
            // Supprimer l'enfant de la BD
            enfants.remove(id);
            return true;
        }
        return false;
    }
    
    // Méthodes pour les réservations
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }
    
    public Reservation getReservationById(int id) {
        return reservations.get(id);
    }
    
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservations.values().stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());
    }
    
    public List<Reservation> getReservationsByEnfant(int enfantId) {
        return reservations.values().stream()
                .filter(r -> r.getEnfant().getId() == enfantId)
                .collect(Collectors.toList());
    }
    
    public List<Reservation> getReservationsByParent(int parentId) {
        return reservations.values().stream()
                .filter(r -> r.getEnfant().getParent().getId() == parentId)
                .collect(Collectors.toList());
    }
    
    public Reservation ajouterReservation(Reservation reservation) {
        if (reservation.getId() == 0) {
            reservation.setId(nextReservationId++);
        }
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }
    
    public boolean supprimerReservation(int id) {
        return reservations.remove(id) != null;
    }
    
    public int getNombrePlacesOccupees(LocalDate date, LocalTime heure) {
        return (int) reservations.values().stream()
                .filter(r -> r.getDate().equals(date))
                .filter(r -> r.getHeureDebut().isBefore(heure) || r.getHeureDebut().equals(heure))
                .filter(r -> r.getHeureFin().isAfter(heure))
                .count();
    }
}
