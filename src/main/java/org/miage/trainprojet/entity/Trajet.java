package org.miage.trainprojet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Trajet {
    public static final long serialVersionUID = 63627647839872L;

    @Id
    private String id;
    private String depart;
    private String arrivee;
    private LocalDateTime jour;
    private Integer nbPlacesCouloir;
    private Integer nbPlacesFenetre;
    private Float prix;
}
