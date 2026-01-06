import modules.CDR;
import modules.Contract;
import modules.RatePlane;
import modules.SiteDAO;

import java.sql.SQLException;
import java.util.List;


public class Rating {

    public static void FIH(CDR cdrData) throws SQLException {
        //knows the type of the service then pass it to RIH
        String serviceType = "";

        if (cdrData.getService_id() == 1) {
            serviceType = "voice";
        } else if (cdrData.getService_id() == 2) {
            serviceType = "cross";
        } else if (cdrData.getService_id() == 3) {
            serviceType = "data";
        } else if (cdrData.getService_id() == 4) {
            serviceType = "sms";
        } else if (cdrData.getService_id() == 5) {
            serviceType = "roaming";
        } else {
            try {
                throw new Exception("Service Not Exist !!, Check the CDR generator");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Print: " + serviceType);
        RIH(cdrData, serviceType);
    }

    public static void RIH(CDR cdr, String typeOfService) throws SQLException {
        //rate the service that user consumed by the help of billingDB
        //========================================================
        //1- Check the contract table and check units based on service
        //2- rate the service (Units based on service) & LE

        int overUnits = 0;
        boolean preRated =false;
        RatePlane uRatePlane;
        List<RatePlane> RatePlanes = SiteDAO.instanceData.getRatePlane(cdr.getRatePlan_id());
        Contract contract = SiteDAO.instanceData.getContract(cdr.getSource_msisdn());


        if (contract == null) {
            System.out.println("the contract not found in contract table");
            return;
        }
        System.out.println(" ==> Blance Under test: " + contract.getCurrent_voice());

        if (RatePlanes == null) {
            System.out.println("the ratePlane id is wrong");
            return;
        }else {
            uRatePlane=RatePlanes.get(0);
        }

        if (cdr.getRate()!=0){
            preRated=true;
            CCH(cdr,overUnits,preRated);
            return;
        }
        switch (typeOfService) {
            case "voice":
                int availableMin = contract.getCurrent_voice();
                int consumedMin = cdr.getDuration();
                int remainder = availableMin - consumedMin;
                if (remainder > 0 || remainder == 0) {
                    cdr.setRate(0);
                } else {
                    if (availableMin == 0) {
                        int extraRate = consumedMin * uRatePlane.getAdditional_minutes_service();
                        cdr.setRate(extraRate);
                    } else if (availableMin > 0) {
                        overUnits = availableMin;
                        int restConsumedMins = consumedMin - availableMin;
                        int extraRate = restConsumedMins * uRatePlane.getAdditional_minutes_service();
                        cdr.setRate(extraRate);
                    }
                }
                System.out.println(overUnits);
                System.out.println("(RIH)the rate in onNet is: " + cdr.getRate());
                CCH(cdr, overUnits,preRated);

                break;
            case "cross":
                int availableCrossMin = contract.getCurrent_cross_voice();
                int consumedCrossMin = cdr.getDuration();
                int crossRemainder = availableCrossMin - consumedCrossMin;
                if (crossRemainder > 0 || crossRemainder == 0) {
                    cdr.setRate(0);
                } else {
                    if (availableCrossMin == 0) {
                        int extraRate = consumedCrossMin * uRatePlane.getAdditional_minutes_service();
                        cdr.setRate(extraRate);
                    } else if (availableCrossMin > 0) {
                        System.out.println("===== > balance not eq zero");
                        overUnits = availableCrossMin;
                        int restConsumedMins = consumedCrossMin - availableCrossMin;
                        int extraRate = restConsumedMins * uRatePlane.getAdditional_minutes_service();
                        cdr.setRate(extraRate);
                    }
                }
                System.out.println("(RIH)the rate in crossNet is: " + cdr.getRate());
                CCH(cdr, overUnits,preRated);
                break;

            case "data":
                int availableMegaBits = contract.getCurrent_data();
                int consumedMegaBits = cdr.getDuration();
                int dataRemainder = availableMegaBits - consumedMegaBits;
                if (dataRemainder > 0 || dataRemainder == 0) {
                    cdr.setRate(0);
                } else {
                    if (availableMegaBits == 0) {
                        int extraRate = consumedMegaBits * uRatePlane.getAdditional_data_service();
                        cdr.setRate(extraRate);
                    } else if (availableMegaBits > 0) {
                        overUnits = availableMegaBits;
                        int restConsumedMBs = consumedMegaBits - availableMegaBits;
                        int extraRate = restConsumedMBs * uRatePlane.getAdditional_data_service();
                        cdr.setRate(extraRate);
                    }
                }
                System.out.println("(RIH)the rate in data is: " + cdr.getRate());
                CCH(cdr, overUnits,preRated);
                break;

            case "sms":
                int smsCount = cdr.getDuration();
                int availableSms = contract.getCurrent_sms();
                int restSms = availableSms - smsCount;
                if (restSms > 0 || restSms == 0) { //the user consumed service inside his bundle
                    cdr.setRate(0);
                } else { //the user exceeded his bundle
                    int additionalRate=0;
                    if (availableSms > 0) { //there is remained sms for that user
                        overUnits = availableSms;
                        additionalRate = (smsCount - availableSms) * uRatePlane.getAdditional_sms_service();
                    } else if (availableSms == 0){  // there is no sms available for that user
                        additionalRate = smsCount * uRatePlane.getAdditional_sms_service();
                    }
                    cdr.setRate(additionalRate);
                }
                System.out.println("(RIH)the rate in sms is: " + cdr.getRate());
                CCH(cdr, overUnits,preRated);
                break;

            case "roaming":
                int consumedRoamingMinutes = cdr.getDuration();
                int availableRoamingMinutes = contract.getCurrent_roaming();
                int restRoamingMinutes = availableRoamingMinutes - consumedRoamingMinutes;
                if (restRoamingMinutes > 0 || restRoamingMinutes == 0) { //the user consumed service inside his bundle
                    cdr.setRate(0);
                } else { //the user exceeded his bundle
                    int additionalRate=0;
                    if (availableRoamingMinutes > 0) { //there is remained sms for that user
                        overUnits = availableRoamingMinutes;
                        additionalRate = (consumedRoamingMinutes - availableRoamingMinutes) * uRatePlane.getAdditional_roaming_service();
                    } else if (availableRoamingMinutes == 0){  // there is no sms available for that user
                        additionalRate = consumedRoamingMinutes * uRatePlane.getAdditional_roaming_service();
                    }
                    cdr.setRate(additionalRate);
                }
                System.out.println("(RIH)the rate in roaming is: " + cdr.getRate());
                CCH(cdr, overUnits,preRated);
                break;
            default:
                break;
        }
    }

    public static void CCH(CDR cdr, int special, boolean preRated) throws SQLException {

        Integer discount = SiteDAO.instanceData.getDiscount(cdr.getSource_msisdn());

        int OldRate = cdr.getRate();
        int OldDuration = cdr.getDuration();

        float NewRate = 0 , NewDuration = 0;
        if (discount == -1 || discount==0) {
            RLH(cdr,(int) NewDuration,special);
        } else {
            if (OldRate != 0) {
                System.out.println("in oldrate condition");
                NewRate = ((float) OldRate * (1 - ((float) discount / 100)));
                System.out.println(NewRate);
                cdr.setRate((int) NewRate);
            } else {
                NewDuration = ((float) OldDuration * (1 - ((float) discount / 100)));
            }

            System.out.println("CCH newDuration"+NewDuration+" rating :"+cdr.getRate());
            RLH(cdr, (int)NewDuration,special);
           // System.out.println("CCH newDuration"+NewDuration+" rating :"+cdr.getRate());
        }
    }

    public static void RLH(CDR cdr, Integer nDuration,int special) throws SQLException {
        int Service_type = cdr.getService_id();
        int FreeU = SiteDAO.instanceData.getAddFreeUnits(cdr.getSource_msisdn());
        RatePlane currentRatePlan = SiteDAO.instanceData.getRatePlane(cdr.getRatePlan_id()).get(0);
        String str = serviceTypeMapping(Service_type);
        int units = SiteDAO.instanceData.getUnits(cdr.getSource_msisdn(), str);
        int oldDuration = cdr.getDuration();
        int nFree = 0, price = 0;
        System.out.println(FreeU);
        //CHECK IF THERE IS FREE SP
        if (FreeU != -1 && FreeU != 0) {
            // IF THERE IS FREE UNITS CHECK IF THE CDR IS  RATED OR NOT
            if (cdr.getRate() == 0) {
                //IF NOT RATED AND NO DISCOUNT ON DURATION THEN DEDUCE THE DURATION
                if (nDuration == 0) nDuration = oldDuration;
                // CHECK IF WHETHER FREE UNITS IS GREATER THAN DURATION OR NOT
                if (nDuration > FreeU) nDuration = nDuration - FreeU;
                else {
                    // CALC THE REMIND FREE UNITS TO BE UPDATED IN THE DATABASE
                    nFree = FreeU - nDuration;
                    nDuration = 0;
                }

            } else {
                // IF THE CDR IS RATED CHECK THEN RERATE THE CDR
                if (nDuration == 0) nDuration = oldDuration;// THEN THERE IS NO DISCOUNT
                if (nDuration > FreeU) {
                    nDuration = nDuration - FreeU;
                    // GET THE EXTERNAL CHARGE OF ADDITIONAL UNITS
                    price = getPrice(currentRatePlan, Service_type);
                    // SET THE NEW RATE IN CDR
                    cdr.setRate(price * nDuration);
                    // RE ASSIGN THE NDURATION TO ZERO
                    nDuration = 0;
                } else {
                    nFree = FreeU - oldDuration;
                    cdr.setRate(0);

                }
                if (special !=0 ){
                    nDuration=special;
                }
            }

        }else{
            if (cdr.getRate() == 0) {
                nDuration = oldDuration;
            }else {
                if (special !=0 ){
                    nDuration=special;
                }
            }
        }

        //CHECK IF THE REST OF UNITS IN MAIN BUNDLE
        units = units - nDuration;
        //SET UNITS IN CONTRACT TABLE
        SiteDAO.instanceData.setUnits(cdr.getSource_msisdn(), str, units, nFree);
        //SAVE THE CDR IN THE RTX DB
        SiteDAO.instanceData.setRTX(cdr);
    }

    private static String serviceTypeMapping(int Service_type) {
        String str = null;
        switch (Service_type) {
            case 1:
                str = "current_voice";
                break;
            case 2:
                str = "current_cross_voice";
                break;
            case 3:
                str = "current_sms";
                break;
            case 4:
                str = "current_data";
                break;
            case 5:
                str = "current_roaming";
                break;
        }
        return str;
    }

    private static int getPrice(RatePlane ratePlane, int Service_type) {

        int price = 0;
        switch (Service_type) {
            case 1:
            case 2:
                price = ratePlane.getAdditional_minutes_service();
                break;
            case 3:
                price = ratePlane.getAdditional_sms_service();
                break;
            case 4:
                price = ratePlane.getAdditional_data_service();
                break;
            case 5:
                price = ratePlane.getAdditional_roaming_service();
                break;
        }
        return price;
    }
}
