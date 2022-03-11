package org.miage.trainprojet;

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
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrajetRepresentationTests {
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
        when().get("/trajets").then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    @DisplayName("GET tous les trajets : 3")
    public void getAllTrajets(){
        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Luxembourg", l1.plusDays(1), 10,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);

        Response response = when().get("/trajets").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(3, nb);
    }

    @Test
    @DisplayName("GET 1 trajet")
    public void getOneTrajet(){
        LocalDateTime l1 = LocalDateTime.now();
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);
        Response response = when().get("/trajets/"+t1.getId()).then().statusCode(HttpStatus.SC_OK).extract().response();
        String jsonAsString = response.asString();
        assertThat(jsonAsString, containsString("Nancy"));
    }

    @Test
    @DisplayName("GET Not Found")
    public void getNotFoundTrajet(){
        when().get("/trajets/150").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Recherche trajet fenetre sans retour")
    public void rechercheTrajetSansRetourFenetre(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Nancy", "Paris", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Response response = when().get("/trajets/depart/Nancy/arrivee/Paris/jour/"+s+"/couloir/0/retour/false").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(2, nb);
    }

    @Test
    @DisplayName("Recherche trajet couloir sans retour")
    public void rechercheTrajetSansRetourCouloir(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Nancy", "Paris", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Response response = when().get("/trajets/depart/Nancy/arrivee/Paris/jour/"+s+"/couloir/1/retour/false").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(2, nb);
    }

    @Test
    @DisplayName("Recherche trajet couloir ou fenetre sans retour")
    public void rechercheTrajetSansRetourCouloirOuFenetre(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Nancy", "Luxembourg", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Nancy", "Paris", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Response response = when().get("/trajets/depart/Nancy/arrivee/Paris/jour/"+s+"/couloir/2/retour/false").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(3, nb);
    }

    @Test
    @DisplayName("Recherche trajet sans résulat")
    public void rechercheTrajetSansRetourSansResultat(){
        Response response = when().get("/trajets/depart/Nancy/arrivee/Paris/jour/2023-09-01 00:00/couloir/2/retour/false").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(0, nb);
    }

    @Test
    @DisplayName("Recherche trajet avec retour : 2")
    public void rechercheTrajetAvecRetour(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Paris", "Nancy", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Paris", "Nancy", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Response response = when().get("/trajets/depart/Nancy/arrivee/Paris/jour/"+s+"/couloir/2/retour/true").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(2, nb);
    }

    @Test
    @DisplayName("Recherche trajet avec retour")
    public void rechercheTrajetAvecRetourSansResultat(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);

        Response response = when().get("/trajets/depart/Nancy/arrivee/Paris/jour/"+s+"/couloir/2/retour/true").then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(0, nb);
    }

    @Test
    @DisplayName("Recherche trajet retour pour réservation inexistante")
    public void rechercheRetourReservationInexistante(){
        when().get("reservation/1/depart/Nancy/arrivee/Paris/jour/2021-09-09 00:00").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Recherche trajet retour pour réservation")
    public void rechercheRetourReservation(){
        when().get("/trajets/reservation/1/depart/Nancy/arrivee/Paris/jour/2021-09-09 00:00").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Recherche retour Fenetre : 1")
    public void rechercheRetourFenetre(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Paris", "Nancy", l1.plusDays(5), 10,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Paris", "Nancy", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,true,false,false);
        rr.save(r1);

        Response response = when().get("/trajets/reservation/1/depart/Paris/arrivee/Nancy/jour/"+s).then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(1, nb);
    }

    @Test
    @DisplayName("Recherche retour couloir: 1")
    public void rechercheRetourCouloir(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Paris", "Nancy", l1.plusDays(5), 0,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Paris", "Nancy", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        Reservation r1 = new Reservation("1",v1, t1,null,1,true,false,false);
        rr.save(r1);

        Response response = when().get("/trajets/reservation/1/depart/Paris/arrivee/Nancy/jour/"+s).then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(1, nb);
    }

    @Test
    @DisplayName("Recherche retour couloir ou fenetre: 2")
    public void rechercheRetourCouloirOuFenetre(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1.plusDays(1), 10,5,10.30F);
        tr.save(t1);
        Trajet t2 = new Trajet("2", "Nancy", "Paris", l1.plusHours(1), 0,5,10.30F);
        tr.save(t2);
        Trajet t3 = new Trajet("3", "Paris", "Nancy", l1.plusDays(5), 0,5,10.30F);
        tr.save(t3);
        Trajet t4 = new Trajet("4", "Paris", "Nancy", l1.plusDays(5), 10,0,10.30F);
        tr.save(t4);

        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        Reservation r1 = new Reservation("1",v1, t1,null,2,true,false,false);
        rr.save(r1);

        Response response = when().get("/trajets/reservation/1/depart/Paris/arrivee/Nancy/jour/"+s).then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(2, nb);
    }

    @Test
    @DisplayName("Recherche retour sans resultat")
    public void rechercheRetourSansResultat(){
        String s = "2022-08-01 10:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse(s, formatter);
        Trajet t1 = new Trajet("1", "Nancy", "Paris", l1, 10,5,10.30F);
        tr.save(t1);

        Voyageur v1 = new Voyageur(UUID.randomUUID().toString(), "Beirao");
        vr.save(v1);

        Reservation r1 = new Reservation("1",v1, t1,null,0,false,true,false);
        rr.save(r1);

        Response response = when().get("/trajets/reservation/1/depart/Paris/arrivee/Nancy/jour/"+s).then().statusCode(HttpStatus.SC_OK)
                .extract().response();
        String reservations = response.asString();
        int nb = StringUtils.countMatches(reservations, "depart");
        assertEquals(0, nb);
    }
}
