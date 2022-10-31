package com.nttdata.bootcamp.msmovement.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Client {

    @Id
    private String idClient;
    private String names;
    private String surnames;
    private String clientType;
    private String documentType;
    private String documentNumber;
    private Integer cellphone;
    private String email;
    private Boolean state;

}
