# Working Notes
## Using Google Civic Info API
You state in your instructions that you can get bonus points for "Augmenting
the address data to derive more OCD division IDs." I then learned on [your
website](https://democracy.works/elections-api/) that you advocate using the
Google Civic Info API for matching an individual address to all of its
political jurisdictions.

I did just that.

If you `export GOOGLE_CIVIC_INFO_API_KEY="$MY_API_KEY"` in your environment
before starting the server, the system will use that key to derive its OCD-IDs.

Interestingly enough, Google appears to resolve "Pryor Creek, OK" as "Pryor,
OK", which brings back different results from the TurboVote API.

## Using Component-like records
You also state in your instructions that you're looking for a basic
implementation for turning an address into a list of OCD-IDs. I wanted to make
sure I provided that as well. So I decided to make that the default
implementation. You can find it at
`candidate-exercise.election-search/->DefaultDistrictDivs`.

In order to make the code agnostic about which implementation it uses, I
decided to create a protocol
`candidate-exercise.election-search/IDistrictDivs` and then define two
implementations, one of which will be chosen on server startup based on the
existence of `GOOGLE_CIVIC_INFO_API_KEY` in the environment.

As long as I was at it, I thought I might as well provide a protocol for
hitting the TurboVote API. I've found this structure to provide a great deal of
value in the past, especially when switching environments (e.g. dev, state,
prod) or writing unit tests.

## Namespace Structure
The scaffolding you provided seemed to imply a namespace per webpage. So that's
the way I went. The only thing I'm uncomfortable about is that both
`candidate-exercise.home` and `candidate-exercise.election-search` share a
dependency on the structure of the address form. In the real world, I would
probably ping the original author and see what they thought about combining the
two namespaces.

## Additional Features
Right now, I just give a generic error page if any error occurs. Given more
time, I would make sure any user errors were properly reported, along with the
corresponding appropriate action. If a server error occurs, I would report it
via a monitoring tool, apologize the user, and explain there's nothing they can
do and that we've been notified.

In addition, the results page is super-duper bland. I would like to spice that
up.

I would also investigate the small discrepancy between Google's resultant
OCD-IDs for Pryor Creek, OK and TurboVote's expectations. I'm wondering whether
it's a space issue. I find it doubtful, but it's hard to diagnose without
knowing more cities that have upcoming elections.

Lastly, external integration points on an application server can be
problematic. I already added timeouts to mitigate this. Given loads more time
and resources, I would:

1. see if there was any way to pre-fetch most of the data into internal resources
2. add circuit-breakers and monitoring (which would then trip the circuit breaker)
