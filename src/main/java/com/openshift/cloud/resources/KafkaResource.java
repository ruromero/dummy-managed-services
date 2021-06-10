package com.openshift.cloud.resources;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.cloud.api.kas.models.KafkaRequest;
import com.openshift.cloud.api.kas.models.KafkaRequestList;
import com.openshift.cloud.api.kas.models.KafkaRequestPayload;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/kafkas")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class KafkaResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaResource.class);

    private Map<String, KafkaRequest> kafkas = new HashMap<>();

    @Inject
    JsonWebToken token;

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        if (kafkas.containsKey(id)) {
            LOGGER.info("Returning existing KafkaRequest {}", kafkas.get(id));
            return Response.ok(kafkas.get(id)).build();
        }
        LOGGER.info("Not found KafkaRequest {}", id);
        return Response.noContent().build();

    }

    @GET
    public Response get() {
        LOGGER.info("List KafkaRequest");
        KafkaRequestList list = new KafkaRequestList();
        kafkas.values().stream().forEach(kafka -> list.addItemsItem(kafka));
        return Response.ok(list).build();
    }

    @POST
    public KafkaRequest create(KafkaRequestPayload request) {
        LOGGER.info("Creating new KafkaRequest {}", request);
        String owner = (String) token.claim("preferred_username").orElse("unknown");
        KafkaRequest kafka = new KafkaRequest();
        kafka.id(UUID.randomUUID().toString());
        kafka.name(request.getName());
        kafka.bootstrapServerHost("http://" + kafka.getId() + ":9092");
        kafka.multiAz(request.getMultiAz());
        kafka.region(request.getRegion());
        kafka.cloudProvider(request.getCloudProvider());
        kafka.status("Provisioning");
        kafka.createdAt(ZonedDateTime.now().toOffsetDateTime());
        kafka.setOwner(owner);
        kafkas.put(kafka.getId(), kafka);
        CompletableFuture
                .delayedExecutor(5, TimeUnit.SECONDS)
                .execute(() -> kafka.status("Running"));
        return kafka;
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        LOGGER.info("Removing new KafkaRequest {}", id);
        kafkas.remove(id);
        return Response.noContent().build();
    }

}