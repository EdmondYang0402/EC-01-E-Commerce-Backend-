package com.ec01.controller;

import com.ec01.exception.PaymentNotificationException;
import com.ec01.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/alipay")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notify(@RequestParam Map<String, String> params) {
        try {
            paymentService.handleAlipayNotify(params);
            return "success";
        } catch (PaymentNotificationException exception) {
            log.warn("Alipay notification rejected: {}", exception.getMessage());
            return "failure";
        } catch (Exception exception) {
            log.error("Alipay notification processing failed", exception);
            return "failure";
        }
    }
}
