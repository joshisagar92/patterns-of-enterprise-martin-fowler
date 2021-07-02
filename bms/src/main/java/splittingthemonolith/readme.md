####Incremental Migration
If you do a big-bang rewrite, the only thing you’re guaranteed of is a big bang.

Break the big journey into lots of little steps. Each step can be carried out and learned from. If it turns out to be a
retrograde step, it was only a small one. Either way, you learn from it, and the next step you take will be informed by
those steps that came before.

By splitting out microservices one at a time, you also get to unlock the value they bring incrementally, 
You won’t appreciate the true horror, pain, and suffering that a microservice architecture can bring until you are
running in production.

#### The Monolith Is Rarely the Enemy
For example a move to improve the ability of the application to handle more load might be satisfied by removing the 10% 
of the functionality that is currently bottlenecked, leaving the remaining 90% in the monolithic system.

#### The Dangers of Premature Decomposition
it became clear that the use cases of SnapCI were subtly different enough that the initial take on the service boundaries
wasn’t quite right. This led to lots of changes being made across services, and an associated high cost of change.
Eventually the team merged the services back into one monolithic system, giving them time to better understand where the
boundaries should exist. A year later, the team was then able to split the monolithic system apart into microservices,
whose boundaries proved to be much more stable.

Prematurely decomposing a system into microservices can be costly, especially if you are new to the domain.
In many ways, having an existing codebase you want to decompose into microservices is much easier than trying to go to
microservices from the beginning for this very reason.

#### What to Split First?
Once you have a firm grasp on why you think microservices are a good idea, you can use this to help prioritize which
microservices to create first.
Want to scale the application? Functionality which currently constrain the system’s ability to handle load are going to
be high on the list.
Want to improve time to market? Look at the system’s volatility to identify those pieces of functionality that change 
most frequently, and see if they would work as microservices.

You can use static analysis tools like Codescene to quickly find volatile parts of your code base.

But you also have to consider what decompositions are viable. Some functionality can be so deeply baked into the existing
monolithic application that it is impossible to see how it can be de-tangled. Or perhaps the functionality in question 
is so critical to the application that any changes are considered high risk.

My advice for the first couple of microservices would be to pick things which lean a bit more towards the “easy” end of 
the spectrum.

#### Decomposition By Layer
Let’s consider we are looking to extract functionality related to managing a customer’s wishlist. There
is some application code that lives in the monolith, and some related data storage in the database. So which bit should 
we extract first?

##### Code First
we have extracted the code associated with the wishlist functionality into a new microservice. The data for the wishlist
remains in the monolithic database at this stage - we haven’t completed the decomposition until we’ve also moved the data
related to the new Wishlist microservice out as well.

If we left the data in the monolithic database we’re storing up lots of pain for the future, so that does need to be 
addressed too, but we have gained a lot from our new microservice.

If we found that it was impossible to extract the application code cleanly, we could abort any further work, avoiding 
the need to de-tangle the database. If however the application code is cleanly extracted but extracting the data proves
to be impossible, we could be in trouble - it’s essential therefore that even if you decide to extract the application 
code before the data, you need to have looked at the associated data storage and have some idea as to whether extraction 
is viable, and how you will go about it. So do the legwork to sketch out how both application code and data will be 
extracted before you start.

##### Data First
This is something I see less often but can be useful in situations where you are unsure if the data can be separately 
cleanly. Here, you prove that this can be done before moving on to the hopefully easier application code extraction.


#### Useful Decompositional Patterns

##### Strangler Fig Pattern
the pattern describes the process of wrapping an old system with the new system over time, allowing the new system to 
take over more and more features of the old system incrementally.

You intercept calls to the existing system, so in our case the existing monolithic application. If the call to that 
piece of functionality is implemented in our new microservice architecture, it is redirected to the microservice.
If the functionality is still provided by the monolith, the call is allowed to continue to the monolith itself.

The beauty of this pattern is that it can often be done without making any changes to the underlying monolithic
application. The monolith is unaware that it has even been “wrapped” with a newer system.

##### Parallel Run
When switching between functionality provided by an existing tried and tested application architecture over to a fancy 
new microservice-based one, there may be some nervousness, especially if the functionality being migrated is critical to
your organization.

A useful pattern to make sure the new functionality is working well, without risking the existing system behavior, is to
make use of the parallel run pattern, running both your monolithic implementation of the functionality and the new 
microservice implementation side-by-side, serving the same requests, and comparing the results.

##### Feature Toggle
A feature toggle is a mechanism that allows for a feature to be switched off or on, or to switch between two different
implementations of some functionality.

With the strangler fig pattern example of using a HTTP proxy, we could implement the feature toggle in the proxy layer 
to allow for a simple control to switch between which implementation is live.

**https://www.martinfowler.com/articles/feature-toggles.html**

#### Data Decomposition Concerns 

##### Performance
Often though when splitting databases apart in the name of microservices we end up having to move join operations from 
the data tier up into the microservices themselves. And try as we might, it’s unlikely to be as fast.

##### Data Integrity
Album and Ledger tables being in the same database, we could (and likely would) define a foreign key relationship between
the rows in the Ledger table and the Album table. This would ensure that we’d always be able to navigate from a record 
in the Ledger table back to information about the album sold, as we wouldn’t be able to delete records from the Album 
table if they were referenced in Ledger.

With these tables now living in different databases, we no longer have enforcement of the integrity of our data model.
There is nothing to stop us from deleting a row in the Album table, causing an issue when we try to work out exactly what
item was sold

There are a number of workarounds, although “coping patterns” would be better definitions for how to deal with this
problem. We could use a soft delete in the Album table, so we don’t actually remove a record, we just mark it as deleted.
Another option could be to copy the name of the album into the Ledger table when a sale is made.

##### Transactions
distributed transactions for managing state changes across multiple microservices, but they do come with new sources of 
complexity

##### Tooling



     