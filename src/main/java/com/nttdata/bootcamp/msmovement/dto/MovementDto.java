package com.nttdata.bootcamp.msmovement.dto;

import com.nttdata.bootcamp.msmovement.exception.ResourceNotFoundException;
import com.nttdata.bootcamp.msmovement.model.BankAccount;
import com.nttdata.bootcamp.msmovement.model.Credit;
import com.nttdata.bootcamp.msmovement.model.MobileWallet;
import com.nttdata.bootcamp.msmovement.model.Movement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@ToString
@Builder
public class MovementDto {

    @Id
    private String idMovement;

    private String accountNumber;

    private String debitCardNumber;

    private Integer numberMovement;

    private Integer creditNumber;

    private Integer loanNumber;

    private String cellphone;

    @NotEmpty(message = "no debe estar vacío")
    private String movementType;

    @NotNull(message = "no debe estar nulo")
    private Double amount;

    private Double balance;

    @NotEmpty(message = "no debe estar vacio")
    private String currency;

    private Double commission;

    private String accountNumberForTransfer;

    public Mono<Boolean> validateMovementType() {
        log.info("ini validateMovementType-------: ");
        return Mono.just(this.getMovementType()).flatMap(mt -> {
            log.info("1 validateMovementType-------this.getMovementType() -> mt: " + mt);
            log.info("--validateMovementType-------this.getAccountNumberForTransfer(): " + (this.getAccountNumberForTransfer() == null ? "" : this.getAccountNumberForTransfer()));
            if (mt.equals("deposit")) { // deposito.
                log.info("--validateMovementType-------deposit: ");
                return Mono.just(true);
            } else if (mt.equals("withdrawal")) { // retiro.
                log.info("--validateMovementType-------withdrawal: ");
                return Mono.just(true);
            } else if (mt.equals("input-transfer")) { // transferencia de entrada.
                log.info("--validateMovementType-------input-transfer: ");
                if (this.getAccountNumberForTransfer() == null) {
                    return Mono.error(new ResourceNotFoundException("Número de Cuenta para transferencia", "AccountNumberForTransfer", ""));
                }
                return Mono.just(true);
            } else if (mt.equals("output-transfer")) { // transferencia de salida.
                log.info("--validateMovementType-------output-transfer: ");
                if (this.getAccountNumberForTransfer() == null) {
                    return Mono.error(new ResourceNotFoundException("Número de Cuenta para transferencia", "AccountNumberForTransfer", ""));
                }
                return Mono.just(true);
            } else if (mt.equals("payment")) { // pago.
                log.info("--validateMovementType-------payment: ");
                return Mono.just(true);
            } else {
                log.info("--validateMovementType-------no getMovementType: ");
                return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
            }
        });
    }

    public Mono<Boolean> validateMovementTypeCreditLoan() {
        log.info("Inicio validateMovementTypeCreditLoan-------: ");
        return Mono.just(this.getMovementType()).flatMap(ct -> {
            Boolean isOk = false;
            if (this.getMovementType().equals("payment")) { // pago.
                log.info("Fin validateMovementTypeCreditLoan-------: ");
                return this.validateCreditCardAndLoanPayment();
            } else if (this.getMovementType().equals("consumption")) { // consumo.
                log.info("Fin validateMovementTypeCreditLoan-------: ");
                return this.validateCreditCardAndLoanConsumption();
                //return Mono.just(isOk);
            } else {
                return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
            }
        });
    }

    public Mono<Double> validateAvailableAmount(BankAccount bankAccount, MobileWallet mobileWallet, MovementDto lastMovement, Boolean mainAccountOnly) {
        log.info("ini dto validateAvailableAmount-------: ");
        log.info("ini dto validateAvailableAmount-------mainAccountOnly: " + mainAccountOnly.toString());
        log.info("ini dto validateAvailableAmount-------: lastMovement.toString() " + lastMovement.toString());
        // log.info("ini dto validateAvailableAmount-------: bankAccount.toString() " + bankAccount.toString());
        log.info("ini dto validateAvailableAmount-------: this.getMovementType() " + this.getMovementType());
        return Mono.just(this.getMovementType()).flatMap(ct -> {
            Double bal = 0.0;
            log.info("dto-------ct: " + ct);

            if (lastMovement.getIdMovement() != null) { // Existe almenos un movimiento

                log.info("if1-----------------: ");
                if (this.getMovementType().equals("withdrawal") || this.getMovementType().equals("output-transfer") || this.getMovementType().equals("payment")) {
                    log.info("if2-----------------: ");
                    log.info("dto-------this.getAmount() -- lastMovement.getBalance(): " + this.getAmount() + " -- " + lastMovement.getBalance());
                    if (mainAccountOnly.equals(true)) {
                        log.info("if22-----------------mobileWallet.getAccount(): " + (mobileWallet.getAccount() != null ? mobileWallet.getAccount() : ""));
                        log.info("if2-----------------mainAccountOnly true: ");
                        Double setBalance = (mobileWallet != null ? mobileWallet.getAccount().getBalance() : mobileWallet.getBalance()) - this.getAmount();
                        if (setBalance < 0) {
                            return Mono.error(new ResourceNotFoundException("Monto", "Amount", this.getAmount().toString()));
                        } else {
                            this.setBalance(setBalance);
                        }
                    } else {
                        log.info("if2-----------------mainAccountOnly false: ");
                        Double setBalance = lastMovement.getBalance() - this.getAmount();
                        if (lastMovement.getBalance() < this.getAmount()) {
                            log.info("dto 1 if-------: ");
                            if (this.getDebitCardNumber() != null) {
                                bal = this.getAmount() - lastMovement.getBalance();
                            } else {
                                return Mono.error(new ResourceNotFoundException("Monto", "Amount", this.getAmount().toString()));
                            }
                        }
                        if (setBalance <= 0) {
                            this.setAmount(lastMovement.getBalance());
                            this.setBalance(0.0);
                        } else {
                            this.setBalance(setBalance);
                        }

                    }
                } else if (this.getMovementType().equals("deposit") || this.getMovementType().equals("input-transfer")) {

                    log.info("if11-----------------: ");
                    this.setBalance((mainAccountOnly.equals(true) ? mobileWallet.getBalance() : lastMovement.getBalance()) + this.getAmount());
                } else {
                    log.info("if1 else-----------------: ");
                    return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
                }

            } else { // No tiene movimientos y se usa el monto incial
                log.info("if2-----------------: ");
                if (this.getMovementType().equals("withdrawal") || this.getMovementType().equals("output-transfer") || this.getMovementType().equals("payment")) {
                    // log.info("dto 2-------this.getAmount() -- bankAccount.getStartingAmount(): " + this.getAmount() + " -- " + bankAccount.getStartingAmount());

                    if (mainAccountOnly.equals(true)) {
                        Double setBalance = (mobileWallet.getAccount() != null ? mobileWallet.getAccount().getBalance() : mobileWallet.getBalance()) - this.getAmount();
                        if (setBalance < 0) {
                            return Mono.error(new ResourceNotFoundException("Monto", "Amount", this.getAmount().toString()));
                        } else {
                            this.setBalance(setBalance);
                        }
                    } else {
                        Double setBalance = bankAccount.getStartingAmount() - this.getAmount();
                        if (bankAccount.getStartingAmount() < this.getAmount()) {
                            log.info("dto 2 if-------: ");
                            if (this.getDebitCardNumber() != null) {
                                bal = this.getAmount() - bankAccount.getBalance();
                            } else {
                                return Mono.error(new ResourceNotFoundException("Monto", "Amount", this.getAmount().toString()));
                            }
                        }
                        if (setBalance <= 0) {
                            this.setAmount(bankAccount.getStartingAmount());
                            this.setBalance(0.0);
                        } else {
                            this.setBalance(setBalance);
                        }
                    }
                    //this.setBalance(bankAccount.getStartingAmount() - this.getAmount());
                } else if (this.getMovementType().equals("deposit") || this.getMovementType().equals("input-transfer")) {

                    this.setBalance((mainAccountOnly.equals(true) ? mobileWallet.getBalance() : bankAccount.getStartingAmount()) + this.getAmount());
                } else {
                    return Mono.error(new ResourceNotFoundException("Tipo movimiento", "getMovementType", this.getMovementType()));
                }

            }
            log.info("fin validateAvailableAmount-------bal: " + bal);
            return Mono.just(bal);
        });
    }

    public Mono<Boolean> validateCreditCardAndLoanPayment() { //Validar Pago de Producto de Credito
        log.info("Inicio validateCreditCardAndLoanPayment-------: ");
        return Mono.just(this.getBalance()).flatMap(ct -> {
            Boolean isOk = false;
            if (this.getBalance() > 0.0) {
                if (this.getAmount() <= this.getBalance()) {
                    isOk = true;
                    //this.setBalance(this.getBalance()-this.getAmount());
                } else {
                    return Mono.error(new ResourceNotFoundException("Monto de movimiento de credito(Pago) supera el saldo por pagar"));
                }
            } else {
                return Mono.error(new ResourceNotFoundException("Movimiento Credito(Pago) no se puede realizar porque no tiene Saldo por pagar"));
            }
            log.info("Fin validateCreditCardAndLoanPayment-------: ");
            return Mono.just(isOk);
        });
    }

    public Mono<Boolean> validateCreditCardAndLoanConsumption() { //Validar consumo de saldo de limite de Credito
        log.info("Inicio validateCreditCardAndLoanConsumption-------: ");
        return Mono.just(this.getBalance()).flatMap(ct -> {
            Boolean isOk = false;
            if (this.getAmount() <= this.getBalance()) {
                isOk = true;
            } else {
                return Mono.error(new ResourceNotFoundException("Movimiento de credito(Consumo) supera tu saldo de tu linea de credito"));
            }
            log.info("Fin validateCreditCardAndLoanConsumption-------: ");
            return Mono.just(isOk);
        });
    }

    public Mono<Movement> MapperToMovement(Credit credit) {
        LocalDateTime date = LocalDateTime.now();
        log.info("ini validateMovementLimit-------: LocalDateTime.now()" + LocalDateTime.now());
        log.info("ini validateMovementLimit-------date: " + date);

        Movement movement = Movement.builder()
                .idMovement(this.getIdMovement())
                .credit(credit)
                .accountNumber(this.getAccountNumber())
                .movementType(this.getMovementType())
                .amount(this.getAmount())
                .balance(this.getBalance())
                .currency(this.getCurrency())
                .movementDate(date)
                .commission(this.getCommission())
                .cellphone(this.getCellphone())
                //.idCredit(this.getIdCredit())
                //.idBankAccount(this.getIdBankAccount())
                //.idLoan(this.getIdLoan())
                .build();

        return Mono.just(movement);
    }
}
