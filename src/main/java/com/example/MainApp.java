package com.example;

import org.apache.camel.main.Main;
import org.h2.tools.Server;


public class MainApp {
    public static void main(String[] args) throws Exception {
        // Iniciar consola web de H2 en http://localhost:8082
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
        Main main = new Main();
        main.configure().addRoutesBuilder(new MessageRoute());
        main.run();
    }
}
