package org.miage.trainprojet.boundary;

import org.miage.trainprojet.Control.TrajetAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Trajet;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping(value="/trajets", produces= MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Trajet.class)
public class TrajetRepresentation {

    private final TrajetRessource tr;
    private final TrajetAssembler ta;
    private final ReservationRessource rr;

    //Grâce au constructeur, Spring injecte une instance de ir
    public TrajetRepresentation(TrajetRessource tr, TrajetAssembler ta, ReservationRessource rr){
        this.tr = tr;
        this.ta = ta;
        this.rr = rr;
    }
    //trajet/depart/Paris/arrivee/nancy/jour/jj-mm-yyyy hh:mm/couloir/0/retour/1
    @GetMapping()
    public ResponseEntity<?> getAllTrajets(){
        return ResponseEntity.ok(ta.toCollectionModel(tr.findAll()));
    }

    @GetMapping(value= "{idTrajet}")
    public ResponseEntity<?> getOneTrajet(@PathVariable("idTrajet") String id){
        return Optional.ofNullable(tr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ta.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value= "/depart/{depart}/arrivee/{arrivee}/jour/{jour}/couloir/{couloir}/retour/{retour}")
    public ResponseEntity<?> recherche(@PathVariable("depart") String depart, @PathVariable("arrivee") String arrivee,
                                        @PathVariable("jour") String jour, @PathVariable("couloir") int couloir, @PathVariable("retour") boolean retour) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(jour, formatter);
        if (couloir == 0) {
            return ResponseEntity.ok(ta.toCollectionModel(tr.trajetsFenetre(depart, arrivee, dateTime)));
        } else if (couloir == 1) {
            return ResponseEntity.ok(ta.toCollectionModel(tr.trajetsCouloir(depart, arrivee, dateTime)));
        } else {
            return ResponseEntity.ok(ta.toCollectionModel(tr.trajets(depart, arrivee, dateTime)));
        }
    }

    @GetMapping(value= "/reservation/{idRes}/depart/{depart}/arrivee/{arrivee}/jour/{jour}/couloir/{couloir}")
    public ResponseEntity<?> rechercheRetour(@PathVariable("idRes") String idRes, @PathVariable("depart") String depart,
                                             @PathVariable("arrivee") String arrivee, @PathVariable("jour") String jour,
                                             @PathVariable("couloir") int couloir) {

        Optional<Reservation> reservation = rr.findById(idRes);
        if(reservation.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(jour, formatter);
        if (couloir == 0) {
            return ResponseEntity.ok(ta.toCollectionModelRetour(tr.trajetsFenetre(depart, arrivee, dateTime), idRes));
        } else if (couloir == 1) {
            return ResponseEntity.ok(ta.toCollectionModelRetour(tr.trajetsCouloir(depart, arrivee, dateTime), idRes));
        } else {
            return ResponseEntity.ok(ta.toCollectionModelRetour(tr.trajets(depart, arrivee, dateTime), idRes));
        }
    }
}
