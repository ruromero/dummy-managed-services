package com.openshift.cloud.resources;

import org.junit.jupiter.api.Test;

import com.openshift.cloud.api.kas.invoker.ApiException;
import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.openshift.cloud.api.kas.models.ServiceAccountList;
import com.openshift.cloud.api.kas.models.ServiceAccountRequest;

import io.quarkus.test.junit.QuarkusTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
class ServiceAccountResourceTest extends AbstractResourceTest {

    @Test
    void testGet() throws ApiException {
        ServiceAccount sa = securityApi.getServiceAccountById("foo");
        assertThat(sa, nullValue());
    }

    @Test
    void testCreateReadDelete() throws ApiException {
        ServiceAccountRequest req = new ServiceAccountRequest();
        req.setName("test");
        req.setDescription("foo");
        ServiceAccount sa = securityApi.createServiceAccount(req);
        assertThat(sa, notNullValue());
        assertThat(sa.getName(), is(req.getName()));
        assertThat(sa.getDescription(), is(req.getDescription()));
        assertThat(sa.getId(), notNullValue());
        assertThat(sa.getCreatedAt(), notNullValue());
        assertThat(sa.getOwner(), is("bob"));
        assertThat(sa.getClientId(), notNullValue());
        assertThat(sa.getClientSecret(), notNullValue());
        assertThat(sa.getKind(), is("ServiceAccount"));
        assertThat(sa.getHref(), is("/api/kafkas_mgmt/v1/service_accounts/" + sa.getId()));

        ServiceAccount testSa = securityApi.getServiceAccountById(sa.getId());
        assertThat(testSa, notNullValue());
        assertThat(testSa.getName(), is(sa.getName()));
        assertThat(testSa.getDescription(), is(sa.getDescription()));
        assertThat(testSa.getId(), is(sa.getId()));
        assertThat(testSa.getCreatedAt(), is(sa.getCreatedAt()));
        assertThat(testSa.getOwner(), is(sa.getOwner()));
        assertThat(testSa.getClientId(), is(sa.getClientId()));
        assertThat(testSa.getClientSecret(), nullValue());
        assertThat(testSa.getKind(), is(sa.getKind()));
        assertThat(testSa.getHref(), is(sa.getHref()));

        securityApi.deleteServiceAccountById(sa.getId());
        assertThat(securityApi.getServiceAccountById(sa.getId()), nullValue());
    }

    @Test
    void testList() throws ApiException {
        for (int i = 0; i < 10; i++) {
            ServiceAccountRequest req = new ServiceAccountRequest();
            req.setName("test-" + i);
            req.setDescription("foo-" + i);
            securityApi.createServiceAccount(req);
        }

        ServiceAccountList serviceAccountList = securityApi.getServiceAccounts();
        assertThat(serviceAccountList, notNullValue());
        assertThat(serviceAccountList.getItems(), hasSize(10));
        assertThat(serviceAccountList.getKind(), is("ServiceAccountList"));
        serviceAccountList.getItems().forEach(i -> {
            try {
                assertThat(i.getId(), notNullValue());
                ServiceAccount sa = securityApi.getServiceAccountById(i.getId());
                assertThat(sa, notNullValue());
                assertThat(sa.getName(), is(i.getName()));
                assertThat(sa.getDescription(), is(i.getDescription()));
                assertThat(sa.getId(), is(i.getId()));
                assertThat(sa.getCreatedAt(), is(i.getCreatedAt()));
                assertThat(sa.getOwner(), is(i.getOwner()));
                assertThat(sa.getClientId(), is(i.getClientId()));
                assertThat(sa.getKind(), is("ServiceAccount"));
                assertThat(sa.getHref(), is("/api/kafkas_mgmt/v1/service_accounts/" + sa.getId()));
            } catch (ApiException e) {
                fail(e);
            }
        });
    }

    @Test
    void testCreateStatic() throws ApiException {
        ServiceAccountRequest req = new ServiceAccountRequest();
        req.setName("static-sa");
        req.setDescription("foo");
        ServiceAccount sa = securityApi.createServiceAccount(req);
        assertThat(sa, notNullValue());
        assertThat(sa.getName(), is(req.getName()));
        assertThat(sa.getDescription(), is(req.getDescription()));
        assertThat(sa.getId(), notNullValue());
        assertThat(sa.getCreatedAt(), notNullValue());
        assertThat(sa.getOwner(), is("bob"));
        assertThat(sa.getClientId(), is("svc-static"));
        assertThat(sa.getClientSecret(), is("the-static-secret"));

        ServiceAccount testSa = securityApi.getServiceAccountById(sa.getId());
        assertThat(testSa, notNullValue());
        assertThat(testSa.getName(), is(sa.getName()));
        assertThat(testSa.getDescription(), is(sa.getDescription()));
        assertThat(testSa.getId(), is(sa.getId()));
        assertThat(testSa.getCreatedAt(), is(sa.getCreatedAt()));
        assertThat(testSa.getOwner(), is(sa.getOwner()));
        assertThat(testSa.getClientId(), is(sa.getClientId()));
        assertThat(testSa.getClientSecret(), nullValue());

        securityApi.deleteServiceAccountById(sa.getId());
        assertThat(securityApi.getServiceAccountById(sa.getId()), nullValue());
    }

}
