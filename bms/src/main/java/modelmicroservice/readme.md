#### Information Hiding

- Improve development Time : parallel work can we done. no fear of adding new devs.
- Comprehensibility : Each module can be looked at in isolation, and understood in isolation
- Flexibility : Module can be change independently

This list of desirable characteristics nicely complements what we are trying to achieve with microservice architectures 
- and indeed I now see microservices as just another form of modular architecture.

modules doesn’t result in you actually achieving these outcomes. A lot depends on how the
module boundaries are formed. From his own research information hiding was a key technique
to help get the most out of our modular architectures, and with a modern eye,
the same applies to microservices too. 

_**The connections between modules are the assumptions which the modules make about each other.**_

By reducing the number of assumptions that one module (or microservice) makes about another, we directly impact the 
connections between them. By keeping the number of assumptions small, it is easier to ensure that we can change one 
module without impacting others. If a developer changing a module has a clear understanding as to how the module is used
by others, it will be easier for them to make changes safely in such a way that upstream callers won’t also have to change.

**https://blog.acolyer.org/2016/09/05/on-the-criteria-to-be-used-in-decomposing-systems-into-modules/**

#### Cohesion & Coupling
**Cohesion** :  the code that changes together, stays together.

**Coupling** : When services are loosely coupled, a change to one service should not require a change to another. The whole
point of a microservice is being able to make a change to one service and deploy it, without needing to change any other
part of the system. This is really quite important.

_**A structure is stable if cohesion is strong and coupling is low**_

Cohesion applies to the relationship between things inside a boundary (a microservice in our context), whereas coupling
describes the relationship between things across a boundary.

##### Domian Coupling
Domain Coupling describes the situation where one microservice needs to interact with another microservice, because it 
needs to make use of the functionality that the other microservice provides

In a microservice architecture, this type of interaction is largely unavoidable.

whenever you see a single microservice depending on multiple downstream services in this way it can be a cause for 
concern - it might imply a microservice that is doing too much.

Domain Coupling can also become problematic as more complex sets of data are sent between services

######  Temporal coupling
situation where concepts are bundled together purely because they happen at the same time.

In distributed system it refers to the situation where one microservice needs another microservice to do something at 
the same time.

Both microservices need to be up and available and communicate with each other at the same time in order for the operation
to complete(Syncrnous Blocking network call).

Cause resource contention issue(blocking call) - solution may be async call.

##### Pass through coupling
a situation where one microservice passes data to another microservice purely because it is needed by some other further
downstream microservice.

The major issue with pass through coupling is that a change to the required data downstream can cause a more significant
upstream change. In our example, if the Shipping now needs the format or content of the data to be changed, then both
Warehouse and Order Processor would likely need to change.

There are a few ways this can be fixed. The first is to consider if it makes sense for the calling microservice to just
bypass the intermediary. In our example, this might mean Order Processor speaks directly to Shipping. 
Now, in this specific situation, this causes some other headaches. Our Order Processor is increasing its domain coupling,
as Shipping is yet another microservice it needs to know about - if that was the only issue, this might still be fine,
as domain coupling is a looser form of coupling of course. This solution gets more complex here though as stock has
to be reserved with Warehouse before we dispatch the package using Shipping, and after the shipping has been done we
need to update the stock accordingly. This pushes more complexity and logic into the Order Processor which was previously
hidden inside Warehouse. 

Second fix may be send some raw data to warehouse and warehouse will create Shipping Manifest for Shipping service. This 
may protect change at some extent. But if Shipping manifest change with new fields it still require change in all service.

Third option is to send Shipping menifest to warehouse and warehouse will directly send it to shipping service as blob.
In this case only 2 service change.

##### Common coupling
Common coupling occurs when two or more microservices make use of a common set of data. A simple and common example of 
this form of coupling would be multiple microservices making use of the same shared database, but this could also 
manifest itself in the use of shared memory or a shared filesystem.

The main issue with common coupling is that changes to the structure of the data can impact multiple microservices at once.

Order Processor and Warehouse service are both reading and writing from a shared Order table.Both microservices are 
updating the STATUS column.

The Order Processor can set the PLACED, PAID, and COMPLETED statuses, whereas the Warehouse will apply PICKING or 
SHIPPED statuses.

Conceptually, we have both the Order Processor and the Warehouse microservices managing different aspects of the 
lifecycle of an order. When making changes in Order Processor, can I be sure that I am not changing the order data in 
such a way that it breaks Warehouse’s view of the world, or vice-versa?

he problem in this specific example is that both Warehouse and +Order Processor share responsibilities for managing this
state machine. How do we ensure that they are both in agreement as to what transitions are allowed?

A potential solution here would be to ensure that one single microservice manages the order state. and other 2 microserviec
will use this service to change the status. Order microservice will be responsible for managing state transition. 

An alternative approach I see in such cases is to implement the Order service as little more than a wrapper around 
database CRUD operations, where requests just map directly to database updates.

_If you see a microservice that just looks like a thin wrapper around database CRUD operations, 
that is a sign that you may have weak cohesion and tighter coupling, as logic that should be in that service to manage
the data is instead spread elsewhere in your system._

Sources of common coupling are also potential sources of resource contention. Multiple microservices making use of the 
same file system or database could overload that shared resource, potentially causing significant problems if the shared
resource becomes slow or even entirely unavailable.

##### Content coupling
Content coupling describes a situation where an upstream service reaches into the internals of a downstream service and
changes its internal state.

The most common manifestation of this is an external service directly accessing another microservice’s database and 
changing it directly. The difference between content coupling and common coupling are subtle. On the face of it,
in both cases two or more microservices are reading and writing to the same set of data. With common coupling,
you understand that you are making use of a shared, external dependency. You know it’s not under your control.
With content coupling, the lines of ownership become less clear, and it becomes more difficult for developers to change
a system.

Warehouse service is directly updating the table where order data is stored, bypassing any functionality in the Order
service which might check for allowable changes.

We have to hope that the Warehouse service has a consistent set of logic to ensure that only valid changes are made.
Best case, this represents a duplication of logic. Worst case, the checking around allowable changes in Warehouse is
different to that in the Order service, and as a result we could end up with orders in very odd, confusing states.

In this situation, we also have the issue that the internal data structure of our order table is exposed to an outside
party. When changing the Order service, we now have to be extremely careful about making changes to that particular
table—that’s even assuming it’s obvious to us that this table is being directly accessed by an outside party.

If you are working on a microservice, it’s vital that you have a clear separation between what can be changed freely,
and what cannot.

#### Just Enough Domain Driven Design
##### Ubiquitous Language
Defining and adopting a common language to be used in code and in describing the domain, to aid communication.
Ubiquitous language refers to the idea that we should strive to use the same terms in our code as the users use .

##### Aggregate
A collection of objects which are managed as a single entity, typically referring to real-world concepts.
consider an aggregate as a representation of a real domain concept—think of something like an Order, 
Invoice, Stock Item, etc. Aggregates typically have a life cycle around them, which opens them up to being implemented 
as a state machine.

As an example in the MusicCorp domain, an Order aggregate might contain multiple line items that represent the items in 
the order. Those line items only have meaning as part of the overall Order aggregate.


We want to treat aggregates as self-contained units; we want to ensure that the code that handles the state transitions
of an aggregate are grouped together, along with the state itself. So one aggregate should be managed by one microservice,
although a single microservice might own management of multiple aggregates.

In general though, think of an aggregate as something which has state, has identity, and has a lifecycle that will be 
managed as part of the system. They typically refer to real-world concepts.

The key thing to understand here is that if an outside party requests a state transition in an aggregate, the aggregate 
can say no. You ideally want to implement your aggregates in such a way that illegal state transitions are impossible.

Aggregates can have relationships with other aggregates.we have a Customer aggregate, which is associated
with one or more Orders, and one or more Wishlists. Each of these aggregates could be managed by the same microservice,
or different microservice.

If these relationships between aggregates exist inside the scope of a single microservice, the relationships could
easily be stored using something like a foreign key relationship if using a relational database. If the relationships
between these aggregates span microservice boundaries though, we need some way to model this relationship.

Now, we could simply store the ID of the aggregate directly in our local database. For example, consider a Finance
microservice which manages a financial ledger, which stores transactions against a customer. 
Locally, within the Finance microservice’s database we could have a CustID column which contains the ID of that customer.
If we wanted to get more information about that customer, we’d have to do a lookup against the Customer microservice 
using that ID.

The problem with this concept is that it isn’t very explicit—in fact, the relationship between the CustID column 
and the remote customer is entirely implicit. To know how that ID was being used, we’d have to look at the code of the 
Finance microservice itself. It would be nice if we could store a reference to a foreign aggregate in a way which is 
more obvious.

we have changed things to make the relationship explicit. Rather than a vanila ID for the customer reference, we instead
store a URI(/customer/123) which we might use if building a REST-based system.
For system which is not using rest
(**https://philcalcado.com/2017/03/22/pattern_using_seudo-uris_with_microservices.html**)

_the more implementation details you leak, the more your service’s clients will depend on it. Big refactorings, even of your own services, can be really hard to orchestrate and execute._



##### Bounded Context
An explicit boundary within a business domain that provides functionality to the wider system, but which also hides complexity.

A bounded context typically represents a larger organizational boundary. Within the scope of that boundary, explicit
responsibilities need to be carried out
 
Bounded contexts hide implementation detail. There are internal concerns—for example, the types of forklift trucks used
is of little interest to anyone other than the folks in the warehouse. These internal concerns should be hidden from the
outside world—they don’t need to know, nor should they care.

From an implementation point of view, bounded contexts contain one or more aggregates. Some aggregates may be exposed
outside the bounded context; others may be hidden internally. As with aggregates, bounded contexts may have relationships
with other bounded contexts

##### Hidden Models
Model which work diffrently internally and share minimum details externally. In this case create to separate model and give
them different names.

##### Shared Models
We can also have concepts which appear in more than one bounded context. In we saw that a customer exists in both
locations. What does this mean? Is the customer copied? The way to think about this is that conceptually, both finance
and warehouse needs to know something about our customer.

When you have a situation like this, a shared model like customer can have different meanings in the different bounded
contexts, and therefore might be called different things. We might be happy to keep the name “customer” in Finance,
but in Warehouse we might call them a “recipient,” as that is the role they play in that context


#### Mapping Aggregates and Bounded Contexts to Microservices
Both the aggregate and the bounded context give us units of cohesion with well-defined interfaces with the wider system.
The aggregate is a self-contained state machine that focuses on a single domain concept in our system, with the bounded
context representing a collection of associated aggregates, again with an explicit interface to the wider world.

one microservice can manage one or more aggregates, but we don’t want one aggregate to be managed by more than one microservice.
   

  
 
   
 

    