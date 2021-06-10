package com.openshift.cloud.resources;

import java.util.ArrayList;
import java.util.List;

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

}
