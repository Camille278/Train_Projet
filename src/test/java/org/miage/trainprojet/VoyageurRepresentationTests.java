package org.miage.trainprojet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VoyageurRepresentationTests {
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
        when().get("/voyageurs").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("GET tous les voyageurs : 2")
    public void getAllApi(){
        Voyageur v1 = new Voyageur("1", "Bouché");
        vr.save(v1);
        Voyageur v2 = new Voyageur("2", "Beirao");
        vr.save(v2);

        when().get("/voyageurs").then().statusCode(HttpStatus.SC_OK)
                .and().assertThat().body("size()",equalTo(2));
    }

    @Test
    @DisplayName("GET 1 voyageur")
    public void getOneVoyageur(){
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);
        Response response = when().get("/voyageurs/"+v1.getId()).then().statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("Beirao"));
    }

    @Test
    @DisplayName("GET Not Found")
    public void getNotFoundApi(){
        when().get("/voyageurs/150").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Recupération reservations Voyageur")
    public void getReservationVoyageur() throws Exception {
        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Luxembourg", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false,10.30F);
        Reservation r2 = new Reservation("2",v1, t2,null,0,false,true,false,10.30F);
        Reservation r3 = new Reservation("3",v1, t3,null,0,false,true,false,10.30F);
        rr.save(r1);
        rr.save(r2);
        rr.save(r3);

        Response response = when().get("/voyageurs/"+v1.getId()+"/reservations").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "Trajet aller");
        assertEquals(3, nb);
    }

    @Test
    @DisplayName("Recupération reservations Voyageur inexistant")
    public void getReservationVoyageurInexistant() throws Exception {
        when().get("/voyageurs/150/reservations").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
