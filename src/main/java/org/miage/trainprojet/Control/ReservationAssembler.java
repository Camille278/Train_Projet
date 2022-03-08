package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.ReservationRepresentation;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.entity.Reservation;
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
public class ReservationAssembler implements RepresentationModelAssembler<Reservation,EntityModel<Reservation>> {
    @Override
    public EntityModel<Reservation> toModel(Reservation reservation) {
        return EntityModel.of(reservation,
                linkTo(methodOn(ReservationRepresentation.class)
                        .getOneReservation(reservation.getId())).withSelfRel(),
                linkTo(methodOn(TrajetRepresentation.class)
                        .rechercheRetour(reservation.getAller().getArrivee(), reservation.getAller().getDepart(),reservation.getAller().getJour().plusDays(1).toString(),2)).withRel("Rechercher un retour"),
                linkTo(methodOn(ReservationRepresentation.class)
                        .patchRetour(reservation.getId(),null)).withRel("Ajouter un retour"),
                linkTo(methodOn(ReservationRepresentation.class)
                        .patchConfirme(reservation.getId())).withRel("Confirmer cette reservation"),
                linkTo(methodOn(ReservationRepresentation.class)
                        .deleteReservation(reservation.getId())).withRel("Annuler cette réservation"));
    }

    @Override
    public CollectionModel<EntityModel<Reservation>> toCollectionModel(Iterable<? extends Reservation> entities) {
        List<EntityModel<Reservation>> reservatiobModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(reservatiobModel,
                linkTo(methodOn(TrajetRepresentation.class).getAllTrajets()).withSelfRel());
    }
}
