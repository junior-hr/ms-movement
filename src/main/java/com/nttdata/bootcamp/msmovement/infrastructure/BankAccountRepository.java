package com.nttdata.bootcamp.msmovement.infrastructure;

import com.nttdata.bootcamp.msmovement.config.WebClientConfig;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
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
public class BankAccountRepository {

    @Value("${local.property.host.ms-bank-account}")
    private String propertyHostMsBankAccount;

    @Autowired
    ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

    @CircuitBreaker(name = Constants.BANKACCOUNT_CB, fallbackMethod = "getDefaultBankAccountByAccountNumber")
    public Mono<BankAccount> findBankAccountByAccountNumber(String accountNumber) {
        log.info("Inicio----findBankAccountByAccountNumber-------accountNumber: " + accountNumber);
        log.info("Inicio----findBankAccountByAccountNumber-------propertyHostMsBankAccount: " + propertyHostMsBankAccount);
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsBankAccount + ":8085")
                .flatMap(d -> webconfig.getWebclient().get().uri("/api/bankaccounts/accountNumber/" + accountNumber).retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                        .bodyToMono(BankAccount.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.just(new BankAccount())) )
                );
    }

    @CircuitBreaker(name = Constants.BANKACCOUNT_CB, fallbackMethod = "getDefaultBankAccountByDocumentNumberAndWithdrawalAmount")
    public Flux<BankAccount> findBankAccountByDocumentNumberAndWithdrawalAmount(String documentNumber, String cardNumber, Double withdrawalAmount) {

        log.info("ini----findMovementsByAccountNumber-------: ");
        WebClientConfig webconfig = new WebClientConfig();
        Flux<BankAccount> movements = webconfig.setUriData("http://" + propertyHostMsBankAccount + ":8085")
                .flatMap(d -> webconfig.getWebclient().get().uri("/api/bankaccounts/documentNumber/" + documentNumber + "/cardNumber/" + cardNumber + "/withdrawalAmount/" + withdrawalAmount).retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                        .bodyToFlux(BankAccount.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Flux.just(new BankAccount())))
                        .collectList()
                )
                .flatMapMany(iterable -> Flux.fromIterable(iterable));
        return movements;
    }

    //UPDATE RACH ELIMINAR
    @CircuitBreaker(name = Constants.BANKACCOUNT_CB, fallbackMethod = "getDefaultUpdateBalanceBankAccount")
    public Mono<Void> updateBalanceBankAccount(String idBankAccount, Double balance) {
        log.info("--updateBalanceBankAccount------- idBankAccount: " + idBankAccount);
        log.info("--updateBalanceBankAccount------- profile: " + balance);
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsBankAccount + ":8085")
                .flatMap(d -> webconfig.getWebclient().put()
                        .uri("/api/bankaccounts/" + idBankAccount + "/balance/" + balance)
                        .accept(MediaType.APPLICATION_JSON).retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                        .bodyToMono(Void.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.empty()))
                );
    }

    @CircuitBreaker(name = Constants.BANKACCOUNT_CB, fallbackMethod = "getDefaultBankAccountByDebitCardNumber")
    public Mono<BankAccount> findBankAccountByDebitCardNumber(String debitCardNumber) {
        log.info("Inicio----findBankAccountByDebitCardNumber-------debitCardNumber: " + debitCardNumber);
        WebClientConfig webconfig = new WebClientConfig();
        return webconfig.setUriData("http://" + propertyHostMsBankAccount + ":8085")
                .flatMap(d -> webconfig.getWebclient().get().uri("/api/bankaccounts/debitCardNumber/" + debitCardNumber).retrieve()
                        .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new Exception("Error 400")))
                        .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new Exception("Error 500")))
                        .bodyToMono(BankAccount.class)
                        // .transform(it -> reactiveCircuitBreakerFactory.create("parameter-service").run(it, throwable -> Mono.just(new BankAccount())) )
                );
    }

    public Mono<BankAccount> getDefaultBankAccountByAccountNumber(String accountNumber, Exception e) {
        log.info("Inicio----getDefaultBankAccountByAccountNumber-------accountNumber: " + accountNumber);
        return Mono.empty();
    }

    public Flux<BankAccount> getDefaultBankAccountByDocumentNumberAndWithdrawalAmount(String documentNumber,String cardNumber, Double withdrawalAmount,  Exception e) {
        log.info("Inicio----getDefaultBankAccountByDocumentNumberAndWithdrawalAmount-------documentNumber: " + documentNumber);
        return Flux.empty();
    }

    public Mono<Void> getDefaultUpdateBalanceBankAccount(String idBankAccount, Double balance,  Exception e) {
        log.info("Inicio----getDefaultUpdateBalanceBankAccount-------idBankAccount: " + idBankAccount);
        return Mono.empty();
    }

    public Mono<BankAccount> getDefaultBankAccountByDebitCardNumber(String debitCardNumber, Exception e) {
        log.info("Inicio----getDefaultBankAccountByDebitCardNumber-------debitCardNumber: " + debitCardNumber);
        return Mono.empty();
    }
}
