package com.redhat.demos.thoughts.resource;

import com.redhat.demos.thoughts.model.Thought;
import com.redhat.demos.thoughts.service.ThoughtEventService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/thoughts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ThoughtResource {

    @Inject
    ThoughtEventService eventService;

    @POST
    @Transactional
    public Response createThought(@Valid Thought thought) {
        thought.persist();
        eventService.publishThoughtCreated(thought);
        return Response.status(Response.Status.CREATED).entity(thought).build();
    }

    @GET
    @Path("/{id}")
    public Response getThought(@PathParam("id") UUID id) {
        Optional<Thought> thought = Thought.findByIdOptional(id);
        if (thought.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(thought.get()).build();
    }

    @GET
    public Response listThoughts(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        List<Thought> thoughts = Thought.findAll()
                .page(page, size)
                .list();
        return Response.ok(thoughts).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateThought(@PathParam("id") UUID id, @Valid Thought updatedThought) {
        Optional<Thought> existingThought = Thought.findByIdOptional(id);
        if (existingThought.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Thought thought = existingThought.get();
        thought.content = updatedThought.content;
        thought.persist();
        eventService.publishThoughtUpdated(thought);

        return Response.ok(thought).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteThought(@PathParam("id") UUID id) {
        Optional<Thought> thoughtOpt = Thought.findByIdOptional(id);
        if (thoughtOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Thought thought = thoughtOpt.get();
        thought.delete();
        eventService.publishThoughtDeleted(thought);

        return Response.noContent().build();
    }

    @GET
    @Path("/random")
    public Response getRandomThought() {
        Optional<Thought> randomThought = Thought.findRandom();
        if (randomThought.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(randomThought.get()).build();
    }

    @POST
    @Path("/thumbsup/{id}")
    @Transactional
    public Response thumbsUp(@PathParam("id") UUID id) {
        Optional<Thought> thoughtOpt = Thought.findByIdOptional(id);
        if (thoughtOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Thought thought = thoughtOpt.get();
        thought.thumbsUp++;
        thought.persist();

        return Response.ok(thought).build();
    }

    @POST
    @Path("/thumbsdown/{id}")
    @Transactional
    public Response thumbsDown(@PathParam("id") UUID id) {
        Optional<Thought> thoughtOpt = Thought.findByIdOptional(id);
        if (thoughtOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Thought thought = thoughtOpt.get();
        thought.thumbsDown++;
        thought.persist();

        return Response.ok(thought).build();
    }
}
