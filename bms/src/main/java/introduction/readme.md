### Why Might You Choose Microservices?

#### Improve Team Autonomy
Autonomy works at the smaller scale too, and most modern organizations I work with are looking to create more autonomous
teams within their organizations, often trying to copy models from other organizations like Amazon’s two-pizza team model,
or the Spotify model

##### How else could you do this?
Working out how you can push more responsibility into the team doesn’t require a shift in architecture.
 - Giving ownership to parts of the codebase to different teams could be one answer 
 - this could also be done by identifying people empowered to make decisions for parts of the codebase on functional grounds 
   (yan knows display ads best, so he’s responsible for that; Jane knows the most about tuning our query performance,
   so run anything in that area past her first).
Improving autonomy can also play out in simply not having to wait for other people to do things for you, so adopting 
self-service approaches to provisioning machines or environments can be a huge enabler, avoiding the need for central
operations teams to have to field tickets for day-to-day activities.
   
#### Reduce Time to Market
By being able to make and deploy changes to individual microservices, and deploy these changes without having to wait for
coordinated releases, we have the potential to release functionality to our customers more quickly.

##### How else could you do this?
So think of all the steps involved with shipping software. Look at how long they take, the durations (both elapsed time 
and busy time) for each step, and highlight the pain points along the way. After all of that, you may well find that 
microservices could be part of the solution, but you’ll probably find many other things you could try in parallel.

#### Scale Cost-Effectively for Load
By breaking our processing into individual microservices, these processes can be scaled independently. This means we can
also hopefully cost-effectively scale—we need to scale up only those parts of our processing that are currently constraining
our ability to handle load.

##### How else could you do this?
- We could just get a bigger box for a star
- Traditional horizontal scaling of the existing monolith—basically running multiple copies—could prove to be highly effective.
- You could also replace technology being used with alternatives that can handle load better. 

#### Improve Robustness
By using microservices, we are able to implement a more robust architecture because functionality is decomposed—that is,
an impact on one area of functionality need not bring down the whole system.

We also get to focus our time and energy on those parts of the application that most require robustness—ensuring critical
parts of our system remain operational.

we are aware that a specific machine could die, so we might bring redundancy into our system by load balancing an instance.
That is an example of addressing robustness. Resiliency is the process of an organization preparing itself for the fact
that it cannot anticipate all potential problems.

Just spreading your functionality across multiple separate processes and separate machines does not guarantee improved robustness;
quite the contrary—it may just increase your surface area of failure.

##### How else could you do this?
By running multiple copies of your monolith, perhaps behind a load balancer or another load distribution mechanism like a queue,

#### Scale the Number of Developers
With clearly identified boundaries, and an architecture that has focused around ensuring our microservices limit their 
coupling with each other, we come up with pieces of code that can be worked on independently. Therefore, we hope we can 
scale the number of developers by reducing the delivery contention.

To successfully scale the number of developers you bring to bear on the problem requires a good degree of autonomy between
the teams themselves. Just having microservices isn’t going to be good enough.
You’ll also need to break up work in such a way that changes don’t need to be coordinated across too many services.

##### How else could you do this?
modular monolith - We still have some form of contention between the different teams, as the software is still all packaged
together, so the act of deployment still requires coordination between the appropriate parties.


#### Embrace New Technology
Monoliths typically limit our technology choices. We normally have one programming language on the backend, making use of one programming idiom. 

By isolating the technology change in one service boundary, we can understand the benefits of the new technology in 
isolation, and limit the impact if the technology turns out to have issues.

In my experience, while mature microservice organizations often limit how many technology stacks they support, they are 
rarely homogeneous in the technologies in use.

##### How else could you do this?
the JVM as one example can happily host code written in multiple languages within the same running process.
new database technology, which is complicated and risky.


### When Might Microservices Be a Bad Idea?

#### Unclear Domain
Getting service boundaries wrong can be expensive. It can lead to a larger number of cross-service changes, overly coupled
components, and in general could be worse than just having a single monolithic system.

This led to a high cost of change and high cost of ownership.
If you feel that you don’t yet have a full grasp of your domain, resolving that before committing to a system decomposition may be a good idea.

#### Startups
Startups, as distinct from scale-ups, are often experimenting with various ideas in an attempt to find a fit with customers.
This can lead to huge shifts in the original vision for the product as the space is explored, resulting in huge shifts in
the product domain.

Microservices primarily solve the sorts of problems startups have once they’ve found that fit with their customer base. 
Put a different way, microservices are a great way of solving the sorts of problems you’ll get once you have initial success
as a startup.

You have code you can examine, and you can speak to people who use and maintain the system. You also know what good looks like

You also have a system that is actually running. You understand how it operates and how it behaves in production.

Only split around those boundaries that are clear at the beginning, and keep the rest on the more monolithic side.
This will also give you time to assess how mature you are from an operational point of view—if you struggle to manage two
services, managing ten is going to be difficult.

#### Customer-Installed and Managed Software
If you create software that is packaged and shipped to customers who then operate it themselves, microservices may well 
be a bad choice. When you migrate to a microservice architecture, you push a lot of complexity into the operational domain.
Previous techniques you used to monitor and troubleshoot your monolithic deployment may well not work with your new distributed system.

The reality is that you cannot expect your customers to have the skills or platforms available to manage microservice architectures. 

#### Not Having a Good Reason!

### Trade-Offs
Create sliders for each priority

### Changing Organizations
#### Establishing a Sense of Urgency
The problem is that your idea is just one of many good ideas that are likely floating around the organization. The trick
is to help people understand that now is the time to make this particular change.

Remember, what you’re trying to do is not say, “We should do microservices now!” You’re trying to share a sense of urgency
about what you want to achieve—and as I’ve stated, microservices are not the goal!

#### Creating the Guiding Coalition
You need to identify the people inside your organization who can help you drive this change forward. 

#### Developing a Vision and Strategy
This is where you get your folks together and agree on what change you’re hoping to bring (the vision) and how you’re 
going to get there (the strategy).

The vision is mostly about the goal—what it is you’re aiming for. The strategy is about the how. Microservices are going
to achieve that goal (you hope; they’ll be part of your strategy). Remember that your strategy may change. Being committed
to a vision is important, but being overly committed to a specific strategy in the face of contrary evidence is dangerous,
and can lead to significant sunk cost fallacy.

#### Communicating the Change Vision
Having a big vision can be great, but don’t make it so big that people won’t believe it’s possible. I saw a statement put
out by the CEO of a large organization recently that said (paraphrasing somewhat)

_In the next 12 months, we will reduce costs and deliver faster by moving to microservices and embracing cloud-native technologies._


#### Empowering Employees for Broad-Based Action
when it comes to microservice adoption, the existing processes around provisioning of infrastructure can be a real problem.
If the way your organization handles the deployment of a new production service involves placing an order for hardware six
months in advance, then embracing technology that allows for the on-demand provisioning of virtualized execution
environments (like virtual machines or containers) could be a huge boon, as could the shift to a public cloud vendor.


#### Generating Short-Term Wins
If it takes too long for people to see progress being made, they’ll lose faith in the vision. So go for some quick wins.
Focusing initially on small, easy, low-hanging fruit will help build momentum. When it comes to microservice decomposition,
functionality that can easily be extracted from our monolith should be high on your list.

#### Consolidating Gains and Producing More Change
Once you’ve got some success, it becomes important not to sit on your laurels. Quick wins might be the only wins if you 
don’t continue to push on. It’s important you pause and reflect after successes (and failures) so you can think about how
to keep driving the change. You may need to change your approach as you reach different parts of the organization.

Once you’ve got some success, it becomes important not to sit on your laurels. Quick wins might be the only wins if you 
don’t continue to push on. It’s important you pause and reflect after successes (and failures) so you can think about how
to keep driving the change. You may need to change your approach as you reach different parts of the organization.

#### Anchoring New Approaches in the Culture
By continuing to iterate, roll out changes, and share the stories of successes (and failures), the new way of working will
start to become business as usual.

This, in turn, can create a new problem. Once the Big New Idea becomes the Established Way of Working, how can you make 
sure that future, better approaches have space to emerge and perhaps displace how things are done?


### Importance of Incremental Migration
Any transition to a microservice architecture should bear these principles in mind. Break the big journey into lots of 
little steps. Each step can be carried out and learned from. If it turns out to be a retrograde step, it was only a small
one. Either way, you learn from it, and the next step you take will be informed by those steps that came before.

As we discussed earlier, breaking things into smaller pieces also allows you to identify quick wins and learn from them.
It is really important to note that the extraction of a microservice can’t be considered complete until it is in production
and being actively used.

Microservice decomposition can cause issues with troubleshooting, tracing flows, latency, referential integrity, cascading
failures, and a host of other things. Most of those problems are things you’ll notice only after you hit production.

### Cost of Change
This allows us to better mitigate the cost of mistakes, but doesn’t remove the chance of mistakes entirely. We can—and 
will—make mistakes, and we should embrace that. What we should also do, though, is understand how best to mitigate the 
costs of those mistakes.

#### Reversible and Irreversible Decisions
The problem is that adopting a microservice architecture brings with it loads of options regarding how you do things—which
means you may need to make many more decisions than before. And if you—or your organization—isn’t used to that, you may find
yourself falling into this trap, and progress will grind to a halt.

The reality is, the vast number of decisions you will make as part of a microservice transition will be toward the Reversible
end of the spectrum. Software has a property where rollbacks or undos are often possible; you can roll back a software change
or a software deployment. What you do need to take into account is the cost of changing your mind later.

#### Easier Places to Experiment
The large cost of change means that these operations are increasingly risky. How can we manage this risk? My approach is
to try to make mistakes where the impact will be lowest.

I tend to do much of my thinking in the place where the cost of change and the cost of mistakes is as low as it can be: 
the whiteboard. Sketch out your proposed design. See what happens when you run use cases across what you think your service
boundaries will be. For our music shop, for example, imagine what happens when a customer searches for a record, registers
with the website, or purchases an album. What calls get made? Do you start seeing odd circular references? Do you see two
services that are overly chatty, which might indicate they should be one thing?









