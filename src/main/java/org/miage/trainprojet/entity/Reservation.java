package org.miage.trainprojet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Reservation {
    public static final long serialVersionUID = 63623564329872L;

    @Id
    private String id;
    @ManyToOne
    private Voyageur voyageur;
    @ManyToOne
    private Trajet aller;
    @ManyToOne
    private Trajet retour;
    private int couloir;
    private boolean choixRetour;
    private boolean confirme;
    private boolean paye;
    private Float prix;
}
