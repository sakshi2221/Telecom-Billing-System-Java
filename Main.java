import modules.CDR;
import modules.SiteDAO;

import java.io.IOException;
import java.sql.SQLException;

class CDRProcessing implements Runnable{

    @Override
    public void run(){

            while (true) {
                try {
                    String filename = GetLastCDR.getLastModified();
                    if (filename != null) {
                        CDR cdrData = CDRParser.parseCDR(filename);
                        Rating.FIH(cdrData);
                        CDRParser.moveCDR(filename);
                        Thread.sleep(5000);
                    }
                } catch (SQLException | IOException | InterruptedException ex){
                    System.out.println("There is an Exception !!!");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                } catch (NullPointerException e){
                    System.out.println("There is no CDR Yet !!!");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }

        }
    }
}

public class Main {
    public static void main(String[] args) throws SQLException, IOException,InterruptedException {
        SiteDAO.connectToDB();
        //String filename = "cdr_1830208061";
        //String filename =  GetLastCDR.getLastModified();
        //CDR cdrData = CDRParser.parseCDR(filename);
        //Rating.FIH(cdrData);
        //CDRParser.moveCDR(filename);
        CDRProcessing obj = new CDRProcessing();
        Thread thread = new Thread(obj);
        thread.start();
    }
}
