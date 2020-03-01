package com.example.xmldecoderissue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.codec.CodecException;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 8484)
class XmlDecoderIssueApplicationTests {

    private static final String INVALID_XML = "<Response><tag>something</tag</Response>";

    @Test
    void invalidXmlResponse() {
        stubFor(get(urlEqualTo("/xml")).willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/xml")
                        .withBody(INVALID_XML)));

        WebClient webClient = WebClient.builder().build();

        Mono<Response> response = webClient.method(HttpMethod.GET)
                .uri("http://localhost:8484/xml")
                .retrieve()
                .bodyToMono(Response.class);

        StepVerifier.create(response)
                .expectError(CodecException.class)
                .verify();
    }

    @Test
    void invalidJsonResponse() {
        stubFor(get(urlEqualTo("/json")).willReturn(
                aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{")));

        WebClient webClient = WebClient.builder().build();

        Mono<Response> response = webClient.method(HttpMethod.GET)
                .uri("http://localhost:8484/json")
                .retrieve()
                .bodyToMono(Response.class);

        StepVerifier.create(response)
                .expectError(CodecException.class)
                .verify();
    }


    @XmlRootElement(name = "Response")
    public static class Response {
        private String tag;

        public String getTag() {
            return tag;
        }

        @XmlElement
        public void setTag(String tag) {
            this.tag = tag;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "tag='" + tag + '\'' +
                    '}';
        }
    }
}
