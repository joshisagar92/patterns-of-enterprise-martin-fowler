#### Looking for the Ideal Technology

##### Make Backwards Compatibility Easy
When making changes to our microservices, we need to make sure we don’t break compatibility with any consuming microservices. 

##### Make Your Interface Explicit
I strongly encourage the use of an explicit schema, as well as enough supporting documentation to be clear about what 
functionality a consumer can expect a microservice to provide.

##### Keep Your APIs Technology-Agnostic
This means avoiding integration technology that dictates what technology stacks we can use to implement our microservices.

##### Make Your Service Simple for Consumers

##### Hide Internal Implementation Detail
We don’t want our consumers to be bound to our internal implementation. This leads to increased coupling. This means that
if we want to change something inside our microservice, we can break our consumers by requiring them to also change.

#### Technology Choices
##### Remote Procedure Calls (RPC)
Frameworks that allow for local method calls to be invoked on a remote process. Common options include SOAP and gRPC.
Most of the technology in this space requires an explicit schema, such as SOAP or gRPC. In the context of RPC,
the schema is often referred to as an Interface Definition Language, or IDL, with SOAP refering to its schema format as
Web Service Definition Language (WSDL). The use of a separate schema makes it easier to generate client and server stubs
for different technology stacks

Typically, using an RPC technology means you are buying into a serialization protocol. The RPC framework defines how data
is serialized and deserialized.

RPC frameworks that have an explicit schema make it very easy to generate client code. This can avoid the need for client
libraries, as any client can just generate their own code against this service specification.

AVRO RPC is an interesting outlier here, as it has the option to send the full schema along with the payload, allowing
for clients to dynamically interpret the schema.

The ease of generation of client-side code is one of the main selling points of RPC

###### Challenges

**_Technology Coupling_** 
ome RPC mechanisms, like Java RMI, are heavily tied to a specific platform, which can limit which technology can be used
in the client and server.

**_Local Calls Are Not Like Remote Calls_** 
Just taking a local API and trying to make it a service boundary without any more thought is likely to get you in trouble.
In some of the worst examples, developers may be using remote calls without knowing it, if the abstraction is overly opaque.

**_Brittleness_**
createCustomer takes the first name, surname, and email address. What happens if we decide to allow the Customer object
to also be created with just an email address? We could add a new method at this point pretty easily
The problem is that now we need to regenerate the client stubs too.

what if it turns out that although we expose the age field in our Customer objects, none of our consumers ever use it?
We decide we want to remove this field. But if the server implementation removes age from its definition of this type,
and we don’t do the same to all the consumers, then even though they never used the field, the code associated with 
deserializing the Customer object on the consumer side will break. To roll out this change, I would have to deploy both 
a new server and clients at the same time.

if I want to restructure the Customer object even if I didn’t remove fields—for example, if I wanted to encapsulate 
firstName and surname into a new naming type to make it easier to manage. I could, of course, fix this by passing 
around dictionary types as the parameters of my calls, but at that point, I lose many of the benefits of the generated
stubs because I’ll still have to manually match and extract the fields I want.

###### Where to Use It
Java RMI for example has a number of issues regarding brittleness and limited technology choices, and SOAP is pretty 
heavyweight from a developer perspective, especially when compared with more modern choices.
ust be aware of some of the potential pitfalls associated with RPC if you’re going to pick this model. Don’t abstract 
your remote calls to the point where the network is completely hidden, and ensure that you can evolve the server interface
without having to insist on lock-step upgrades for clients.

If I was looking at options in this space, gRPC would be top of my list. Built to take advantage of HTTP/2
gRPC fits a synchronous request-response model well, but can also work in conjunction with reactive extensions. It’s high
on my list whenever I’m in situations where I have a good deal of control over both the client and server ends of the 
spectrum.
  
 



##### REST
An architectural style where you expose resources (Customer, Order etc.) that can be accessed using a common set of verbs
(GET, POST). There is a bit more to REST than that, but we’ll get to that shortly.

REST itself doesn’t really talk about underlying protocols, although it is most commonly used over HTTP. I have seen 
of REST using very different protocols before, although this can require a lot of work. Some of the features that HTTP
gives us as part of the specification, such as verbs, make implementing REST over HTTP easier, whereas with other protocols
you’ll have to handle these features yourself.

###### REST and HTTP
HTTP itself defines some useful capabilities that play very well with the REST style. For example, the HTTP verbs
(e.g., GET, POST, and PUT) already have well-understood meanings in the HTTP specification as to how they should work
with resources.

HTTP also brings a large ecosystem of supporting tools and technology. We get to use HTTP caching proxies like Varnish 
and load balancers like mod_proxy, and many monitoring tools already have lots of support for HTTP out of the box.
We also get to use all the available security controls with HTTP to secure our communications. From basic auth to client certs,

Note that HTTP can be used to implement RPC too. SOAP, for example, gets routed over HTTP, but unfortunately uses very 
little of the specification. Verbs are ignored, as are simple things like HTTP error codes. gRPC on the other hand has 
been designed to take advantage of the capabilities of HTTP/2 such as the ability to send multiple request-response 
streams over a single connection. But of course, when using gRPC you’re not doing REST just because you’re using HTTP!

###### Hypermedia As the Engine of Application State
Another principle introduced in REST that can help us avoid the coupling between client and server is the concept of 
hypermedia as the engine of application state

###### Challenges

In terms of ease of consumption, historically you wouldn’t be able to generate client-side code for your REST over HTTP 
application protocol like you can with RPC implementations. This has often lead to people creating REST APIs providing 
client libraries for consumers to make use of.
The OpenAPI specification that grew out of the Swagger project, now provides you with the ability to define enough info
rmation on a REST endpoint to allow for the generation of client-side code in a variety of languages.

Performance may also be an issue. REST over HTTP payloads can actually be more compact than SOAP because it supports
alternative formats like JSON or even binary, but it will still be nowhere near as lean a binary protocol as Thrift might
be. The overhead of HTTP for each request may also be a concern for low-latency requirements.  

With respect to HATEOS specifically, you can encounter additional performance issues. 

###### Where to Use It
It’s a widely understood style of interface, that most people are familiar with, and guarantees interoperability from a
huge variety of technologies.

###### Richardson Maturity Model
####### Level 0
The starting point for the model is using HTTP as a transport system for remote interactions, but without using any of the
mechanisms of the web. Essentially what you are doing here is using HTTP as a tunneling mechanism for your own remote 
interaction mechanism, usually based on Remote Procedure Invocation.

####### Level 1 - Resources
The first step towards the Glory of Rest in the RMM is to introduce resources. So now rather than making all our requests
to a singular service endpoint, we now start talking to individual resources.

####### Level 2 - HTTP Verbs
I've used HTTP POST verbs for all my interactions here in level 0 and 1, but some people use GETs instead or in addition.
At these levels it doesn't make much difference, they are both being used as tunneling mechanisms allowing you to tunnel
your interactions through HTTP. Level 2 moves away from this, using the HTTP verbs as closely as possible to how they are
used in HTTP itself

The important part of this response is the use of an HTTP response code to indicate something has gone wrong. In this case
a 409 seems a good choice to indicate that someone else has already updated the resource in an incompatible way.
Rather than using a return code of 200 but including an error response, at level 2 we explicitly use some kind of error 
response like this.

The key elements that are supported by the existence of the web are the strong separation between safe (eg GET) and non-safe
operations, together with using status codes to help communicate the kinds of errors you run into.

####### Level 3 - Hypermedia Controls

##### GraphQL
A relatively new protocol that allows for consumers to define custom queries that can fetch information from multiple 
downstream microservices, filtering the results to return only what is needed.

Namely, it makes it possible for a client-side device to define queries that can avoid the need to make multiple requests
to retrieve the same information. This can offer significant improvements in terms of the performance of constrained
client-side devices, and also avoid the need to implement bespoke server-side aggregation.

GraphQL allows for the mobile device to issue a single query that can pull back all the required information. For this to
work, you need a microservice which exposes a GraphQL endpoint to the client device. This GraphQL endpoint is the entry 
for all client queries, and exposes a schema for the client devices to use. This schema exposes the types available to 
the client, and a nice graphical query builder is also available to make creating these queries easier. By reducing the 
amount of calls and amount of data retrieved by the client device, you can deal neatly with some of the challenges that 
occur when building user interfaces with microservice architectures.

###### Challenges
Early on, one challenge was lack of language support for the GraphQL specification, with JavaScript being your only choice initially.
As the client device can issue dynamically changing queries, this can potentially cause an issue with server-side load.
I’ve heard of teams who have had issues with GraphQL queries causing significant load on the server-side as a result of
this. To compare GraphQL with something like SQL, we have the same issue there. An expensive SQL statement can cause 
significant problems for a database.

The difference is that at least with SQL we have tools like query planners for our databases, which can help us diagnose
problematic queries, whereas a similar problem with GraphQL can be harder to track down. Server-side throttling of 
requests is one potential solution, but as the execution of the call may be spread across multiple microservices, this is
far from straightforward.

Compared with normal REST-based HTTP APIs, caching is also more complex.

Another issue, is that while GraphQL theoretically can handle writes, it doesn’t seem to fit as well as reads. This does
lead to situations where teams are using GraphQL for read, but REST for writes.

###### Where To Use It
If you have an external API which often requires external clients to make multiple calls to get the information they need,
then GraphQL can help make these APIs much more efficient and friendly.
Fundamentally, GraphQL is a call aggregation and filtering mechanism, so in the context of a microservice architecture it
would be used to aggregate calls over multiple downstream microservices.
 
 
##### Message Brokers
Middleware that allows for asynchronous communication either via queues or topics.
At first glance, a queue just looks like a topic with a single consumer group. A large part of the distinction between
the two is that when sending a message over a queue, there is knowledge of what the message is being sent to. With a topic,
this information is hidden from the sender of the message—they are unaware of who (if anyone) will end up receiving the
message.

Guaranteed delivery describes a commitment by the broker to ensure that the message is delivered.
Most brokers can guarantee the order in which messages will be delivered, but this isn’t universal, and even then the 
scope of this guarantee can be limited.

Some brokers provide transactions on write—Kafka as an example allows you to write to multiple topics in a single transaction. 

Some brokers can also provide read transactionality.Another, somewhat controversial feature promised by some brokers is 
that of exactly once delivery.


#### Serialization Formats

##### Textual Formats
The use of standard textual formats gives clients a lot of flexibility as to how they consume resources. REST APIs mostly
typically use a textual format for the request and response bodies, even if theoretically you can quite happily send
binary data over HTTP.
 
##### Binary Formats   
Where textual formats have benefits like making it easy for humans to read them, and provide a lot of interoperability 
with different tools and technologies, the world of binary serialization protocols is where you want to be if you start
getting worried about payload size, or the efficiencies of writing and reading the payloads 

#### Schemas
As I’ve already discussed, I am in favour of having explicit schemas for microservice endpoints. This is for two key reason
s. Firstly, it goes a long way to being an explicit representation of what a microservice endpoint exposes, and what it 
can accept. This makes life easier for both developers working on the microservice, but also their consumers. Schemas may
not replace the need for good documentation, but they certainly can help reduce the amount of documentation required.

##### Structural vs Semantic Contract Breakages
Structural breakages refer to situations where the structure of the endpoint changes in such a way that a consumer is now
incompatible - this could represent fields or methods being removed, or new required fields being added

Semantic breakages refer to situations where the structure of the microservices endpoint remains the same, but the behavior
changes in such a way as to break consumers expectations.

##### Should You Use Schemas?
Really, the question isn’t actually if you have a schema or not - it’s whether or not that schema is explicit. If you are
consuming data from a schemaless API, you still have expectations as to what data should be in there, and how that data 
should be structured. Your code that will handle the data will be written with a set of assumptions in mind as to how 
that data is structured. In such a case, I’d ague that you do have a schema, it’s just totally implicit, rather than 
explicit7. A lot of my desire for an explicit schema is driven by the fact that I think it’s important to be as explicit
as possible as to what a microservice does (or doesn’t) expose.Really, the question isn’t actually if you have a schema
or not - it’s whether or not that schema is explicit. If you are consuming data from a schemaless API, you still have 
expectations as to what data should be in there, and how that data should be structured. Your code that will handle the
data will be written with a set of assumptions in mind as to how that data is structured. In such a case, I’d ague that
you do have a schema, it’s just totally implicit, rather than explicit7. A lot of my desire for an explicit schema is
driven by the fact that I think it’s important to be as explicit as possible as to what a microservice does (or doesn’t) expose.

#### Handling Change Between Microservices

##### Expansion Changes
Probably the easiest place to start is by only adding new things to a microservice contract, and don’t remove anything else.
  
##### Tolerant Reader
Specifically, we want to avoid client code binding too tightly to the interface of a microservice. 
The data our email service wants is still there, and still with the same name, but if our code makes very explicit assumptions
as to where the firstname and lastname fields will be stored, then it could break again. In this instance,
we could instead use XPath to pull out the fields we care about, allowing us to be ambivalent about where the fields are,
as long as we can find them.

The example of a client trying to be as flexible as possible in consuming a service demonstrates Postel’s Law 
(otherwise known as the robustness principle), which states: “Be conservative in what you do, be liberal in what you 
accept from others.”

https://martinfowler.com/bliki/TolerantReader.html

https://blog.iancartwright.com/2006/11/schema-validation-offers-false-sense-of.html 

https://blog.iancartwright.com/2006/11/dangers-of-serialization.html

https://martinfowler.com/articles/consumerDrivenContracts.html

##### Right Technology
Each entry in a protocol buffer has to define a field number, which client code expects to find. If new fields are added,
the client doesn’t care. AVRO allows for the schema to be sent along with the payload, allowing clients to potentially 
interpret a payload much like a dynamic type.

##### Explicit Interface
Having an explicit schema for RPC is long established, and is in fact a requirement for many RPC implementations. REST 
on the other hand has typically viewed the concept of a schema as optional, to the point where I find explicit schemas 
for REST endpoints to be vanishingly rare. This is changing, with things like the aforementioned OpenAPI specification 
gaining traction, and the JSON Schema specification also gaining in maturity.

For event deriven this is dificult but there are specification - AsyncAPI & CloudEvents
##### Catch Accidental Breaking Changes Early

#### Managing Breaking Changes

##### Lock-Step Deployment
If we want to be able to deploy a new version of our microservice with a breaking change to it’s interface, but still do
this in an independent fashion, we need to give our consumers time to upgrade to the new interface.

###### Coexist Incompatible Microservice Versions
Another versioning solution often cited is to have different versions of the service live at once.
First, if I need to fix an internal bug in my service, I now have to fix and deploy two different sets of services. 
Second, it means I need smarts to handle directing consumers to the right microservice.
Customers created by either version of the service need to be stored and made visible to all services, no matter which
version was used to create the data in the first place. This can be an additional source of complexity

###### Emulate The Old Interface
One approach I have used successfully to handle this is to coexist both the old and new interfaces in the same running
service. So if we want to release a breaking change, we deploy a new version of the service that exposes both the old
and new versions of the endpoint.

Once all of the consumers are no longer using the old endpoint, you can remove it along with any associated code.

###### Which Approach Do I Prefer?
For situations where the same team manages both the microservice and all consumers, I am somewhat relaxed about a lock-step
release in limited situations. Assuming it really is a one-off situation, then doing this in a situation where the impact
is limited to a single team can be justifiable.

My general preference is where possible to use emulation of old endpoints. The challenges of implementing emulation are 
in my opinion much easier to deal with than co-existing of microservice versions.

#### DRY and the Perils of Code Reuse in a Microservice World

##### Sharing Code Via Libraries
For example, at one client we had a library of common domain objects that represented the core entities in use in our 
system. This library was used by all the services we had. But when a change was made to one of them, all services had to
be updated.


##### Client Libraries
The argument is that this makes it easy to use your service, and avoids the duplication of code required to consume the
service itself.
if the same people create both the server API and the client API, there is the danger that logic that should exist on the
server starts leaking into the client.you find yourself having to change multiple clients to roll out fixes to your server.
You also limit technology choices, especially if you mandate that the client library has to be used.

#### Service Discovery
Perhaps you want to know what is running in a given environment so you know what you should be monitoring.
Maybe it’s as simple as knowing where your Accounts microservice is so that its consumers know where find it. 
Or perhaps you just want to make it easy for developers in your organization to know what APIs are available so they don’t 
reinvent the wheel. Broadly speaking, all of these use cases fall under the banner of service discovery.

##### Domain Name System (DNS)
DNS entries for domain names have a time to live (TTL). This is how long a client can consider the entry fresh. When we 
want to change the host to which the domain name refers, we update that entry, but we have to assume that clients will be
holding on to the old IP for at least as long as the TTL states. DNS entries can get cached in multiple places 
(even the JVM will cache DNS entries unless you tell it not to), and the more places they are cached in, the more stale 
the entry can be.

DNS is well understood and widely supported. But it does have one or two downsides. I would suggest investigating whether
it is a good fit for you before picking something more complex. For a situation where you have only single nodes, having
DNS refer directly to hosts is probably fine. But for those situations where you need more than one instance of a host,
have DNS entries resolve to load balancers that can handle putting individual hosts into and out of service as appropriate.

##### Dynamic Service Registries
The downsides of DNS as a way of finding nodes in a highly dynamic environment have led to a number of alternative systems,
most of which involve the service registering itself with some central registry, which in turn offers the ability to look
up these services later on.


#### Service Meshes and API Gateways

In typical data center speak, we’d talk about “east-west” traffic as being inside a data centre, with “north-south” traffic
relating to interactions that enter or leave the data centre from the outside world.

Speaking generally, an API Gateway sits on the permitter of your system, and deals with north-south traffic. It’s primary
concerns are managing access from the outside world to your internal microservices. A service mesh on the other hand 
deals very narrowly with communication between microservices inside your perimeter - the east-west traffic

service meshes and API gateways can work as proxies between microservices.This can mean that they can be used to implement
some microservice-agnostic behavior which might otherwise have to be done in code, such as service discovery or logging.

##### API Gateways
API Gateways typically build more features on top of existing HTTP proxy products, and they largely function as reverse
proxies.
API Gateways can be used to implement mechanisms like API keys for external parties, logging, rate limiting and the like.

 
  
 



 
