package com.openshift.cloud.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.openshift.cloud.ApiException;
import com.openshift.cloud.api.DefaultApi;
import com.openshift.cloud.api.models.ServiceAccount;
import com.openshift.cloud.api.models.ServiceAccountRequest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.RestAssured;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@QuarkusTestResource(OidcWiremockTestResource.class)
class ServiceAccountResourceTest {

    private DefaultApi api = new DefaultApi();

    @BeforeEach
    void init() {
        api.getApiClient().setBasePath("http://localhost:" + RestAssured.port);
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
        assertThat(sa.getOwner(), is("unknown"));
        assertThat(sa.getClientID(), notNullValue());
        assertThat(sa.getClientSecret(), notNullValue());

        ServiceAccount testSa = api.getServiceAccountById(req.getName());
        assertThat(testSa, notNullValue());
        assertThat(testSa.getName(), is(sa.getName()));
        assertThat(testSa.getDescription(), is(sa.getDescription()));
        assertThat(testSa.getId(), is(sa.getId()));
        assertThat(testSa.getCreatedAt(), is(sa.getCreatedAt()));
        assertThat(testSa.getOwner(), is(sa.getOwner()));
        assertThat(testSa.getClientID(), nullValue());
        assertThat(testSa.getClientSecret(), nullValue());

        api.deleteServiceAccount(sa.getName());
        testSa = api.getServiceAccountById(req.getName());
        assertThat(testSa, nullValue());
    }
}
