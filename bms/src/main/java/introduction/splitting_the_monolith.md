## Migration Patterns

### Pattern: Strangler Fig Application (system rewrites)
The idea is that the old and the new can coexist, giving the new system time to grow and potentially entirely replace the
old system. The key benefit to this pattern, as we’ll see shortly, is that it supports our goal of allowing for incremental
migration to a new system. Moreover, it gives us the ability to pause and even stop the migration altogether, 
while still taking advantage of the new system delivered so far.

This may involve actually copying the code from the monolith (if possible), or else reimplementing the functionality in 
question. In addition, if the functionality in question requires the persistence of state, then consideration needs to be
given to how that state can be migrated to the new service, and potentially back again.

First, identify parts of the existing system that you wish to migrate. You’ll need to use judgement as to which parts of
the system to tackle first, using the sort of trade-off activity(slider for each aspect). You then need to implement 
this functionality in your new microservice. With your new implementation ready, you need to be able to reroute calls 
from the monolith over to your shiny new microservice.

It’s worth noting that until the call to the moved functionality is redirected, that the new functionality isn’t technically
live—even if it is deployed into a production environment. This means you could take your time getting that functionality
right, working on implementing this functionality over a period of time.

_Separating the concepts of deployment from release is important. Just because software is deployed into a given environment
doesn’t mean it’s actually being used by customers. By treating the two things as separate concepts, you enable the ability
to validate your software in the final production environment before it is being used, allowing you to de-risk the rollout
of the new software. Patterns like the strangler fig, parallel run, and canary release are among those patterns that make
use of the fact that deployment and release are separate activities._

you can extract an entire end-to-end slice of functionality in one piece. This simplifies the extraction greatly, aside
from concerns around data.In order to perform a clean end-to-end extraction like this, you might be inclined to extract 
larger groups of functionality to simplify this process.

If you do want to take a smaller bite, you may have to consider more “shallow” extractions.
Here we are extracting Payroll functionality, despite the fact it makes use of other functionality that remains inside the
monolith—in this example, the ability to send User Notifications.

For the strangler to work, though, you need to be able to clearly map the inbound call to the functionality you care about
to the asset that you want to move.we’d ideally like to move out the ability to send User Notifications to our customers
into a new service. However, notifications are fired as a result of multiple inbound calls to the existing monolith. 
Therefore, we can’t clearly redirect the calls from outside the system itself.

#### Example: HTTP Reverse Proxy
HTTP has some interesting capabilities, among them that it is very easy to intercept and redirect in a way that can be 
made transparent to the calling system. This means that an existing monolith with an HTTP interface is amenable to migration
through use of a strangler fig pattern.

- Step 1: Insert proxy
- Step 2: Migrate functionality :  First, get a basic service up and running without any of the functionality being 
  implemented. Your service will need to accept the calls made to the matching functionality, but at this stage you could
  just return a 501 Not Implemented. Even at this step, I’d get this service deployed into the production environment.
  This allows you to get comfortable with the production deployment process, and test the service in situ. At this point,
  your new service isn’t released, as you haven’t redirected the existing upstream calls yet.
  
- Step 3: Redirect calls : It’s only once you’ve completed movement of all the functionality that you reconfigure the proxy
  to redirect the call. If this fails for whatever reason, then you can switch the redirection 
  back—for most proxies, this is a very quick and easy process, giving you a fast rollback.

### Changing Behavior While Migrating Functionality
strangler fig pattern works well if the monolith and microservice Payroll functionality is functionally equivalent, but 
what if we’d changed how Payroll behaves as part of the migration?

If the Payroll microservice had a few bug fixes applied to how it works that hadn’t been back-ported to the equivalent 
functionality in the monolith, then a rollback would also cause those bugs to reappear in the system. This can get more 
problematic if you’d added new functionality to the Payroll microservice—a rollback would then require removing features
from your customers.


### Pattern: UI Composition 
user interface presents us with some useful opportunities to splice together functionality served in part from an existing
monolith or new microservice architecture.

At The Guardian, the Travel vertical was the first one identified to be migrated to the new platform. The rationale was 
partly that it had some interesting challenges around categorization, but also that it wasn’t the most high-profile part
of the site. Basically, we were looking to get something live, learn from that experience, but also make sure that if 
something did go wrong, then it wouldn’t affect the prime parts of the site.

UI composition as a technique to allow for re-platforming systems is highly effective, as it allows for whole vertical
slices of functionality to be migrated. For it to work, though, you need to have the ability to change the existing user
interface to allow for new functionality to be safely inserted. We’ll cover compositional techniques later in the book, 
but it’s worth noting that which techniques you can use will often depend on the nature of the technology used to implement
the user interface. A good old-fashioned website makes UI composition easy, whereas single-page app technology does add 
some complexity and an often bewildering array of implementation approaches!

### Pattern: Branch by Abstraction
For the useful strangler fig pattern, to work, we need to be able to intercept calls at the perimeter of our monolith. 
However, what happens if the functionality we want to extract is deeper inside our existing system?

In order to perform this extraction, we will need to make changes to the existing system. These changes could be significant,
and disruptive to other developers working on the codebase at the same time. We have competing tensions here. On the one hand,
we want to make our changes in incremental steps. On the other hand, we want to reduce the disruption to other people
working on other areas of the codebase. This will naturally drive us toward wanting to complete the work quickly.

Branch by abstraction consists of five steps:

- Create an abstraction for the functionality to be replaced.

- Change clients of the existing functionality to use the new abstraction.

- Create a new implementation of the abstraction with the reworked functionality. In our case, this new implementation will
  call out to our new microservice.

- Switch over the abstraction to use our new implementation.

- Clean up the abstraction and remove the old implementation.

### Pattern: Parallel Run
When using a parallel run, rather than calling either the old or the new implementation, instead we call both, allowing
us to compare the results to ensure they are equivalent. Despite calling both implementations, only one is considered the
source of truth at any given time. Typically, the old implementation is considered the source of truth until the ongoing
verification reveals that we can trust our new implementation.

This technique can be used to verify not just that our new implementation is giving the same answers as the existing 
implementation, but that it is also operating within acceptable nonfunctional parameters. For example, is our new service
responding quickly enough? Are we seeing too many time-outs?

GitHub’s Scientist library is a notable library to help implement this pattern at a code level.

It’s worth calling out that a parallel run is different from what is traditionally called canary releasing. A canary 
release involves directing some subset of your users to the new functionality, with the bulk of your users seeing the old implementation.

Dark launching, parallel runs, and canary releasing are techniques that can be used to verify that our new functionality
is working correctly, and reduce the impact if this turns out not to be the case. All these techniques fall under the banner
of what is called progressive delivery—an umbrella term coined by James Governor to describe methods to help control how
software is rolled out to your users in a more nuanced fashion, allowing you to release software more quickly while validating
its efficacy and reducing the impact of problems should they occur.

Implementing a parallel run is rarely a trivial affair, and is typically reserved for those cases where the functionality
being changed is considered to be high risk.

#### Verification Techniques
With a parallel run, we want to compare functional equivalence of the two implementations. If we take the example of the
credit derivative pricer from before, we can treat both versions as functions—given the same inputs, we expect the same 
outputs. But we also can (and should) validate the nonfunctional aspects, too.

##### Using Spies
In the case of our previous notification example, we wouldn’t want to send an email to our customer twice. In that situation,
a Spy could be handy. A pattern from unit testing, a Spy can stand in for a piece of functionality, and allows us to verify
after the fact that certain things were done. The Spy stands in and replaces a piece of functionality, stubbing it out.


### Pattern: Decorating Collaborator
What happens if you want to trigger some behavior based on something happening inside the monolith, but you are unable to
change the monolith itself? The decorating collaborator pattern can help greatly here. The widely known decorator pattern
allows you to attach new functionality to something without the underlying thing knowing anything about it. We are going
to use a decorator to make it appear that our monolith is making calls to our services directly, even though we haven’t 
actually changed the underlying monolith.

Music Corp is all about our customers! We want to add the ability for them to earn points based on orders being placed, 
but our current order placement functionality is complex enough that we’d rather not change it right now. So the order 
placement functionality will stay in the existing monolith, but we will use a proxy to intercept these calls, and based 
on the outcome decide how many points to deliver

Now our proxy is having to embody quite a few more “smarts.”The more code you start adding here, the more it ends up 
becoming a microservice in its own right

Another potential challenge is that we need enough information from the inbound request to be able to make the call to 
the microservice. For example, if we want to reward points based on the value of the order, but the value of the order 
isn’t clear from either the Place Order request or response, we may need to look up additional information—perhaps 
calling back into the monolith to extract the required information

My gut feeling is that if the request and response to and from the monolith don’t contain the information you need, then
think carefully before using this pattern.

### Pattern: Change Data Capture
With change data capture, rather than trying to intercept and act on calls made into the monolith, we react to changes 
made in a datastore. For change data capture to work, the underlying capture system has to be coupled to the monolith’s 
datastore. That’s really an unavoidable challenge with this pattern.

#### Implementing Change Data Capture
- Database triggers
- Transaction log pollers
- Batch delta copier Probably the most simplistic approach is to write a program that on a regular schedule scans the 
  database in question for what data has changed, and copies this data to the destination. These jobs are often run using
  tools like cron or similar batch scheduling tools.

