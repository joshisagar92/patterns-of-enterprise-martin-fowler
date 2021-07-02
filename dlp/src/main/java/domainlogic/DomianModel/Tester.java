package domainlogic.DomianModel;

import domainlogic.MfDate;
import domainlogic.Money;

import java.sql.Date;

public class Tester {


    public static void main(String[] args) {
        Product word = Product.newWordProcessor("Thinking Word");
        Product calc = Product.newSpreadsheet("Thinking Calc");
        Product db = Product.newDatabase("Thinking DB");


        MfDate whenSigned = new MfDate(Date.valueOf(""));
        Contract contract = new Contract(word, new Money(), whenSigned);
        contract.calculateRecognitions();

    }

}
