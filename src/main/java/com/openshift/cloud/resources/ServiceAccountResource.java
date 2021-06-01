package com.openshift.cloud.resources;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.cloud.api.models.ServiceAccount;
import com.openshift.cloud.api.models.ServiceAccountRequest;

import io.quarkus.security.Authenticated;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/serviceaccounts")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ServiceAccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountResource.class);
    private static final Random RND = new Random();

    private Map<String, ServiceAccount> accounts = new HashMap<>();

    @Inject
    JsonWebToken token;

    @GET
    @Path("/{name}")
    public Response get(@PathParam("name") String name) {
        if (accounts.containsKey(name)) {
            LOGGER.info("Returning existing ServiceAccount {}", accounts.get(name));
            return Response.ok(accounts.get(name)).build();
        }
        LOGGER.info("Not found ServiceAccount {}", name);
        return Response.noContent().build();

    }

    @POST
    @Path("/{name}/reset-credentials")
    public Response reset(@PathParam("name") String name) {
        if (accounts.containsKey(name)) {
            LOGGER.info("Reset credentials for existing ServiceAccount {}", accounts.get(name));
            return Response.ok(setCredentials(accounts.get(name))).build();
        }
        LOGGER.info("Not found ServiceAccount {}", name);
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    public ServiceAccount create(ServiceAccountRequest request) {
        LOGGER.info("Creating new ServiceAccount {}", request);
        String owner = (String) token.claim("preferred_username").orElse("unknown");
        ServiceAccount serviceAccount = new ServiceAccount();
        serviceAccount.id(UUID.randomUUID().toString());
        serviceAccount.name(request.getName());
        serviceAccount.description(request.getDescription());
        serviceAccount.createdAt(ZonedDateTime.now().toOffsetDateTime());
        serviceAccount.setOwner(owner);
        accounts.put(request.getName(), serviceAccount);
        return setCredentials(serviceAccount);
    }

    @DELETE
    @Path("/{name}")
    public Response delete(@PathParam("name") String name) {
        accounts.remove(name);
        return Response.noContent().build();
    }

    private ServiceAccount setCredentials(ServiceAccount original) {
        ServiceAccount acc = new ServiceAccount();
        acc.setOwner(original.getOwner());
        acc.setName(original.getName());
        acc.setId(original.getId());
        acc.setCreatedAt(original.getCreatedAt());
        acc.setDescription(original.getDescription());
        acc.setClientID("svc-acc" + RND.nextInt());
        acc.setClientSecret(UUID.randomUUID().toString());
        return acc;
    }
}