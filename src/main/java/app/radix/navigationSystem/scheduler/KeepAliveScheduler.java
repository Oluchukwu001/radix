package app.radix.navigationSystem.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final RestTemplate restTemplate;

    @Value("${app.self-ping-url}")
    private String pingUrl;

    public KeepAliveScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 60000) // every 15,000 ms = 15 seconds
    public void pingSelf() {
        try {
            String response = restTemplate.getForObject(pingUrl, String.class);
            logger.info("Self-ping successful: {}", response);
        } catch (Exception e) {
            logger.warn("Self-ping failed: {}", e.getMessage());
        }
    }
}