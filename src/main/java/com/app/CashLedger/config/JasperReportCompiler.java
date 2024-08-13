package com.app.CashLedger.config;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

public class JasperReportCompiler {

    public static void main(String[] args) {
        try {
            // Path to your .jrxml file
            String sourceFile = "src/main/resources/templates/AllTransactionsReport.jrxml";

            // Path to output the .jasper file
            String outputFile = "src/main/resources/templates/AllTransactionsReport.jasper";

            // Compile the .jrxml file
            JasperCompileManager.compileReportToFile(sourceFile, outputFile);

            System.out.println("Report compiled successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}