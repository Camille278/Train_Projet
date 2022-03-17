package org.miage.trainprojet.boundary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.miage.trainprojet.Control.ReservationAssembler;
import org.miage.trainprojet.Control.VoyageurAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.entity.Voyageur;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value="/voyageurs", produces= MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Voyageur.class)
public class VoyageurRepresentation {

    private final VoyageurAssembler va;
    private final VoyageurRessource vr;
    private final ReservationAssembler ra;
    private final ReservationRessource rr;

    public VoyageurRepresentation(VoyageurAssembler va, VoyageurRessource vr, ReservationAssembler ra, ReservationRessource rr) {
        this.va = va;
        this.vr = vr;
        this.ra = ra;
        this.rr = rr;
    }

    @Operation(summary = "Get tous les voyageurs", description = "Retourner tous les voyageurs", tags = {"voyageurs"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Voyageur.class)))})
    @GetMapping()
    public ResponseEntity<?> getAllVoyageurs() {
        return ResponseEntity.ok(va.toCollectionModel(vr.findAll()));
    }

    @Operation(summary = "Get un voyageur", description = "Retourner un voyageur", tags = {"voyageurs"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idVoyageur", description = "Voyageur id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Voyageur.class))),
            @ApiResponse(responseCode = "404", description = "Voyageur introuvable")})
    @GetMapping(value = "{idVoyageur}")
    public ResponseEntity<?> getOneVoyageur(@PathVariable("idVoyageur") String id) {
        return Optional.ofNullable(vr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(va.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get les réservations d'un voyageur", description = "Retourner les réservations d'un voyageur", tags = {"voyageurs, reservations"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idVoyageur", description = "Voyageur id")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Voyageur.class))),
            @ApiResponse(responseCode = "404", description = "Voyageur introuvable")})
    @GetMapping(value= "{idVoyageur}/reservations")
    public ResponseEntity<?> getReservationVoyageur(@PathVariable("idVoyageur") String id){
        Optional<Voyageur> toTest = vr.findById(id);

        if(toTest.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ra.toCollectionModel(rr.reservationsVoyageur(id)));

    }

}