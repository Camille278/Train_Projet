package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.ReservationRepresentation;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.entity.ReponseBanque;
import org.miage.trainprojet.entity.Reservation;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ReponseBanqueAssembler implements RepresentationModelAssembler<ReponseBanque, EntityModel<ReponseBanque>> {

    @Override
    public EntityModel<ReponseBanque> toModel(ReponseBanque entity) {
        if (entity.getReservation() != null ){
            return EntityModel.of(entity,
                    linkTo(methodOn(ReservationRepresentation.class)
                            .getOneReservation(entity.getReservation().getId())).withRel("Retour sur la réservation"));
        }
        return null;

    }
}
