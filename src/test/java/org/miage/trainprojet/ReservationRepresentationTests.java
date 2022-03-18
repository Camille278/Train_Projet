package org.miage.trainprojet;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.miage.trainprojet.boundary.BanqueService;
import org.miage.trainprojet.entity.ReponseBanque;
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Trajet;
import org.miage.trainprojet.entity.Voyageur;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReservationRepresentationTests {
    @LocalServerPort
    int port;

    @Autowired
    VoyageurRessource vr;

    @Autowired
    ReservationRessource rr;

    @Autowired
    TrajetRessource tr;

    @MockBean
    BanqueService bs;

    @BeforeEach
    public void setupContext(){
        rr.deleteAll();
        vr.deleteAll();
        tr.deleteAll();
        RestAssured.port = port;
    }

    @Test
    @DisplayName("GET simple")
    public void get(){
        when().get("/reservations").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("GET une reservation existante")
    public void getOneReservation(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        when().get("/reservations/"+r1.getId()).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("GET une reservation existante pour le body")
    public void getOneApi(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        Response response = when().get("/reservations/"+r1.getId()).then().statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("aller"));
    }

    @Test
    @DisplayName("GET une reservation inexistante")
    public void getOneReservationNotFound(){
        when().get("/reservations/150").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE une reservation")
    public void deleteOneReservation(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,false,false,10.30F);
        rr.save(r1);

        when().delete("/reservations/"+r1.getId()+"/delete").then().statusCode(HttpStatus.SC_NO_CONTENT);
        when().get("/reservations/"+r1.getId()).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("DELETE une reservation deja confirmée")
    public void deleteOneReservationDejaConfirmee(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        when().delete("/reservations/"+r1.getId()+"/delete").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("DELETE une reservation not found")
    public void deleteOneReservationNotFound(){
        when().delete("/reservations/1/delete").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("POST une reservation")
    public void postOneReservation(){
        Voyageur v1 = new Voyageur("1", "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        String body = "{\"id\":\"1\"}";

        Response response = given().body(body).contentType(ContentType.JSON)
                .when().post("/reservations/aller/1/couloir/2/retour/false").then().statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String location = response.getHeader("Location");
        when().get(location).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("POST une reservation avec trajet inexistant")
    public void postOneReservationTrajetNotFound(){
        Voyageur v1 = new Voyageur("1", "Beirao");
        vr.save(v1);
        String body = "{\"id\":\"1\"}";

        given().body(body).contentType(ContentType.JSON)
                .when().post("/reservations/aller/1/couloir/2/retour/false").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("POST une reservation avec voyageur inexistant")
    public void postOneReservationVoyageurNotFound(){
        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        String body = "{\"id\":\"1\"}";

        given().body(body).contentType(ContentType.JSON)
                .when().post("/reservations/aller/1/couloir/2/retour/false").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("PATCH confirmer une reservation inexistante")
    public void patchOneReservationConfirmNotFound(){
        when().patch("/reservations/1/confirm").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("PATCH confirmer une reservation")
    public void patchOneReservationConfirm(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/confirm").then().statusCode(HttpStatus.SC_OK);

        Optional<Reservation> r1b = rr.findById("1");
        assertEquals(r1b.get().isConfirme(), true);
    }

    @Test
    @DisplayName("PATCH confirmer une reservation confirmée")
    public void patchOneReservationConfirmDejaConfirmee(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/confirm").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("PATCH confirmer une reservation impossible : plus de places couloir")
    public void patchOneReservationConfirmPlusPlacesCouloir(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 0,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,1,false,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/confirm").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("PATCH confirmer une reservation impossible : plus de places fenetre")
    public void patchOneReservationConfirmPlusPlacesFenetres(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 5,0,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/confirm").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("PATCH confirmer une reservation impossible : plus de places")
    public void patchOneReservationConfirmPlusPlaces(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 0,0,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,2,false,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/confirm").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("PATCH ajouter un retour dans une reservation")
    public void patchOneReservationRetour(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Trajet t2 = new Trajet("2", "Paris", "Nancy", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/retour/2").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("PATCH ajouter un retour dans une reservation inexistante")
    public void patchOneReservationRetourNotFound(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/retour/150").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("PATCH ajouter un retour dans une reservation confirmée")
    public void patchOneReservationRetourConfirmee(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Trajet t2 = new Trajet("2", "Paris", "Nancy", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/retour/2").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("PATCH ajouter un retour inexistant dans une reservation")
    public void patchOneReservationRetourInexistant(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/retour/2").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("PATCH payer une réservation inexistante")
    public void patchPayerReservationNotFound(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/2/payer").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("PATCH payer une réservation non confirmée")
    public void patchPayerReservationNonConfirmee(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,false,false,10.30F);
        rr.save(r1);

        when().patch("/reservations/1/payer").then().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("PATCH payer une réservation : OK")
    public void patchPayerReservation(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,true,false,10.30F);
        rr.save(r1);

        ReponseBanque rb = new ReponseBanque(r1, port, "Financement effectué");

        Mockito.when(bs.appelServiceBanque(any())).thenReturn(rb);

        Response response = when().patch("/reservations/1/payer").then().statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("Financement effectué"));
    }

    @Test
    @DisplayName("PATCH payer une réservation : KO")
    public void patchPayerReservationKO(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,true,false,10.30F);
        rr.save(r1);

        ReponseBanque rb = new ReponseBanque(r1, port, "Financement impossible");

        Mockito.when(bs.appelServiceBanque(any())).thenReturn(rb);

        Response response = when().patch("/reservations/1/payer").then().statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("Financement impossible"));
    }

    @Test
    @DisplayName("PATCH payer une réservation : OK - Maj places train")
    public void patchPayerReservationMaj(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,true,false,10.30F);
        rr.save(r1);

        ReponseBanque rb = new ReponseBanque(r1, port, "Financement effectué");

        Mockito.when(bs.appelServiceBanque(any())).thenReturn(rb);

        when().patch("/reservations/1/payer").then().statusCode(HttpStatus.SC_OK);
        Optional<Reservation> r = rr.findById("1");
        assertTrue(r.get().isPaye());
    }

    @Test
    @DisplayName("PATCH payer une réservation : KO - Maj places train")
    public void patchPayerReservationKOMaj(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,true,false,10.30F);
        rr.save(r1);

        ReponseBanque rb = new ReponseBanque(r1, port, "Financement impossible");

        Mockito.when(bs.appelServiceBanque(any())).thenReturn(rb);

        when().patch("/reservations/1/payer").then().statusCode(HttpStatus.SC_OK);
        Optional<Reservation> r = rr.findById("1");
        assertFalse(r.get().isPaye());
    }
}
