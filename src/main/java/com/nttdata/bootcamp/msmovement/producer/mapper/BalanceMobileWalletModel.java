package com.nttdata.bootcamp.msmovement.producer.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class BalanceMobileWalletModel.
 * Movement microservice class BalanceMobileWalletModel.
 */
@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BalanceMobileWalletModel {

    @JsonIgnore
    private String id;

    private String idMobileWallet;

    private Double balance;
}
