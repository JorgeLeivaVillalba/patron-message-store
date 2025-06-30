package com.example;

import org.apache.camel.builder.RouteBuilder;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class MessageRoute extends RouteBuilder { // Ruta del mensaje

    @Override
    public void configure() throws Exception {

        DataSource ds = setupDataSource();
        getContext().getRegistry().bind("myDataSource", ds);

        // Crear tabla al iniciar
        new MessageStoreService(ds).createTable();

        from("timer:messageTimer?period=2000") //Timer que genera un mensaje cada 2 segundos
            .routeId("generateMessage") //Id de la ruta
            .setBody().simple("Mensaje generado en fecha: ${date:now:yyyy-MM-dd HH:mm:ss}") // Set para el cuerpo del mensaje
            .setHeader("messageId", simple("${id}")) // ID del mensaje
            .setHeader("timestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}")) //Timestamp
            .multicast() // Multicast para enviar a múltiples destinos
                .to("direct:mainFlow", "direct:storeMessage"); //destinos establecidos

        from("direct:mainFlow") // Mensaje en el flujo principal
            .log("Mensaje principal recibido: ${body}");

        from("direct:storeMessage") // Mensaje para almacenar en la base de datos
            .bean(new MessageStoreService(ds), "storeMessage");
    }

    private DataSource setupDataSource() { // Configuración de la fuente de datos H2 en memoria
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:messagedb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
