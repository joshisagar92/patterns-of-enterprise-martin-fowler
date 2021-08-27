### Building Topologies
This term is often used to mean the processing logic of an individual microservice. It may also be used to refer to the 
graph-like relationship between individual microservices, event streams, and request-response APIs.

#### Microservice Topology
A microservice topology is the event-driven topology internal to a single microservice.
This defines the data-driven operations to be performed on incoming events, including transformation, storage, and emission.

#### Business Topology
A business topology is the set of microservices, event streams, and APIs that fulfill complex business functions. It is 
an arbitrary grouping of services and may represent the services owned by a single team or department or those that 
fulfill a superset of complex business functionality.

**_A microservice topology details the inner workings of a single microservice. A business topology, on the other hand, 
details the relationships between services.**_



### The Contents of an Event
An event can be anything that has happened within the scope of the business communication structure. Receiving an invoice,
booking a meeting room, requesting a cup of coffee.It is important to recognize that events can be anything that is 
important to the business.
An event is a recording of what happened, much like how an application’s information and error logs record what takes 
place in the application.
Unlike these logs, however, events are also the single source of truth. As such, they must contain
all the information required to accurately describe what happened.

### The Structure of an Event
Events are typically represented using a key/value format. There are three main event types

#### Unkeyed Event
Unkeyed events are used to describe an event as a singular statement of fact. An example could be an event indicating that
a customer interacted with a product, such as a user opening a book entity on a digital book platform. As the name implies,
there is no key involved in this event.

#### Entity Event
An entity is a unique thing and is keyed on the unique ID of that thing. The entity event describes the properties and s
tate of an entity—most commonly an object in the business context—at a given point in time. For a book publisher, an 
example could be a book entity, keyed on ISBN. The value field contains all the necessary information related to the unique entity.

Entity events are particularly important in event-driven architectures. They provide a continual history of the state of
an entity and can be used to materialize state (covered in the next section). Only the latest entity event is needed to 
determine the current state of an entity.


#### Keyed Event
A keyed event contains a key but does not represent an entity. Keyed events are usually used for partitioning the stream
of events to guarantee data locality within a single partition of an event stream

An example could be a stream of events, keyed on ISBN, indicating which user has interacted with the book.

### Materializing State from Entity Events
You can materialize a stateful table by applying entity events, in order, from an entity event stream. Each entity event
is upserted into the key/value table, such that the most recently read event for a given key is represented. Conversely,
you can convert a table into a stream of entity events by publishing each update to the event stream. This is known as the
table-stream duality, and it is fundamental to the creation of state in an event-driven microservice.

A relational database table, for instance, is created and populated through a series of data insertion, update, and deletion
commands. These commands can be produced as events to an immutable log, such as a local append-only file
(like the binary log in MySQL) or an external event stream. By playing back the entire contents of the log, you can exactly
reconstruct the table and all of its data contents.

This table-stream duality is used for communicating state between event-driven microservices. Any consumer client can read
an event stream of keyed events and materialize it into its own local state store. This simple yet powerful pattern allows
microservices to share state through events alone, without any direct coupling between producer and consumer services.

The deletion of a keyed event is handled by producing a tombstone. A tombstone is a keyed event with its value set to null.
This is a convention that indicates to the consumer that the event with that key should be removed from the materialized
data store, as the upstream producer has declared that it is now deleted.

Compaction reduces both disk usage and the number of events that must be processed to reach the current state, at the 
expense of eliminating the history of events otherwise provided by the event stream.
if your business is retail, you will need to know your stock level to identify when you need to reorder and to avoid selling
customers items you do not have. You also want to be able to keep track of your accounts payable and accounts receivable.
Perhaps you want to have a weekly promotion sent to all the customers who have provided you their email addresses. 
All of these systems require that you have the ability to materialize streams of events into current state representations.

### Event Data Definitions and Schemas
Event data serves as the means of long term and implementation agnostic data storage, as well as the communication mechanism
between services. Therefore, it is important that both the producers and consumers of events have a common understanding
of the meaning of the data. Ideally, the consumer must be able to interpret the contents and meaning of an event without
having to consult with the owner of the producing service. This requires a common language for communication between producers
and consumers and is analogous to an API definition between synchronous request-response services.


Schematization selections such as Apache Avro and Google’s Protobuf provide two features that are leveraged heavily in 
event-driven microservices.
- First, they provide an evolution framework, where certain sets of changes can be safely made to the schemas without 
  requiring downstream consumers to make a code change. 
- Second, they also provide the means to generate typed classes 
  (where applicable) to convert the schematized data into plain old objects in the language of your choice. 
  This makes the creation of business logic far simpler and more transparent in the development of microservices.


### Microservice Single Writer Principle
Each event stream has one and only one producing microservice. This microservice is the owner of each event produced to 
that stream. This allows for the authoritative source of truth to always be known for any given event, by permitting the
tracing of data lineage through the system.


### Powering Microservices with the Event Broker
Multiple, distributed event brokers work together in a cluster to provide a platform for the production and consumption 
of event streams. This model provides several essential features that are required for running an event-driven ecosystem at scale:

**_Scalability_**
Additional event broker instances can be added to increase the cluster’s production, consumption, and data storage capacity.

**_Durability_**
Event data is replicated between nodes. This permits a cluster of brokers to both preserve and continue serving data when
a broker fails.

**_High availability_**
A cluster of event broker nodes enables clients to connect to other nodes in the case of a broker failure. This permits 
the clients to maintain full uptime.

**_High-performance_**
Multiple broker nodes share the production and consumption load. In addition, each broker node must be highly performant
to be able to handle hundreds of thousands of writes or reads per second.


#### Event Storage and Serving

_**Partitioning**_
Event streams can be partitioned into individual substreams, the number of which can vary depending on the needs of the 
producer and consumer. This partitioning mechanism allows for multiple instances of a consumer to process each substream
in parallel, allowing for far greater throughput. Note that queues do not require partitioning, though it may be useful 
to partition them anyway for performance purposes.

**_Strict ordering_**
Data in an event stream partition is strictly ordered, and it is served to clients in the exact same order that it was 
originally published.

_**Indexing**_
Events are assigned an index when written to the event stream. This is used by the consumers to manage data consumption,
as they can specify which offset to begin reading from. The difference between the consumer’s current index and the tail
index is the consumer lag. This metric can be used to scale up the number of consumers when it is high, and scale them 
down when it is low. Additionally, it can also be used to awaken Functions-as-a-Service logic.

_**Infinite retention**_
Event streams must be able to retain events for an infinite period of time. This property is foundational for maintaining
state in an event stream.

_**Replayability**_
Event streams must be replayable, such that any consumer can read whatever data it requires. This provides the basis for
the single source of truth and is foundational for communicating state between microservices.




### Event Brokers Versus Message Brokers
Event brokers, on the other hand, are designed around providing an ordered log of facts. Event brokers meet two very specific
needs that are not satisfied by the message broker. For one, the message broker provides only queues of messages, where 
the consumption of the message is handled on a per-queue basis. Applications that share consumption from a queue will each
receive only a subset of the records. This makes it impossible to correctly communicate state via events, since each 
consumer is unable to obtain a full copy of all events. Unlike the message broker, the event broker maintains a single
ledger of records and manages individual access via indices, so each independent consumer can access all required events.
Additionally, a message broker deletes events after acknowledgment, whereas an event broker retains them for as long as 
the organization needs. The deletion of the event after consumption makes a message broker insufficient for providing the
indefinitely stored, globally accessible, replayable, single source of truth for all applications.

**_Event brokers enable an immutable, append-only log of facts that preserves the state of event ordering. The consumer can
pick up and reprocess from anywhere in the log at any time. This pattern is essential for enabling event-driven microservices,
but it is not available with message brokers.**_


#### Consuming from the Immutable Log
Though not a definitive standard, commonly available event brokers use an append-only immutable log. Events are appended
at the end of the log and given an autoincrementing index ID. Consumers of the data use a reference to the index ID to access data.

Events can then be consumed as either an event stream or a queue, depending on the needs of the business and the available
functionality of the event broker.

##### Consuming as an event stream
Each consumer is responsible for updating its own pointers to previously read indices within the event stream. This index,
known as the offset, is the measurement of the current event from the beginning of the event stream. Offsets permit multiple
consumers to consume and track their progress independently of one another.

The consumer group allows for multiple consumers to be viewed as the same logical entity and can be leveraged for horizontal
scaling of message consumption.

The new consumer consumes events only from its assigned partitions, just as older consumer instances previously in the 
group continue to consume only from their remaining assigned partitions. In this way, event consumption can be balanced 
across the same consumer group, while ensuring that all events for a given partition are exclusively consumed by a single
consumer instance. The number of active consumer instances in the group is limited to the number of partitions in the 
event stream.

##### Consuming as a queue
In queue-based consumption, each event is consumed by one and only one microservice instance. Upon being consumed, that 
event is marked as “consumed” by the event broker and is no longer provided to any other consumer. Partition counts do 
not matter when consuming as a queue, as any number of consumer instances can be used for consumption.

Event order is not maintained when processing from a queue. Parallel consumers consume and process events out of order, 
while a single consumer may fail to process an event, return it to the queue for processing at a later date, and move on
to the next event.

#### Providing a Single Source of Truth
The durable and immutable log provides the storage mechanism for the single source of truth, with the event broker becoming
the only location in which services consume and produce data. This way, every consumer is guaranteed to be given an identical
copy of the data.

The adoption of event-driven microservices enables the creation of services that use only the event broker to store and 
access data. While local copies of the events may certainly be used by the business logic of the microservice, the event
broker remains the single source of truth for all data.





