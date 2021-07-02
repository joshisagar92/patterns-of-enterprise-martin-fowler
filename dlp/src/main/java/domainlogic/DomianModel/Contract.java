package domainlogic.DomianModel;

import domainlogic.MfDate;
import domainlogic.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Used when behavior of the business is subject to a lot of change.
 *
 * Simple Domain Model : one domain object for each database table.
 *                       Use  Active Record
 * Rich Domain Model : look different from the database design, with
 *                     inheritance, strategies and other design patterns
 *                     Use Data Mappers
 *
 * With Domain Model whole object graph will be added in a memory
 *
 * If you have complicated and everchanging business rules involving
 * validation, calculations, and derivations, chances are that you’ll
 * want an object model to handle them. On the other hand,
 * if you have simple not-null checks and a couple of sums to
 * calculate, a Transaction Script is a better bet.
 *
 * If you’re using Domain Model, my first choice for database
 * interaction is Data Mapper. This will help keep your Domain
 * Model independent from the database and is the best approach to
 * handle cases where the Domain Model and database schema diverge.
 *
 * A common thing you find in domain models is how multiple classes interact
 * to do even the simplest tasks
 *
 * Domain Models (116) are preferable to Transaction Scripts (110) for avoiding domain
 * logic duplication and for managing complexity using classical design patterns.
 *
 *
 * **/


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

    public Money recognizedRevenue(MfDate asOf) {
        Money result = Money.dollars(BigDecimal.valueOf(0));
        Iterator it = revenueRecognitions.iterator();
        while (it.hasNext()) {
            RevenueRecognition r = (RevenueRecognition) it.next();
            if (r.isRecognizableBy(asOf))
                result = result.add(r.getAmount());
        }
        return result;
    }

    public void calculateRecognitions() {
        product.calculateRevenueRecognitions(this);
    }

    public MfDate getWhenSigned() {
            return null;
    }

    public Money getRevenue() {
        return null;
    }

    public void addRevenueRecognition(RevenueRecognition revenueRecognition) {

    }
}
