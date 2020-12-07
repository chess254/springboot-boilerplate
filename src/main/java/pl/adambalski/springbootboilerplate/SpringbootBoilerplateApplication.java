package pl.adambalski.springbootboilerplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pl.adambalski.springbootboilerplate.logger.Logger;
import pl.adambalski.springbootboilerplate.security.SecurityConfiguration;

/**
 * Class that starts the project<br><br>
 *
 * @author Adam Balski
 */
@SpringBootApplication
public class SpringbootBoilerplateApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(SpringbootBoilerplateApplication.class, args);
        Logger logger = applicationContext.getBean("slf4jLogger", Logger.class);

        logJwtKey(logger);
    }

    private static void logJwtKey(Logger logger) {
        logger.log("Key used to make JWTs: " + SecurityConfiguration.KEY, SpringbootBoilerplateApplication.class);
    }


}
