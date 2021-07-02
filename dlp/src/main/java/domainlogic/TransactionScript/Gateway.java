package domainlogic.TransactionScript;

import domainlogic.MfDate;
import domainlogic.Money;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Gateway {

    public ResultSet findRecognitionsFor(long contractID, MfDate asof) throws SQLException {
        PreparedStatement stmt = db.prepareStatement(findRecognitionsStatement);
        stmt.setLong(1, contractID);
        stmt.setDate(2, asof.toSqlDate());
        ResultSet result = stmt.executeQuery();
        return result;
    }


    private static final String findRecognitionsStatement =
        "SELECT amount " +
        "  FROM revenueRecognitions " +
        "  WHERE contract = ? AND recognizedOn  <= ?";

    private Connection db;

    public ResultSet findContract(long contractNumber) {
        return null;
    }

    public void insertRecognition(long contractNumber, Money money, MfDate recognitionDate) {

    }
}
