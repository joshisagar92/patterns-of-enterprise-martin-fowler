### From Logical to Physical
We could talk about how our Invoice microservice communicates with the Order microservice without actually looking at the
physical topology of how these services are deployed. A logical view of an architecture typically abstracts away underling
physical deployment concerns - that notion needs to change for the scope of this chapter.

#### Multiple Instances
To start with, it seems quite likely that we’ll have more than one instance of each service. Having multiple instances of
a service allows you to handle more load, and can also improve the robustness of your system as you can more easily tolerate
the failure of a single instance. So, we’ve potentially got one or more instances of Invoice talking to one or more instances of Order.

If you are having multiple instances of a service for robustness reasons, you’d likely want to make sure that these instances
aren’t all on the same underlying hardware. Taken further, this might require that you have different instances distributed
not only across multiple machines, but also different data centers, to give protection against a whole data centre being made unavailable

When it comes to something like a managed virtual machine, neither AWS, Azure nor Google will give you an SLA for a 
single machine, nor do they give you an SLA for a single availability zone.

#### The Database
Share db withing same microservice.But doesn’t this violate our “don’t share the database” rule? Not really. One of our 
major concerns is that when sharing a database across multiple different microservices, that the logic associated with accessing 
and manipulating that state is now spread across different microservices. But here, the data is being shared by different
instances of the same microservice.The logic for accessing and manipulating state is still held within a single logical microservice.

##### Database Deployment And Scaling
Broadly speaking, a physical database deployment might be hosted on multiple machines, for a host of reasons. A common 
xample would be to split load for read and writes between a primary and one or more nodes that are designated for read-only purposes

All read-only traffic goes to one of the read replica nodes, and you can further scale read traffic by adding additional
read-nodes. Due to the way that relational databases work it’s more difficult to scale writes by adding additional machines
(typically sharding models are required, which adds additional complexity) so moving read-only traffic to these read replicas
can often free up more capacity on the write node to allow for more scaling.

Added to this complex picture is the fact that the same database infrastructure can support multiple logically isolated 
databases. So, the database for Invoice and Order might both be served from the same underlying database engine and hardware
. This can have significant benefits as it allows you to pool hardware to serve multiple microservices, can reduce licencing
costs, and can also help reduce the work around management of the database itself.

The one major thing to consider is the fact that if this shared database infrastructure fails, that you might impact 
multiple microservices, which could have catastrophic impact.

On the other hand, teams that run on public cloud providers are much more likely to provision dedicated database 
infrastructure on a per-microservice basis.

#### Environments
Some environments will have production data, some won’t. Some environments may have all services in them, others might 
just have a small number, with any non-present services replaced with fake ones for the purposes of testing.

Typically, we think of our software as moving through a number of pre-production environments, with each one serving some
purpose to allow the software to be developed and its readiness for production to be tested - we explored this earlier in
“Tradeoffs and Environments”.

Ideally, each environment in this process would be an exact copy of the production environment. This would give us even 
more confidence that our software will work when it reaches production. However, in reality, we often can’t afford to run
multiple copies of our entire production environment due to how expensive this is.

### Principles Of Microservice Deployment

#### Isolated Execution
just put all of your microservice instances on a single machine (which could be a single physical machine, or single VM)

First, it can make monitoring more difficult. For example, when tracking CPU, do I need to track the CPU of one service 
independent of the others? Or do I care about the CPU of the host as a whole? Side effects can also be hard to avoid. If
one service is under significant load, it can end up reducing the resources available to other parts of the system.

Deployment of services can be somewhat more complex too, as ensuring one deployment doesn’t affect another leads to additional headaches.

This model can also inhibit autonomy of teams.

Fundamentally, running lots of microservice instances on the same machine (virtual or physical) ends up drastically undermining
one of the key principles of microservices as a whole - independent deployability. It follows therefore, that we really 
want to run microservice instances in isolation

In general, the isolation around containers has improved sufficiently making them a more natural choice for microservice
workloads. The difference in isolation between containers and VMs has reduced to the point where for the vast majority of
workloads it is “good enough”, which is in large part why they are such a popular choice, and tend to be my default choice
in most situations.

#### Focus On Automation
As you add more microservices, you’ll have more moving parts to deal with. More processes, more things to configure, more
instances to monitor. Moving to microservices pushes a lot of complexity into the operational space, and if you are managing
your operational processes in a mostly manual way, this means that more services will require more and more people to do things.

Picking technology that enables automation starts with the tools used to manage hosts. Can you write a line of code to 
launch a virtual machine, or shut one down? Can you deploy the software you have written automatically? Can you deploy 
database changes without manual intervention? Embracing a culture of automation is key if you want to keep the complexities
of microservice architectures in check.

#### Infrastructure As Code (IAC)
Taking the concept of automation further, Infrastructure As Code (IAC) is the concept whereby your infrastructure is configured
by using machine-readable code.

Version controlling your infrastructure code gives you transparency over who has made changes, something that auditors love.
It also makes it easier to reproduce an environment at a given point in time. This is something that can be especially useful
when trying to track down defects. In one memorable example, one of my clients had to recreate an entire running system 
as of a specific time some years before, down to the patch levels of the operating systems and the contents of message brokers.

#### Zero-downtime Deployment
Implementing the ability for zero-downtime deployment can be a huge step up in allowing microservices to be developed and
deployed. Without zero-downtime deployment, I may have to co-ordinate with upstream consumers when I release software to
alert them of a potential outage.

Concepts like rolling upgrades can be handy here, and this is one area where the use of a platform like Kubernetes makes
your life much easier. With a rolling upgrade, your microservice isn’t totally shut down before the new version is deployed,
instead instances of your microservice are slowly ramped down as new instances running new versions of your software are ramped
up. It’s worth noting though that if the only thing you are looking for is something to help with zero-downtime deployments,
then implementing Kubernetes is likely huge overkill. Something simple like a blue-green deployment mechanism (which we’ll
explore more in “Separating Deployment From Release”) can work just as effectively.

#### Desired State Management
Desired state management is the ability for you to specify the infrastructure requirements you have for your application,
and for those requirements to be maintained without manual intervention. If the running system changes in such a way that
your desired state is no longer maintained, the underlying platform takes the required steps to bring the system back 
into desired state.

As a simple example of how desired state management might work, you could specify the number of instances your microservice
requires, and perhaps also specifying how much memory and CPU those instances need. Some underlying platform takes this 
configuration and applies it, bringing the system into the desired state. It’s up to the platform to, amongst other things,
identify which machines have spare resources that can be allocated to run the requested number of instances.

It also means that in the event of a problem occurring, like an instance dying, the underlying hardware failing, or a data
centre shutting down, the platform can handle these issues for you without human intervention being required.

Unlike Kubernetes, which is focused on deploying and managing container-based workloads, Nomad has a very flexible model
around running other sorts of application workloads as well like Java applications, VMs, Hadoop jobs and more. It may be
worth a look if you want a platform for managing mixed workloads but that still makes use of concepts like desired state management.

##### Prerequisites

-  fully automated deployment for microservice instances
- how long it takes your instances to launch

Use kubenates when there are lots of things to manage.

##### And GitOps
With GitOps, your desired state for your infrastructure is defined in code, and stored in source control. When changes are
made to this desired state, some tooling ensures that this updated desired state is applied to the running system. The idea
is giving developers a simplified workflow for working with their applications.


If you’ve used infrastructure configuration tools like Chef or Puppet, this model is familiar for managing infrastructure.
When using Chef Server or Puppet Master, you had a centralized system capable of pushing out changes dynamically when they
were made. The shift with GitOps is that this tooling is making use of capabilities inside Kubernetes to help manage applications,
rather than just infrastructure.

It’s worth noting of course that while tools can make it easier for you to change the way you work, they can’t force you
into adopting new working approaches. Put differently, just because you have Flux (or another GitOps tool), it doesn’t mean
you’re embracing the ideas of desired state management or infrastructure as code.

If you’re in the world of Kubernetes, adopting a tool like Flux and the workflows they promote may well speed up the introduction
of concepts like desired state management and infrastructure as code. Just make sure you don’t lose sight of the goals of
the underlying concepts and get blinded by all the new technology in this space!

### Deployment Options

#### Physical Machines
you may find yourself deploying microservices directly on to physical machines
This has become less and less common for a few reasons,

- If I have a single instance of a microservice running on a physical machine, and I only use half the CPU, memory, or 
IO provided by the hardware, then the remaining resources are wasted.This problem has lead to the virtualization of most 
computing infrastructure, allowing you to co-exist multiple virtual machines on the same physical machine. It gives you 
much higher utilization of your infrastructure, which has some obvious benefits in terms of cost effectiveness.

- The problem is that if you are only working at the level of a single physical machine, implementing concepts like desired
  state management, zero-downtime deployment etc require us to work at a higher-level of abstraction, using some sort of
  management layer on top.
  
#### Virtual Machines
Virtualization has transformed data centres, by allowing us to chunk up existing physical machines into smaller, virtual
machines. Traditional virtualization like VMWare or that used by the main cloud providers managed virtual machine
infrastructure (such as AWS’s EC2 service) has yielded huge benefits in increasing the utilization of computing infrastructure,
whilst at the same time reducing the overhead of host management.

Each virtual machine contains a full operating system and set of resources which can be used by the software running inside the VM.
This ensures that have a very good degree of isolation between instances when each instance is deployed on to a separate VM.
Each microservice instance can fully configure the operating system in the VM to their own local needs. We still have the issue
though that that if the underlying hardware running these virtual machines fails, then we can lose multiple microservice instances.

##### Cost Of Virtualization
Type 2 virtualization is the sort implemented by AWS, VMWare, VSphere, Xen, and KVM. (Type 1 virtualization refers to technology
where the VMs run directly on hardware, not on top of another operating system.) On our physical infrastructure we have 
a host operating system. On this OS we run something called a hypervisor, which has two key jobs. First, it maps resources
like CPU and memory from the virtual host to the physical host. Second, it acts as a control layer, allowing us to manipulate
the virtual machines themselves.

Inside the VMs, we get what looks like completely different hosts. They can run their own operating systems, with their own kernels. 

The problem with type 2 virtualization is that the hypervisor here needs to set aside resources to do its job. This takes
away CPU, I/O, and memory that could be used elsewhere. The more hosts the hypervisor manages, the more resources it needs.
At a certain point, this overhead becomes a constraint in slicing up your physical infrastructure any further. In practice,
this means that there are often diminishing returns in slicing up a physical box into smaller and smaller parts, as proportionally
more and more resources go into the overhead of the hypervisor.

##### Good for Microservices?
Yes as it provides isolation. Managed VM (AWS,Azure) give an api to do state managemen and zer downtime so can we used with microservice.

#### Containers

##### Isolated, differently
On Linux, processes are run by a given user and have certain capabilities based on how the permissions are set. Processes
can spawn other processes. For example, if I launch a process in a terminal, that process is generally considered a child
of the terminal process. The Linux kernel’s job is maintaining this tree of processes, ensuring that only permitted users
can access the processes. Additionally, the Linux kernel is capable of assigning resources to these different processes—this
is all part and parcel of building a viable multiuser operating system, where you don’t want the activities of one user 
to kill the rest of the system.


Containers running on the same machine make use of the same underlying kernel (although there are exceptions to this rule
that we’ll explore shortly). Rather than managing processes directly, you can think of a container as an abstraction over
a subtree of the overall system process tree, with the kernel doing all the hard work.

If we look at the stack diagram for a host running a container, we see a few differences when comparing it
with type 2 virtualization. First, we don’t need a hypervisor. Second, the container doesn’t seem to have a kernel—that’s
because it makes use of the kernel of the underlying machine. 

With containers, we don’t just benefit from the resources saved by not needing a hypervisor; we also gain in terms of feedback.
Linux containers are much faster to provision than full-fat virtual machines. It isn’t uncommon for a VM to take many minutes
to start—but with Linux containers, startup can take just a few seconds.

You also have finer-grained control over the containers themselves in terms of assigning resources to them, which makes 
it much easier to tweak the settings to get the most out of the underlying hardware.

Due to the more lightweight nature of containers, we can have many more of them running on the same hardware than would 
be possible with VMs. By deploying one service per container, as in Figure 8-16, we get a degree of isolation from other
containers (although this isn’t perfect) and can do so much more cost-effectively than would be possible if we wanted to
run each service in its own VM.

Containers can be used well with full-fat virtualization too; in fact, this is common. I’ve seen more than one project 
provision a large AWS EC2 instance and run multiple containers on it to get the best of both worlds: an on-demand ephemeral
compute platform in the form of EC2, coupled with highly flexible and fast containers running on top of it.

##### Not perfect
Linux containers aren’t without some problems, however. Imagine I have lots of microservices running in their own containers
on a host. How does the outside world see them? With earlier technology like LXC, this was something you had to handle 
yourself—this is one area where Docker’s take on containers has helped hugely.

in general you should view containers as a great way of isolating execution of trusted software. If you are running code
written by others and are concerned about a malicious party trying to bypass container-level isolation, then you’ll want
to do some deeper examination yourself regarding the current state of the art for handling such situations

##### Windows containers
One of the initial stumbling blocks in the adoption of Windows containers has been the size of the Windows operating system
itself. Remember that you need to run an operating system inside each container, so when downloading a container image, 
you’re also downloading an operating system. Windows, though, is big—so big that it made containers very heavy, not just
in terms of the size of the images but also in terms of the resources required to run them.

While Microsoft has continued to try and reduce the size of Nano Server, this size disparity still exists. In practice, 
though, due to the way that common layers across container images can be cached, this may not be a massive issue.

Having flexibility about running images in different types of isolation can have its benefits. In some situations, your 
threat model may dictate that you want stronger isolation between your running processes than simple process-level isolation.
For example, you might be running “untrusted” third-party code alongside your own processes. In such a situation, being 
able to run those container workloads as Hyper-V containers is very useful. Note, of course, that Hyper-V isolation is 
likely to have an impact in terms of spin-up time and a runtime cost closer to that of normal virtualization.

##### Docker
The Docker image abstraction is a useful one for us, as the details of how our microservice is implemented are hidden.
Docker can also alleviate some of the downsides of running lots of services locally for dev and test purposes.


#### Application Containers
This setup can also yield benefits in terms of reducing overhead of language runtimes. Consider running five Java services
in a single Java servlet container. I have the overhead of only a single JVM. Compare this with running five independent
JVMs on the same host when using containers.The idea is that the application container your services live in gives you
benefits in terms of improved manageability, such as clustering support to handle grouping multiple instances together, 
monitoring tools, and the like.

This setup can also yield benefits in terms of reducing overhead of language runtimes. Consider running five Java services
in a single Java servlet container. I have the overhead of only a single JVM. Compare this with running five independent
JVMs on the same host when using containers.First among the downsides is that they inevitably constrain technology choice.
You have to buy into a technology stack. 

Many of them tout the ability to manage clusters to support shared in-memory session state, something we absolutely want
to avoid in any case due to the challenges this creates when scaling our services. And the monitoring capabilities they 
provide won’t be sufficient when we consider the sorts of joined-up monitoring we want to do in a microservices world

Analyzing resource use and threads is also much more complex, as you have multiple applications sharing the same process.
And remember, even if you do get value from technology-specific containers, they aren’t free.

Fundamentally, the lack of isolation this model provides is one of the main reasons why this model is increasingly rare 
for people adopting microservice architectures.

#### Platform as a Service (PaaS)
When using Platform as a Service (PaaS), you are working at a higher-level abstraction than a single host. Some of these 
platforms rely on taking a technology-specific artifact, such as a Java WAR file or Ruby gem

When PaaS solutions work well, they work very well indeed. However, when they don’t quite work for you, you often don’t 
have much control in terms of getting under the hood to fix things.

The more nonstandard your application, the more likely it is that it might not play nicely with a PaaS.

I think the growth of serverless products offered primarily by the public cloud providers has started to fill this need.
Rather than offering black-box platforms for hosting an application, they instead provide turnkey managed solutions for 
things like message brokers, databases, storage, and such that allow us to mix and match the parts we like to build what
we need.

#### Function as a Service (FaaS)
The detail of managing and configuring machines is taken away from you. In the words of Ken Fromm 
(who as far as I can tell coined the term serverless):
Function as a Service, or FaaS, has become such a major part of serverless that for many the two terms are interchangeable.
This is unfortunate, as it overlooks the importance of other serverless products like databases, queues, storage solutions,
and the like.

When your function triggers, it runs, and when it finishes, it shuts down. The underlying platform handles spinning these
functions up or down on demand and will handle concurrent executions of your functions so that you can have multiple copies
running at once where appropriate.

Code that isn’t running isn’t costing you money—you pay only for what you use.
The underlying platform handles spinning the functions up and down for you, giving you some degree of implicit high 
availability and robustness without you having to do any work.
reduce the amount of operational overhead you need to worry about.

##### Limitations
Under the hood, all the FaaS implementations I’m aware of make use of some sort of container technology.
This means, though, that you lack a degree of control over what exactly can be run; as a result you need the FaaS provider
to support your language of choice.

Across Google Cloud, Azure, and AWS, you can only control the memory given to each function. This in turn seems to imply
that a certain amount of CPU and I/O is given to your function runtime, but you can’t control those aspects directly.

Another limitation to be aware of is that function invocations can provide limits in terms of how long they can run for. 

Finally, most function invocations are considered to be stateless. Conceptually, this means that a function cannot access
state left by a previous function invocation unless that state is stored elsewhere (for example, in a database).

##### Challenges
- spin-up time. conceptually - functions are not running at all unless they are needed.

Functions are launched when triggered. All the platforms I’ve used have a hard limit on the maximum number of concurrent
function invocations, which is something you might have to take careful note of.

If one part of your system can dynamically scale but the other parts of your system don’t, then you might find that this
mismatch can cause significant headaches.

##### Mapping to microservices

**_Function per microservice_** :
When invoked, the FaaS platform will trigger a single entry point in your deployed function. This means that if you’re 
going to have a single function deployment for your entire service, you’ll need to have some way of dispatching from that
entry point to the different pieces of functionality in your microservice. If you were implementing the Expenses service
as a REST-based microservice, you might have various resources exposed, like /receipt, /claim, or /report. With this model,
a request for any of these resources would come in through this same entry point, so you’d need to direct the inbound call
to the appropriate piece of functionality based on the inbound request path.

**_Function per aggregate_**:
This ensures that all the logic for a single aggregate is self-contained inside the function, making it easier to ensure
a consistent implementation of the life-cycle management of the aggregate.

I would strongly urge you to maintain a coarser-grained external interface. To upstream consumers, they are still talking
to the Expenses service—they are unaware that requests get mapped to smaller-scoped aggregates. This ensures that should
you change your mind and want to recombine things or even restructure the aggregate model, you won’t impact upstream consumers.

#### Which Deployment Option Is Right for You?

If it ain’t broke, don’t fix it.4

Give up as much control as you feel happy with, and then give away just a little bit more. If you can offload all your work
to a good PaaS like Heroku (or a FaaS platform), then do it and be happy. Do you really need to tinker with every last setting?

Containerizing your microservices it is not pain-free, but is a really good compromise around cost of isolation and has 
some fantastic benefits for local development, while still giving you a degree of control over what happens. 
Expect Kubernetes in your future.

Well, before I go any further, I really hope that it goes without saying that if what you are currently doing works for 
you, then keep doing it! Don’t let fashion dictate your technical decisions.

one of the most important aspects we focused on was that of ensuring isolation of our microservices. 

#### Kubernetes and Container Orchestration
Containers are created by isolating a set of resources on an underlying machine. Tools like Docker allow us to define what
a container should look like and create an instance of that container on a machine. But most solutions require that our 
software be defined on multiple machines, perhaps to handle sufficient load, or to ensure that the system has redundancy
in place to tolerate the failure of a single node. Container orchestration platforms handle how and where container 
workloads are run. The term “scheduling” starts to make more sense in this context. The operator says, “I want this thing
to run,” and the orchestrator works out how to schedule that job—finding available resources, reallocating them if necessary,
and handling the details for the operator.

The various container orchestration platforms also handle desired state management for us, ensuring that the expected 
state of a set of containers (microservice instances, in our case) is maintained. They also allow us to specify how we want
these workloads to be distributed, allowing us to optimize for resource utilization, latency between processes, or robustness
reasons.

#### Progressive Delivery
These organizations make use of techniques like feature toggles, canary releases, parallel runs, and more, which we’ll 
detail in this section. This shift in how we think about releasing functionality falls under the banner of what is called
progressive delivery. Functionality is released to users in a controlled manner; instead of a big-bang deployment,
we can be smart about who sees what functionality—for example, by rolling out a new version of our software to a subset 
of our users.

##### Separating Deployment from Release

_Deployment is what happens when you install some version of your software into a particular environment (the production 
environment is often implied). Release is when you make a system or some part of it (for example, a feature) available to users._


#### Feature Toggles
With feature toggles (otherwise known as feature flags), we hide deployed functionality behind a toggle that can be used
to switch functionality off or on. This is most commonly used as part of trunk-based development, where functionality that
isn’t yet finished can be checked in and deployed but still hidden from end users, but it has lots of applications outside
of this. This could be useful to turn on a feature at a specified time, or turn off a feature that is causing problems.

Fully managed solutions exist for managing feature toggles, including LaunchDarkly and Split.

#### Canary Release
For a microservice architecture, a toggle could be configured at an individual microservice level, turning functionality
on (or off) for requests to that functionality from the outside world or other microservices. Another technique is to have
two different versions of a microservice running side by side, and use the toggle to route to either the old or the new 
version. Here, the canary implementation has to be somewhere in the routing/networking path, rather than being in one microservice.

Tools like Spinnaker for example have the ability to automatically ramp up calls based on metrics, such as increasing the
percentage of calls to a new microservice version if the error rates are at an acceptable level.

#### Parallel Run
With a parallel run you do exactly that—you run two different implementations of the same functionality side by side, and
send a request to the functionality to both implementations. With a microservice architecture, the most obvious approach
might be to dispatch a service call to two different versions of the same service and compare the results. An alternative
is to coexist both implementations of the functionality inside the same service, which can often make comparison easier.

GitHub makes use of this pattern when reworking core parts of its codebase, and have released an open source tool Scientist
to help with this process. Here, the parallel run is done within a single process, with Scientist helping to compare the
invocations.










