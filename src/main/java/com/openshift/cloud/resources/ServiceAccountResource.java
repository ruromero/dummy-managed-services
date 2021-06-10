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

import com.openshift.cloud.api.kas.models.ServiceAccount;
import com.openshift.cloud.api.kas.models.ServiceAccountList;
import com.openshift.cloud.api.kas.models.ServiceAccountListItem;
import com.openshift.cloud.api.kas.models.ServiceAccountRequest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/serviceaccounts")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ServiceAccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountResource.class);
    private final Random rnd = new Random();

    private Map<String, ServiceAccount> accounts = new HashMap<>();

    @Inject
    JsonWebToken token;

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        if (accounts.containsKey(id)) {
            LOGGER.info("Returning existing ServiceAccount {}", accounts.get(id));
            return Response.ok(accounts.get(id)).build();
        }
        LOGGER.info("Not found ServiceAccount {}", id);
        return Response.noContent().build();

    }

    @GET
    public Response get() {
        LOGGER.info("List ServiceAccounts");
        ServiceAccountList list = new ServiceAccountList();
        accounts.values().stream().forEach(sa -> list.addItemsItem(toListItem(sa)));
        return Response.ok(list).build();
    }

    private ServiceAccountListItem toListItem(ServiceAccount sa) {
        ServiceAccountListItem item = new ServiceAccountListItem();
        item.setId(sa.getId());
        item.setName(sa.getName());
        item.setDescription(sa.getDescription());
        item.setCreatedAt(sa.getCreatedAt());
        item.setOwner(sa.getOwner());
        item.setHref(sa.getHref());
        item.setKind(sa.getKind());
        item.setClientID(sa.getClientID());
        return item;
    }

    @POST
    @Path("/{id}/reset-credentials")
    public Response reset(@PathParam("id") String id) {
        if (accounts.containsKey(id)) {
            LOGGER.info("Reset credentials for existing ServiceAccount {}", accounts.get(id));
            return Response.ok(setCredentials(accounts.get(id))).build();
        }
        LOGGER.info("Not found ServiceAccount {}", id);
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
        accounts.put(serviceAccount.getId(), serviceAccount);
        return setCredentials(serviceAccount);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        accounts.remove(id);
        return Response.noContent().build();
    }

    private ServiceAccount setCredentials(ServiceAccount original) {
        ServiceAccount acc = new ServiceAccount();
        acc.setOwner(original.getOwner());
        acc.setName(original.getName());
        acc.setId(original.getId());
        acc.setCreatedAt(original.getCreatedAt());
        acc.setDescription(original.getDescription());
        acc.setClientID("svc-acc" + rnd.nextInt());
        acc.setClientSecret(UUID.randomUUID().toString());
        return acc;
    }
}