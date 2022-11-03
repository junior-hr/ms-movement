package com.nttdata.bootcamp.msmovement.infrastructure;

import com.nttdata.bootcamp.msmovement.config.WebClientConfig;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
import com.nttdata.bootcamp.msmovement.model.MobileWallet;
import com.nttdata.bootcamp.msmovement.util.Constants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class MobileWalletRepository {

    @Value("${local.property.host.ms-mobile-wallet}")
    private String propertyHostMsMobileWallet;

    @Autowired
    ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

    @CircuitBreaker(name = Constants.MOBILEWALLET_CB, fallbackMethod = "getDefaultMobileWalletByAccountNumber")
    public Mono<MobileWallet> findMobileWalletByAccountNumber(String cellphone) {
        log.info("Inicio----findMobileWalletByAccountNumber-------accountNumber: " + cellphone);
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsMobileWallet + ":8090")
                .flatMap(d -> webconfig.getWebclient().get().uri("/api/mobilewallet/cellphone/" + cellphone).retrieve()
                                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                                .bodyToMono(MobileWallet.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.just(new MobileWallet())) )
                );
    }

    @CircuitBreaker(name = Constants.MOBILEWALLET_CB, fallbackMethod = "getDefaultUpdateBalanceMobilWallet")
    public Mono<Void> updateBalanceMobilWallet(String idMobileWallet, Double balance) {
        log.info("--updateBalanceMobilWallet------- idMobileWallet: " + idMobileWallet);
        log.info("--updateBalanceMobilWallet------- profile: " + balance);
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsMobileWallet + ":8090")
                .flatMap(d -> webconfig.getWebclient().put()
                                .uri("/api/mobilewallet/" + idMobileWallet + "/balance/" + balance.toString() )
                                .accept(MediaType.APPLICATION_JSON).retrieve()
                                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                                .bodyToMono(Void.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.empty()))
                );
    }


    public Mono<MobileWallet> getDefaultMobileWalletByAccountNumber(String cellphone, Exception e) {
        log.info("Inicio----getDefaultMobileWalletByAccountNumber-------cellphone: " + cellphone);
        return Mono.empty();
    }

    public Mono<Void> getDefaultUpdateBalanceMobilWallet(String idMobileWallet, Double balance,  Exception e) {
        log.info("Inicio----getDefaultUpdateBalanceMobilWallet-------idMobileWallet: " + idMobileWallet);
        return Mono.empty();
    }
}
