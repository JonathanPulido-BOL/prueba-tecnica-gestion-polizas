package com.bolivar.polizas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI (Swagger UI).
 * Expone la información general de la API en /swagger-ui.html y /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gestionPolizasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Pólizas")
                        .description("API REST para la gestión de pólizas de arrendamiento "
                                + "(individuales y colectivas) y sus riesgos. "
                                + "Todas las operaciones requieren el header 'api-key'.")
                        .version("v1")
                        .contact(new Contact().name("Jonathan Pulido")));
    }
}
