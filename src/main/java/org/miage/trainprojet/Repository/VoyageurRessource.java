package org.miage.trainprojet.Repository;

import org.miage.trainprojet.entity.Trajet;
import org.miage.trainprojet.entity.Voyageur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoyageurRessource extends CrudRepository<Voyageur, String> {



}
