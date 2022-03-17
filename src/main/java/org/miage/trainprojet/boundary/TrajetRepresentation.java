package org.miage.trainprojet.boundary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.ArrayList;
import java.util.List;
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

    @Operation(summary = "Get tous les trajets", description = "Retourner tous les trajets", tags = {"trajets"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Trajet.class)))})
    @GetMapping()
    public ResponseEntity<?> getAllTrajets(){ return ResponseEntity.ok(ta.toCollectionModel(tr.findAll())); }

    @Operation(summary = "Get un trajet", description = "Retourner un trajet", tags = {"trajets"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idTrajet", description = "Trajet id")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Trajet.class))),
            @ApiResponse(responseCode = "404", description = "Trajet introuvable")})
    @GetMapping(value= "{idTrajet}")
    public ResponseEntity<?> getOneTrajet(@PathVariable("idTrajet") String id){
        return Optional.ofNullable(tr.findById(id))
                .filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(ta.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Rechercher un trajet", description = "Retourner les trajets correspondant à la demande", tags = {"trajets"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "depart", description = "Ville de départ"),
            @Parameter(in = ParameterIn.PATH, name = "arrivee", description = "Ville d'arrivée"),
            @Parameter(in = ParameterIn.PATH, name = "jour", description = "Jour et heure souhaités : yyyy-MM-dd HH:mm"),
            @Parameter(in = ParameterIn.PATH, name = "couloir", description = "Position : fenetre -> 0, couloir -> 1, peu importe -> 2"),
            @Parameter(in = ParameterIn.PATH, name = "retour", description = "Retour souhaité : true ou false")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Trajet.class)))})
    @GetMapping(value= "/depart/{depart}/arrivee/{arrivee}/jour/{jour}/couloir/{couloir}/retour/{retour}")
    public ResponseEntity<?> recherche(@PathVariable("depart") String depart, @PathVariable("arrivee") String arrivee,
                                        @PathVariable("jour") String jour, @PathVariable("couloir") int couloir, @PathVariable("retour") boolean retour) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(jour, formatter);

        List<Trajet> aller = listTrajet(depart, arrivee, dateTime, couloir);

        if((retour && (listTrajet(arrivee, depart, dateTime.plusDays(1), couloir)).isEmpty())){
            return ResponseEntity.ok(ta.toCollectionModelCouloir(new ArrayList<>(), couloir, retour));
        }

        return ResponseEntity.ok(ta.toCollectionModelCouloir(aller, couloir, retour));
    }

    @Operation(summary = "Rechercher un retour", description = "Retourner les trajets retour correspondant à la demande", tags = {"trajets, reservations"})
    @Parameters(value = {
            @Parameter(in = ParameterIn.PATH, name = "idRes", description = "Id de la réservation où ajouter ce trajet retour"),
            @Parameter(in = ParameterIn.PATH, name = "depart", description = "Ville de départ"),
            @Parameter(in = ParameterIn.PATH, name = "arrivee", description = "Ville d'arrivée"),
            @Parameter(in = ParameterIn.PATH, name = "jour", description = "Jour et heure souhaités : yyyy-MM-dd HH:mm")})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succès",
                    content = @Content(schema = @Schema(implementation = Trajet.class))),
            @ApiResponse(responseCode = "404", description = "Reservation introuvable")})
    @GetMapping(value= "/reservation/{idRes}/depart/{depart}/arrivee/{arrivee}/jour/{jour}")
    public ResponseEntity<?> rechercheRetour(@PathVariable("idRes") String idRes, @PathVariable("depart") String depart,
                                             @PathVariable("arrivee") String arrivee, @PathVariable("jour") String jour) {

        Optional<Reservation> reservation = rr.findById(idRes);
        if(reservation.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(jour, formatter);

        List<Trajet> res = listTrajet(depart, arrivee, dateTime, reservation.get().getCouloir());

        return ResponseEntity.ok(ta.toCollectionModelRetour(res, idRes));
    }

    public List<Trajet> listTrajet(String depart, String arrivee, LocalDateTime dateTime, int couloir){
        if (couloir == 0) {
            return tr.trajetsFenetre(depart, arrivee, dateTime);
        } else if (couloir == 1) {
            return tr.trajetsCouloir(depart, arrivee, dateTime);
        } else {
            return tr.trajets(depart, arrivee, dateTime);
        }
    }
}
