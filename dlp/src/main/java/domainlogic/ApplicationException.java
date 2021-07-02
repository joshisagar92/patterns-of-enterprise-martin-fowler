package domainlogic;

import java.sql.SQLException;

public class ApplicationException extends Throwable {
    public ApplicationException(SQLException e) {
    }
}
