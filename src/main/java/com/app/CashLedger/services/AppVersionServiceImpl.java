package com.app.CashLedger.services;

import org.springframework.stereotype.Service;

@Service
public class AppVersionServiceImpl implements AppVersionService {
    @Override
    public Double getCurrentAppVersion() {
        return 96.0;
    }
}
