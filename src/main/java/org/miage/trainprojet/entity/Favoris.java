package org.miage.trainprojet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Favoris {
    @Id
    String id;
    String depart;
    String arrivee;
    BigInteger nombre;

    public static List<Favoris> parser(List<Object[]> list) {
        List<Favoris> favs = new ArrayList<>();
        for (Object[] o : list) {
            Favoris fav = Favoris.builder()
                    .depart(o[0].toString()).arrivee(o[1].toString())
                    .nombre((BigInteger) o[2])
                    .build();
            favs.add(fav);
        }
        return favs;
    }
}
