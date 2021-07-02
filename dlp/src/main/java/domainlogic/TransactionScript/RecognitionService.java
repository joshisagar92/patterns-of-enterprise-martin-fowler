package domainlogic.TransactionScript;

import domainlogic.ApplicationException;
import domainlogic.MfDate;
import domainlogic.Money;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;


//Organize business logic by procedure. each procedure handle single request from presentation

public class RecognitionService {

    public Money recognizedRevenue(long contractNumber, MfDate asOf) throws ApplicationException {
        Money result = Money.dollars(BigDecimal.valueOf(0));
        try {
            ResultSet rs = db.findRecognitionsFor(contractNumber, asOf);
            while (rs.next()) {
                result = result.add(Money.dollars(rs.getBigDecimal("amount")));
            }
            return result;
        } catch (SQLException e) {throw new ApplicationException (e);
        }
    }


    public void calculateRevenueRecognitions(long contractNumber) throws ApplicationException {
        try {
            ResultSet contracts = db.findContract(contractNumber);
            contracts.next();
            Money totalRevenue = Money.dollars(contracts.getBigDecimal("revenue"));
            MfDate recognitionDate = new MfDate(contracts.getDate("dateSigned"));
            String type = contracts.getString("type");
            if (type.equals("S")){
                Money[] allocation = totalRevenue.allocate(3);
                db.insertRecognition
                        (contractNumber, allocation[0], recognitionDate);
                db.insertRecognition
                        (contractNumber, allocation[1], recognitionDate.addDays(60));
                db.insertRecognition
                        (contractNumber, allocation[2], recognitionDate.addDays(90));
            } else if (type.equals("W")){
                db.insertRecognition(contractNumber, totalRevenue, recognitionDate);
            } else if (type.equals("D")) {
                Money[] allocation = totalRevenue.allocate(3);
                db.insertRecognition
                        (contractNumber, allocation[0], recognitionDate);
                db.insertRecognition
                        (contractNumber, allocation[1], recognitionDate.addDays(30));
                db.insertRecognition
                        (contractNumber, allocation[2], recognitionDate.addDays(60));
            }
        } catch (SQLException e) {throw new ApplicationException (e);
        }
    }

    private Gateway db;

}
