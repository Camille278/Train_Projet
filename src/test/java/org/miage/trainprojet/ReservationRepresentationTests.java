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
import org.miage.trainprojet.entity.Reservation;
import org.miage.trainprojet.entity.Trajet;
import org.miage.trainprojet.entity.Voyageur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        rr.save(r1);

        when().delete("/reservations/"+r1.getId()+"/delete").then().statusCode(HttpStatus.SC_NO_CONTENT);
        when().get("/reservations/"+r1.getId()).then().statusCode(HttpStatus.SC_NOT_FOUND);
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
    public void postOneReservationNotFound(){
        Voyageur v1 = new Voyageur("1", "Beirao");
        vr.save(v1);
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
    @DisplayName("PATCH confirmer une reservation existante")
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
    @DisplayName("PATCH un retour dans une reservation")
    public void patchOneReservationRetour(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Trajet t2 = new Trajet("2", "Paris", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);

        when().patch("/reservations/1/retour/2").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("PATCH un retour dans une reservation")
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


}
