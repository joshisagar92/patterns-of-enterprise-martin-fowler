
### Ownership at Scale
Strong code ownership
All services have owners. If someone outside that ownership group wants to make a change, they have to submit that change
to the owners, who decide whether it is allowed. The use of a pull request for people outside the ownership group is one
example of how this could be handled.

Weak code ownership
Most, if not all, services are owned by someone, but anyone can still directly change their modules without resorting to
the need for things like pull requests. Effectively, source control is set up to still allow anyone to change anything,
but there is the expectation that if you change someone else’s service, you’ll speak to them first.

Collective code ownership
No one owns anything, and anyone can change anything they want.

As you grow your number of services and number of developers, you may start to experience problems with collective ownership.
For collective ownership to work, the collective needs to be well-connected enough to have a common shared understanding
of what a good change looks like, and in which direction you want to take a specific service from a technical point of view.

For many teams starting out small, a collective code ownership model makes sense. With a small number of colocated developers
(around 20), I’m happy with this model. As the number of developers increases, or those developers are distributed, it
becomes harder to keep everyone on the same page regarding things like what makes for a good commit or how individual
services should evolve.

For teams experiencing fast growth, a collective ownership model is problematic.
strong code ownership is almost universally the model adopted by organizations implementing large-scale microservice
architectures consisting of multiple teams and over 100 developers.


### Breaking Changes
A microservice exists as part of a wider system. It either consumes functionality provided by other microservices, exposes
its own functionality to other microservice consumers, or possibly does both.

We can think of the functionality we expose to other microservices in terms of a contract. It’s not just about saying,
“This is the data I’ll return.” It’s also about defining the expected behavior of your service. Whether or not you’ve
made this contract with your consumers explicit, it exists. When you make a change to your service, you need to make sure
you don’t break this contract; otherwise, nasty production issues can occur.

I find this to be a fairly early growing pain that teams encounter, especially when development is spread across more
than one team.

I have a set of rules for managing breaking contracts. They’re pretty simple:

1.Don’t break contracts.

2.See rule 1.

OR

**Eliminate accidental breaking changes**
Having an explicit schema for your microservice can quickly detect structural breakages in your contract.
You also have semantic breakages to consider.

**Think twice before making a breaking change**
If possible, prefer expansion changes to your contract if you can. Add new methods, resources, topics, or whatever that
support the new functionality without removing the old. Try to find ways to support the old while still supporting the new.

**Give consumers time to migrate**
you need to allow consumers to still use the old contract even if your newer contract is available. You’ll then need to
give all consumers time to change their services to migrate over to your newer service version.

I’ve seen this done in two ways. The first is to run two versions of your microservice,
The primary challenges with this approach are that you have to have more infrastructure to run the extra services, you
probably have to maintain data compatibility between the service versions, and bug fixes may need to be made to all
running versions, which inevitably requires source code branching.

The approach I prefer is to have one running version of your microservice, but have it support both contracts. This could
involve exposing two APIs on different ports.

### Reporting
With a monolithic system, you typically have a monolithic database. This means that stakeholders who want to analyze all
of the data together, often involving large join operations across data, have a ready-made schema against which to run their
reports. They can just run them directly against the monolithic database, perhaps against a read replica

This one tends to bite fairly early, and normally comes at the stage when you’re starting to consider decomposing a monolithic
schema. Hopefully, this is discovered before it becomes an issue, but I’ve seen more than one project where the team doesn’t
realize until halfway through that the architecture direction was going to create misery for stakeholders interested in 
reporting use cases.

You may be able to sidestep this problem if your monolith already uses a dedicated data source for reporting purposes, 
like a data warehouse or data lake.

We’ve already looked at potential solutions to this problem in Chapter 4. A change data capture system is an obvious 
potential solution for solving this, but techniques like views can also be useful, as you may be able to project a single
reporting schema from views exposed from the schemas of multiple microservice databases.

### Monitoring and Troubleshooting
- Log Aggregation(Humio, ELK stack)
- Tracing
- Monitoring & Observability

### Local Developer Experience
As you have more and more services, the developer experience can start to suffer. More resource-intensive runtimes like 
the JVM can limit the number of microservices that can be run on a single developer machine. I could probably run four or
five JVM-based microservices as separate processes on my laptop, but could I run ten or twenty? Probably not.

If I want to develop locally but reduce the number of services that I have to run, a common technique is to “stub out” 
those services I don’t want to run myself, or else have a way to point them against instances running elsewhere. A pure 
remote developer setup allows for you to develop against lots of services hosted on more capable infrastructure. However,
with that comes associated challenges of needing connectivity

Telepresence is an example of a tool that is aiming to make a hybrid local/remote developer workflow easier for Kubernetes
users. You can develop your service locally, but Telepresence can proxy calls to other services to a remote cluster, 
allowing you (hopefully) the best of both worlds.


### Running Too Many Things
As you have more services, and more service instances of those services, you have more processes that need to be deployed,
configured, and managed. Your existing techniques for handling the deployment and configuration of your monolithic 
application may well not scale well as you increase the number of moving parts that need to be managed.

Desired state management in particular becomes increasingly important.

You want a tool that allows for a high degree of automation, that can allow developers ideally to self-service provision
deployments, and that handles automated desired state management.

For microservices, Kubernetes has emerged as the tool of choice in this space.

### End-to-End Testing
But with a microservice architecture, the “scope” of our end-to-end tests gets very large. We are now having to run tests
across multiple services, all of which need to be deployed and appropriately configured for the test scenarios.

- Limit scope of functional automated tests
- Use consumer-driven contracts
- Use automated release remediation and progressive delivery

### Global Versus Local Optimization
Assuming you embrace the model of teams having more responsibility for local decision-making, perhaps owning the entire 
life cycle of the microservices they manage, you’ll get to the point where you start to need to balance local decision-making
with more global concerns.

The Invoicing team decides to use Oracle as a database, as they know it well. The Notifications team wants to use MongoDB
because it fits their programming model well. Meanwhile, the Fulfillment team wants to use PostgreSQL, as they already have it.
When you look at each decision in turn, it makes perfect sense, and you understand how that team would have made that choice.

If you step back and look at the big picture, though, you have to ask yourself whether or not as an organization you want
to build skills, and pay license fees, for three databases with somewhat similar capabilities. 

I remember speaking to people at REA, a real estate company in Australia. After many years building microservices, they 
got to the point where they realized that there were many ways that teams would deploy services. This caused problems when
people moved from one team to another, as they had to learn the new way of doing things. It also became hard to justify 
the duplicate work that each team was doing. As a result, they decided to put some work into coming up with a common way to handle this.

The trick is helping people in teams realize where their decisions might tend toward the irreversible or reversible ends
of this spectrum. The more a decision tends toward irreversible, the more important it might be for them to involve other
people outside their team boundary in their decision-making. For this to work well, teams need to have at least a basic 
understanding of the bigger-picture concerns to see where they may overlap, and they’ll also need a network where they 
can surface these issues and get involvement from their colleagues in other teams.

### Robustness and Resiliency
Distributed systems can exhibit a whole host of failure modes that may be unfamiliar to you if you are more accustomed to
monolithic systems. Network packets can get lost, network calls can time out, machines can die or stop responding.

A good starting point is to ask yourself a couple of questions about each service call you make. First, do I know the way
in which this call might fail? Second, if the call does fail, do I know what I should do?

### Orphaned Services
I refer to these services as orphaned services, as fundamentally no one in the company is taking ownership or responsibility for them.
    


















