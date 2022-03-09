package org.miage.trainprojet.boundary;

import org.miage.trainprojet.Control.FavorisAssembler;
import org.miage.trainprojet.Control.VoyageurAssembler;
import org.miage.trainprojet.Repository.FavorisRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.entity.Favoris;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value="/favoris", produces= MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Favoris.class)
public class FavorisRepresentation {

    private final VoyageurAssembler va;
    private final VoyageurRessource vr;
    private final FavorisAssembler fa;
    private final FavorisRessource fr;

    public FavorisRepresentation(VoyageurAssembler va, VoyageurRessource vr, FavorisAssembler fa, FavorisRessource fr) {
        this.va = va;
        this.vr = vr;
        this.fa = fa;
        this.fr = fr;
    }

    @GetMapping(value = "{idFavoris}")
    public ResponseEntity<?> getOneFavoris(@PathVariable("idFavoris") String id) {
        return Optional.ofNullable(fr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(fa.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value= "/voyageur/{idVoyageur}/")
    public ResponseEntity<?> getReservationVoyageur(@PathVariable("idVoyageur") String id){
        return ResponseEntity.ok(fa.toCollectionModel(fr.favorisVoyageur(id)));
    }

    @GetMapping()
    public ResponseEntity<?> getAllFavoris() {
        return ResponseEntity.ok(fa.toCollectionModel(fr.findAll()));
    }

}