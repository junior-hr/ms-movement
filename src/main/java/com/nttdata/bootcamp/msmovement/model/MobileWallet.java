package com.nttdata.bootcamp.msmovement.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.nttdata.bootcamp.msmovement.dto.DebitCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * Class MobileWallet.
 * Movement microservice class MobileWallet.
 */
@Document(collection = "MobileWallet")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MobileWallet {

    @Id
    private String idMobileWallet;

    private Client client;

    private DebitCardDto debitCard;

    private Double balance;

    private BankAccount account;
}
