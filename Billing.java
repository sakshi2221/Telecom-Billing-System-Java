import modules.Bill_Info;
import modules.ContractCons;
import modules.SiteDAO;
import modules.Users;
import net.sf.jasperreports.engine.JRException;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Billing {

    public static Bill_Info billCycle(String msisdn) throws SQLException {
        Bill_Info contractBill = new Bill_Info();
        //Apply Bill Cycle Queries to retrieve required Data
        ContractCons cont = SiteDAO.instanceData.getUserRatePlaneInfo(msisdn);

        int acv = SiteDAO.instanceData.getAddConsumedUnits(msisdn,1); // Additional Consumed Unit for OnNet voice
        int racv = SiteDAO.instanceData.getRatedAddUnits(msisdn,1); // Total Rating for Additional Consumed Unit for OnNet voice

        int acc = SiteDAO.instanceData.getAddConsumedUnits(msisdn,2); // Additional Consumed Unit for CrossNet voice
        int racc = SiteDAO.instanceData.getRatedAddUnits(msisdn,2); // Total Rating for Additional Consumed Unit for CrossNet voice

        int acd = SiteDAO.instanceData.getAddConsumedUnits(msisdn,3); // Additional Consumed Unit for Data
        int racd = SiteDAO.instanceData.getRatedAddUnits(msisdn,3); // Total Rating for Additional Consumed Unit for Data

        int acs = SiteDAO.instanceData.getAddConsumedUnits(msisdn,4); // Additional Consumed Unit for SMS
        int racs = SiteDAO.instanceData.getRatedAddUnits(msisdn,4); // Total Rating for Additional Consumed Unit for SMS

        int acr = SiteDAO.instanceData.getAddConsumedUnits(msisdn,5); // Additional Consumed Unit for Roaming
        int racr = SiteDAO.instanceData.getRatedAddUnits(msisdn,5); // Total Rating for Additional Consumed Unit for Roaming

        // Calculations
        int totalExtraFees = (racv + racc + racd + racs + racr)/100;
        int totalBillFees = cont.getFee() + totalExtraFees;

        // Mapping values to the class
        contractBill.setMsisdn(msisdn);
        contractBill.setMonthlyFees(cont.getFee());
        contractBill.setRateplane(cont.getRateplaneName());

        contractBill.setExtraConsumedVoice(acv);
        contractBill.setRatedExtraVoice(racv/100);

        contractBill.setExtraConsumedCross(acc);
        contractBill.setRatedExtraCross(racc/100);

        contractBill.setExtraConsumedData(acd);
        contractBill.setRatedExtraData(racd/100);

        contractBill.setExtraConsumedSMS(acs);
        contractBill.setRatedExtraSMS(racs/100);

        contractBill.setExtraConsumedRoaming(acr);
        contractBill.setRatedExtraRoaming(racr/100);

        contractBill.setExtraFees(totalExtraFees);
        contractBill.setTotalFees(totalBillFees);

        return contractBill;
    }

    public static List<Bill_Info> generateBillsForUser(int NID) throws SQLException, JRException, FileNotFoundException {

        List<Bill_Info> numbersBills = new ArrayList<>();
        List<String> userNumbers = SiteDAO.instanceData.getUserMSISDNs(NID);
        for (String num: userNumbers) {
            System.out.println(num);
        }

        for (String msisdn: userNumbers) {
            numbersBills.add(Billing.billCycle(msisdn));
        }

        //System.out.println(numbersBills.get(0).getRatedExtraCross());
        Users user=SiteDAO.instanceData.getUser(NID);
        String fileName = "Reports_"+NID+".pdf";
        InvoiceGenerator.generate(numbersBills,user,fileName);

        return numbersBills;
    }

    public static void generateUsersBills () throws SQLException,JRException, FileNotFoundException {
        List<Users> users = SiteDAO.instanceData.getUsers();
        for (Users us :users)
        {
            Billing.generateBillsForUser(us.getNational_id());
        }

    }

    public static void main(String[] args) throws SQLException, JRException, FileNotFoundException {
        SiteDAO.connectToDB();
        Billing.generateUsersBills();
    }
}
