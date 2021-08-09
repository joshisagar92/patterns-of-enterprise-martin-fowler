### Pattern: The Shared Database(Low Cohesion and Implementation coupling)
it’s implementation coupling that often occupies us most when considering databases, because of the prevalence of people
sharing a database among multiple schemas

The major issue, though, is that we deny ourselves the opportunity to decide what is shared and what is hidden—which flies
in the face of our drive toward information hiding. This means it can be difficult to understand what parts of a schema 
can be changed safely.

Another issue is that it becomes unclear as to who “controls” the data. Where is the business logic that manipulates this
data? Is it now spread across services?
As we discussed previously, when thinking about a microservice as being a combination of behavior and state, encapsulating
one or more state machines. If the behavior that changes this state is now spread around the system, making sure this 
state machine can be properly implemented is a tricky issue.

three services can directly change order information, what happens if that behavior is inconsistent across the services?
What happens when this behavior does need to change—do I have to apply those changes to all these services?

2 place where this pattern is appropriate,

- I think direct sharing of a database is appropriate for a microservice architecture in only two situations. The first is
when considering read-only static reference data(country or zipcode table).
- when a service is directly exposing a database as a defined endpoint that is designed and managed in order to handle multiple consumers.   

### Pattern: Database View(Information Hiding)
In a situation where we want a single source of data for multiple services, a view can be used to mitigate the concerns 
regarding coupling. With a view, a service can be presented with a schema that is a limited projection from an underlying
schema. This projection can limit the data that is visible to the service, hiding information it shouldn’t have access to.

_If each actor (e.g., a human or an external system) has a different set of credentials, it becomes much easier to restrict
access to certain parties, reduce the impact of revoking and rotating credentials, and better understand what each actor
is doing. Managing different sets of credentials can be painful, especially in a microservice system that may have multiple
sets of credentials to manage per service. I like the use of dedicated secret stores to solve this problem. HashiCorp’s 
Vault is an excellent tool in this space, as it can generate per-actor credentials for things like databases that can be
short lived and limited in scope._

Depending on the nature of the database, you may have the option to create a materialized view. With a materialized view,
the view is precomputed—typically, through the use of a cache. This means a read from a view doesn’t need to generate a 
read on the underlying schema, which can improve performance. The trade-off then is around how this pre-computed view is
updated; it may well mean you could be reading a “stale” set of data from the view.

I typically make use of a database view in situations where I think it is impractical to decompose the existing monolithic
schema. Ideally, you should try to avoid the need for a view if possible, if the end goal is to expose this information 
via a service interface.


### Pattern: Database Wrapping Service(control what is shared and what is hidden)
Sometimes, when something is too hard to deal with, hiding the mess can make sense. With the database wrapping service 
pattern, we do exactly that: hide the database behind a service that acts as a thin wrapper, moving database dependencies
to become service dependencies

### Pattern: Database-as-a-Service Interface

Tableau, which are often used to gain insights into business metrics). In these situations, allowing clients to view data
that your service manages in a database can make sense, but we should take care to separate the database we expose from 
the database we use inside our service boundary.

I’d probably utilize a dedicated change data capture system, perhaps something like Debezium to transfer data.
This pattern is more sophisticated than a simple database view. Database views are typically tied to a particular technology
stack: if I want to present a view of an Oracle database, both the underlying database and the schema hosting the views 
both run on Oracle. With this approach, the database we expose can be a totally different technology stack.

### Transferring Ownership

So far, we’ve really not tackled the underlying problem. We’ve just put a variety of different bandages on a big, shared database.

#### Pattern: Aggregate Exposing Monolith
When the data you want to access is still “owned” by the database, this pattern works well to allow your new services the
access they need. When extracting services, having the new service call back to the monolith to access the data it needs
is likely little more work than directly accessing the database of the monolith—but in the long term is a much better idea.

I’d consider using a database view over this approach only if the monolith in question cannot be changed to expose these
new endpoints. In such cases, a database view on the monolith’s database could work, as could the previously discussed 
change data capture pattern

#### Pattern: Change Data Ownership
We’ve looked at what happens when our new Invoice service needs to access data that is owned by other functionality, as 
in the previous section, where we needed to access Employee data. However, what happens when we consider data that is
currently in the monolith that should be under the control of our newly extracted service?

This one is a little more clear-cut. If your newly extracted service encapsulates the business logic that changes some data,
that data should be under the new service’s control. The data should be moved from where it is, over into the new service.

### Data Synchronization (In case of Rollback, Data should be in sync)
We are in the process of switching over to a new Invoice service. But the new service, and the existing equivalent code
in the monolith also manages this data. To maintain the ability to switch between implementations, we need to ensure that
both sets of code can see the same data, and that this data can be maintained in a consistent way.

This would lead us toward probably having our new Invoice service read its data directly from the monolith for a short 
space of time, perhaps making use of a view.

Another approach could be to consider keeping the two databases in sync via our code.

### Pattern: Synchronize Data in Application
- Step 1: Bulk Synchronize Data(from old to ew)
- Step 2: Synchronize on Write, Read from Old Schema
- Step 3: Synchronize on Write, Read from New Schema

With the Danish medical record system, we had a single application to deal with. But we’ve been talking about situations
where we are looking to split out microservices. So does this pattern really help? The first thing to consider is that
this pattern may make a lot of sense if you want to split the schema before splitting out the application code.

If implemented correctly, both data sources should always be in sync, offering us significant benefits in situations where
we need fast switching between sources for rollback scenarios, etc. The use of this pattern in the example of the Danish
medical records system seems sensible because of the inability to take the application offline for any length of time.

Now you could consider using this pattern where you have both your monolith and microservice accessing the data, but this
gets extremely complicated.

This complexity is greatly mitigated if you can be sure that at any point in time either the Invoice service is making 
writes, or the monolith’s Invoice functionality is—which would work well if using a simple switchover technique, as we 
discussed with the strangler fig pattern. If, however, requests could hit either the monolith’s Invoice functionality or
the new Invoice functionality, perhaps as part of a canary, then you may not want to use this pattern, as the resulting 
synchronization will be tricky.


### Pattern: Tracer Write(Sync with new source of truth incrementally)
With a tracer write, we move the source of truth for data in an incremental fashion, tolerating there being two sources 
of truth during the migration.

You identify a new service that will host the relocated data. The current system still maintains a record of this data 
locally, but when making changes also ensures this data is written to the new service via its service interface.

A pattern like the tracer write allows for a phased switchover, reducing the impact of each release, in exchange for 
being more tolerant of having more than one source of truth.

The reason this pattern is called a tracer write is that you can start with a small set of data being synchronized and 
increase this over time, while also increasing the number of consumers of the new source of data. If we take the example
where invoice-related data was being moved from the monolith over to our new Invoice microservice
, we could first synchronize the basic invoice data, then migrate the contact information for the invoice, and finally 
synchronize payment records

Other services that wanted invoice-related information would have a choice to source this from either the monolith or the
new service itself, depending on what information they need.

Initially, we’re writing only basic invoice information to both sources of truth. Once we’ve established that this 
information is being properly synchronized, the monolith can start to read its data from the new service. As more data 
is synchronized, the monolith can use the new service as a source of truth for more and more of the data. Once all the 
data is synchronized, and the last consumer of the old source of truth has been switched over, we can stop synchronizing
the data.

The biggest problem that needs to be addressed with the tracer write pattern is the issue that plagues any situation where
data is duplicated-inconsistency. To resolve this, you have a few options:

Careful thought does need to be given regarding how long you can tolerate inconsistency between the two systems. Some use
cases might not care, others may want the replication to be almost immediate. The shorter the window of acceptable
inconsistency, the more difficult this pattern will be to implement.

### Splitting the Database First, or the Code?

#### Database First
The flip side is that this approach is unlikely to yield much short-term benefit. We still have a monolithic code deployment.
Arguably, the pain of a shared database is something you feel over time, so we’re spending time and effort now to give us
return in the long run, without getting enough of the short-term benefit. For this reason, I’d likely go this route only
if I’m especially concerned about the potential performance or data consistency issues. We also need to consider that if
the monolith itself is a black-box system, like a piece of commercial software, this option isn’t available to us.

##### Pattern: Repository per bounded context
This pattern is really effective in any situation where you are looking to rework the monolith in order to better understand
how to split it apart. Breaking down these repository layers along the lines of domain concepts will go a long way to helping
you understand where seams for microservices may exist not only in your database, but also in the code itself.

##### Pattern: Database per bounded context
At first glance, the extra work in maintaining the separate databases doesn’t make much sense if you keep things as a monolith.
I view this as a pattern that is all about hedging your bets. It’s a bit more work than a single database, but keeps your
options open regarding moving to microservices later. Even if you never move to microservices, having the clear separation
of schema backing the database can really help, especially if you have lots of people working on the monolith itself.

This is a pattern I nearly always recommend for people building brand-new systems (as opposed to reimplementing an existing
system). I’m not a fan of implementing microservices for new products or startups; your understanding of the domain is 
unlikely to be mature enough to identify stable domain boundaries. With startups especially, the nature of the thing you
are building can change drastically. This pattern can be a nice halfway point, though. Keep schema separation where you
think you may have service separation in the future. That way, you get some of the benefits of decoupling these ideas, 
while reducing the complexity of the system.


#### Split the Code First
In general, I find that most teams split the code first, then the database, as shown in Figure 4-29. They get the short-term
improvement (hopefully) from the new service, which gives them confidence to complete the decomposition by separating out
the database.

By splitting out the application tier, it becomes much easier to understand what data is needed by the new service. 
You also get the benefit of having an independently deployable code artifact earlier. The concerns I’ve always had with 
this approach is that teams may get this far and then stop, leaving a shared database in play on an ongoing basis. If this
is the direction you take, you have to understand that you’re storing up trouble for the future if you don’t complete the
separation into the data tier. I’ve seen teams that have fallen into this trap, but can happily also report speaking to
organizations that have done the right thing here. JustSocial is one such organization that used this approach as part of
its own microservices migration. The other potential challenge here is that you may be delaying finding out nasty surprises
caused by pushing join operations up into the application tier.

If this is the direction you take, be honest with yourself: are you confident that you will be able to make sure that any
data owned by the microservice gets split out as part of the next step?

##### Pattern: Monolith as data access layer
Rather than accessing the data from the monolith directly, we can just move to a model in which we create an API in the 
monolith itself. 

Part of the reason this isn’t used more widely is likely because people sort of have in their minds the idea that the 
monolith is dead, and of no use.

I’d be more inclined to adopt this model if I felt that the data in the monolith was going to stay there. But it can work
well, especially if you think that your new service will effectively be pretty stateless.

This pattern works best when the code managing this data is still in the monolith. As we talked about previously, one way
to think of a microservice when it comes to data is the encapsulation of the state and the code that manages the transitions
of that state. So if the state transitions of this data are still provided in the monolith, it follows that the microservice
that wants to access (or change) that state needs to go via the state transitions in the monolith.

If the data you’re trying to access in the monolith’s database should really be “owned” by the microservice instead, I’m
more inclined to suggest skipping this pattern and instead looking to split the data out.

##### Pattern: Multischema storage
The invoice core data still lives in the monolith, which is where we (currently) access it from. We’ve added the ability
to add reviews to Invoices; this represents brand-new functionality not in the monolith. To support this, we need to store
a table of reviewers, mapping employees to Invoice IDs. If we put this new table in the monolith, we’d be helping grow the
database! Instead, we’ve put this new data in our own schema.

This pattern works well when adding brand-new functionality to your microservice that requires the storage of new data. 
It’s clearly not data the monolith needs (the functionality isn’t there), so keep it separate from the beginning. This 
pattern also makes sense as you start moving data out of the monolith into your own schema—a process that may take some time.

If the data you are accessing in the monolith’s schema is data that you never planned to move into your schema, I strongly
recommend use of the monolith as data access layer pattern


### Schema Separation Examples
So far, we’ve looked at schema separation at a fairly high level, but there are complex challenges associated with database
decomposition and a few tricky issues to explore.

### Pattern: Split Table

It was easy to separate ownership of data on a column-by-column basis. But what happens when multiple pieces of code update
the same column?

Remember, we want, where possible, to keep the state machines for our domain entities inside a single service boundary,
and updating a Status certainly feels like part of the state machine for a customer! This means that when the service split
has been made, our new Finance service will need to make a service call to update this status

### Pattern: Move Foreign-Key Relationship to Code
We’ve gone from a world where we have a single SELECT statement, to a new world where we have a SELECT query against the
Ledger table, followed by a service call to the Catalog service, which in turn triggers a SELECT statement against the 
Albums table

You need to have an understanding of acceptable latency for key operations, and be able to measure what the latency 
currently is. Distributed systems like Jaeger can really help here, as they provide the ability to get accurate timing of
operations that span multiple services. Making an operation slower may be acceptable if it is still fast enough, especially
if as a trade-off you gain some other benefits.

A trickier consideration is that with Catalog and Finance being separate services, with separate schemas, we may end up 
with data inconsistency. With a single schema, I wouldn’t be able to delete a row in the Albums table if there was a 
reference to that row in the Ledger table. My schema was enforcing data consistency. In our new world, no such 
enforcement exists.

#### Check before deletion
Transaction is not there is an issue
Another issue with checking if the record is already in use is that creates a de facto reverse dependency from the Catalog
service. Now we’d need to check with any other service that uses our records. This is bad enough if we have only one other
service using our information, but becomes significantly worse as we have more consumers.

I strongly urge you not to consider this option, because of the difficulty in ensuring that this operation is implemented
correctly as well as the high degree of service coupling that this introduces.

#### Handle deletion gracefully
This could be as simple as having our report show “Album Information Not Available” if we can’t look up a given SKU.

If we wanted to get really advanced, we could ensure that our Finance service is informed when a Catalog item is removed,
perhaps by subscribing to events. When we pick up a Catalog deletion event, we could decide to copy the now deleted Album
information into our local database. This feels like overkill in this particular situation, but could be useful in other
scenarios, especially if you wanted to implement a distributed state machine to perform something like a cascading deletion
across service boundaries.

#### Don’t allow deletion
we could just implement a soft delete capability. (deleteFlag)


When you start considering effectively breaking foreign-key relationships, one of the first things you need to ensure is
that you aren’t breaking apart two things that really want to be one. If you’re worried that you are breaking apart an
aggregate, pause and reconsider. In the case of the Ledger and Albums here, it seems clear we have two separate aggregates
with a relationship between them.

### Example: Shared Static Data
#### Pattern: Dedicated reference data schema
Why not just have each service have its own copy of the data
- Concerns around duplication of data tend to come down to two things. First, each time I need to change the data, I have to do so in multiple places.
  and what happens if the data is inconsistent
- Whether or not inconsistency is an issue comes down to how the data is used

This pattern should be used only rarely, and you should prefer some of the options we consider later. It is sometimes 
useful for large volumes of data, when it’s not essential for all services to see the exact same set of data. Something
like postal code files in the UK might be a good fit, where you periodically get updates of the mapping from postal codes to addresses.


#### Pattern: Dedicated reference data schema
If you really want one source of truth for your country codes, you could relocate this data to a dedicated schema, 
perhaps one set aside for all static reference data

We do have to consider all the challenges of a shared database. To an extent, the concerns around coupling and change 
are somewhat offset by the nature of the data.

This option has a lot of merits. We avoid the concerns around duplication, and the format of the data is highly unlikely
to change, so some of our coupling concerns are mitigated. For large volumes of data, or when you want the option of cross-schema
joins, it’s a valid approach. Just remember, any changes to the schema format will likely cause significant impact across
multiple services.

#### Pattern: Static reference data library
In fact, storing small volumes of static reference data in code form is something that I’ve done a number of times, and 
something I’ve seen done for microservice architectures.

Randy Shoup, who was VP of engineering at Stitch Fix said the sweet spot for this technique was for types of data that 
were small in volume and that changed infrequently or not at all

This is a neat solution, but it’s not without drawbacks. Obviously, if we have a mix of technology stacks, we may not be
able to share a single shared library. Also, remember the golden rule of microservices? We need to ensure that our microservices
are independently deployable. If we needed to update our country codes library, and have all services pick up the new data
immediately, we’d need to redeploy all services at the moment the new library is available.

If your microservices use shared libraries, remember that you have to accept that you might have different versions of 
the library deployed in production!

For small volumes of data, where you can be relaxed about different services seeing different versions of this data, this
is an excellent but often overlooked option. The visibility regarding which service has what version of data is especially useful.


#### Pattern: Static reference data service
I suspect you can see where this is ending up. This is a book about creating microservices, so why not consider creating
 dedicated service just for country codes,

The word “overkill” comes up frequently!

People who work in an environment where creating and managing a microservice is low are much more likely to consider this
option. If, on the other hand, creating a new service, even one as simple as this, requires days or even weeks of work,
then people will understandably push back on creating a service like this.

Even better, a Country Code service would be a great fit for something like a Function-as-a-Service platform like Azure 
Cloud Functions or AWS Lambda. 

Another concern cited is that by adding a service for country codes, we’d be adding yet another networked dependency that
could impact latency. 

This data can, of course, also be aggressively cached at the client side. We don’t add new entries to this data often, 
after all! We could also consider using events to let consumers know when this data has changed

---------------------------------------------------------------------------------------------------------------------------------
