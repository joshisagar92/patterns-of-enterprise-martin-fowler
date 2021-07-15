### A Brief Introduction to Continuous Integration
With CI, the core goal is to keep everyone in sync with each other on a frequent basis, which we achieve by making sure 
that newly checked-in code properly integrates with existing code. To do this, a CI server detects that the code has been
committed, checks it out, and carries out some verification like making sure the code compiles and that tests pass.

CI has a number of benefits. We get fast feedback as to the quality of our code, through the use of static analysis and testing.
It also allows us to automate the creation of our binary artifacts. All the code required to build the artifact is itself
version controlled, so we can re-create the artifact if needed.

#### Are you really Doing the CI?
So how do you know if you’re actually practising CI? I really like Jez Humble’s three questions he asks people to test if
they really understand what CI is about - it might be interesting to ask yourself these same questions:

**Do you check in to mainline once per day?**
You need to make sure your code integrates. If you don’t check your code together with everyone else’s changes frequently,
you end up making future integration harder. Even if you are using short-lived branches to manage changes, integrate as 
frequently as you can into a single mainline branch, at least once a day.

**Do you have a suite of tests to validate your changes?**
Without tests, we just know that syntactically our integration has worked, but we don’t know if we have broken the behavior
of the system. CI without some verification that our code behaves as expected isn’t CI.

**When the build is broken, is it the #1 priority of the team to fix it?**
A passing green build means our changes have safely been integrated. A red build means the last change possibly did not 
integrate. You need to stop all further check-ins that aren’t involved in fixing the builds to get it passing again. If 
you let more changes pile up, the time it takes to fix the build will increase drastically.

#### Branching Models
Branching in source code allows for development to be done in isolation, without disrupting the work being done by others.
On the surface of it, creating a source code branch for each feature being worked on - otherwise known as feature branching
- seems like a useful concept.

The problem is that when you work on a feature branch, you aren’t regularly integrating your changes with everyone else.

The alternative approach is to have everyone check in to the same “trunk” of source code. To keep changes from impacting
other people, techniques like feature flags are used to “hide” incomplete work. This technique of everyone working off the
same trunk is called Trunk-Based Development.

Integrate early, and integrate often. Avoid the use of long-lived branches for feature development, and consider Trunk-Based
Development instead. If you really have to use branches, keep them short!

**We found that having branches or forks with very short lifetimes (less than a day) before being merged into trunk, and 
less than three active branches in total, are important aspects of continuous delivery, and all contribute to higher 
performance. So does merging code into trunk or master on a daily basis.**

### Build Pipelines and Continuous Delivery
This build pipeline concept gives us a nice way of tracking the progress of our software as it clears each stage, helping
give us insight into the quality of our software. We create deployable artifact, the thing that will ultimately be deployed
into production, and use this artifact throughout the pipeline.

**CONTINUOUS DELIVERY VS CONTINUOUS DEPLOYMENT**
Continuous Delivery is the concept whereby each checkin is treated as a release candidate, and where we can assess the 
quality of each release candidate to decide if it’s ready to be deployed. With Continuous Deployment on the other hand, 
all checkins have to be validated using automated mechanisms (for example tests), and any software which passes these verification
checks is deployed automatically, without human intervention. Continuous Deployment can therefore be considered an 
extention of Continuous Delivery. Without Continuous Delivery, you can’t do Continuous Deployment.

#### Tooling
Ideally you want a tool that embraces CD as a first-class concept. I have seen many people try to hack and extend CI tools
to make them do CD, often resulting in complex systems that are nowhere near as easy to use as tools that build in CD from
the beginning. Tools that fully support CD allow you to define and visualize these pipelines, modeling the entire path to
production for your software. As a version of our code moves through the pipeline, if it passes one of these automated 
verification stages it moves to the next stage.

#### Tradeoffs and Environments
Structuring a pipeline, and therefore working out what environments you’ll need, is in and of itself a balancing act. 
Early on in the pipeline, we’re looking for fast feedback as to the production readiness of our software. We want to let
developers know as soon as possible if there is a problem - the sooner you get feedback about a problem occurring, the 
quicker it is to fix it. As our software gets closer to production, we want more certainty that the software will work, 
and we’ll therefore be deploying into increasingly production-like environments

You get fastest feedback on your development laptop - but that is far from production-like. You could roll out every commit
to environment that is a faithful reproduction of your actual production environment, but that will likely take longer and
cost more. So finding the balance is key, and continuing to review the tradeoff between fast feedback and the need for
production-like environments can be an incredibly important ongoing activity.

#### Artifact Creation
Build your deployable artifact once and once only, and ideally do this pretty early in the pipeline.
Building the same thing over and over again is a waste of time, bad for the planet, and can theoretically introduce problems
if the build configuration isn’t exactly the same. On some programming languages a different build flag can make the 
software behave quite differently. Secondly, the artifact you verify should be the artifact you deploy! If you build a 
microservice, test it, say “yes it’s working”, and then build it again for deployment into production, how do you know that
the software you validated is the same software you deployed?

I would typically do this after compiling the code (if required) and running my fast tests. Once created, this artifact 
is stored in an appropriate repository - this could be something like Artifactory or Nexus, or perhaps a container registry.
Your choice of deployment artifact likely dictates the nature of the artifact store. This same artifact can then be used
for all stages in the pipeline that follow, up to and including deployment into production.

If the same artifact is going to be used across multiple environments, any aspects of configuration which varies from 
environment to environment need to be kept outside of the artifact itself. As a simple example, I might want to configure
application logs so that everything at DEBUG level and above is logged when running the Slow Tests stage so I have more 
information to diagnose why a test fails. I might decide though to change this to INFO to reduce the log volume for the 
Performance Tests and Production deployment.

### Mapping Source Code and Builds to Microservices
#### One Giant Repo, One Giant Build
If we start with the simplest option, we could lump everything in together. We have a single, giant repository storing all
our code, and have one single build, as we see in Figure 7-5. Any check-in to this source code repository will cause our
build to trigger, where we will run all the verification steps associated with all our microservices, and produce multiple
artifacts, all tied back to the same build.

- fewer repositories to worry about, and a conceptually simpler build. 
- From a developer point of view, things are pretty straightforward too. I just check code in. If I have to work on multiple
  services at once, I just have to worry about one commit.

This model can work perfectly well if you buy into the idea of lock-step releases, where you don’t mind deploying multiple
services at once. In general, this is absolutely a pattern to avoid, but very early on in a project, especially if only 
one team is working on everything, this might make sense for short periods of time.

If I make a one-line change to a single service—for example, changing behavior in the User service all 
the other services get verified and built. 

Furthermore, if my one-line change to the user service breaks the build, no other changes can be made to the other services
until that break is fixed. 

#### Pattern: One Repository Per Microservice
The straightforward nature of this pattern does create some challenges though. Specifically developers may find themselves
working with multiple repositories at a time, which is especially painful if they are trying to make changes across multiple 
repositories at once. Additionally, changes cannot be made in an atomic fashion across separate repositories, at least not with Git.

##### Reusing Code Across Repositories
We can see an example of that, where the Invoice and Payroll services both make use of the Connection library.

If you wanted to roll out a change to the Connection library, you’d have to make the changes in the matching source code
repository, and wait for its build to complete giving you a new versioned artifact. To actually deploy new versions of the
Invoice or Payroll services using this new version of the library, you’d need to change the version of the Connection
library they use.

The important thing to remember of course is that if you want to roll out the new version of the Connection library, then
we also need to deploy both the newly built Invoice and Payroll services. Remember, all the caveats we explored in “DRY 
and the Perils of Code Reuse in a Microservice World” regarding reuse and microservices still apply - if you choose to reuse
code via libraries, then you must be OK with the fact that these changes cannot be rolled out in an atomic fashion, 
otherwise we undermine our goal of independent deployability. You also have to be aware that it can be more challenging 
to know if some microservices are using a specific version of a library, which may be problematic if you’re trying to 
deprecate the use of an old version of the library.

##### Working Across Multiple Repositories
So, aside from reusing code via libraries, how else can we make a change across more than one repository? Let’s look at 
another example.I want to change the API exposed by the Inventory service, and I also need to update the
Shipping service so it can make use of the new change. If the code for both Inventory and Shipping was in the same repository,
I could commit the code once. Now, I’ll have to break the changes into two - one commit for Inventory, and another for Shipping.

Having these changes split could cause problems if one commit fails but the other works - I may need to make two changes
to rollback the change for example, and that could be complicated if other people have checked in in the meantime. The 
reality is that in this specific situation, I’d likely want to stage the commits somewhat in any case. I’d want to make 
sure the commit to change the Inventory service worked before I change any client code in the Shipping service

If you are continually making changes across multiple microservices, it points to the fact that your service boundaries 
might not be in the right place, and could imply too much coupling between your services.


#### Pattern: Monorepo
With a monorepo approach, code for multiple microservices (or other types of projects) are stored in the same source code
repository. I have seen situations where a monorepo is used just by one team to manage source control for all their services,
although the concept has been popularized by some very large tech companies where multiple teams and hundreds if not
thousands of developers can all work on the same source code repository.

##### Mapping To Build
A simple starting point is to map folders inside the monorepo to a build, as shown in Figure 7-10. A change made to the 
user-service folder would trigger the User service build for example. If you checked in code that changed both files in 
the user-service folder and the catalog-service folder, then both the User build and the Catalog build would get triggered.

##### Defining Ownership
With Strong Ownership, some code is specifically owned by a group of people. If someone from outside that group wants to
make a change, they have to ask the owners to make that change for them. Weak Ownership still has the concept of defined
owners, but people outside of this ownership group are allowed to make changes, although any of these changes must be reviewed
and accepted by one of the ownership group. This would cover the use of pull requests being sent to the core ownership team
for review, before the pull request is merged. With Collective Ownership, any developer can change any piece of code.

With a small number of developers (20 or less, as a general guide), you can afford to practice Collective Ownership - where
any developer can change any other microservice. As you have more people though, you’re more likely to want to move towards
either Strong or Weak ownership model to create more defined boundaries of responsibility. This can cause a challenge for
teams using monorepos if their source control tool doesn’t support finer-grained ownership controls.

Some source code tools allow you to specify ownership of specific directories or even specific file paths inside a single
repository. Google initially implemented this system on top of Perforce for their own monorepo, before developing their 
own source control system, and it’s also something that GitHub has supported since 20167. With GitHub, you create a CODEOWNERS
file, which lets you map owners to directory or file paths.

GitHub’s own code ownership concept ensures that code owners for source files are requested for review when any pull request
is raised for the relevant files. This could be a problem with larger pull requests as you could end up needing sign-off
from multiple reviewers, but there are lots of good reasons to aim for smaller pull requests in any case.

