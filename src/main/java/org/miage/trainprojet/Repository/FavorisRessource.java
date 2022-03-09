package org.miage.trainprojet.Repository;

import org.miage.trainprojet.entity.Favoris;
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Voyageur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavorisRessource extends CrudRepository<Favoris, String> {

    @Query("SELECT t FROM Favoris t WHERE voyageur.id = ?1")
    List<Favoris> favorisVoyageur(String idVoyageur);
}
