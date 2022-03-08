package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.ReservationRepresentation;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.entity.Trajet;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TrajetAssembler implements RepresentationModelAssembler<Trajet,EntityModel<Trajet>> {

    @Override
    public EntityModel<Trajet> toModel(Trajet trajet) {
        return EntityModel.of(trajet,
                linkTo(methodOn(TrajetRepresentation.class)
                        .getOneTrajet(trajet.getId())).withSelfRel(),
                linkTo(methodOn(ReservationRepresentation.class)
                        .post(trajet.getId(),null)).withRel("Créer une reservation de ce trajet"));
    }

    public EntityModel<Trajet> toModelRetour(Trajet trajet, String idReservation) {
        return EntityModel.of(trajet,
                linkTo(methodOn(TrajetRepresentation.class)
                        .getOneTrajet(trajet.getId())).withSelfRel(),
                linkTo(methodOn(ReservationRepresentation.class)
                        .patchRetour(idReservation, trajet.getId())).withRel("Ajouter ce retour à la reservation"));
    }

    @Override
    public CollectionModel<EntityModel<Trajet>> toCollectionModel(Iterable<? extends Trajet> entities) {
        List<EntityModel<Trajet>> trajetModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(trajetModel,
                linkTo(methodOn(TrajetRepresentation.class).getAllTrajets()).withSelfRel());
    }


    public CollectionModel<EntityModel<Trajet>> toCollectionModelRetour(Iterable<? extends Trajet> entities, String idReservation) {
        List<EntityModel<Trajet>> trajetModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModelRetour(i, idReservation))
                .toList();

        return CollectionModel.of(trajetModel,
                linkTo(methodOn(TrajetRepresentation.class).getAllTrajets()).withSelfRel());
    }
}
