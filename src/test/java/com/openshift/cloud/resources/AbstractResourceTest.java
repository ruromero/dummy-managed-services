package com.openshift.cloud.resources;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;

import com.openshift.cloud.api.kas.DefaultApi;
import com.openshift.cloud.api.kas.invoker.auth.HttpBearerAuth;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.RestAssured;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
@QuarkusTestResource(OidcWiremockTestResource.class)
class AbstractResourceTest {

    DefaultApi api = new DefaultApi();

    @BeforeEach
    void init() {
        api.getApiClient().setBasePath("http://localhost:" + RestAssured.port);
        HttpBearerAuth bearer = (HttpBearerAuth) api.getApiClient().getAuthentication("Bearer");
        bearer.setBearerToken(getAccessToken("bob", Set.of("user")));
    }

    String getAccessToken(String userName, Set<String> groups) {
        return Jwt.preferredUserName(userName)
                .groups(groups)
                .issuer("https://server.example.com")
                .audience("https://service.example.com")
                .jws()
                .keyId("1")
                .sign();
    }
}
