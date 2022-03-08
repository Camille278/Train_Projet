package org.miage.trainprojet.Repository;

import org.miage.trainprojet.entity.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRessource extends CrudRepository<Reservation, String> {
}
