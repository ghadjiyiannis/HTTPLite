# HTTPLite

An implementation of a simple web server to serve files from the local filesystem.

## Scope

1. The server imlpements only two of the HTTP methods: GET and HEAD. A request for any of the toehr methods will return a response of 405 Method Not Allowed
2. The server only serves existing files from the local filesystem. There is no facility for executing server-side code of any kind.
3. The server is multithreaded, with a pool of threads of configurable size
4. The server implements only "http:" and not "https:".
5. The server implements Content-type headers, but the supported types depend on which tpe detectors are installed on the system under test.
6. The system implements Content-Length headers.
7. For the HEAD method, teh system implements the Last-Modified headers.
8. The server implements keep-alive when requested by the browser (including the response header).
9. A significant number of automated tests are provided, with high coverage.
10. Architecturally, the code implements Inversion of Control (IoC) through a custom written injector.
11. The tests demonstrate the use of mocking to increase coverage (especially of error conditions)
12. There is an appliaction log containing error, warning, info, and debug messages

## Known Issues

1. There seems to be some weirdness on Firefox with the favicon.ico request. As far as I can tell this is due to Firefox closing the connection before the response is sent.
2. When the browser closes a (kept alive) connection, the RawHTTP library tries to read a request and throws an exception. This is logged but safely ignored.
3 There seems to be no content type detector for javascript on my system so the corresponding test is ignored

## Design notes

### Inversion of Control

I opted to implement IoC through a hand-crafted injector to keep clarity and to demonstrate how it works. In a bigger project I would have used a framework (most likely Spring) to achieve the same effect.

### Testing

Although implemented exclusively in Junit 4, the testing is a mixture of system testing and integration testing. I opted for this approach for two reasons:
1. It provides good coverage without large amounts of effort invested in mocking.
2. It demonstrates both types of testing well. 

When it comes to mocking, I have iluustreated the concept by mocking only teh FileUtil class and injecting it in some tests to show how one can use mocking to test conditions that are otherwise difficult to test (especially error conditions). In reality, I would use mocking more extensively to increase coverage and to make sure the tests remain robsut in the face of changes to the code. 

Also, I chose to hand-craft the mock objects for simplicity and clarity, but on a bigger project I would once again use a mocking framework (perhaps mockito).
