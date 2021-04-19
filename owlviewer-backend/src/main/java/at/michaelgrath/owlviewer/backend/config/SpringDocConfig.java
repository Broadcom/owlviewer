package at.michaelgrath.owlviewer.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Owlviewer API")
                        .description("API for working with ontologies defined with OWL")
                        .contact(getContact())
                        .version(getClass().getPackage().getImplementationVersion()));
    }

    private Contact getContact() {
        Contact contact = new Contact();
        contact.setName("Michael Grath");
        contact.setEmail("office@itmatters.at");
        contact.setUrl("http://www.michaelgrath.at");
        return contact;
    }
}

