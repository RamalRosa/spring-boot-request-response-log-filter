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
    public Map<String, Object> hello(@RequestBody @Valid TestRequest testRequest) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("greeting", "Hello world !");
        map.put("data", testRequest);
        return map;
    }

    @PostMapping("/doc")
    public Map<String, Object> hello() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", "ok");
        map.put("data", new Byte[1000]);
        return map;
    }
}
