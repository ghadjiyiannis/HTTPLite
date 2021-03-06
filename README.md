# HTTPLite

An implementation of a simple web server to serve files from the local filesystem.

## Scope

1. The server imlpements only two of the HTTP methods: GET and HEAD. A request for any of the other methods will return a response of 405 Method Not Allowed
2. The server only serves existing files from the local filesystem. There is no facility for executing server-side code of any kind.
3. The server is multithreaded, with a pool of threads of configurable size.
4. The server implements only "http:" and not "https:".
5. The server implements Content-type headers, but the supported types depend on which of the detectors are installed on the system under test.
6. The system implements Content-Length headers.
7. The system implements the Last-Modified headers.
8. The server implements keep-alive when requested by the browser (including the response header).
9. A significant number of automated tests are provided, with high coverage.
10. Architecturally, the code implements Inversion of Control (IoC) through a custom written injector.
11. The tests demonstrate the use of mocking to increase coverage (especially of error conditions)
12. There is an application log containing error, warning, info, and debug messages
13. The server is not currently locaization intenationalization ready. This can be done at a later time. 
14. The current implementation does not implement an access log. This can be implemented later.

## Known Issues

1. There seems to be some weirdness on Firefox with the favicon.ico request. As far as I can tell this is due to Firefox closing the connection before the response is sent.
2. When the browser closes a (kept alive) connection, the RawHTTP library tries to read a request and throws an exception. This is logged but safely ignored.
3. There seems to be no content type detector for javascript on my system so the corresponding test is ignored

## Design notes

### Inversion of Control

I opted to implement IoC through a hand-crafted injector to keep clarity, and to demonstrate how it works. In a bigger project I would have used a framework (most likely Spring) to achieve the same effect.

### Testing

Although implemented exclusively in Junit 4, the testing is a mixture of unit testing and integration testing. I opted for this approach for two reasons:
1. It provides good coverage without large amounts of effort invested in mocking.
2. It demonstrates both types of testing well. 

When it comes to mocking, I have illustrated the concept by mocking only the FileUtil class and injecting it in some tests to show how one can use mocking to test conditions that are otherwise difficult to test (especially error conditions). In reality, I would use mocking more extensively to increase coverage, and to make sure the tests remain robust in the face of changes to the code. 

Also, I chose to hand-craft the mock objects for simplicity and clarity, but on a bigger project I would once again use a mocking framework (perhaps mockito).

## How to get the code and run the tests

1. Clone this repository.
2. The project is maven based - you should be able to build it using maven.
3. Adjust the config.properties file (in HTTPLite/src/main/resources) to point to the sample document root. The sample document root is the directory named httpd in the repository.
4. Make the file "httpd/not_readable.html" not readable. How to do this will depend on your system. If this file is readable then the corresopnding FileUtilTest will fail, as well as the test for a 403 Forbdden response.
5. Run the AllTests Junit test suite (either through maven or by invoking a different runner)
6. Bring up the root page in a browser and navigate around.

NOTE: Some of the tests check the length of file. This can vary depending on how you have git configured (CRLF convention). If the tests fail only where they check file lengths, you may want to switch your CRLF settings
