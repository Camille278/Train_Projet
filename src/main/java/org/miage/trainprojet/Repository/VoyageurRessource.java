package org.miage.trainprojet.Repository;

import org.miage.trainprojet.entity.Voyageur;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoyageurRessource extends CrudRepository<Voyageur, String> {

}
