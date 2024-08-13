package com.app.CashLedger.config;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

public class JasperReportCompiler {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException("Two arguments are required: <input .jrxml file> <output .jasper file>");
            }

            String jrxmlFile = args[0];
            String jasperFile = args[1];

            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFile);
            JasperCompileManager.writeReportToXmlFile(jasperReport, jasperFile);

            System.out.println("Compiled Jasper report to: " + jasperFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}