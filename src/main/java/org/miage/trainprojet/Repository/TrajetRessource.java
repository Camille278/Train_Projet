package org.miage.trainprojet.Repository;

import org.miage.trainprojet.entity.Trajet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrajetRessource extends CrudRepository<Trajet, String> {

    @Query("SELECT t FROM Trajet t WHERE depart = ?1 AND arrivee = ?2 AND jour >= ?3 AND nbPlacesCouloir >0")
    List<Trajet> trajetsCouloir(String depart, String arrivee, LocalDateTime jour);

    @Query("SELECT t FROM Trajet t WHERE depart = ?1 AND arrivee = ?2 AND jour >= ?3 AND nbPlacesFenetre >0")
    List<Trajet> trajetsFenetre(String depart, String arrivee, LocalDateTime jour);

    @Query("SELECT t FROM Trajet t WHERE depart = ?1 AND arrivee = ?2 AND jour >= ?3 AND nbPlacesFenetre >0 OR nbPlacesCouloir >0")
    List<Trajet> trajets(String depart, String arrivee, LocalDateTime jour);
}
