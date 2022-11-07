package com.nttdata.bootcamp.msmovement.model;

import org.springframework.data.annotation.Id;
import javax.validation.constraints.NotEmpty;
import com.nttdata.bootcamp.msmovement.dto.DebitCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class BankAccount.
 * Movement microservice class BankAccount.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BankAccount {

    @Id
    private String idBankAccount;

    private Client client;

    @NotEmpty(message = "no debe estar vacío")
    private String accountType;

    // private String cardNumber;
    private DebitCardDto debitCard;

    @NotEmpty(message = "no debe estar vacío")
    private String accountNumber;

    private Double commission;

    private Integer movementDate;

    private Integer maximumMovement;

    private Double startingAmount;

    private Double transactionLimit;

    private Double commissionTransaction;

    @NotEmpty(message = "no debe estar vacío")
    private String currency;

    private Double minimumAmount;

    private Double balance;

}
