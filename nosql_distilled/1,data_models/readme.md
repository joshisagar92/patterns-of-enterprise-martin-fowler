One of the most obvious shifts with NoSQL is a move away from the relational model. Each NoSQL solution has a different 
model that it uses, which we put into four categories widely used in the NoSQL ecosystem: key-value, document, column-family,
and graph. Of these, the first three share a common characteristic of their data models which we will call aggregate orientation.
In this chapter we’ll explain what we mean by aggregate orientation and what it means for data models.

### Aggregates
The relational model takes the information that we want to store and divides it into tuples (rows). A tuple is a limited
data structure: It captures a set of values, so you cannot nest one tuple within another to get nested records, nor can 
you put a list of values or tuples within another. This simplicity underpins the relational model—it allows us to think 
of all operations as operating on and returning tuples.

Aggregate orientation takes a different approach. It recognizes that often, you want to operate on data in units that have
a more complex structure than a set of tuples. It can be handy to think in terms of a complex record that allows lists 
and other record structures to be nested inside it. As we’ll see, key-value, document, and column-family databases all 
make use of this more complex record. However, there is no common term for this complex record; in this book we use the 
term “aggregate.”

In Domain-Driven Design, an aggregate is a collection of related objects that we wish to treat as a unit. In particular,
it is a unit for data manipulation and management of consistency. Typically, we like to update aggregates with atomic 
operations and communicate with our data storage in terms of aggregates. This definition matches really well with how key-value,
document, and column-family databases work. Dealing in aggregates makes it much easier for these databases to handle 
operating on a cluster, since the aggregate makes a natural unit for replication and sharding. Aggregates are also often
easier for application programmers to work with, since they often manipulate data through aggregate structures.

Like most things in modeling, there’s no universal answer for how to draw your aggregate boundaries. It depends entirely
on how you tend to manipulate your data. If you tend to access a customer together with all of that customer’s orders at
once, then you would prefer a single aggregate. However, if you tend to focus on accessing a single order at a time, then
you should prefer having separate aggregates for each order. Naturally, this is very context-specific; some applications
will prefer one or the other, even within a single system, which is exactly why many people prefer aggregate ignorance.

####  Consequences of Aggregate Orientation
When working with aggregate-oriented databases, we have a clearer semantics to consider by focusing on the unit of 
interaction with the data storage. It is, however, not a logical data property: It’s all about how the data is being used
by applications—a concern that is often outside the bounds of data modeling.

Relational databases have no concept of aggregate within their data model, so we call them aggregate-ignorant. In the NoSQL
world, graph databases are also aggregate-ignorant. Being aggregate-ignorant is not a bad thing. It’s often difficult to
draw aggregate boundaries well, particularly if the same data is used in many different contexts. An order makes a good 
aggregate when a customer is making and reviewing orders, and when the retailer is processing orders. However, if a 
retailer wants to analyze its product sales over the last few months, then an order aggregate becomes a trouble. To get 
to product sales history, you’ll have to dig into every aggregate in the database. So an aggregate structure may help with
some data interactions but be an obstacle for others. An aggregate-ignorant model allows you to easily look at the data 
in different ways, so it is a better choice when you don’t have a primary structure for manipulating your data.

The clinching reason for aggregate orientation is that it helps greatly with running on a cluster, which as you’ll remember
is the killer argument for the rise of NoSQL. If we’re running on a cluster, we need to minimize how many nodes we need 
to query when we are gathering data. By explicitly including aggregates, we give the database important information about
which bits of data will be manipulated together, and thus should live on the same node.

Aggregates have an important consequence for transactions. Relational databases allow you to manipulate any combination 
of rows from any tables in a single transaction. Such transactions are called ACID transactions: Atomic, Consistent, 
Isolated, and Durable. ACID is a rather contrived acronym; the real point is the atomicity: Many rows spanning many tables
are updated as a single operation. This operation either succeeds or fails in its entirety, and concurrent operations are
isolated from each other so they cannot see a partial update.

It’s often said that NoSQL databases don’t support ACID transactions and thus sacrifice consistency. This is a rather 
sweeping simplification. In general, it’s true that aggregate-oriented databases don’t have ACID transactions that span 
multiple aggregates. Instead, they support atomic manipulation of a single aggregate at a time. This means that if we need
to manipulate multiple aggregates in an atomic way, we have to manage that ourselves in the application code. In practice,
we find that most of the time we are able to keep our atomicity needs to within a single aggregate; indeed, that’s part 
of the consideration for deciding how to divide up our data into aggregates. We should also remember that graph and other
aggregate-ignorant databases usually do support ACID transactions similar to relational databases.

### Key-Value and Document Data Models
We said earlier on that key-value and document databases were strongly aggregate-oriented. What we meant by this was that
we think of these databases as primarily constructed through aggregates. Both of these types of databases consist of lots
of aggregates with each aggregate having a key or ID that’s used to get at the data.

The two models differ in that in a key-value database, the aggregate is opaque to the database—just some big blob of mostly
meaningless bits. In contrast, a document database is able to see a structure in the aggregate. The advantage of opacity
is that we can store whatever we like in the aggregate. The database may impose some general size limit, but other than 
that we have complete freedom. A document database imposes limits on what we can place in it, defining allowable structures
and types. In return, however, we get more flexibility in access.

With a key-value store, we can only access an aggregate by lookup based on its key. With a document database, we can submit
queries to the database based on the fields in the aggregate, we can retrieve part of the aggregate rather than the whole
thing, and the database can create indexes based on the contents of the aggregate.

Databases classified as key-value databases may allow you structures for data beyond just an opaque aggregate. For example,
Riak allows you to add metadata to aggregates for indexing and interaggregate links, Redis allows you to break down the 
aggregate into lists or sets. You can support querying by integrating search tools such as Solr. As an example, Riak 
includes a search facility that uses Solr-like searching on any aggregates that are stored as JSON or XML structures.

Despite this blurriness, the general distinction still holds. With key-value databases, we expect to mostly look up 
aggregates using a key. With document databases, we mostly expect to submit some form of query based on the internal structure
of the document; this might be a key, but it’s more likely to be something else.

###  Column-Family Stores
Most databases have a row as a unit of storage which, in particular, helps write performance. However, there are many scenarios
where writes are rare, but you often need to read a few columns of many rows at once. In this situation, it’s better to 
store groups of columns for all rows as the basic storage unit—which is why these databases are called column stores.

Column-family databases organize their columns into column families. Each column has to be part of a single column family,
and the column acts as unit for access, with the assumption that data for a particular column family will be usually accessed together.

This also gives you a couple of ways to think about how the data is structured.


    




