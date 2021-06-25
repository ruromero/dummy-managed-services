package com.openshift.cloud.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.openshift.cloud.api.kas.models.ServiceAccountRequest;
import com.openshift.cloud.service.ServiceAccountService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/service_accounts")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ServiceAccountResource {

    @Inject
    ServiceAccountService service;

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        var sa = service.get(id);
        if (sa == null) {
            return Response.noContent().build();
        }
        return Response.ok(sa).build();
    }

    @GET
    public Response list() {
        return Response.ok(service.list()).build();
    }

    @POST
    @Path("/{id}/reset-credentials")
    public Response reset(@PathParam("id") String id) {
        var sa = service.reset(id);
        if (sa == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(sa).build();
    }

    @POST
    public Response create(ServiceAccountRequest request) {
        return Response.ok(service.create(request)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        service.delete(id);
        return Response.noContent().build();
    }
}