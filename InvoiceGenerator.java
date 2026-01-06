import modules.Bill_Info;
import modules.Users;
import modules.ContractCons;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceGenerator {
    static final String fileName = "Cdr_Processor/src/main/resources/JasperDesign.jrxml";
    static final String outFile = "Cdr_Processor/src/main/resources/Bills/";

    public static void generate(List<Bill_Info> invoice, Users user, String path) throws FileNotFoundException, JRException {
        Map<String, Object> parameter  = new HashMap<String, Object>();

        JRBeanCollectionDataSource userCollectionDataSource =
                new JRBeanCollectionDataSource(invoice);
        int total =TotalFeeHelper(invoice);
        parameter.put("studentDataSource", userCollectionDataSource);
        parameter.put("uname", user.getU_name());
        parameter.put("id", user.getNational_id());
        parameter.put("address", user.getAddress());
        parameter.put("title", "Monthly Invoice");
        parameter.put("totalFee", total);

        JasperReport jasperDesign = JasperCompileManager.compileReport(fileName);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperDesign, parameter,new JREmptyDataSource());

        File file = new File(outFile+path);
        OutputStream outputSteam = new FileOutputStream(file);
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputSteam);

        System.out.println("Report Generated!");
    }

    private static int TotalFeeHelper(List<Bill_Info> invoice) {
        int totalFee=0;
        for (Bill_Info contractCons:invoice){
            totalFee+=contractCons.getTotalFees();
        }
        return totalFee;
    }
}
