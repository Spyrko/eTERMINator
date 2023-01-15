package de.chiworks.eterminator.eterminservice.service;

import de.chiworks.eterminator.error.LoginException;
import de.chiworks.eterminator.eterminservice.data.Qualification;
import de.chiworks.eterminator.eterminservice.data.QualificationQueryResult;
import de.chiworks.eterminator.eterminservice.data.SearchParameters;
import de.chiworks.eterminator.eterminservice.data.SearchQueryResult;
import io.netty.handler.logging.LogLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import static java.util.stream.Collectors.joining;

@Service
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class TerminService {

    public QualificationQueryResult getQualifications() {
        WebClient client = createWebClient();

        ResponseSpec responseSpec = client.get()
                .uri("https://eterminservice.de/assets/ets/qualifikationen.json")
                .retrieve();

        return responseSpec.bodyToMono(QualificationQueryResult.class).share().block();

    }

    public SearchQueryResult getAppointments(SearchParameters searchParameters) {
        String qualificationIds = searchParameters.getQualificationSubgroup().getQualifications().stream().map(Qualification::getId).collect(joining(","));

        WebClient client = createWebClient();

        ResponseSpec responseSpec = client.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("eterminservice.de")
                        .path("/web/api/v1/search")
                        .queryParam("someOf", qualificationIds)
                        .queryParam("plz", searchParameters.getZipCode())
                        .queryParam("daytime", "00000000000000")
                        .queryParam("radius", searchParameters.getRadius())
                        .build())
                .header("Authorization", "Basic " + searchParameters.getToken())
                .retrieve();

        return responseSpec.bodyToMono(SearchQueryResult.class).share().block();
    }

    public boolean validateToken(String token) {
        WebClient client = createWebClient();

        ResponseSpec responseSpec = client.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("eterminservice.de")
                        .path("/web/api/v1/login")
                        .build())
                .header("Authorization", "Basic " + token)
                .retrieve()
                .onStatus(httpStatusCode -> httpStatusCode.value() == 401, clientResponse -> Mono.error(LoginException::new));

        try {
            responseSpec.toBodilessEntity().block();
        } catch (LoginException e) {
            return false;
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    private WebClient createWebClient() {
        HttpClient httpClient = HttpClient
                .create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

}
