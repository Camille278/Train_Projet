package org.miage.trainprojet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Voyageur {
    public static final long serialVersionUID = 63627646566872L;

    @Id
    private String id;
    private String nom;
}
