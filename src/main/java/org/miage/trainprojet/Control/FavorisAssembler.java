package org.miage.trainprojet.Control;

import org.miage.trainprojet.boundary.FavorisRepresentation;
import org.miage.trainprojet.boundary.VoyageurRepresentation;
import org.miage.trainprojet.entity.Favoris;
import org.miage.trainprojet.entity.Voyageur;
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
                linkTo(methodOn(FavorisRepresentation.class)
                        .getOneFavoris(favoris.getId())).withSelfRel(),
                linkTo(methodOn(VoyageurRepresentation.class)
                        .getOneVoyageur(favoris.getVoyageur().getId())).withRel("Voyageur"));
    }

    @Override
    public CollectionModel<EntityModel<Favoris>> toCollectionModel(Iterable<? extends Favoris> entities) {
        List<EntityModel<Favoris>> favorisModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i-> toModel(i))
                .toList();

        return CollectionModel.of(favorisModel,
                linkTo(methodOn(FavorisRepresentation.class).getAllFavoris()).withSelfRel());
    }

}
