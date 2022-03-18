package org.miage.trainprojet.boundary;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.miage.trainprojet.Control.ReponseBanqueAssembler;
import org.miage.trainprojet.Control.ReservationAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.entity.*;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

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

    @Operation(summary = "Get toutes les réservations", description = "Retourner toutes les reservations", tags = {"reservations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Reservation.class)))})
    @GetMapping()
    public ResponseEntity<?> getAllReservations() {
        return ResponseEntity.ok(ra.toCollectionModel(rr.findAll()));
    }

    @Operation(summary = "Get une reservation", description = "Retourner une reservation", tags = {"reservations"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idReservation", description = "Reservation id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Reservation.class))),
            @ApiResponse(responseCode = "404", description = "Reservation introuvable")})
    @GetMapping(value = "/{idReservation}")
    public ResponseEntity<?> getOneReservation(@PathVariable("idReservation") String id) {
        return Optional.ofNullable(rr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ra.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Post une reservation", description = "Création d'une réservation avec un trajet", tags = {"reservation, trajet"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "aller", description = "Trajet aller id"),
            @Parameter(in = ParameterIn.PATH, name = "couloir", description = "Position : fenetre -> 0, couloir -> 1, peu importe -> 2"),
            @Parameter(in = ParameterIn.PATH, name = "retour", description = "Retour souhaité : true ou false")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Succès"),
            @ApiResponse(responseCode = "404", description = "Trajet introuvable"),
            @ApiResponse(responseCode = "404", description = "Voyageur introuvable")})
    @PostMapping(value = "/aller/{aller}/couloir/{couloir}/retour/{retour}")
    @Transactional
    public ResponseEntity<?> post(@PathVariable("aller") String id, @PathVariable("couloir") int couloir, @PathVariable("retour") boolean retour,
                                  @RequestBody(description = "Identifiant du voyageur qui effectue la réservation", required = true, content = @Content(
                                          schema = @Schema(implementation = Voyageur.class)))
                                  @org.springframework.web.bind.annotation.RequestBody @Valid VoyageurInput voyageur) {
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

    @Operation(summary = "Delete une réservation", description = "Delete une réservation", tags = {"reservations"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idReservation", description = "Reservation id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Succès"),
            @ApiResponse(responseCode = "400", description = "Reservation déjà confirmée ou déjà payée"),
            @ApiResponse(responseCode = "404", description = "Reservation introuvable")})
    @DeleteMapping(value = "/{idReservation}/delete")
    public ResponseEntity<?> deleteReservation(@PathVariable("idReservation") String id) {
        Optional<Reservation> toDelete = rr.findById(id);
        if (toDelete.isEmpty())
            return ResponseEntity.notFound().build();

        if (toDelete.get().isConfirme() || toDelete.get().isPaye() )
            return ResponseEntity.badRequest().build();

        toDelete.ifPresent(rr::delete);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Patch une réservation", description = "Confirmer une réservation", tags = {"reservations", "trajet"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idReservation", description = "Reservation id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès"),
            @ApiResponse(responseCode = "400", description = "Reservation déjà confirmée"),
            @ApiResponse(responseCode = "400", description = "Plus de places dans le trajet"),
            @ApiResponse(responseCode = "404", description = "Reservation introuvable")})
    @PatchMapping(value = "/{idReservation}/confirm")
    @Transactional
    public ResponseEntity<?> patchConfirme(@PathVariable("idReservation") String id) {
        Optional<Reservation> toUpdate = rr.findById(id);
        if (toUpdate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (toUpdate.get().isConfirme() ) {
            return ResponseEntity.badRequest().build();
        }

        Reservation toSave = toUpdate.get();
        toSave.setConfirme(true);

        Trajet updateAller = toUpdate.get().getAller();
        Trajet updateRetour = toUpdate.get().getRetour();

        if (toUpdate.get().getCouloir() == 0) {
            if (updateAller.getNbPlacesFenetre() == 0 || (updateRetour != null && updateRetour.getNbPlacesFenetre() == 0))
                return ResponseEntity.badRequest().build();
            updateAller.setNbPlacesFenetre(updateAller.getNbPlacesFenetre() - 1);
        } else {
            if (updateAller.getNbPlacesCouloir() == 0 || (updateRetour != null && updateRetour.getNbPlacesCouloir() == 0))
                return ResponseEntity.badRequest().build();
            updateAller.setNbPlacesCouloir(updateAller.getNbPlacesCouloir() - 1);
        }

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

    @Operation(summary = "Patch une réservation", description = "Ajouter un retour à une réservation", tags = {"reservations", "trajet"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idReservation", description = "Reservation id"),
            @Parameter(in = ParameterIn.PATH, name = "idTrajet", description = "Trajet id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès"),
            @ApiResponse(responseCode = "400", description = "Réservation confirmée"),
            @ApiResponse(responseCode = "404", description = "Trajet introuvable"),
            @ApiResponse(responseCode = "404", description = "Reservation introuvable")})
    @PatchMapping(value = "/{idReservation}/retour/{idTrajet}")
    @Transactional
    public ResponseEntity<?> patchRetour(@PathVariable("idReservation") String idReservation, @PathVariable("idTrajet") String idRetour) {
        Optional<Reservation> toUpdate = rr.findById(idReservation);
        Optional<Trajet> toAdd = trajetRessource.findById(idRetour);
        if (toUpdate.isEmpty() || toAdd.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (toUpdate.get().isConfirme()) {
            return ResponseEntity.badRequest().build();
        }

        Reservation toSave = toUpdate.get();
        toSave.setRetour(toAdd.get());
        toSave.setPrix(toUpdate.get().getPrix() + toAdd.get().getPrix());

        rr.save(toSave);
        return ResponseEntity.ok(ra.toModel(toSave));

    }

    @Operation(summary = "Patch une réservation", description = "Payer une réservation", tags = {"booking"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idReservation", description = "Reservation id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réponse receptionnée de banque-service"),
            @ApiResponse(responseCode = "400", description = "Reservation déjà payée"),
            @ApiResponse(responseCode = "400", description = "Reservation non confirmée"),
            @ApiResponse(responseCode = "404", description = "Reservation introuvable")})
    @CircuitBreaker(name = "train-projet", fallbackMethod = "fallbackBanqueCall")
    @Retry(name = "fallbackExemple", fallbackMethod = "fallbackBanqueCall")
    @PatchMapping(value = "/{idReservation}/payer")
    public ResponseEntity<?> patchPayer(@PathVariable("idReservation") String idReservation) {
        Optional<Reservation> toUpdate = rr.findById(idReservation);
        if (toUpdate.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!toUpdate.get().isConfirme() || toUpdate.get().isPaye()) {
            return ResponseEntity.badRequest().build();
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

    private ResponseEntity<?> fallbackBanqueCall(RuntimeException re){
        ReponseBanque rep =  ReponseBanque.builder()
                .message("Erreur de connection au service banque")
                .build();
        return ResponseEntity.internalServerError().body(rep);
    }


}
