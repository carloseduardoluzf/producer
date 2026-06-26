package org.acme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;

@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    @Inject
    AgroalDataSource dataSource;

    @Inject
    @Channel("orders-out")
    Emitter<String> emitter;

    @POST
    @Transactional
    public Response createOrder(OrderRequest req) throws Exception {
        long id;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO orders(product, qty, price) VALUES (?, ?, ?) RETURNING id")) {
            ps.setString(1, req.product());
            ps.setInt(2, req.qty());
            ps.setBigDecimal(3, req.price());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                id = rs.getLong("id");
            }
        }

        String event = "{\"id\":%d,\"product\":\"%s\",\"qty\":%d}".formatted(id, req.product(), req.qty());
        emitter.send(event);
        LOG.infof("Pedido %d criado e publicado no Kafka TAM DESAFIO", id);

        return Response.status(Response.Status.CREATED).entity(Map.of("id", id)).build();
    }

    @GET
    public List<OrderView> listOrders() throws Exception {
        List<OrderView> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, product, qty, price FROM orders ORDER BY id");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(new OrderView(
                        rs.getLong("id"),
                        rs.getString("product"),
                        rs.getInt("qty"),
                        rs.getBigDecimal("price")));
            }
        }
        return orders;
    }
}
