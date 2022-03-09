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
public class Favoris {
    @Id
    String Id;
    @ManyToOne
    private Voyageur voyageur;
    String depart;
    String arrivee;
}
