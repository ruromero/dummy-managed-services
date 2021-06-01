package com.openshift.cloud.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.openshift.cloud.ApiException;
import com.openshift.cloud.api.DefaultApi;
import com.openshift.cloud.api.models.ServiceAccount;
import com.openshift.cloud.api.models.ServiceAccountList;
import com.openshift.cloud.api.models.ServiceAccountRequest;
import com.openshift.cloud.auth.HttpBearerAuth;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.RestAssured;
import io.smallrye.jwt.build.Jwt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
@QuarkusTestResource(OidcWiremockTestResource.class)
class ServiceAccountResourceTest {

    private DefaultApi api = new DefaultApi();

    @BeforeEach
    void init() {
        api.getApiClient().setBasePath("http://localhost:" + RestAssured.port);
        HttpBearerAuth bearer = (HttpBearerAuth) api.getApiClient().getAuthentication("Bearer");
        bearer.setBearerToken(getAccessToken("bob", Set.of("user")));
    }

    @Test
    void testGet() throws ApiException {
        ServiceAccount sa = api.getServiceAccountById("foo");
        assertThat(sa, nullValue());
    }

    @Test
    void testCreateReadDelete() throws ApiException {
        ServiceAccountRequest req = new ServiceAccountRequest();
        req.setName("test");
        req.setDescription("foo");
        ServiceAccount sa = api.createServiceAccount(req);
        assertThat(sa, notNullValue());
        assertThat(sa.getName(), is(req.getName()));
        assertThat(sa.getDescription(), is(req.getDescription()));
        assertThat(sa.getId(), notNullValue());
        assertThat(sa.getCreatedAt(), notNullValue());
        assertThat(sa.getOwner(), is("bob"));
        assertThat(sa.getClientID(), notNullValue());
        assertThat(sa.getClientSecret(), notNullValue());

        ServiceAccount testSa = api.getServiceAccountById(sa.getId());
        assertThat(testSa, notNullValue());
        assertThat(testSa.getName(), is(sa.getName()));
        assertThat(testSa.getDescription(), is(sa.getDescription()));
        assertThat(testSa.getId(), is(sa.getId()));
        assertThat(testSa.getCreatedAt(), is(sa.getCreatedAt()));
        assertThat(testSa.getOwner(), is(sa.getOwner()));
        assertThat(testSa.getClientID(), nullValue());
        assertThat(testSa.getClientSecret(), nullValue());

        api.deleteServiceAccount(sa.getId());
        assertThat(api.getServiceAccountById(sa.getId()), nullValue());
    }

    @Test
    void testList() throws ApiException {
        List<ServiceAccount> accounts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ServiceAccountRequest req = new ServiceAccountRequest();
            req.setName("test-" + i);
            req.setDescription("foo-" + i);
            accounts.add(api.createServiceAccount(req));
        }

        ServiceAccountList serviceAccountList = api.listServiceAccounts();
        assertThat(serviceAccountList, notNullValue());
        assertThat(serviceAccountList.getItems(), hasSize(10));
        serviceAccountList.getItems().stream().forEach(i -> {
            try {
                ServiceAccount sa = api.getServiceAccountById(i.getId());
                assertThat(sa, notNullValue());
                assertThat(sa.getName(), is(i.getName()));
                assertThat(sa.getDescription(), is(i.getDescription()));
                assertThat(sa.getId(), is(i.getId()));
                assertThat(sa.getCreatedAt(), is(i.getCreatedAt()));
                assertThat(sa.getOwner(), is(i.getOwner()));
                assertThat(sa.getClientID(), is(i.getClientID()));
            } catch (ApiException e) {
                fail(e);
            }
        });
    }

    private String getAccessToken(String userName, Set<String> groups) {
        return Jwt.preferredUserName(userName)
                .groups(groups)
                .issuer("https://server.example.com")
                .audience("https://service.example.com")
                .jws()
                .keyId("1")
                .sign();
    }
}
