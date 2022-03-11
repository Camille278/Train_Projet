package org.miage.trainprojet;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.miage.trainprojet.Control.TrajetAssembler;
import org.miage.trainprojet.Repository.ReservationRessource;
import org.miage.trainprojet.Repository.TrajetRessource;
import org.miage.trainprojet.boundary.TrajetRepresentation;
import org.miage.trainprojet.entity.Trajet;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrajetRepresentationTestsUnitaires {
    @LocalServerPort
    int port;

    @MockBean
    TrajetRepresentation trajetRepresentation;

    @MockBean
    TrajetRessource tr;

    @MockBean
    ReservationRessource rr;

    @BeforeEach
    public void setupContext(){
        rr.deleteAll();
        tr.deleteAll();
        trajetRepresentation = new TrajetRepresentation(tr, new TrajetAssembler(), rr);
        RestAssured.port = port;
    }

    @Test
    @DisplayName("listTrajet Couloir")
    public void listTrajetTestCouloir(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse("2021-01-09 00:00", formatter);
        List<Trajet> toReturn = new ArrayList();

        Mockito.when(tr.trajetsCouloir(anyString(),anyString(),any(LocalDateTime.class))).thenReturn(toReturn);

        trajetRepresentation.listTrajet("Nancy", "Paris",l1,1);
        verify(tr, times(1)).trajetsCouloir(anyString(),anyString(),any(LocalDateTime.class));
    }

    @Test
    @DisplayName("listTrajet Fenetre")
    public void listTrajetTestFenetre(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse("2021-01-09 00:00", formatter);
        List<Trajet> toReturn = new ArrayList();

        Mockito.when(tr.trajetsFenetre(anyString(),anyString(),any(LocalDateTime.class))).thenReturn(toReturn);

        trajetRepresentation.listTrajet("Nancy", "Paris",l1,0);
        verify(tr, times(1)).trajetsFenetre(anyString(),anyString(),any(LocalDateTime.class));
    }

    @Test
    @DisplayName("listTrajet Fenetre ou Couloir")
    public void listTrajetFenetreOuCouloir(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime l1 = LocalDateTime.parse("2021-01-09 00:00", formatter);
        List<Trajet> toReturn = new ArrayList();

        Mockito.when(tr.trajets(anyString(),anyString(),any(LocalDateTime.class))).thenReturn(toReturn);

        trajetRepresentation.listTrajet("Nancy", "Paris",l1,2);
        verify(tr, times(1)).trajets(anyString(),anyString(),any(LocalDateTime.class));
    }
}
