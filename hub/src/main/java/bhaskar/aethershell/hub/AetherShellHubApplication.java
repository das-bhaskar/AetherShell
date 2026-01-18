package bhaskar.aethershell.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.Banner;

@SpringBootApplication
public class AetherShellHubApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AetherShellHubApplication.class);

		// Disable the ASCII Spring Banner
		app.setBannerMode(Banner.Mode.OFF);

		// Silence the technical startup logs
		System.setProperty("spring.main.log-startup-info", "false");
		System.setProperty("logging.level.root", "INFO");

		app.run(args);
	}

}
