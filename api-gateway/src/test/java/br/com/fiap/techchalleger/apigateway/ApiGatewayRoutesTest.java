package br.com.fiap.techchalleger.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiGatewayRoutesTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void deveCarregarTodasAsRotas() {

        List<Route> routes = routeLocator
                .getRoutes()
                .collectList()
                .block();

        assertEquals(4, routes.size());
    }

    @Test
    void deveConfigurarRotaDeAuth() {

        Route route = encontrarRota("agendamento-auth");

        assertEquals(
                "http://localhost:8081",
                route.getUri().toString()
        );

        assertTrue(
                route.getPredicate().toString()
                        .contains("/api/v1/auth")
        );
    }

    @Test
    void deveConfigurarRotaDeUsuarios() {

        Route route = encontrarRota("agendamento-usuarios");

        assertEquals(
                "http://localhost:8081",
                route.getUri().toString()
        );

        assertTrue(
                route.getPredicate().toString()
                        .contains("/api/v1/usuarios")
        );
    }

    @Test
    void deveConfigurarRotaDeConsultas() {

        Route route = encontrarRota("agendamento-consultas");

        assertEquals(
                "http://localhost:8081",
                route.getUri().toString()
        );

        assertTrue(
                route.getPredicate().toString()
                        .contains("/api/v1/consultas")
        );
    }

    @Test
    void deveConfigurarRotaDoHistoricoGraphQL() {

        Route route = encontrarRota("historico-graphql");

        assertEquals(
                "http://localhost:8082",
                route.getUri().toString()
        );

        assertTrue(
                route.getPredicate().toString()
                        .contains("/graphql")
        );
    }

    private Route encontrarRota(String id) {

        return routeLocator
                .getRoutes()
                .filter(route -> route.getId().equals(id))
                .next()
                .block();
    }
}