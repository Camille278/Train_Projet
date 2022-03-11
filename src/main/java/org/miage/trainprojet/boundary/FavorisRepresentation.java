package org.miage.trainprojet.boundary;

import org.miage.trainprojet.Control.FavorisAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.entity.*;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping(value="/favoris", produces= MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Favoris.class)
public class FavorisRepresentation {

    private final ReservationRessource rr;
    private final VoyageurRessource vr;
    private final FavorisAssembler fa;

    public FavorisRepresentation(ReservationRessource rr, VoyageurRessource vr, FavorisAssembler fa) {
        this.rr = rr;
        this.vr = vr;
        this.fa = fa;
    }


    @GetMapping(value="/voyageur/{idVoyageur}")
    public ResponseEntity<?> getAllFavorisVoyageur(@PathVariable("idVoyageur") String id){
        Optional<Voyageur> toUpdate = vr.findById(id);

        if(toUpdate.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        List<Object[]> list = rr.favoris(id);
        List<Favoris> listF = Favoris.parser(list);
        listF = listF.size() > 3 ? listF.subList(0,3) : listF;
        return ResponseEntity.ok(fa.toCollectionModelVoyageur(listF, id));
    }

}