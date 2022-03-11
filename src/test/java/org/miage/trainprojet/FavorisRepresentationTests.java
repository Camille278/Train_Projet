package org.miage.trainprojet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
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

import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FavorisRepresentationTests {
    @LocalServerPort
    int port;

    @Autowired
    ReservationRessource rr;

    @Autowired
    VoyageurRessource vr;

    @Autowired
    TrajetRessource tr;

    @BeforeEach
    public void setupContext() {
        rr.deleteAll();
        vr.deleteAll();
        tr.deleteAll();
        RestAssured.port = port;
    }

    @Test
    @DisplayName("3 favoris à afficher pour le voyageur")
    public void affichageFavorisVoyageur3() {
        Voyageur v1 = new Voyageur("1", "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Luxembourg", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Nancy", "Lyon", l1.plusDays(2), 10,5,10.30F);
        tr.save(t4);
        Trajet t5 = new Trajet("5", "Nancy", "Cannes", l1.plusDays(3), 10,5,10.30F);
        tr.save(t5);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false);
        Reservation r2 = new Reservation("2",v1, t2,null,0,false,true,false);
        Reservation r3 = new Reservation("3",v1, t3,null,0,false,true,false);
        Reservation r4 = new Reservation("4",v1, t4,null,0,false,true,false);
        Reservation r5 = new Reservation("5",v1, t5,null,0,false,true,false);
        rr.save(r1);
        rr.save(r2);
        rr.save(r3);
        rr.save(r4);
        rr.save(r5);

        Response response = when().get("/favoris/voyageur/1").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String favoris = response.asString();
        int nb = StringUtils.countMatches(favoris, "nombre");
        assertEquals(3, nb);
    }

    @Test
    @DisplayName("1 favoris à afficher pour le voyageur")
    public void affichageFavorisVoyageur1() {
        Voyageur v1 = new Voyageur("1", "Beirao");
        vr.save(v1);

        LocalDateTime l1 = LocalDateTime.now();
        Trajet t2 = new Trajet("2", "Nancy", "Luxembourg", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);

        Reservation r2 = new Reservation("2",v1, t2,null,0,false,true,false);
        Reservation r3 = new Reservation("3",v1, t3,null,0,false,true,false);
        rr.save(r2);
        rr.save(r3);

        Response response = when().get("/favoris/voyageur/1").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String favoris = response.asString();
        int nb = StringUtils.countMatches(favoris, "nombre");
        assertEquals(1, nb);
    }

    @Test
    @DisplayName("Pas de favoris à afficher pour le voyageur")
    public void affichagePasFavorisVoyageur() {
        Voyageur v1 = new Voyageur("1", "Beirao");
        vr.save(v1);

        Response response = when().get("/favoris/voyageur/1").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String favoris = response.asString();
        int nb = StringUtils.countMatches(favoris, "nombre");
        assertEquals(0, nb);
    }

    private String toJsonString(Object r) throws Exception{
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(r);
    }
}
