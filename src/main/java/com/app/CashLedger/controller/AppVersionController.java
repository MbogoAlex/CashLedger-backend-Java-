package com.app.CashLedger.controller;

import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.AppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static java.util.Map.of;

@RestController
@RequestMapping("/api/")
public class AppVersionController {
    private final AppVersionService appVersionService;
    @Autowired
    public AppVersionController(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;
    }
    @GetMapping("version")
    public ResponseEntity<Response> getAppVersion() {
        return buildResponse("version", appVersionService.getCurrentAppVersion(), "Version checked", HttpStatus.OK);
    }

    private ResponseEntity<Response> buildResponse(String desc, Object data, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Response.builder()
                        .timestamp(LocalDateTime.now())
                        .data(data == null ? null : of(desc, data))
                        .message(message)
                        .status(status)
                        .statusCode(status.value())
                        .build());
    }
}
