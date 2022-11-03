package com.nttdata.bootcamp.msmovement.infrastructure;

import com.nttdata.bootcamp.msmovement.config.WebClientConfig;
import com.nttdata.bootcamp.msmovement.model.Credit;
import com.nttdata.bootcamp.msmovement.util.Constants;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class CreditRepository {

    @Value("${local.property.host.ms-credits}")
    private String propertyHostMsCredits;

    @Autowired
    ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

	@CircuitBreaker(name = Constants.CREDIT_CB, fallbackMethod = "getDefaultCreditByCreditNumber")
    public Mono<Credit> findCreditByCreditNumber(String creditNumber) { //RACH Falta LoanNumber
        log.info("Inicio----findLastMovementByMovementNumber-------: ");
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsCredits + ":8084")
                .flatMap(d ->  webconfig.getWebclient().get().uri("/api/credits/creditNumber/" + creditNumber).retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                        .bodyToMono(Credit.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.just(new Credit())))
                );
    }

	public Mono<Credit> getDefaultCreditByCreditNumber(String creditNumber, Exception e) {
		log.info("Inicio----getDefaultCreditByCreditNumber-------creditNumber: " + creditNumber);
		return Mono.empty();
	}
}
