package com.openshift.cloud.resources;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.openshift.cloud.ApiException;
import com.openshift.cloud.api.models.Error;
import com.openshift.cloud.api.models.KafkaRequest;
import com.openshift.cloud.api.models.KafkaRequestList;
import com.openshift.cloud.api.models.KafkaRequestPayload;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
class KafkaResourceTest extends AbstractResourceTest {

    @Test
    void testGet() throws ApiException {
        KafkaRequest kafka = api.getKafkaById("foo");
        assertThat(kafka, nullValue());
    }

    @Test
    void testCreateReadDelete() throws ApiException {
        KafkaRequestPayload req = new KafkaRequestPayload();
        req.setName("test");
        req.setCloudProvider("foo provider");
        req.setRegion("Teruel");
        req.setMultiAz(Boolean.TRUE);
        KafkaRequest kafka = api.createKafka(Boolean.TRUE, req);
        assertThat(kafka, notNullValue());
        assertThat(kafka.getName(), is(req.getName()));
        assertThat(kafka.getCloudProvider(), is(req.getCloudProvider()));
        assertThat(kafka.getMultiAz(), is(req.getMultiAz()));
        assertThat(kafka.getRegion(), is(req.getRegion()));
        assertThat(kafka.getId(), notNullValue());
        assertThat(kafka.getOwner(), is("bob"));
        assertThat(kafka.getCreatedAt(), notNullValue());

        assertThat(kafka.getBootstrapServerHost(), notNullValue());
        assertThat(kafka.getStatus(), is("Provisioning"));

        KafkaRequest other = api.getKafkaById(kafka.getId());
        assertThat(other, notNullValue());
        assertThat(other.getName(), is(kafka.getName()));
        assertThat(other.getCloudProvider(), is(kafka.getCloudProvider()));
        assertThat(other.getMultiAz(), is(kafka.getMultiAz()));
        assertThat(other.getRegion(), is(kafka.getRegion()));
        assertThat(other.getId(), is(kafka.getId()));
        assertThat(other.getOwner(), is(kafka.getOwner()));
        assertThat(other.getCreatedAt(), is(kafka.getCreatedAt()));
        assertThat(other.getBootstrapServerHost(), is(kafka.getBootstrapServerHost()));
        assertThat(other.getStatus(), is(kafka.getStatus()));

        Error error = api.deleteKafkaById(other.getId(), Boolean.TRUE);
        assertThat(error, nullValue());
    }

    @Test
    void testList() throws ApiException {
        List<KafkaRequest> kafkas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            KafkaRequestPayload req = new KafkaRequestPayload();
            req.setName("test" + i);
            req.setCloudProvider("foo provider" + i);
            req.setRegion("Teruel" + i);
            req.setMultiAz(Boolean.TRUE);
            kafkas.add(api.createKafka(Boolean.FALSE, req));
        }

        KafkaRequestList list = api.listKafkas(null, null, null, null);
        assertThat(list, notNullValue());
        assertThat(list.getItems(), hasSize(10));
        list.getItems().stream().forEach(i -> {
            try {
                KafkaRequest kafka = api.getKafkaById(i.getId());
                assertThat(kafka, notNullValue());
                assertThat(kafka.getName(), is(i.getName()));
                assertThat(kafka.getCloudProvider(), is(i.getCloudProvider()));
                assertThat(kafka.getMultiAz(), is(i.getMultiAz()));
                assertThat(kafka.getRegion(), is(i.getRegion()));
                assertThat(kafka.getId(), notNullValue());
                assertThat(kafka.getOwner(), is("bob"));
                assertThat(kafka.getCreatedAt(), notNullValue());
            } catch (ApiException e) {
                fail(e);
            }
        });
    }
}
