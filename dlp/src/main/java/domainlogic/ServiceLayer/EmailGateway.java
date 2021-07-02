package domainlogic.ServiceLayer;

public interface  EmailGateway {
    void sendEmailMessage(String toAddress, String subject, String body);
}
