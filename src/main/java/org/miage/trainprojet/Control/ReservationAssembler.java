package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.FavorisRepresentation;
import org.miage.trainprojet.boundary.ReservationRepresentation;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.boundary.VoyageurRepresentation;
import org.miage.trainprojet.entity.Reservation;
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
                linkTo(methodOn(VoyageurRepresentation.class)
                        .getOneVoyageur(reservation.getVoyageur().getId())).withRel("Voyageur"),
                linkTo(methodOn(TrajetRepresentation.class)
                        .getOneTrajet(reservation.getAller().getId())).withRel("Trajet aller"));

        if (reservation.isChoixRetour()){
            if(reservation.getRetour() == null){
                link.add(linkTo(methodOn(TrajetRepresentation.class)
                        .rechercheRetour(reservation.getId(), reservation.getAller().getArrivee(), reservation.getAller().getDepart(),reservation.getAller().getJour().plusDays(1).format(formatters))).withRel("Rechercher un retour"));
            } else {
                link.add(linkTo(methodOn(TrajetRepresentation.class)
                        .getOneTrajet(reservation.getAller().getId())).withRel("Trajet retour"));
            }
        }

        if(!reservation.isConfirme()){
            link.add(linkTo(methodOn(ReservationRepresentation.class)
                    .patchConfirme(reservation.getId())).withRel("Confirmer cette reservation"));

            link.add(linkTo(methodOn(ReservationRepresentation.class)
                    .deleteReservation(reservation.getId())).withRel("Annuler cette réservation"));
        }else {
            if (!reservation.isPaye()) {
                link.add(linkTo(methodOn(ReservationRepresentation.class)
                        .patchConfirme(reservation.getId())).withRel("Payer cette reservation"));
            }
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

}
