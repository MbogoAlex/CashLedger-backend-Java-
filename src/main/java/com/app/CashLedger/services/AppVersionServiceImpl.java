package com.app.CashLedger.services;

import org.springframework.stereotype.Service;

@Service
public class AppVersionServiceImpl implements AppVersionService {
    @Override
    public Double getCurrentAppVersion() {
        Double appVersion = 79.0;
        return appVersion;
    }
}
