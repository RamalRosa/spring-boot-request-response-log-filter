package com.wallet.flowpay.models.test;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TestRequest {
    private String userName;
    private String password;
}
