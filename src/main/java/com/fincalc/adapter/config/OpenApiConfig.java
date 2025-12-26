package com.fincalc.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8002}")
    private int serverPort;

    @Bean
    public OpenAPI finCalcOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Numerai Finance API")
                        .description("Smart Financial Intelligence - Professional-grade loan calculations, compound interest projections, and tax estimations via Model Context Protocol (MCP)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Numerai Finance")
                                .email("support@fincalc.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }
}
