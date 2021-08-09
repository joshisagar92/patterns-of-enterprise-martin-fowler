- Observability is the extent to which you can understand what the system is doing based on external inputs. Logs, events,
and metrics might help you make things observable, but be sure to focus on making the system understandable rather than 
throwing in lots of tools.

Cynically, I could suggest that the pushing of this simplistic narrative is a way to sell you tools. You need one tool 
for metrics, a different tool for logs, and yet another tool for traces! And you need to send all that information 
differently! It’s much easier to sell features by way of a box-ticking exercise when trying to market a product, rather 
than talking about outcomes.

When it comes to making your system observable, think about the outputs you need from your system in terms of events that
you can collect and interrogate. You might need to use different tooling to expose different types of events right now, 
but that may not be the case in the future.

### Log Aggregation
Before you do anything else to build out your microservice architecture, get a log aggregation tool up and running. Consider
it a prerequisite for building a microservice architecture. You’ll thank me later.

#### Common format
- use common  formats used by servers like apache/nginx.
- JSON will be usefull for parsing/searching by tools.

#### Correlating log lines
simple correlation ID in a logfile can be incredibly useful initially, meaning you can defer the use of a dedicated tracing
tool until your system is complex enough to warrant it.
Once you have log aggregation, get correlation IDs in as soon as possible. Easy to do at the start and hard to retrofit 
later, they will drastically improve the value of your logs.

#### Timing
Unfortunately, we cannot guarantee that the clocks on these different machines are in sync. This means that the clock on
the machine where the Email microservice is running might be a few seconds ahead of the clock on the machine where Payment is running

Fundamentally, this means we have two limitations when it comes to time in logs. We can’t get fully accurate timing information
for the overall flow of calls, nor can we understand causality.

if I wanted more accurate information regarding the order of calls, and I also wanted more accurate timing, I’d be more 
inclined to make use of a distributed tracing tool, which will address both issues for me. 

#### Implementations

**_Fluentd --> Elasticsearch --> Kibana_**
- managing kibana is overhead
- Firstly, a lot of effort has gone into marketing Elasticsearch as a database. Personally, this has always sat uneasily with me.
Taking something that was always billed as a search index and rebadging it as a database can be highly problematic.
We implicitly make assumptions about how databases act and behave, and we treat them accordingly, regarding them as a 
source of truth for vital data. But by design, a search index isn’t the source of truth; it’s a projection of the source
of truth. Elasticsearch has suffered from problems in the past that give me pause for thought

- Having a search index that might occasionally lose data isn’t an issue if you can already re-index. But treating it like
a database is another thing entirely. If I were using this stack and couldn’t afford to lose log information, I would want
to ensure that I can re-index the original logs if anything went wrong.

- Splunk is expensive
  
- I’m personally a big fan of Humio, many people like to use Datadog for log aggregation, and you have basic but workable
out-of-the-box solutions for log aggregation with some public cloud providers, such as CloudWatch for AWS or Application Insights for Azure.
  
#### Shortcomings of logs
- due to clock skew they can’t always be relied on to help you understand the order in which calls occurred. This clock 
  skew between machines also means the accurate timing of a sequence of calls will be problematic, potentially limiting 
  the logs’ usefulness in tracking down latency bottlenecks.
  
- The main issue with logs, though, is that as you have more microservices and more calls, you end up generating a LOT of
  data. Loads. Huge quantities. This can result in higher costs in terms of requiring more hardware, and it can also increase
  the fee you pay to your service provider
  
- And depending on how your log aggregation toolchain is built, this can also result in scaling challenges. Some log 
  aggregation solutions try and create an index when receiving log data to make queries faster. The problem is that maintaining
  an index is computationally expensive—and the more logs you receive, and the larger the index grows, the more problematic
  this can become. This results in the need to be more tailored in what you log to reduce this issue, which in turn can 
  generate more work and which runs the risk that you might put off logging information that would otherwise be valuable
  
### Metrics Aggregation
As with the challenge of looking at logs for different hosts, we need to look at better ways to gather and view data about
our systems. It can be hard to know what “good” looks like when we’re looking at metrics for a more complex system. Our 
website is seeing nearly 50 4XX HTTP error codes per second. Is that bad? The CPU load on the catalog service has increased
by 20% since lunch; has something gone wrong? The secret to knowing when to panic and when to relax is to gather metrics
about how your system behaves over a long-enough period of time that clear patterns emerge.

Another key benefit of understanding your trends is when it comes to capacity planning. Are we reaching our limit? How long
until we need more hosts? In the past when we bought physical hosts, this was often an annual job. In the new age of on-demand
computing provided by infrastructure as a service (IaaS) vendors, we can scale up or down in minutes, if not seconds. 
This means that if we understand our usage patterns, we can make sure we have just enough infrastructure to serve our needs.
The smarter we are in tracking our trends and knowing what to do with them, the more cost effective and responsive our 
systems can be.

#### Low versus high cardinality
There are a number of ways to describe cardinality, but you can think of it as the number of fields that can be easily
queried in a given data point.

As I increase the number of things I might want to query on, the cardinality increases, and the more problems systems will
have that aren’t built with this use case in mind.

As Charity Majors,7 founder of Honeycomb, explains:

_**It boils down, essentially, to the metric. The metric is a dot of data, a single number with a name and some identifying
tags. All of the context you can get has to be stuffed into those tags. But the write explosion of writing all those tags
is expensive because of how metrics are stored on disk. Storing a metric is dirt cheap, but storing a tag is expensive; 
and storing lots of tags per metric will bring your storage engine to a halt fast.**_

Practically speaking, systems built with low cardinality in mind will struggle greatly if you try to put higher-cardinality
data into them. Systems like Prometheus, for example, were built to store fairly simple pieces of information, such as the
CPU rate for a given machine. In many ways, we can see Prometheus and similar tools as being a great implementation of 
traditional metrics storage and querying.

The Prometheus devs are quite open about this limitation:

**_Remember that every unique combination of key-value label pairs represents a new time series, which can dramatically increase
the amount of data stored. Do not use labels to store dimensions with high cardinality (many different label values), 
such as user IDs, email addresses, or other unbounded sets of values._**


#### Implementations
- Prometheus has become a popular open source tool for the use of gathering and aggregating metrics
- If you are looking for systems that are able to store and manage high cardinality data -  honeycomb or Lightstep. Although
  these tools are often seen as solutions to distributed tracing (which we’ll explore more later), they are highly capable
  at storing, filtering, and querying high-cardinality data.


**_With a growing set of tools to help us manage our microservice architecture, we must remember that these tools are themselves
production systems. Log aggregation platforms, distributed tracing tools, alerting systems—they all are mission-critical
applications that are just as vital as our own software, if not more so. The same degree of diligence needs to be applied
in terms of maintaining our production monitoring tools as we apply to the software that we write and maintain.
We should also recognize that these tools can become potential vectors of attack from outside parties. **_

### Distributed Tracing
Although the exact implementations vary, broadly speaking, distributed tracing tools all work in a similar way. Local
activity within a thread is captured in a span. These individual spans are correlated using some unique identifier. The 
spans are then sent to a central collector, which is able to construct these related spans into a single trace.

These spans allow you to collect a host of information. Exactly what data you collect will depend on the protocol you are
using, but in the case of the OpenTracing API, each span contains a start and end time, a set of logs associated with the
span, and an arbitrary set of key-value pairs to help with later querying

Sampling strategies can be very basic. Google’s Dapper system, which inspired many of the distributed tracing tools that
came afterwards, performed a highly aggressive random sampling. A certain percentage of calls were sampled, and that was it.
Jaeger, for example, will capture only 1 in 1,000 calls in its default setting. The idea here is to capture enough information
to understand what our system is doing but not capture so much information that the system itself cannot cope.

Tools like Honeycomb and Lightstep can provide more nuanced, dynamic sampling than this simple random-based sampling. An
example of dynamic sampling could be where you want more samples for certain types of events—for example, you might want
to sample anything that generates an error but would be happy sampling only 1 in 100 successful operations if they are all
pretty similar.

#### Implementing distributing tracing
If you are using a standard API like OpenTracing or the newer OpenTelemetry API, you might find that some of the third-party
libraries and frameworks will come with support for these APIs built in and will already send useful information

But even if they do, chances are you’ll still want to instrument your own code, providing useful information about what 
your microservice is doing at any given point of time.





