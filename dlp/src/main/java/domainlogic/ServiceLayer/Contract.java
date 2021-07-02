package domainlogic.ServiceLayer;

import domainlogic.DomianModel.Product;
import domainlogic.DomianModel.RevenueRecognition;
import domainlogic.MfDate;
import domainlogic.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Contract {

    private Product product;
    private Money revenue;
    private MfDate whenSigned;
    private Long id;
    public Contract(Product product, Money revenue, MfDate whenSigned) {
        this.product = product;
        this.revenue = revenue;
        this.whenSigned = whenSigned;
    }

    private List revenueRecognitions = new ArrayList();

    public static Contract readForUpdate(long contractNumber) {
        return null;
    }

    public static Contract read(long contractNumber) {
        return null;
    }

    public Money recognizedRevenue(Date asOf) {
        Money result = Money.dollars(BigDecimal.valueOf(0));
        Iterator it = revenueRecognitions.iterator();
        while (it.hasNext()) {
            RevenueRecognition r = (RevenueRecognition) it.next();
           /* if (r.isRecognizableBy(asOf))
                result = result.add(r.getAmount());*/
        }
        return result;
    }

    public void calculateRecognitions() {
       // product.calculateRevenueRecognitions(this);
    }

    public MfDate getWhenSigned() {
            return null;
    }

    public Money getRevenue() {
        return null;
    }

    public void addRevenueRecognition(RevenueRecognition revenueRecognition) {

    }

    public String getAdministratorEmailAddress() {
            return null;
    }
}
