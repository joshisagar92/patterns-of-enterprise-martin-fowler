### Event-Driven Data Contracts
The format of the data to be communicated and the logic under which it is created form the data contract. This contract 
is followed by both the producer and the consumer of the event data. It gives the event meaning and form beyond the context 
in which it is produced and extends the usability of the data to consumer applications.

You must take care when changing the data definition, so as not to delete or alter fields that are being used by downstream
consumers. Similarly, you must also be careful when modifying the triggering logic. It is far more common to change the 
data definition than the triggering mechanism, as altering the latter often breaks the meaning of the original event definition.


#### Using Explicit Schemas as Contracts
In doing so, the producer provides a mechanism for communicating its event format to all prospective consumers. The consumers,
in turn, can confidently build their microservice business logic against the schematized data.

A consumer must be able to extract the data necessary for its business processes, and it cannot do that without having a
set of expectations about what data should be available.

There is also substantial risk in requiring each consumer to independently interpret the data, as a consumer may interpret
it differently than its peers, which leads to inconsistent views of the single source of truth.

Producers are also at a disadvantage with implicit schemas. Even with the best of intentions, a producer may not notice 
(or perhaps their unit tests don’t reveal) that they have altered their event data definition. Without an explicit check
of their service’s event format, this situation may go unnoticed until it causes downstream consumers to fail. Explicit 
schemas give security and stability to both consumers and producers.

#### Schema Definition Comments
Support for integrated comments and arbitrary metadata in the schema definition is essential for communicating the meaning of an event. 

- Specifying the triggering logic of the event. This is typically done in a block header at the top of the schema definition
  and should clearly state why an event has been generated.
  
- Giving context and clarity about a particular field within the structured schema. For example, a datetime field’s comments
  could specify if the format is UTC, ISO, or Unix time.
  
#### Full-Featured Schema Evolution
chema evolution enables producers to update their service’s output format while allowing consumers to continue consuming
the events uninterrupted.

An explicit set of schema evolution rules goes a long way in enabling both consumers and producers to update their applications
in their own time. These rules are known as compatibility types.

##### Forward compatibility
Allows for data produced with a newer schema to be read as though it were produced with an older schema. This is a particularly
useful evolutionary requirement in an event-driven architecture, as the most common pattern of system change begins with
the producer updating its data definition and producing data with the newer schema. The consumer is required only to update
its copy of the schema and code should it need access to the new fields.

##### Backward compatibility
Allows for data produced with an older schema to be read as though it were produced with a newer schema. This enables a 
consumer of data to use a newer schema to read older data.

- The consumer is expecting a new feature to be delivered by the upstream team. If the new schema is already defined, the
  consumer can release its own update prior to the producer release.
  
-  Updates can be made to the schema format for new producer releases, while maintaining the compatibility with previous releases.

- The consumer application may need to reprocess data in the event stream that was produced with an older schema version.
  Schema evolution ensures that the consumer can translate it to a familiar version. If backward compatibility is not 
  followed, the consumer will only be able to read messages with the latest format.
  
##### Full compatibility
The union of forward compatibility and backward compatibility, this is the strongest guarantee and the one you should use
whenever possible. You can always loosen the compatibility requirements at a later date, but it is often far more difficult
to tighten them.

#### Code Generator Support
A code generator is used to turn an event schema into a class definition or equivalent structure for a given programming
language. This class definition is used by the producer to create and populate new event objects.

The biggest benefit of code generator support is being able to write your application against a class definition in the 
language of your choice.


#### Breaking Schema Changes
The most important thing when dealing with breaking schema changes is to communicate early and clearly with downstream
consumers. Ensure that any migration plans have the understanding and approval of everyone involved and that no one is 
caught unprepared.

_**Accommodating breaking schema changes for entities**_
Breaking changes to an entity schema are fairly rare, as this circumstance typically requires a redefinition of the original
domain model such that the current model cannot simply be extended. New entities will be created under the new schema, 
while previous entities were generated under the old schema.

- Contend with both the old and new schemas.
- Re-create all entities in the new schema format (via migration, or by re-creating them from source).

The first option is the easiest for the producer, but it simply pushes off the resolution of the different entity 
definitions onto the consumer. This contradicts the goal of reducing the need for consumers to interpret the data individually
and increases the risk of misinterpretation, inconsistent processing between services, and significantly higher complexity
in maintaining systems.

The second option is more difficult for the producer, but ensures that the business entities, both old and new, are redefined 
consistently. In practice, the producer must reprocess the source data that led to the generation of the old entities and
apply the new business logic to re-create the entities under the new format. This approach forces the organization as a 
whole to resolve what these entities mean and how they should be understood and used by producer and consumer alike.

The reality is that the consumer will never be in a better position than the producer for resolving divergent schema 
definitions. It is bad practice to defer this responsibility to the consumer.

Leave the old entities under the old schema in their original event stream, because you may need them for reprocessing 
validation and forensic investigations. Produce the new and updated entities using the new schema to a new stream.

_**Accommodating breaking schema changes for events**_
Nonentity events tend to be simpler to deal with when you are incorporating breaking changes. The simplest option is to 
create a new event stream and begin producing the new events to that stream. The consumers of the old stream must be 
notified so that they can register themselves as consumers of the new event stream. Each consuming service must also 
account for the divergence in business logic between the two event definitions.


### Selecting an Event Format
I recommend instead choosing a strongly defined, explicit schema format that supports controlled schema evolution, such 
as Apache Avro or Protobuf. I do not recommend JSON, as it does not provide full-compatibility schema evolution.

### Designing Events

- A good event definition is not simply a message indicating that something happened, but rather the complete description
  of everything that happened during that event. In business terms, this is the resultant data that is produced when input
  data is ingested and the business logic is applied. This output event must be treated as the single source of truth and
  must be recorded as an immutable fact for consumption by downstream consumers. It is the full and total authority on 
  what actually occurred, and consumers should not need to consult any other source of data to know that such an event took place.
- An event stream should contain events representing a single logical event. It is not advisable to mix different types 
  of events within an event stream, because doing so can muddle the definitions of what the event is and what the stream
  represents. It is difficult to validate the schemas being produced, as new schemas may be added dynamically in such a 
  scenario. Though there are special circumstances where you may wish to ignore this principle, the vast majority of event
  streams produced and consumed within your architectural workflow should each have a strict, single definition. 
  
- Use the narrowest types for your event data. This lets you rely on the code generators, language type checking (if supported),
  and serialization unit tests to check the boundaries of your data. It sounds simple, but there are many cases where ambiguity
  can creep in when you don’t use the proper types.
  Using string to store a numeric value
  Using integer as a boolean
  Using string as an enum
  Enums are often avoided because producers fear creating a new enum token that isn’t present in the consumer’s schema. However,
  the consumer has a responsibility to consider enum tokens that it does not recognize, and determine if it should process
  them using a default value or simply throw a fatal exception and halt processing until someone can work out what needs to
  be done. Both Protobuf and Avro have elegant ways of handling unknown enum tokens and should be used if either is selected
  for your event format.
  
- One common anti-pattern is adding a type field to an event definition, where different type values indicate specific 
  ubfeatures of the event. This is generally done for data that is “similar yet different” and is often a result of the 
  implementer incorrectly identifying the events as single-purpose. Though it may seem like a time-saving measure or a 
  simplification of a data access pattern, overloading events with type parameters is rarely a good idea.
  It is also possible for these meanings to change over time and for the scope that an event covers to creep. Some of
  these types may require the addition of new parameters to track type-specific information, whereas other types require
  separate parameters. Eventually you could have a situation where there are several very distinct events all inhabiting
  the same event schema, making it difficult to reason about what the event truly represents.
  Avoid adding type fields in your events that overload the meaning of the event. This can cause significant difficulty 
  in evolving and maintaining the event format.
  
- There are several considerations when you’re looking at a design that produces a very large event. Make sure that the 
  data is directly related and relevant to the event. Additional data may have been added to an event “just in case,” but
  it may not be of any real use to the downstream consumers.
  This scenario is not always avoidable, though—some event processors produce very large output files (perhaps a large image)
  that are much too big to fit into a single message of an event stream. In these scenarios you can use a pointer to the
  actual data, but do this sparingly. This approach adds risk in the form of multiple sources of truth and payload mutability,
  as an immutable ledger cannot ensure the preservation of data outside of its system.
  
- When designing a new event, it is important to involve any anticipated consumers of this data. Consumers will understand
  their own needs and anticipated business functions better than the producers and may help in clarifying requirements. 
  Consumers will also get a better understanding of the data coming their way. A joint meeting or discussion can shake out
  any issues around the data contract between the two systems.
  
- Avoid using events as a semaphore or a signal. These events simply indicate that something has occurred without being 
  the single source of truth for the results.
  
- Consider a very simple example where a system outputs an event indicating that work has been completed for an arbitrary
  job. Although the event itself indicates the work is done, the actual result of the work is not included in the event.
  This means that to consume this event properly, you must find where the completed work actually resides. Once there are
  two sources of truth for a piece of data, consistency problems arise.



