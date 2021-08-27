### Core Principles
the problem with security is that you’re only as secure as your least secure aspect.

#### Principle of Least Privilege
The principle of least privilege describes the idea that when granting access we should grant the minimum access a party
needs to carry out the required functionality, and only for the time period they need it. The main benefit of this is to
ensure that if credentials are compromised by an attacker, those credentials will give the malicious party as limited
access as possible.

- read only access
- This concept can be extended to limit which microservices can be communicated with by specific parties.

#### Defense in Depth
castles can be a great example of the principle of defense in depth.

Having only one protection mechanism is a problem if an attacker finds a way to breach that defense, or if the protection
mechanism defends against only certain types of attackers.

By breaking our functionality apart into different microservices and limiting the scope of what those microservices can do,
we’re already applying defense in depth. We can also run microservices on different network segments, apply network-based
protections at more places, and even make use of a mix of technology for building and running these microservices such that
a single zero-day exploit may not impact everything we have.

#### Automation
automation can help us recover in the wake of an incident. We can use it to revoke and rotate security keys and also
make use of tooling to help detect potential security issues more easily. As with other aspects of microservice architecture,
embracing a culture of automation will help you immensely when it comes to security.

#### Build Security into the Delivery Process
We need to ensure that developers have a more general awareness of security-related concerns, that specialists find a way
to embed themselves into delivery teams when required, and that tooling improves to allow us to build security-related 
thinking into our software. Add security tools in CI pipelines

### The Five Functions of Cybersecurity

#### Identify
Before we can work out what we should protect, we need to work out who might be after our stuff and what exactly they
might be looking for.

Threat Modeling
I can recommend Threat Modeling: Designing for Security5 by Adam Shostack.

#### Protect
Once we’ve identified our most valuable—and most vulnerable—assets, we need to ensure that they are properly protected. 

#### Detect
With a microservice architecture, detecting an incident can be more complex. We have more networks to monitor and more 
machines to keep an eye on. The sources of information are greatly increased, which can make detecting problems all the more difficult.

The software to deal with the increasing complexity of our systems is improving, especially in the space of container
workloads with tools like Aqua.

#### Respond
Aside from the external communication aspects of response, how you handle things internally is also critical. Organizations
that have a culture of blame and fear are likely going to fare badly in the wake of a major incident. Lessons won’t be 
learned, and contributing factors won’t come to light.

#### Recover
Recovery refers to our ability to get the system up and running again in the wake of an attack, and also our ability to 
implement what we have learned to ensure problems are less likely to happen again.

### Foundations of Application Security

#### Credentials
Broadly speaking, credentials give a person (or computer) access to some form of restricted resource. This could be a 
database, a computer, a user account, or something else.

##### User credentials
User credentials, such as email and password combinations, remain essential to how many of us work with our software, 
but they also are a potential weak spot when it comes to our systems being accessed by malicious parties.

##### Secrets
Broadly speaking, secrets are critical pieces of information that a microservice needs to operate and that are also sensitive
enough that they require protecting from malicious parties. Examples of secrets that a microservice might need include:

- Certificates for TLS
- SSH keys
- Public/private API keypairs
- Credentials for accessing databases

If we consider the life cycle of a secret, we can start to tease apart the various aspects of secrets management that might
require different security needs:

- Creation
How do we create the secret in the first place?

- Distribution
Once the secret is created, how do we make sure it gets to the right place (and only the right place)?

- Storage
Is the secret stored in a way that ensures only authorized parties can access it?

- Monitoring
Do we know how this secret is being used?

- Rotation
Are we able to change the secret without causing problems?

If you were looking for a more sophisticated tool in this space, Hashicorp’s Vault is worth a look. An open source tool 
with commercial options available, it’s a veritable Swiss Army knife of secrets management, handling everything from the
basic aspects of distributing secrets to generating time-limited credentials for databases and cloud platforms. Vault 
has the added benefit that the supporting consul-template tool is able to dynamically update secrets in a normal configuration 
file. This means parts of your system that want to read secrets from a local filesystem don’t need to change to support
the secrets management tool. When a secret is changed in Vault, consul-template can update this entry in the configuration
file, allowing your microservices to dynamically change the secrets they are using. This is fantastic for managing 
credentials at scale.

Some public cloud providers also offer solutions in this space; for example, AWS Secrets Manager or Azure’s Key Vault come
to mind. Some people dislike the idea of storing critical secret information in a public cloud service like this, however.

_**Rotation**_
A great example of rotation for operator credentials would be the generation of time-limited API keys for using AWS. Many
organizations now generate API keys on the fly for their staff, with the public and private keypair being valid only for
a short period of time—typically less than an hour.

The use of time-limited credentials can be useful for systems too. Hashicorp’s Vault can generate time-limited credentials
for databases. Rather than your microservice instance reading database connection details from a configuration store or a
text file, they can instead be generated on the fly for a specific instance of your microservice.

The most sensible way forward would likely be to adopt tooling to help automate this process while also limiting the scope
of each set of credentials at the same time.

**_Revocation_**
you’d ideally like to be able to automatically revoke and potentially regenerate credentials when breach happens.


_Accidentally checking in private keys to source code repositories is a common way for credentials to be leaked to unauthorized
parties—it happens a surprising amount. GitHub automatically scans repositories for some types of secrets, but you can also
run your own scanning too. It would be great if you could pick up secrets before you check in, and git-secrets lets you 
do just that. It can scan existing commits for potential secrets, but by setting it up as a commit hook, it can stop commits
even being made. There is also the similar gitleaks, which, in addition to supporting pre-commit hooks and general scanning
of commits, has a few features that make it potentially more useful as a general tool for scanning local files._


**_Limiting scope_**
Limiting the scope of credentials is core to the idea of embracing the principle of least privilege. 

_**Patching**_
Containers throw us an interesting curveball here. We treat a given container instance as immutable. But a container 
contains not just our software but also an operating system. And do you know where that container has come from? Containers
are based on an image, which in turn can extend other images—are you sure the base images you are using don’t already 
have backdoors in them? If you haven’t changed a container instance in six months, that’s six months worth of operating 
system patches that haven’t been applied. Keeping on top of this is problematic, which is why companies like Aqua provide
tooling to help you analyze your running production containers so you can understand what issues need to be addressed.

At the very top of this set of layers, of course, is our application code. Is that up to date? It’s not just the code we
write; what about the third-party code we use? A bug in a third-party library can leave our application vulnerable to attack.

At scale, working out which microservices are linking to libraries with known vulnerabilities can be incredibly difficult.
This is an area in which I strongly recommend the use of tools like Snyk or GitHub code scanning, which is able to
automatically scan your third-party dependencies and alert you if you are linking to libraries with known vulnerabilities.

If it finds any, it can send you a pull request to help update to the latest patched versions. You can even build this 
into your CI process and have a microservice build fail if it links to libraries with issues.

**_Backups_**
Disks are more reliable than they used to be. Databases are more likely to have built-in replication to avoid data loss.
With such systems, we may convince ourselves that we don’t need backups. But what if a catastrophic error occurs and your
entire Cassandra cluster is wiped out? Or what if a coding bug means your application actually deletes valuable data?
Backups are as important as ever. So please, back up your critical data.

So make sure you back up critical data, keep those backups in a system separate to your main production environment, and
make sure the backups actually work by regularly restoring them.

**_Rebuild_**
Your ability to rebuild a given microservice, or even an entire system, comes down to the quality of your automation and
backups. If you can deploy and configure each microservice from scratch based on information stored in source control, 
you’re off to a good start.

### Implicit Trust Versus Zero Trust

#### Implicit Trust
Our first option could be to just assume that any calls to a service made from inside our perimeter are implicitly trusted.

Depending on the sensitivity of the data, this might be fine. Some organizations attempt to ensure security at the perimeter
of their networks, and they therefore assume they don’t need to do anything else when two services are talking together.

#### Zero Trust
When operating in a zero-trust environment, you have to assume that you are operating in an environment that has already
been compromised—the computers you are talking to could have been compromised, the inbound connections could be from hostile
parties, the data you are writing could be read by bad people. Paranoid? Yes! Welcome to zero trust.

Zero trust, fundamentally, is a mindset. It’s not something you can magically implement using a product or tool; it’s an
idea and that idea is that if you operate under the assumption that you are operating in a hostile environment in which 
bad actors could already be present, then you have to carry out precautions to make sure that you can still operate safely.

_[With zero trust] you can actually make certain counter-intuitive access decisions and for example allow connections to 
internal services from the internet because you treat your “internal” network as equally trustworthy as the internet_ 

#### It’s a Spectrum

I don’t mean to imply that you have a stark choice between implicit and zero trust. The extent to which you trust (or don’t)
other parties in your system could change based on the sensitivity of the information being accessed. You might decide,
for example, to adopt a concept of zero trust for any microservices handling PII but be more relaxed in other areas. Again,
the cost of any security implementation should be justified (and driven) by your threat model.

Microservices within each zone could communicate with each other but were unable to directly reach across to access data
or functionality in the lower, more secure zones. Microservices in the more secure zones could reach up to access functionality
running in the less secure zones, though.

### Securing Data

#### Data in Transit
The nature of the protections you have will depend largely on the nature of the communication protocols you have picked.
If you are using HTTP, for example, it would be natural to look at using HTTP with Transport Layer Security (TLS)

I think instead it is important to consider more generically the four main areas of interest when it comes to securing data
in transit, and to look at how these concerns could be addressed with HTTP as an example

##### Server identity
it is vital to ensure that when we go to a website it really is the website it claims to be.
With HTTPS, our browser can look at the certificate for that website and make sure it is valid.
It’s worth noting that some communication protocols that use HTTP under the hood can take advantage of HTTPS—so we can 
easily run SOAP or gRPC over HTTPS without issue.

##### Client identity
We can verify the identity of a client in a number of ways. We could ask that the client send us some information in the
request telling us who they are. An example might be to use some sort of shared secret or a client-side certificate to'
sign the request.

I struggle to think of a situation in which I would verify client identity without also verifying server identity—to verify
both, you would typically end up implementing some form of mutual authentication. With mutual authentication, both parties
authenticate each other.

We can do this through the use of mutual TLS, in which case both the client and server make use of certificates. On the 
public internet, verifying the identity of a client device is typically less important than verifying the identity of the
human using that device. As such, mutual TLS is rarely used. In our microservice architecture, though, especially where 
we might be operating in a zero-trust environment, this is much more common.

Tools like Vault can make distributing certificates much easier, and wanting to simplify the use of mutual TLS is one 
of the main reasons for people to implement service meshes, which we explored in “Service Meshes and API Gateways”.

##### Visibility of data
When you use either plain old HTTPS or mutual TLS, data won’t be visible to intermediate parties—this is because TLS 
encrypts the data being sent.

##### Manipulation of data
Typically, the types of protections that make data invisible will also ensure that the data can’t be manipulated (HTTPS 
does that, for instance). However, we could decide to send data in the open but still want to ensure it cannot be manipulated.
For HTTP, one such approach is to use a hash-based message authentication code (HMAC) to sign the data being sent. With 
HMAC, a hash is generated and sent with the data, and the receiver can check the hash against the data to confirm that 
the data hasn’t been changed.

#### Data at Rest
Many of the high-profile security breaches we hear of involve data at rest being acquired by an attacker, and that data 
being readable by the attacker. This happens either because the data was stored in an unencrypted form or because the 
mechanism used to protect the data had a fundamental flaw.

##### Go with the well known
Whatever programming language you use, you’ll have access to reviewed, regularly patched implementations of well-regarded
encryption algorithms. Use those! And subscribe to the mailing lists/advisory lists for the technology you choose to make
sure you are aware of vulnerabilities as they are found, so you can keep them patched and up to date.

For securing passwords, you should absolutely be using a technique called salted password hashing. This ensures that passwords
are never held in plain text, and that even if an attacker brute-forces one hashed password they cannot then automatically
read other passwords.

Badly implemented encryption could be worse than having none, as the false sense of security (pardon the pun) can lead you
to take your eye off the ball.

##### Pick your targets
By subdividing your system into more fine-grained services, you might identify an entire data store that can be encrypted
wholesale, but that is unlikely. Limiting this encryption to a known set of tables is a sensible approach.

##### Be frugal
The advantages to being frugal with data collection are manifold. First, if you don’t store it, no one can steal it. Second,
if you don’t store it, no one (e.g., a governmental agency) can ask for it either!

##### It’s all about the keys
One solution is to use a separate security appliance to encrypt and decrypt data. Another is to use a separate key vault
that your service can access when it needs a key. The life-cycle management of the keys (and access to change them) can 
be a vital operation, and these systems can handle this for you. This is where HashiCorp’s Vault can also come in very handy.

Encrypt data when you first see it. Only decrypt on demand, and ensure that data is never stored anywhere.

##### Encrypt backups

### Authentication and Authorization
Generally, when we’re talking abstractly about who or what is being authenticated, we refer to that party as the principal.

Authorization is the mechanism by which we map from a principal to the action we are allowing them to do.
Often, when a principal is authenticated, we will be given information about them that will help us decide what we should
let them do. We might, for example, be told what department or office they work in—a piece of information that our system
can use to decide what the principal can and cannot do.

Ease of use is important—we want to make it easy for our users to access our system. We don’t want everyone to have to log
in separately to access different microservices, using a different username and password for each one. So we also need to
look at how we can implement single sign-on (SSO) in a microservices environment.

#### Service-to-Service Authentication
Earlier we discussed mutual TLS, which, aside from protecting data in transit, also allows us to implement a form of 
authentication. When a client talks to a server using mutual TLS, the server is able to authenticate the client, and the
client is able to authenticate the server—this is a form of service-to-service authentication. Other authentication schemes
can be used besides mutual TLS. A common example is the use of API keys, where the client needs to use the key to hash a
request in such a way that the server is able to verify that the client used a valid key.

#### Human Authentication
MFA would most commonly involve the use of a normal username and password combo, in addition to providing at least one additional factor.

The different types of authentication factors have grown in recent years—from codes sent over SMS and magic links sent 
via email to dedicated mobile apps like Authy and USB and NFC hardware devices like the YubiKey. Biometric factors are 
more commonly used now as well, as users have more access to hardware that supports things like fingerprint or face recognition.

##### Common Single Sign-On Implementations
When a principal tries to access a resource (like a web-based interface), they are directed to authenticate with an identity
provider. The identity provider may ask them to provide a username and password or might require the use of something more
advanced like MFA. Once the identity provider is satisfied that the principal has been authenticated, it gives information
to the service provider, allowing it to decide whether to grant them access to the resource.

This identity provider could be an externally hosted system or something inside your own organization. Google, for example,
provides an OpenID Connect identity provider. For enterprises, though, it is common to have your own identity provider, 
which may be linked to your company’s directory service. A directory service could be something like the Lightweight 
Directory Access Protocol (LDAP) or Active Directory. These systems allow you to store information about principals, 
such as what roles they play in the organization. Often the directory service and the identity provider are one and the 
same, while at other times they are separate but linked. Okta, for example, is a hosted SAML identity provider that handles
tasks like two-factor authentication but can link to your company’s directory services as the source of truth.

SAML is a SOAP-based standard and is known for being fairly complex to work with despite the libraries and tooling available
to support it, and since the first edition of this book it has rapidly fallen out of favor.15 OpenID Connect is a standard
that has emerged as a specific implementation of OAuth 2.0, based on the way Google and others handle SSO. It uses simpler
REST calls, and due in part to its relative simplicity and widespread support, it is the dominant mechanism for end-user
SSO, and has gained significant inroads into enterprises.

##### Single Sign-On Gateway
Rather than having each service manage handshaking with our identity provider, a more common approach is to use a gateway
to act as a proxy, sitting between your services and the outside world

However, we still need to solve the problem of how the downstream service receives information about principals, such as
their username or what roles they play. If you’re using HTTP, you could configure your gateway to populate headers with 
this information. Shibboleth is one tool that can do this for you, and I’ve seen it used with the Apache web server to
handle integration with SAML-based identity providers, to great effect. An alternative, which we’ll look at in more detail
shortly, is to create a JSON Web Token (JWT) containing all the information about the principal; this has a number of benefits,
including being something we can more easily pass from microservice to microservice.


Another consideration with using a single sign-on gateway is that if we have decided to offload responsibility for authentication
to a gateway, it can be harder to reason about how a microservice behaves when looking at it in isolation.
If you decide to use a gateway, make sure your developers can launch their services behind one without too much work.

Again, I like to return to the idea of defense in depth—from network perimeter to subnet, firewall, machine, operating
system, and the underlying hardware. You have the ability to implement security measures at all of these points. I have 
seen some people put all their eggs in one basket, relying on the gateway to handle every step for them. And we all know
what happens when we have a single point of failure…

##### Fine-Grained Authorization
These decisions need to be local to the microservice in question. I have seen people use the various attributes supplied
by identity providers in horrible ways, using really fine-grained roles like CALL_CENTER_50_DOLLAR_REFUND, where they end
up putting information specific to one piece of microservice functionality into their directory services. This is a nightmare
to maintain and gives very little scope for our services to have their own independent life cycle, as suddenly a chunk of
information about how a service behaves lives elsewhere, perhaps in a system managed by a different part of the organization.

