package domainlogic.ServiceLayer;


import domainlogic.Money;

import java.util.Date;


/**
 *  Defines an application’s boundary with a layer of services that establishes a set of available
 *  operations and coordinates the application’s response in each operation.
 *
 *  Enterprise applications typically require different kinds of interfaces to the data they store and
 *  the logic they implement: data loaders, user interfaces, integration gateways, and others. Despite
 *  their different purposes, these interfaces often need common interactions with the application to
 *  access and manipulate its data and invoke its business logic.
 *
 *  A Service Layer defines an application’s boundary [Cockburn PloP] and its set of available operations
 *  from the perspective of interfacing client layers.
 *
 *  Domain Models (116) are preferable to Transaction Scripts (110) for avoiding domain logic duplication
 *  and for managing complexity using classical design patterns. But putting application logic into pure domain
 *  object classes has a couple of undesirable consequences.
 *  First, domain object classes are less reusable across applications if they implement application-specific
 *  logic and depend on application-specific packages. Second, commingling both kinds of logic in the same
 *  classes makes it harder to reimplement the application logic in, say, a workflow tool if that should ever
 *  become desirable.
 *
 *  The two basic implementation variations are the domain facade approach and the operation script approach.
 *  In the domain facade approach a Service Layer is implemented as a set of thin facades over a
 *  Domain Model (116). The classes implementing the facades don’t implement any business logic.
 *  Rather, the Domain Model (116) implements all of the business logic. The thin facades establish a
 *  boundary and set of operations through which client layers interact with the application,
 *  exhibiting the defining characteristics of Service Layer.
 *
 *  In the operation script approach a Service Layer is implemented as a set of thicker classes
 *  that directly implement application logic but delegate to encapsulated domain object classes
 *  for domain logic. The operations available to clients of a Service Layer are implemented as scripts,
 *  organized several to a class defining a subject area of related logic. Each such class forms an
 *  application “service,”
 *
 *  A Service Layer is comprised of these application service classes, which should extend a Layer Supertype (475),
 *  abstracting their responsibilities and common behaviors.
 *
 *  Identifying the operations needed on a Service Layer boundary is pretty straightforward.
 *  They’re determined by the needs of Service Layer clients, the most significant (and first) of which
 *  is typically a user interface.there’s almost always a one-to-one correspondence between CRUD use
 *  cases and Service Layer operations.
 *
 *  The easier question to answer is probably when not to use it. You probably don’t need a Service Layer
 *  if your application’s business logic will only have one kind of client—say, a user interface—and its
 *  use case responses don’t involve multiple transactional resources.
 *
 *
 *
 * **/

public class RecognitionService extends ApplicationService {

    public void calculateRevenueRecognitions(long contractNumber) {
        Contract contract = Contract.readForUpdate(contractNumber);
        contract.calculateRecognitions();
        getEmailGateway().sendEmailMessage(
                contract.getAdministratorEmailAddress(),
                "RE:  Contract  #" + contractNumber,
                contract + "  has had revenue recognitions calculated.");
        getIntegrationGateway().publishRevenueRecognitionCalculation(contract);
    }
    public Money recognizedRevenue(long contractNumber, Date asOf) {
        return Contract.read(contractNumber).recognizedRevenue(asOf);
    }
}
