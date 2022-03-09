package org.miage.trainprojet.Repository;

import org.miage.trainprojet.entity.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRessource extends CrudRepository<Reservation, String> {@

        Query("SELECT t FROM Reservation t WHERE voyageur.id = ?1")
        List<Reservation> reservationsVoyageur(String idVoyageur);
}
