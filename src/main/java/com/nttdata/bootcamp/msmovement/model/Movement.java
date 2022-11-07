package com.nttdata.bootcamp.msmovement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Movement.
 * Movement microservice class Movement.
 */
@Document(collection = "Movement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Movement {

    @Id
    private String idMovement;

    private String accountNumber;

    private String cellphone;

    @NotNull(message = "no debe estar nulo")
    private Integer numberMovement;

    @NotEmpty(message = "no debe estar vacío")
    private String movementType;

    @NotNull(message = "no debe estar nulo")
    private Double amount;

    private Double balance;

    @NotEmpty(message = "no debe estar vacio")
    private String currency;

    private LocalDateTime movementDate;

    private Credit credit;

    private String idBankAccount;

    private Loan loan;

    private Double commission;

    private BankAccount bankAccountTransfer;

}