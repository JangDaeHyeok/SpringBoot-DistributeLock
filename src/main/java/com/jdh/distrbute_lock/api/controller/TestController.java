package com.jdh.distrbute_lock.api.controller;

import com.jdh.distrbute_lock.api.application.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @GetMapping("test")
    public void test() {
        testService.test("test");
    }

}
