#### From In-Process to Inter-Process
Namely, calls between different processes across a network (inter-process) are very different to calls within a single
process (in-process). At one level, we can ignore this distinction. It’s easy, for example, to think of one object making
a method call on another object, then just map this interaction to two microservices communicating via a network. Putting
aside the fact that microservices aren’t just objects, this thinking can get us into a lot of trouble.

##### Performance
The performance of an in-process call and an inter-process call is fundamentally different. When I make an in-process call,
the underlying compiler and runtime can carry out a whole host of optimizations to reduce the impact of the call,
including inlining the invocation so it’s as though there was never a call in the first place. No such optimizations are
possible with inter-process calls. Packets have to be sent. Expect the overhead of an inter-process call to be significant
compared to the overhead of an in-process call.

I can make 1000 calls across an API boundary in-process without concern. Do I want to make 1000 network calls between two
microservices? Perhaps not.

When I pass a parameter into a method, the data structure I pass in typically doesn’t move—what’s more likely is that I
pass around a pointer to a memory location. Passing in an object or data structure to another method doesn’t necessitate
more memory to be allocated in order to copy the data.

When making calls between microservices over a network on the other hand, the data actually has to be serialized into some
form that can be transmitted over a network. The data then needs to be sent, and deserialized at the other end.
We therefore may need to be more mindful about the size of payloads being sent between processes.

This might lead you to reduce the amount of data being sent or received (perhaps not a bad thing if we think about 
information hiding), pick more efficient serialization mechanisms, or even offload data to a file system and pass around
a reference to that file location instead.

##### Changing Interfaces
When we consider changes to an interface inside a process, the act of rolling out the change is straightforward. Both the
code implementing the interface, and the code calling the interface, are all packaged together in the same process.
In fact if I change a method signature using an IDE with refactoring capability, often the IDE itself will automatically
refactor calls to this changing method. Rolling out such a change can be done in an atomic fashion—both sides of the 
interface are packaged together in a single process.

With communication between microservices, however, the microservice exposing an interface, and the consuming microservices
using that interface, are separately deployable microservices. When making a backwards incompatible change to a microservice
interface, we either need to do a lock-step deployment with consumers, making sure they are updated to use the new
interface, or else find some way to phase the rollout of the new microservice contract.

##### Error handling
Within a process, if I call a method, the nature of the errors tends to be pretty straightforward. 
With a distributed system, the nature of errors can be different. You are vulnerable to a host of errors that are outside
of your control. Networks time out. Downstream microservices might be temporarily unavailable. Networks get disconnected,
containers get killed due to consuming too much memory, and in extreme situations, bits of your data centre can catch fire

Failure in distributed systems,

- Crash Failure : Everything was fine, till the server crashed. Reboot!
- Omission Failure : We sent something, but we didn’t get a response
- Timing Failure : Something happened too late (we didn’t get it in time), or something happened too early!
- Response Failure : We got a response, but it just seems wrong
- Arbitrary Failure : when something has gone wrong, but all participants are unable to agree if the failure has occurred

Whether or not you pick a HTTP-based protocol for communication between microservices, if you have a rich set of semantics
around the nature of the error, you’ll make it easier for clients to carry out compensating actions, which in turn should
help you build more robust systems.

#### Technology for Inter-process Communication: So Many Choices

The problem with this is that when you buy into a specific technology choice, you are often buying into a set of ideas 
(and constraints) that come along for the ride. These constraints might not be the right ones for you—and the mindset
behind the technology may not actually line up with the problem you are trying to solve.
I therefore think it is important to talk first about the style of communication you want, and only then look for the 
right technology to implement these styles.


#### Styles of Microservice Communication
- Synchronous Blocking:
A microservice makes a call to another microservice and blocks operation waiting for the response.

- Asynchronous Non-Blocking:
The microservice emitting a call is able to carry on processing whether or not the call is received.

- Request-response:
A Microservice sends a request to another microservice asking for something to be done. It expects to receive a response
to the request informing it of the result.

- Event-Driven:
Microservices emit events, which other microservices consume and react to accordingly. The microservice emitting the event
is unaware of which microservices, if any, consume the events it emits.

- Common Data:
Not often seen as a communication style, microservices collaborate via some shared data source
 
When using this model to help teams decide on the right approach, I spend a lot of time understanding the context in which
they are operating. Their needs in terms of reliable communication, acceptable latency and volume of communication are 
all going to play a part in making a technology choice.   

If I’m looking at request-response, then both synchronous and asynchronous implementations are still available to me, 
so I have a second choice to make. If picking an event-driven collaboration style though, my implementation choices will
be limited to non-blocking asynchronous choices.

#### Pattern: Synchronous Blocking

#####Advantage
Known technology
With HTTP provide good semantics

##### Disavantage
if downstream service is down/slow request will be blocked for that time.

if upstream service is down response will be lost. have to integrate retry/queue system to if downstream service is down/slow

The use of synchronous calls can therefore make a system more vulnerable to cascading issues caused by downstream outages
more readily than asynchronous calls.

##### Where to use it
For simple microservice architectures.

Where these types of calls start to be problematic is when you start having more chains of calls.
- A issue in any of the involved microservices, or in the network calls between them, could cause the whole operation to fail.
- This is quite aside from the fact that these kinds of long chains can cause significant resource contention.
 
To improve this situation, we could re-examine the interactions between the microservices in the first place by changin the 
workflow/breaking the chain.

#### Pattern: Asynchronous Non-blocking
- Communication Though Common Data:
The upstream microservice changes some common data, which one or more m -icroservices later make use of.

- Request-Response:
A microservice sends a request to another microservice asking it to do something. When the requested operation completes, successfully or not, the upstream microservice receives the response. Specifically, any instance of the upstream microservice should be able to handle the response.

- Event-Driven Interaction:
A microservice broadcasts an event, which can be thought of as a factual statement as to something that has happened. Other microservices can listen for the events they are interested in and react accordingly.
 
##### Advantage 
we avoid the concerns of temporal decoupling

##### Disadvantages
level of complexity and range of choice

##### Where to use it
- Long running processes are an obvious candidate
- you have long call chains

#### Pattern: Communication Through Common Data
This pattern is used in a situation where one microservice puts data into a defined location, and another microservice
(or potentially multiple) then make use of this data.

A file system in many cases can be enough. I’ve built many systems which just periodically scan a file system, note the
presence of a new file, and react on it accordingly. You could also use some sort of robust distributed memory store as
well of course. It’s worth noting that any downstream microservice which is going to act on this data will need it’s own
mechanism to identify that new data is available—polling is a frequent solution to this problem.

Two common examples of this pattern are the data lake and the data warehouse. In both cases, these solutions are typically
designed to help processing large volumes of data, but arguably they exist at opposite ends of the spectrum regarding
coupling. With data lake, sources upload raw data in whatever format they see fit, and downstream consumers of this raw
data are expected to know how to process that information. With a data warehouse, the warehouse itself is a structured
data store. Microservices pushing data to the data warehouse need to know the structure of the data warehouse - if the
structure changes in a backwards compatible way, then these producers will need to be updated.

##### Advantages
- This pattern can be implemented very simply, using commonly understood technology.
- Data volumes are also less of a concern here - if you’re sending lots of data in one big go, this pattern can work well.

##### Disadvantages
- common coupling is the issue as datastore is shared
- If you’re dropping a file on a file system, you might want to make sure that the filesystem itself isn’t going to fail
  in interesting ways.
  
##### Where to Use It
Older systems may have limitations on what technology they can support, and may have high costs of change. Even old 
mainframe systems should be able to read data out of a file on the other hand.

I could also implement this pattern using something like a redis cache.  

Another major sweet spot for this pattern is when sharing large volumes of data. If you need to send a multi gigabyte
file onto a file system, or load in a few million rows into a database, then this pattern is the way to go.


#### Pattern: Request-Response Communication
This interaction can be undertaken via a synchronous blocking call, or could be implemented in an asynchronous non-blocking fashion.

So with a non-blocking asynchronous interaction, the microservice that receives the request either needs to implicitly
know where to route the response, or else be told where the response should go. When using a queue, we have the added 
benefit that multiple requests could be buffered up in the queue waiting to be handled. This can help in situations 
where the requests can’t be handled quickly enough. The microservice can consume the next request when it is ready,
rather than being overwhelmed by too many calls. A lot of course then depends on the queue absorbing these requests.

Request-response calls make perfect sense for any situation where the result of a request is needed before further processing
can take place. It also fits really well in situations where a microservice wants to know if a call didn’t work, 
so that it can carry out some sort of compensating action, like a retry. If that fits your situation, request-response is
a sensible approach—the only remaining question then is to decide on a synchronous versus asynchronous implementation, 
with the same tradeoffs we discussed earlier.

#### Pattern: Event-Driven Communication
It is unaware of who the recipients of the events are, making event-driven interactions much more loosely coupled in 
general. When compared to a request-response call though, this is an inversion of responsibility that it can take a while
to get your head around.

##### Just an ID
There are some downsides with this approach. Firstly, the Notification microservice now has to know about the Customer
microservice, adding additional domain coupling. While domain coupling, as we discussed in Chapter 2, is on the looser
end of the coupling spectrum, we’d still like to avoid it where possible. If the event that Notification received 
contained all the information it needed, then this call back wouldn’t be required. This call back from the receiving 
microservice can also lead to the other major downside—namely that in a situation with a large number of receiving 
microservices, the microservice emitting the event might get a barrage of requests as a result. Imagine if five different
microservices all received the same customer creation event, and all needed to request additional information—they’d all
need to immediately send a request to the Customer microservice to get what they needed. As the number of microservices 
interested in a particular event increases, the impact of these calls could become significant.

##### Fully Detailed Events
The alternative, which I prefer, is to put everything into an event that you would be happy otherwise sharing via an API.
Firstly, if the data associated with an event is large, we might have concerns about the size of the event. (Kafka limit 1MB)
Loyalty doesn’t need to know the email address or name of the customer, and yet because it is being sent this information
via the event it nonetheless receives it. This could lead to concerns if we are trying to limit the scope of which 
microservices can see what kind of data.

A way to solve this could be to send two different types of events

This then adds complexity in terms of managing visibility of different events, and also ensuring that both events actually
get fired. What happens in the case when a microservice sends the first type of event, but dies before the second event 
can be sent?

Another consideration is that once we put data into an event, it becomes part of our contract with the outside world.
We have to be aware that if we remove a field from an event that we may break external parties. Information hiding is
still an important concept in event-driven collaboration—the more data we put into an event, the more assumptions external
parties will have about an event. My general rule is that I am OK putting information into an event if I’d be happy
sharing the same data over a request-response API.



  

 

 
 

