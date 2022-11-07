package com.nttdata.bootcamp.msmovement.producer.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * Class BalanceBankAccountModel.
 * Movement microservice class BalanceBankAccountModel.
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BalanceBankAccountModel {

    @JsonIgnore
    private String id;

    private String idBankAccount;

    private Double balance;
}
