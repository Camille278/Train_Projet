package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.ReservationRepresentation;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Trajet;
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
public class ReservationAssembler implements RepresentationModelAssembler<Reservation,EntityModel<Reservation>> {

    @Override
    public EntityModel<Reservation> toModel(Reservation reservation) {
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        EntityModel<Reservation> link = EntityModel.of(reservation,
                linkTo(methodOn(ReservationRepresentation.class)
                        .getOneReservation(reservation.getId())).withSelfRel(),
                linkTo(methodOn(TrajetRepresentation.class)
                        .getOneTrajet(reservation.getAller().getId())).withRel("Trajet aller"));

        if(reservation.getRetour() == null){
            link.add(linkTo(methodOn(TrajetRepresentation.class)
                    .rechercheRetour(reservation.getId(), reservation.getAller().getArrivee(), reservation.getAller().getDepart(),reservation.getAller().getJour().plusDays(1).format(formatters),2)).withRel("Rechercher un retour"));
        } else {
            link.add(linkTo(methodOn(TrajetRepresentation.class)
                    .getOneTrajet(reservation.getAller().getId())).withRel("Trajet retour"));
        }

        if(!reservation.isConfirme()){
            link.add(linkTo(methodOn(ReservationRepresentation.class)
                    .patchConfirme(reservation.getId())).withRel("Confirmer cette reservation"));

            link.add(linkTo(methodOn(ReservationRepresentation.class)
                    .deleteReservation(reservation.getId())).withRel("Annuler cette réservation"));
        }

        return link;
    }

    @Override
    public CollectionModel<EntityModel<Reservation>> toCollectionModel(Iterable<? extends Reservation> entities) {
        List<EntityModel<Reservation>> reservationModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(reservationModel,
                linkTo(methodOn(TrajetRepresentation.class).getAllTrajets()).withSelfRel());
    }

    public CollectionModel<EntityModel<Reservation>> toCollectionModelRetour(Iterable<? extends Reservation> entities) {
        List<EntityModel<Reservation>> reservationModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(reservationModel,
                linkTo(methodOn(TrajetRepresentation.class).getAllTrajets()).withSelfRel());
    }
}
