package com.example;

import org.apache.camel.builder.RouteBuilder;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class MessageRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        DataSource ds = setupDataSource();
        getContext().getRegistry().bind("myDataSource", ds);

        // Crear tabla al iniciar
        new MessageStoreService(ds).createTable();

        from("timer:messageTimer?period=2000")
            .routeId("generateMessage")
            .setBody().simple("Mensaje generado en fecha: ${date:now:yyyy-MM-dd HH:mm:ss}")
            .setHeader("messageId", simple("${id}"))
            .setHeader("timestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .multicast()
                .to("direct:mainFlow", "direct:storeMessage");

        from("direct:mainFlow")
            .log("Mensaje principal recibido: ${body}");

        from("direct:storeMessage")
            .bean(new MessageStoreService(ds), "storeMessage");
    }

    private DataSource setupDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:messagedb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
