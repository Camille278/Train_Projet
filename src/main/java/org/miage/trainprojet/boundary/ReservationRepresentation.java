package org.miage.trainprojet.boundary;

import org.miage.trainprojet.Control.ReservationAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Trajet;
import org.miage.trainprojet.entity.Voyageur;
import org.miage.trainprojet.entity.VoyageurInput;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value="/reservations", produces= MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Reservation.class)
public class ReservationRepresentation {
    private final TrajetRessource trajetRessource;
    private final VoyageurRessource voyageurRessource;
    private final ReservationRessource rr;
    private final ReservationAssembler ra;

    //Grâce au constructeur, Spring injecte une instance de ir
    public ReservationRepresentation(TrajetRessource trajetRessource, VoyageurRessource voyageurRessource, ReservationRessource rr, ReservationAssembler ra){
        this.trajetRessource = trajetRessource;
        this.voyageurRessource = voyageurRessource;
        this.rr = rr;
        this.ra = ra;
    }

    @PostMapping(value = "aller/{aller}")
    @Transactional
    public ResponseEntity<?> post(@PathVariable("aller") String id, @RequestBody @Valid VoyageurInput voyageur){
        Optional<Trajet> t = trajetRessource.findById(id);
        Optional<Voyageur> v = voyageurRessource.findById(voyageur.getId());
        if(t.isEmpty() || v.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Reservation toSave = Reservation.builder().id(UUID.randomUUID().toString())
                .voyageur(v.get())
                .aller(t.get()).build();

        Reservation saved = rr.save(toSave);
        URI location = linkTo(ReservationRepresentation.class).slash(saved.getId()).toUri();
        return ResponseEntity.created(location).body(ra.toModel(saved));
    }

    @GetMapping(value= "{idReservation}")
    public ResponseEntity<?> getOneReservation(@PathVariable("idReservation") String id){
        return Optional.ofNullable(rr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ra.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }
}
