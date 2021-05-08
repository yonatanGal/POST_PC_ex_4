"I pledge the highest level of ethical principles in support of academic excellence.
I ensure that all of my work reflects my own abilities and not those of someone else."
**********
Theoretical question:
If we wish to control the time it takes for the CalculateRootsService to time out, we could add
a parameter that tells the service how long it should run before timing out.
we can pass this parameter with the intent the "wakes up" the service, and the service can determine
that the default value for timing out is 20 seconds - so in production the real time would be 20 
seconds, and in the tests we can control it and pass 20 ms as a argument of the intent.

