package com.nttdata.bootcamp.msmovement.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DebitCardDto {

    @Id
    private String idDebitCard;
    private String cardNumber;
    private Boolean isMainAccount;
    private Integer order;

}
