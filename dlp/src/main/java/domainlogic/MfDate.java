package domainlogic;

import java.sql.Date;

public class MfDate {
    private Date dateSigned;

    public MfDate(Date dateSigned) {

        this.dateSigned = dateSigned;
    }

    public Date toSqlDate() {
        return null;
    }

    public MfDate addDays(int i) {
        return null;
    }

    public boolean after(MfDate date) {
            return false;
    }
}
