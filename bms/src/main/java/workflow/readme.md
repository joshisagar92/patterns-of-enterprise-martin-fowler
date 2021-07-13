### Database Transactions

#### ACID Transactions
Typically, when we talk about database transactions, we are talking about ACID transactions. ACID is an acronym outlining
the key properties of database transactions that lead to a system we can rely on to ensure the durability and consistency
of our data storage. ACID stands for atomicity, consistency, isolation, and durability, and here is what these properties
give us:

**Atomicity**
Ensures that all operations completed within the transaction either all complete or all fail. If any of the changes we’re
trying to make fail for some reason, then the whole operation is aborted, and it’s as though no changes were ever made.

**Consistency**
When changes are made to our database, we ensure it is left in a valid, consistent state.

**Isolation**
Allows multiple transactions to operate at the same time without interfering. This is achieved by ensuring that any interim
state changes made during one transaction are invisible to other transactions.

**Durability**
Makes sure that once a transaction has been completed, we are confident the data won’t get lost in the event of some system failure.

#### Still ACID, but Lacking Atomicity?
. We’ve reached the end of the process, which involves changing the Status of the customer 2346 from PENDING to VERIFIED.
As the enrollment is now complete, we also want to remove the matching row from the PendingEnrollments table. With a single
database, this is done in the scope of a single ACID database transaction—these two state changes either both occur, or neither occurs.

Compare this wit We’re making exactly the same change, but now each change is made in a different database.
This means there are two transactions to consider, each of which could work or fail independently of the other.

We could decide to sequence these two transactions, of course, removing a row from the PendingEnrollments table only if 
we were able to change the row in the Customer table. But we’d still have to reason about what to do if the deletion from
the PendingEnrollments table then failed

But fundamentally by decomposing this operation into two separate database transactions, we have to accept that we’ve lost
guaranteed atomicity of the operation as a whole.

### Distributed Transactions: Two-Phase Commits

The two-phase commit algorithm (sometimes shortened to 2PC) is frequently used to attempt to give us the ability to make
transactional changes in a distributed system, where multiple separate processes may need to be updated as part of the overall operation.


The 2PC is broken into two phases (hence the name two-phase commit): a voting phase and a commit phase. During the voting phase,
a central coordinator contacts all the workers who are going to be part of the transaction, and asks for confirmation as to whether
or not some state change can be made. In Figure 6-3, we see two requests, one to change a customer status to VERIFIED, another to
remove a row from our PendingEnrollments table. If all the workers agree that the state change they are asked for can take place,
the algorithm proceeds to the next phase. If any workers say the change cannot take place, perhaps because the requested state
change violates some local condition, the entire operation aborts.

It’s important to highlight that the change does not take effect immediately after a worker indicates that it can make 
the change. Instead, the worker is guaranteeing that it will be able to make that change at some point in the future. How
would the worker make such a guarantee?
To guarantee that this change can be made later, Worker A will likely have to lock that record to ensure that such a change cannot take place.

If any workers didn’t vote in factor of the commit, a rollback message needs to be sent to all parties, to ensure that they 
can clean up locally, which allows the workers to release any locks they may be holding. If all workers agreed to make 
the change, we move to the commit phase.

The more latency there is between the coordinator, and the slower it is for the workers to process the response, the wider
this window of inconsistency might be. Coming back to our definition of ACID, isolation ensures that we don’t see intermediate
states during a transaction. But with this two-phase commit, we’ve lost that.

When two-phase commits work, at their heart they are very often just coordinating distributed locks. 

Managing locks, and avoiding deadlocks in a single-process system, isn’t fun. Now imagine the challenges of coordinating
locks among multiple participants. It’s not pretty.

There are a host of failure modes associated with two-phase commits that we don’t have time to explore. Consider the
problem of a worker voting to proceed with the transaction, but then not responding when asked to commit. What should we do then?

The more participants you have, and the more latency you have in the system, the more issues a two-phase commit will have.
2PC can be a quick way to inject huge amounts of latency into your system, especially if the scope of locking is large, or
the duration of the transaction is large. It’s for this reason two-phase commits are typically used only for very short-lived operations.
The longer the operation takes, the longer you’ve got resources locked!

### Sagas
Unlike a two-phase commit, a saga is by design an algorithm that can coordinate multiple changes in state, but avoids the
need for locking resources for long periods of time. Sagas do this by modeling the steps involved as discrete activities
that can be executed independently.

If you directly mapped an LLT(Long lived transaction) to a normal database transaction, a single database transaction would span the entire life
cycle of the LLT. This could result in multiple rows or even full tables being locked for long periods of time while the LLT is taking place,
causing significant issues if other processes are trying to read or modify these locked resources.

Instead, the authors of the paper suggest we should break down these LLTs into a sequence of transactions, each of which
can be handled independently. The idea is that the duration of each of these “sub” transactions will be shorter lived,
and will modify only part of the data affected by the entire LLT. As a result, there will be far less contention in the 
underlying database as the scope and duration of locks is greatly reduced.


While sagas were originally envisaged as a mechanism to help with LLTs acting against a single database, the model works
just as well for coordinating change across multiple services. We can break a single business process into a set of calls
that will be made to collaborating services - this is what constitutes a saga.


saga does not give us atomicity in ACID terms we are used to with a normal database transaction. As we break the LLT into
individual transactions, we don’t have atomicity at the level of the saga itself. We do have atomicity for each individual
transaction inside the overall saga, as each one of them can relate to an ACID transactional change if needed.

#### Saga Failure Modes
With a saga being broken into individual transactions, we need to consider how to handle failure—or, more specifically, 
how to recover when a failure happens.

Backward recovery involves reverting the failure, and cleaning up afterwards—a rollback. For this to work, we need to define
compensating actions that allow us to undo previously committed transactions. Forward recovery allows us to pick up from
the point where the failure occurred, and keep processing. For that to work, we need to be able to retry transactions, 
which in turn implies that our system is persisting enough information to allow this retry to take place.

It’s really important to note that a saga allows us to recover from business failures, not technical failures. For example,
if we try and take payment from the customer but the customer has insufficient funds, then this is a business failure that
the saga should be expected to handle. On the other hand, if the Payment Gateway times out, or throws an 500 Internal Service Error,
then this is a technical failure which we need to handle separately. The Saga assumes the underlying components are working properly 
- that the underlying system is reliable, and that we are then co-ordinating the work of reliable components.

#### Saga rollbacks
With an ACID transaction, if we hit a problem, we trigger a rollback before a commit occurs. After the rollback, it is 
like nothing ever happened: the change we were trying to make didn’t take place. With our saga, though, we have multiple
transactions involved, and some of those may have already committed before we decide to roll back the entire operation. 
So how can we roll back transactions after they have already been committed?

. There is no simple “rollback” for the entire operation.

Instead, if you want to implement a rollback, you need to implement a compensating transaction. A compensating transaction
is an operation that undoes a previously committed transaction. To roll back our order fulfillment process, we would trigger
the compensating transaction for each step in our saga that has already been committed

It’s worth calling out the fact that these compensating transactions may not behave exactly as that of a normal database
rollback. A database rollback happens before the commit; and after the rollback, it is as though the transaction never happened.
In this situation, of course, these transactions did happen. We are creating a new transaction that reverts the changes 
made by the original transaction, but we can’t roll back time and make it as though the original transaction didn’t occur.

Because we cannot always cleanly revert a transaction, we say that these compensating transactions are semantic rollbacks.
We cannot always clean up everything, but we do enough for the context of our saga. As an example, one of our steps may have
involved sending an email to a customer to tell them their order was on the way. If we decide to roll that back, you can’t
unsend an email!

Instead, your compensating transaction could cause a second email to be sent to the customer, informing them that there 
had been a problem with the order and it had been canceled.

##### Reordering Workflow Steps To Reduce Rollbacks
we could have made our likely rollback scenarios somewhat simpler by reordering the steps in our workflow. A simple change
would be to award points only when the order was actually dispatched

##### Mixing fail-backward and fail-forward situations
It is totally appropriate to have a mix of failure recovery modes. Some failures may require a rollback (fail backwards);
others may be fail forward. For the order processing, for example, once we’ve taken money from the customer, and the item
has been packaged, the only step left is to dispatch the package. If for whatever reason we can’t dispatch the package
(perhaps the delivery firm we have doesn’t have space in their vans to take an order today), it seems very odd to roll the
whole order back. Instead, we’d probably just retry the dispatch (perhaps queuing it for the following day), and if that 
fails, require human intervention to resolve the situation.

#### Implementing Sagas

##### Orchestrated sagas
Orchestrated sagas use a central coordinator (what we’ll call an orchestrator from now on) to define the order of execution
and to trigger any required compensating action. You can think of orchestrated sagas as a command-and-control approach:
the central orchestrator controls what happens and when, and with that comes a good degree of visibility as to what is happening
with any given saga.

Here, our central Order Processor, playing the role of the orchestrator, coordinates our fulfillment process. It knows 
what services are needed to carry out the operation, and it decides when to make calls to those services. If the calls fail,
it can decide what to do as a result. In general, orchestrated sagas tend to make heavy use of request-response interactions
between services: the Order Processor sends a request to services (such as a Payment Gateway), and expects a response 
letting it know if the request was successful and providing the results of the request.

Our Order Processor needs to know about all the associated services, resulting in a higher degree of domain coupling.
While not inherently bad, we’d still like to keep domain coupling to a minimum if possible.

The other issue, which is more subtle, is that logic that should otherwise be pushed into the services can start to instead
become absorbed in the orchestrator.

##### Choreographed sagas
Choreographed sagas aim to distribute responsibility for the operation of the saga among multiple collaborating services.
If orchestration is command-and-control, choreographed sagas represent a trust-but-verify architecture. As we’ll see in our
example, choreographed sagas will often make heavy use of events for collaboration between services.

In the preceding architecture, no one service knows about any other microservice. They only need to know what to do when
a certain event is received - we’ve drastically reduced the amount of domain coupling. Inherently, this makes for a much
less coupled architecture. As the implementation of the process is decomposed and distributed among the three microservices
here, we also avoid the concerns about centralization of logic (if you don’t have a place where logic can be centralized,
then it won’t be centralized!).

The flip side of this is that it can now be harder to work out what is going on. With orchestration, our process was 
explicitly modeled in our orchestrator. Now, with this architecture as it is presented, how would you build up a mental 
model of what the process is supposed to be? You’d have to look at the behavior of each service in isolation and reconstitute 
this picture in your own head.

The lack of an explicit representation of our business process is bad enough, but we also lack a way of knowing what state
a saga is in, which can also deny us the chance to attach compensating actions when required. We can push some responsibility
to the individual services for carrying out compensating actions, but fundamentally we need a way of knowing what state a
saga is in for some kinds of recovery. The lack of a central place to interrogate around the status of a saga is a big problem.

###### Mixing Style
You may have some business processes in your system that more naturally fit one model or another. You may also have a 
single saga that has a mix of styles. In the order fulfillment use case, for example, inside the boundary of the Warehouse
service, when managing the packaging and dispatch of a package, we may use an orchestrated flow even if the original request
was made as part of a larger choreographed saga

Whether you chose choreography or orchestration, when implementing business process using multiple microservices it’s common
to want to be able to trace all the calls related to a given process. This can sometimes be just to help you understand 
if the business process is working correctly, or could be to help you diagnose a problem

### THE LIMITS OF THE SAGA PATTERN
_“If you have a transaction that spans multiple services and their databases, do not use distributed transactions. Use 
the Saga pattern instead: Call the affected services in a row – typically by using events for activity propagation. If a services
fails to update its database, roll back the transaction by triggering compensating actions in the services that already
ran their updates. Avoid the coordination overheads of distributed transactions by accepting eventual consistency and 
actively undoing partial updates if something goes wrong.”_

- The Saga pattern can only be used to logically roll back transactions due to business errors.
- The Saga pattern cannot be used to respond to technical errors.

A business error occurs if an existing business rule is violated. E.g., if you try to pay with an expired credit card or
if a pending payment would exceed your credit limit: That is a business error. From a technical point of view, everything is fine.

A technical error on the other hand is if something goes wrong on the technology, the infrastructure level. E.g., if your
database does not respond or throws an unexpected technical error (the dreaded “IOException”), if a service is down or latent,
if a message is lost or corrupted, if your replicated and eventually consistent database is out of sync: All that are 
technical errors. From a business point of view, everything is fine.

Why does it make a huge difference if you encounter a business or a technical error?

Let us say, you get an unexpected error message when trying to write to your database – a technical error. Then you decide
to abort the business transaction, using the Saga pattern and trigger a compensating action. Now assume that the compensating
action fails due to another technical error (keep in mind that technical errors often occur in clusters).

What now? A compensating compensating action?

And if that one fails? A compensating compensating compensating action?

And so on …

We could go on for a long time, but the problem should be clear:

Technical errors in a distributed system landscape are non-deterministic 1. You cannot predict when they will occur and 
how they will manifest. The only thing you know for sure is that they will happen, i.e., their probability is bigger than 0.

#### DEALING WITH TECHNICAL ERRORS
if you face a technical error, you need to retry in some (non-naive) way until you eventually overcome the error and your
activity succeeds. This means retrying in combination with waiting, escalation strategies, etc.
The point is that you cannot simply roll back a partial distributed update if a technical error occurs. 

It is also not simple to implement eventual completion. Arbitrary errors can happen at any time and it can take a while 
until the cause for the error is fixed. This means you must make sure that you do not lose the pending changes before you
can complete them. We need to store the information about the desired change in a reliable way (outside the place where 
we finally want to persist it) until we have completed the update. We also need to make sure that accidental duplicate 
updates do not lead to undesired effects

Up to now we assumed that an error condition will not persist eternally, i.e., that the system will eventually recover 
from the error and the pending change can be applied. But sometimes error conditions are persistent.

This is an example of a potential persistent error situation. There are many more such situations. Basically, whenever a
stateful node crashes, there is a chance that you end up in a persistent, non-recoverable error state unless you implement
the required countermeasures.

https://www.ufried.com/blog/limits_of_saga_pattern/    








