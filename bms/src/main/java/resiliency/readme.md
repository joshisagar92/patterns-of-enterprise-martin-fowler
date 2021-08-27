### What Is Resiliency?

#### Robustness
Robustness is the concept whereby we build mechanisms into our software and processes to accommodate expected problems. 
We have an advanced understanding of the kinds of perturbations we might face, and we put measures in place so that when
these issues arise, our system can deal with them.

#### Rebound
How well we recover—rebound—from disruption is a key part of building a resilient system. All too often I see people focusing
their time and energy on trying to eliminate the possibility of an outage, only to be totally unprepared once an outage 
actually occurs. By all means, do your best to protect against the bad things that you think might happen—improving your
system’s robustness—but also understand that as your system grows in scale and complexity, eliminating any potential 
problem becomes unsustainable.

#### Graceful Extensibility
With rebound and robustness, we are primarily dealing with the expected. We are putting mechanisms in place to deal with
problems that we can foresee. But what happens when we are surprised?

Often, in a drive to optimize our system, we can as an unfortunate side effect increase the brittleness of our system. 
Take automation as an example. Automation is fantastic—it allows us to do more with the people we have, but it can also 
allow us to reduce the people we have, as more can be done with automation. This reduction in staff can be concerning, 
though. Automation can’t handle surprise—our ability to gracefully extend our system, to handle surprise, comes from 
having people in place with the right skills, experience, and responsibility to handle these situations as they arise.

#### Sustained Adaptability
_**“No matter how good we have done before, no matter how successful we’ve been, the future could be different, and we might
not be well adapted. We might be precarious and fragile in the face of that new future.”_**

Creating a culture that prioritizes creating an environment in which people can share information freely, without fear of
retribution, is vital to encourage learning in the wake of an incident. Having the bandwidth to really examine such surprises
and extract the key learnings requires time, energy, and people—all things that will reduce the resources available to 
you to deliver features in the short term.

To work toward sustained adaptability means that you are looking to discover what you don’t know. This requires continuing
investment, not one-off transactional activities—the term sustained is important here. It’s about making sustained 
adaptability a core part of your organizational strategy and culture.

#### And Microservice Architecture
Taken more broadly, the ability to deliver resiliency is a property not of the software itself but of the people building
and running the system. Given the focus of this book, much of what follows in this chapter will focus primarily on what 
a microservice architecture can help deliver in terms of resiliency—which is almost entirely limited to improving the 
robustness of applications.

### Failure Is Everywhere
fallacies of distributed computing

https://web.archive.org/web/20171107014323/http://blog.fogcreek.com/eight-fallacies-of-distributed-computing-tech-talk/

We can also spend a bit less of our time trying to stop the inevitable and a bit more of our time dealing with it gracefully.
I’m amazed at how many organizations put processes and controls in place to try to stop failure from occurring but put 
little to no thought into actually making it easier to recover from failure in the first place. Understanding the things
that are likely to fail is key to improving the robustness of our system.

the hard drives in these servers were attached with Velcro rather than screws to make it easy to replace drives—helping 
Google get the machine up and running quickly when a drive failed, and in turn helping that component of the system rebound
more effectively.

### How Much Is Too Much?
How much failure you can tolerate or how fast your system needs to be is driven by the users of your system. That information
in turn helps you understand which techniques will make the most sense for you. That said, your users won’t always be 
able to articulate what their exact requirements are. So you need to ask questions to help extract the right information
and help them understand the relative costs of providing different levels of service.

When it comes to considering if and how to scale out your system to better handle load or failure, start by trying to 
understand the following requirements:

#### Response time/latency
How long should various operations take? It can be useful to measure this with different numbers of users to understand 
how increasing load will impact the response time. Given the nature of networks, you’ll always have outliers, so setting
targets for a given percentile of the responses monitored can be useful. The target should also include the number of 
concurrent connections/users you will expect your software to handle. So you might say, “We expect the website to have a
90th-percentile response time of 2 seconds when handling 200 concurrent connections per second.”

#### Availability
Can you expect a service to be down? Is this considered a 24/7 service? Some people like to look at periods of acceptable
downtime when measuring availability, but how useful is this to someone calling your service? Either I should be able to
rely on your service responding or I shouldn’t. Measuring periods of downtime is really more useful from a historical reporting angle.

#### Durability of data
How much data loss is acceptable? How long should data be kept for? This is highly likely to change on a case-by-case basis.
For example, you might choose to keep user session logs for a year or less to save space, but your financial transaction
records might need to be kept for many years.

### Degrading Functionality
If the stock levels are unavailable, we might make the decision to still go ahead with the sale and work out the details
later. If the shopping cart microservice is unavailable, we’re probably in a lot of trouble, but we could still show the
web page with the listing. Perhaps we just hide the shopping cart or replace it with an icon saying “Be Back Soon!”

### Stability Patterns

systems that just act slow are much harder to deal with than systems that just fail fast. In a distributed system, latency kills.

Even if we’d had the time-outs on the pool set correctly, we were also sharing a single HTTP connection pool for all outbound
requests. This meant that one slow downstream service could exhaust the number of available workers all by itself, even
if everything else was healthy. Lastly, it was clear due to the frequent time-outs and errors that the downstream service 
in question wasn’t healthy, but despite this we kept sending traffic its way. In our situation, this meant we were actually 
making a bad situation worse, as the downstream service had no chance to recover. We ended up implementing three fixes
to avoid this happening again: getting our time-outs right, implementing bulkheads to separate out different connection
pools, and implementing a circuit breaker to avoid sending calls to an unhealthy system in the first place.

#### Time-Outs
Time-outs are easy to overlook, but in a distributed system they are important to get right. How long can I wait before 
I should give up on a call to a downstream service? If you wait too long to decide that a call has failed, you can slow 
the whole system down. Time out too quickly, and you’ll consider a call that might have worked as failed. Have no time-outs
at all, and a downstream service being down could hang your whole system.

In the case of AdvertCorp, we had two time-out related issues. Firstly, we had a missing time-out on the HTTP request pool,
meaning that when asking for a worker to make a downstream HTTP request, the request thread would block forever until a 
worker became available. Secondly, when we finally had an HTTP worker available to make a request to the turnip ad system,
we were waiting way too long before giving up on the call.

Time-outs are incredibly useful. Put time-outs on all out-of-process calls, and pick a default time-out for everything. 
Log when time-outs occur, look at what happens, and change them accordingly. Look at “normal” healthy response times for
your downstream services, and use that to guide where you set the time-out threshold.

it can make sense to have a time-out for the overall operation and to give up if this time-out is exceeded. For that to 
work, the current time left for the operation would need to be passed downstream. For example, if the overall operation 
to render a page had to complete within 1,000 ms, and by the time we made the call to the downstream turnip ad service 
300 ms had already passed, we would then need to make sure we waited no longer than 700 ms for the rest of the calls to complete.

Don’t just think about the time-out for a single service call; also think about a time-out for the overall operation, and
abort the operation if this overall time-out budget is exceeded.

#### Retries
It can be useful to consider what sort of downstream call failures should even be retried. If using a protocol like HTTP,
for example, you may get back some useful information in the response codes that can help you determine if a retry is warranted.
If you got back a 404 Not Found, a retry is unlikely to be a useful idea. On the other hand, a 503 Service Unavailable or
a 504 Gateway Time-out could be considered temporary errors and could justify a retry.

You will likely need to have a delay before retrying. If the initial time-out or error was caused by the fact that the 
downstream microservice was under load, then bombarding it with additional requests may well be a bad idea.

If you are going to retry, you need to take this into account when considering your time-out threshold. If the time-out 
threshold for a downstream call is set to 500 ms, but you allow up to three retries with one second between each retry, 
then you could end up waiting for up to 3.5 seconds before giving up. As mentioned earlier, having a budget for how long
an operation is allowed to take can be a useful idea—you might not decide to do the third (or even second) retry if you’ve
already exceeded the overall time-out budget. On the other hand, if this is happening as part of a non-user-facing operation,
waiting longer to get something done might be totally acceptable.

#### Bulkheads
n Release It!,4 Michael Nygard introduces the concept of a bulkhead as a way to isolate yourself from failure. In shipping,
a bulkhead is a part of the ship that can be sealed off to protect the rest of the ship. So if the ship springs a leak, 
you can close the bulkhead doors. You lose part of the ship, but the rest of it remains intact.

We should have used different connection pools for each downstream connection. That way, if one connection pool got 
exhausted, the other connections wouldn’t be impacted

Separation of concerns can also be a way to implement bulkheads. By teasing apart functionality into separate microservices,
we reduce the chance of an outage in one area affecting another.

In many ways, bulkheads are the most important of the patterns we’ve looked at so far. Time-outs and circuit breakers help
you free up resources when they are becoming constrained, but bulkheads can ensure they don’t become constrained in the 
first place. They can also give you the ability to reject requests in certain conditions to ensure that resources don’t 
become even more saturated; this is known as load shedding. Sometimes rejecting a request is the best way to stop an 
important system from becoming overwhelmed and being a bottleneck for multiple upstream services.

#### Circuit Breakers
We can think of our circuit breakers as an automatic mechanism to seal a bulkhead, not only to protect the consumer from
the downstream problem but also to potentially protect the downstream service from more calls that may be having an adverse
impact. Given the perils of cascading failure, I’d recommend mandating circuit breakers for all your synchronous downstream calls.

With a circuit breaker, after a certain number of requests to the downstream resource have failed (due either to error or
to a time-out), the circuit breaker is blown. All further requests that go through that circuit breaker fail fast while 
the breaker is in its blown (open) state

After a certain period of time, the client sends a few requests through to see if the downstream service has recovered,
and if it gets enough healthy responses it resets the circuit breaker.

Getting the settings right can be a little tricky. You don’t want to blow the circuit breaker too readily, nor do you want
to take too long to blow it.

Circuit breakers help our application fail fast—and failing fast is always better than failing slow. The circuit breakers
allow us to fail before wasting valuable time (and resources) waiting for an unhealthy downstream microservice to respond.
Rather than waiting until we try to use the downstream microservice to fail, we could check the status of our circuit 
breakers earlier. If a microservice we will rely on as part of an operation is currently unavailable, we can abort the 
operation before we even start.

#### Isolation
There is another benefit to increasing isolation between services. When services are isolated from each other, much less
coordination is needed between service owners. The less coordination needed between teams, the more autonomy those teams
have, as they are able to operate and evolve their services more freely.

Consider two microservices that appear to be entirely isolated from one another. They don’t communicate with each other 
in any way. A problem with one of them shouldn’t impact the other, right? But what if both microservices are running on 
the same host, and one of the microservices starts using up all the CPU, causing that host to have issues?

Consider another example. Two microservices each have their own, logically isolated database. But both databases are 
deployed onto the same database infrastructure. A failure in that database infrastructure would impact both microservices.

Isolation, like so many of the other techniques we have looked at, can help improve the robustness of our applications, 
but it’s rare that it does so for free. Deciding on the acceptable trade-offs around isolation versus cost and increased
complexity, like so many other things, can be vital.

#### Redundancy
Having more of something can be a great way to improve the robustness of a component. Having more than one person who knows
how the production database works seems sensible, in case someone leaves the company or is away on leave. Having more than
one microservice instance makes sense as it allows you to tolerate the failure of one of those instances and still have 
a chance of delivering the required functionality.

On AWS, for example, you do not get an SLA for the uptime of a single EC2 (virtual machine) instance. You have to work on
the assumption that it can and will die on you. So it makes sense to have more than one. But going further, EC2 instances
are deployed into availability zones (virtual data centers), and you also have no guarantees regarding the availability 
of a single availability zone, meaning you’d want that second instance to be on a different availability zone to spread the risk.

#### Middleware
So using middleware like message brokers to help offload some robustness concerns can be useful, but not in every situation.

#### Idempotency
In idempotent operations, the outcome doesn’t change after the first application, even if the operation is subsequently 
applied multiple times. If operations are idempotent, we can repeat the call multiple times without adverse impact. 
This is very useful when we want to replay messages that we aren’t sure have been processed, a common way of recovering from error.


### Spreading Your Risk
One way to scale for resilience is to ensure that you don’t put all your eggs in one basket. A simplistic example of this
is making sure that you don’t have multiple services on one host, where an outage would impact multiple services.


### CAP Theorem



