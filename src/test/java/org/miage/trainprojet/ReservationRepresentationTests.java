package org.miage.trainprojet;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.Repository.VoyageurRessource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

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
}
