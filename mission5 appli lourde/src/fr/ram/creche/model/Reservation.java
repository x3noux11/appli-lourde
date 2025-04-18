package fr.ram.creche.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private int id;
    private Enfant enfant;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private TypeAccueil typeAccueil;
    private boolean confirme;
    private double tarif;

    public enum TypeAccueil {
        REGULIER("Accueil régulier"),
        OCCASIONNEL("Accueil occasionnel");
        
        private final String libelle;
        
        TypeAccueil(String libelle) {
            this.libelle = libelle;
        }
        
        public String getLibelle() {
            return libelle;
        }
    }

    public Reservation() {}

    public Reservation(int id, Enfant enfant, LocalDate date, LocalTime heureDebut, LocalTime heureFin, 
                      TypeAccueil typeAccueil, boolean confirme, double tarif) {
        this.id = id;
        this.enfant = enfant;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.typeAccueil = typeAccueil;
        this.confirme = confirme;
        this.tarif = tarif;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public TypeAccueil getTypeAccueil() {
        return typeAccueil;
    }

    public void setTypeAccueil(TypeAccueil typeAccueil) {
        this.typeAccueil = typeAccueil;
    }

    public boolean isConfirme() {
        return confirme;
    }

    public void setConfirme(boolean confirme) {
        this.confirme = confirme;
    }

    public double getTarif() {
        return tarif;
    }

    public void setTarif(double tarif) {
        this.tarif = tarif;
    }
    
    public int getDureeEnMinutes() {
        return (heureFin.getHour() * 60 + heureFin.getMinute()) - 
               (heureDebut.getHour() * 60 + heureDebut.getMinute());
    }
    
    @Override
    public String toString() {
        return "Réservation du " + date + " pour " + enfant.getPrenom() + " " + enfant.getNom();
    }
}
