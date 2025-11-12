package com.wallet.flowpay.controllers;

import com.wallet.flowpay.models.test.TestRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {
    @PostMapping("/hello")
//    public Map<String, String> hello(@RequestBody @Valid TestRequest testRequest) {
//        HashMap<String, String> map = new HashMap<>();
//        map.put("greeting", "Hello, " + testRequest.getName() + " !");
//        return map;
//    }

    public TestRequest hello(@RequestBody @Valid TestRequest testRequest) {
        return testRequest;
    }
}
