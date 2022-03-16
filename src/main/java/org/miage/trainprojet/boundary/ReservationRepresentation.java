package org.miage.trainprojet.boundary;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.miage.trainprojet.Control.ReponseBanqueAssembler;
import org.miage.trainprojet.Control.ReservationAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.entity.*;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
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
    private final ReponseBanqueAssembler rba;
    private final BanqueService bs;

    //Grâce au constructeur, Spring injecte une instance de ir
    public ReservationRepresentation(BanqueService bs, TrajetRessource trajetRessource, VoyageurRessource voyageurRessource, ReservationRessource rr, ReservationAssembler ra, ReponseBanqueAssembler rba) {
        this.bs = bs;
        this.trajetRessource = trajetRessource;
        this.voyageurRessource = voyageurRessource;
        this.rr = rr;
        this.ra = ra;
        this.rba = rba;
    }

    @PostMapping(value = "/aller/{aller}/couloir/{couloir}/retour/{retour}")
    @Transactional
    public ResponseEntity<?> post(@PathVariable("aller") String id, @PathVariable("couloir") int couloir, @PathVariable("retour") boolean retour, @RequestBody @Valid VoyageurInput voyageur) {
        Optional<Trajet> t = trajetRessource.findById(id);
        Optional<Voyageur> v = voyageurRessource.findById(voyageur.getId());

        if (t.isEmpty() || v.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reservation toSave = Reservation.builder().id(UUID.randomUUID().toString())
                .voyageur(v.get())
                .aller(t.get())
                .confirme(false)
                .paye(false)
                .choixRetour(retour)
                .couloir(couloir)
                .prix(t.get().getPrix()).build();

        Reservation saved = rr.save(toSave);
        URI location = linkTo(ReservationRepresentation.class).slash(saved.getId()).toUri();
        return ResponseEntity.created(location).body(ra.toModel(saved));
    }

    @GetMapping(value = "/{idReservation}")
    public ResponseEntity<?> getOneReservation(@PathVariable("idReservation") String id) {
        return Optional.ofNullable(rr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ra.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/{idReservation}/delete")
    public ResponseEntity<?> deleteReservation(@PathVariable("idReservation") String id) {
        Optional<Reservation> toDelete = rr.findById(id);
        toDelete.ifPresent(rr::delete);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{idReservation}/confirm")
    @Transactional
    public ResponseEntity<?> patchConfirme(@PathVariable("idReservation") String id) {
        Optional<Reservation> toUpdate = rr.findById(id);
        if (toUpdate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reservation toSave = toUpdate.get();
        toSave.setConfirme(true);

        Trajet updateAller = toUpdate.get().getAller();
        if (toUpdate.get().getCouloir() == 0) {
            updateAller.setNbPlacesFenetre(updateAller.getNbPlacesFenetre() - 1);
        } else {
            updateAller.setNbPlacesCouloir(updateAller.getNbPlacesCouloir() - 1);
        }

        Trajet updateRetour = toUpdate.get().getRetour();
        if (updateRetour != null) {
            if (toUpdate.get().getCouloir() == 0) {
                updateRetour.setNbPlacesFenetre(updateRetour.getNbPlacesFenetre() - 1);
            } else {
                updateRetour.setNbPlacesCouloir(updateRetour.getNbPlacesCouloir() - 1);
            }
        }

        rr.save(toSave);
        return ResponseEntity.ok(ra.toModel(toSave));
    }

    @PatchMapping(value = "/{idReservation}/retour/{idTrajet}")
    @Transactional
    public ResponseEntity<?> patchRetour(@PathVariable("idReservation") String idReservation, @PathVariable("idTrajet") String idRetour) {
        Optional<Reservation> toUpdate = rr.findById(idReservation);
        Optional<Trajet> toAdd = trajetRessource.findById(idRetour);
        if (toUpdate.isEmpty() || toAdd.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reservation toSave = toUpdate.get();
        toSave.setRetour(toAdd.get());
        toSave.setPrix(toUpdate.get().getPrix() + toAdd.get().getPrix());

        rr.save(toSave);
        return ResponseEntity.ok(ra.toModel(toSave));

    }

    @CircuitBreaker(name = "train-projet", fallbackMethod = "fallbackConversionCall")
    @Retry(name = "fallbackExemple", fallbackMethod = "fallbackConversionCall")
    @PatchMapping(value = "/{idReservation}/payer")
    public ResponseEntity<?> patchPayer(@PathVariable("idReservation") String idReservation) {
        Optional<Reservation> toUpdate = rr.findById(idReservation);
        if (toUpdate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!toUpdate.get().isConfirme()) {
            return ResponseEntity.ok("Vous devez confirmer votre réservation avant de la payer");
        }

        ReponseBanque response = bs.appelServiceBanque(toUpdate.get());

        if (response.getMessage().contains("Financement effectué")) {
            Reservation toSave = toUpdate.get();
            toSave.setPaye(true);
            rr.save(toSave);
            response.setReservation(toSave);
            return ResponseEntity.ok(rba.toModel(response));
        }

        return ResponseEntity.ok(rba.toModel(response));
    }

    private ResponseEntity<?> fallbackConversionCall(RuntimeException re){
        ReponseBanque rep =  ReponseBanque.builder()
                .message("Erreur de connection au service banque")
                .build();
        return ResponseEntity.internalServerError().body(rep);
    }


}
