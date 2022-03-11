package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.boundary.VoyageurRepresentation;
import org.miage.trainprojet.entity.Favoris;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FavorisAssembler implements RepresentationModelAssembler<Favoris,EntityModel<Favoris>> {

    @Override
    public EntityModel<Favoris> toModel(Favoris favoris) {
        return EntityModel.of(favoris,
                linkTo(methodOn(TrajetRepresentation.class)
                        .recherche(favoris.getDepart(), favoris.getArrivee(), null, 2, false)).withRel("Rechercher ce trajet"));
    }

    @Override
    public CollectionModel<EntityModel<Favoris>> toCollectionModel(Iterable<? extends Favoris> entities) {
        List<EntityModel<Favoris>> favorisModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(favorisModel,
                linkTo(methodOn(TrajetRepresentation.class).getAllTrajets()).withSelfRel());
    }

    public CollectionModel<EntityModel<Favoris>> toCollectionModelVoyageur(Iterable<? extends Favoris> entities, String idVoyageur) {
        List<EntityModel<Favoris>> FavorisModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(FavorisModel,
                linkTo(methodOn(VoyageurRepresentation.class).getOneVoyageur(idVoyageur)).withSelfRel());
    }

}
