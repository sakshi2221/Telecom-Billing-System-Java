import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import modules.CDR;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class CDRParser {
    public static String[] cdrdata = new String[1];

    public static CDR parseCDR(String fileName) throws IOException {
        CSVReader reader = null;
        String srcExt = "Cdr_Processor/CDRFiles/"+fileName;
        CDR cdrinfo = null;
        try {
            reader = new CSVReader(new FileReader(srcExt));
            String[] nextline;
            cdrinfo = new CDR();

            if((nextline=reader.readNext()) != null){

                cdrdata = nextline;

                /* for(String token : cdrdata) {
                    System.out.println(token);
                } */

                cdrinfo.setId(Integer.parseInt(cdrdata[0]));
                cdrinfo.setSource_msisdn(cdrdata[1]);
                cdrinfo.setTerminated_msisdn(cdrdata[2]);
                cdrinfo.setTimestamp(cdrdata[3]);
                cdrinfo.setDuration(Integer.parseInt(cdrdata[4]));
                cdrinfo.setRate(Integer.parseInt(cdrdata[5]));
                cdrinfo.setService_id(Integer.parseInt(cdrdata[6]));
                cdrinfo.setRatePlan_id(Integer.parseInt(cdrdata[7]));

            } else {
                System.out.println("File is Empty");
            }
            reader.close();

        }
        catch (FileNotFoundException ex){
            System.out.println(ex.toString());
            System.out.println("File Not Exist");
        }
        catch (IOException | CsvValidationException e){
            System.out.println("There is no new lines");
        }
        return cdrinfo;
    }

    public static void moveCDR(String fileName) throws IOException{

        String srcExt = "Cdr_Processor/CDRFiles/"+fileName;
        String desExt = "Cdr_Processor/ProcessedCDRs/"+fileName;
        Path mvCDR = Files.move(Paths.get(srcExt),Paths.get(desExt));

        if(mvCDR != null)
        {
            System.out.println("File renamed and moved successfully");
        }
        else
        {
            System.out.println("Failed to move the file");
        }
    }
}
