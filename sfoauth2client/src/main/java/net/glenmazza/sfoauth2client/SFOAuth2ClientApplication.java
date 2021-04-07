package net.glenmazza.sfoauth2client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class SFOAuth2ClientApplication {

	public static void main(String[] args) {
		ApplicationContext applicationContext = SpringApplication.run(SFOAuth2ClientApplication.class, args);
		SalesforceService service = applicationContext.getBean(SalesforceService.class);
		service.makeCalls();
	}

}

@Service
class SalesforceService {

	@Autowired
	private WebClient webClient;

	@Value("${salesforce.api.base-url}")
	private String baseUrl;

	void makeCalls() {

		while (true) {
			String result = webClient
					.get()
					.uri(baseUrl + "/services/data/v50.0/query?q=SELECT+Name,Type+FROM+Account+LIMIT+1")
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.retrieve()
					.bodyToMono(String.class)
					// retry of 1: if access token expired, will be removed after
					// first failed call and obtained & used during second.
					// Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
					// and comparing results vs. retry of 0
					.retry(1)
					.block();

			System.out.println(result);

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				System.out.println("Interrupted");
				System.exit(0);
			}
		}
	}
}
