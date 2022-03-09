package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.ReservationRepresentation;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.boundary.VoyageurRepresentation;
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Voyageur;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VoyageurAssembler implements RepresentationModelAssembler<Voyageur,EntityModel<Voyageur>> {

    @Override
    public EntityModel<Voyageur> toModel(Voyageur voyageur) {
        return EntityModel.of(voyageur,
                linkTo(methodOn(VoyageurRepresentation.class)
                        .getOneVoyageur(voyageur.getId())).withSelfRel(),
                linkTo(methodOn(VoyageurRepresentation.class)
                        .getReservationVoyageur(voyageur.getId())).withRel("Voir ces réservations"));
    }

    @Override
    public CollectionModel<EntityModel<Voyageur>> toCollectionModel(Iterable<? extends Voyageur> entities) {
        List<EntityModel<Voyageur>> voyageurModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(voyageurModel,
                linkTo(methodOn(VoyageurRepresentation.class).getAllVoyageurs()).withSelfRel());
    }

}
