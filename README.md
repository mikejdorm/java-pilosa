# Java Client for Pilosa

<a href="https://travis-ci.com/pilosa/java-pilosa"><img src="https://api.travis-ci.com/pilosa/java-pilosa.svg?token=vqssvEWV3KAhu8oVFx9s&branch=master"></a>

<img src="https://dc3kpxyuw05cb.cloudfront.net/img/ee.svg" style="float: right" align="right" height="301">

Java client for Pilosa high performance index.

## Changelog

* 2017-05-01: Initial version

## Requirements

* JDK 7 and higher
* Maven 3 and higher

## Install

Add the following dependency in your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.pilosa</groupId>
        <artifactId>pilosa-client</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

This repository supports creating an uber JAR to drop in your projects. Follow the steps below to create it:

```
git clone https://github.com/pilosa/java-pilosa.git
cd java-pilosa/com.pilosa.client
make build
```

If you are on a platform which doesn't have `make` (e.g., Windows), you can view the `build` step of the `Makefile`.

The uber JAR is created at `target/pilosa-client-X.X.X.jar`.

## Usage

### Quick overview

Assuming [Pilosa](https://github.com/pilosa/pilosa) server is running at `localhost:10101` (the default):

```java
// Create the default client
PilosaClient client = PilosaClient.defaultClient();

// Create a Database object
Database mydb = Database.withName("mydb");

// Make sure the index exists on the server
client.ensureDatabase(mydb);

// Create a Frame object
Frame myframe = mydb.frame("myframe");

// Make sure the frame exists on the server
client.ensureFrame(myframe);

// Send a SetBit query. PilosaException is thrown if execution of the query fails.
client.query(myframe.setBit(5, 42));

// Send a Bitmap query. PilosaException is thrown if execution of the query fails.
QueryResponse response = client.query(myframe.bitmap(5));

// Get the result
QueryResult result = response.getResult();

// Deal with the result
if (result != null) {
    List<Long> bits = result.getBitmap().getBits();
    System.out.println("Got bits: " + bits);
}

// You can batch queries to improve throughput
QueryResponse response = client.query(
    mydb.batchQuery(
        myframe.bitmap(5),
        myframe.bitmap(10),
    )    
);
for (Object result : response.getResults()) {
    // Deal with the result
}
```

### Data Model and Queries

#### Databases and Frames

*Database* and *frame*s are the main data models of Pilosa. You can check the [Pilosa documentation](https://www.pilosa.com/docs) for more detail about the data model.

`Database.withName` class method is used to create a index object. Note that, this does not create a index on the server, the index object just defines the schema.

```java
Database repository = Database.withName("repository");
```

Databases support changing the column label and time quantum (*resolution*). `DatabaseOptions` objects store that kind of data. In order to associate a `DatabaseOptions` object to a `Database` object, just pass it as the second argument to `Database.withName`:

```java
DatabaseOptions options = DatabaseOptions.builder()
    .setColumnLabel("repo_id")
    .setTimeQuantum(TimeQuantum.YEAR_MONTH)
    .build();

Database repository = Database.withName("repository", options);
```

Frames are created with a call to `Database.frame` method:

```java
Frame stargazer = repository.frame("stargazer");
```

Similar to index objects, you can pass custom options to frames:

```java
FrameOptions stargazerOptions = FrameOptions.builder()
    .setRowLabel("stargazer_id")
    .setTimeQuantum(TimeQuantum.YEAR_MONTH_DAY)
    .build();

Frame stargazer = repository.frame("stargazer", stargazerOptions);
```

#### Queries

Once you have index and frame objects created, you can create queries for those. Some of the queries work on the columns; corresponding methods are attached to the index. Other queries work on rows, with related methods attached to frames.

For instance, `Bitmap` queries work on rows; use a frame object to create those queries:

```java
PqlQuery bitmapQuery = stargazer.bitmap(1, 100);  // corresponds to PQL: Bitmap(frame='stargazer', stargazer_id=1)
```

`Union` queries work on columns; use the index object to create them:

```java
PqlQuery query = repository.union(bitmapQuery1, bitmapQuery2);
```

In order to increase througput, you may want to batch queries sent to the Pilosa server. `index.batchQuery` method is used for that purpose:

```java
PqlQuery query = repository.batchQuery(
    stargazer.bitmap(1, 100),
    repository.union(stargazer.bitmap(100, 200), stargazer.bitmap(5, 100))
);
```

The recommended way of creating query objects is, using dedicated methods attached to index and frame objects. But sometimes it would be desirable to send raw queries to Pilosa. You can use `index.rawQuery` method for that. Note that, query string is not validated before sending to the server:

```java
PqlQuery query = repository.rawQuery("Bitmap(frame='stargazer', stargazer_id=5)");
```

Please check [Pilosa documentation](https://www.pilosa.com/docs) for PQL details. Here is a list of methods corresponding to PQL calls:

Database:

* `PqlQuery union(PqlBitmapQuery bitmapQuery1, PqlBitmapQuery bitmapQuery2, ...)`
* `PqlQuery intersect(PqlBitmapQuery bitmapQuery1, PqlBitmapQuery bitmapQuery2, ...)`
* `PqlQuery difference(PqlBitmapQuery bitmapQuery1, PqlBitmapQuery bitmapQuery2, ...)`
* `PqlQuery count(PqlBitmapQuery bitmap)`
* `PqlQuery setProfileAttrs(long id, Map<String, Object> attributes)`

Frame:

* `PqlBitmapQuery bitmap(long rowID)`
* `PqlQuery setBit(long rowID, long columnID)`
* `PqlQuery clearBit(long rowID, long columnID)`
* `PqlBitmapQuery topN(long n)`
* `PqlBitmapQuery topN(long n, PqlBitmapQuery bitmap)`
* `PqlBitmapQuery topN(long n, PqlBitmapQuery bitmap, String field, Object... values)`
* `PqlBitmapQuery range(long rowID, Date start, Date end)`
* `PqlQuery setBitmapAttrs(long rowID, Map<String, Object> attributes)`

### Pilosa URI

A Pilosa URI has the `${SCHEME}://${HOST}:${PORT}` format:
* **Scheme**: Protocol of the URI. Default: `http`.
* **Host**: Hostname or ipv4/ipv6 IP address. Default: localhost.
* **Port**: Port number. Default: `10101`.

All parts of the URI are optional, but at least one of them must be specified. The following are equivalent:

* `http://localhost:10101`
* `http://localhost`
* `http://:10101`
* `localhost:10101`
* `localhost`
* `:10101`

A Pilosa URI is represented by `com.pilosa.client.URI` class. Below is a few ways to create `URI` objects:

```java
import com.pilosa.client.URI;

// create the default URI: http://localhost:10101
URI uri1 = URI.defaultURI();

// create a URI from string address
URI uri2 = URI.address("db1.pilosa.com:20202");

// create a URI with the given host and port
URI uri3 = URI.fromHostPort("db1.pilosa.com", 20202);
``` 

### Pilosa Client

In order to interact with a Pilosa server, an instance of `com.pilosa.client.PilosaClient` should be created. The client is thread-safe and uses a pool of connections to the server, so we recommend creating a single instance of the client and share it with other objects when necessary.

If the Pilosa server is running at the default address (`http://localhost:10101`) you can create the default client with default options using:

```java
PilosaClient client = PilosaClient.defaultClient();
```

To use a a custom server address, you can use the `withAddress` class method:

```java
PilosaClient client = PilosaClient.withAddress("http://db1.pilosa.com:15000");
```

If you are running a cluster of Pilosa servers, you can create a `Cluster` object that keeps addresses of those servers for increased robustness:

```java
Cluster cluster = Cluster.withURI(
    URI.address(":10101"),
    URI.address(":10110"),
    URI.address(":10111"),
);

// Create a client with the cluster
PilosaClient client = PilosaClient.withCluster(cluster);
```

It is possible to customize the behaviour of the underlying HTTP client by passing a `ClientOptions` object to the `withCluster` class method:

```java
ClientOptions options = ClientOptions.builder()
    .setConnectTimeout(1000)  // if can't connect in  a second, close the connection
    .setSocketTimeout(10000)  // if no response received in 10 seconds, close the connection
    .setConnectionPoolSizePerRoute(3)  // number of connections in the pool per host
    .setConnectionPoolTotalSize(10)  // number of total connections in the pool
    .setRetryCount(5)  // number of retries before failing the request
    .build();

PilosaClient client = PilosaClient.withCluster(cluster, options);
```

Once you create a client, you can create databases, frames and start sending queries.

Here is how you would create a index and frame:

```java
// materialize repository index instance initialized before
client.createDatabase(repository);

// materialize stargazer frame instance initialized before
client.createFrame(stargazer);
```

If the index or frame was created before, you would receive a `PilosaException`. You can use `ensureDatabase` and `ensureFrame` methods to ignore existing databases and frames.

You can send queries to a Pilosa server using the `query` method of client objects:

```java
QueryResponse response = client.query(frame.bitmap(5));
```

`query` method accepts an optional argument of type `QueryOptions`:

```java
QueryOptions options = QueryOptions.builder()
    .setProfiles(true)  // return column data in the response
    .build();

QueryResponse response = client.query(frame.bitmap(5), options);
```

### Server Response

When a query is sent to a Pilosa server, the server fulfills the query or sends an error message. In the latter case, `PilosaException` is thrown, otherwise a `QueryResponse` object is returned.

A `QueryResponse` object may contain zero or more results of `QueryResult` type. You can access all results using `getResults` method of `QueryResponse`, which returns a list of `QueryResult` objects. Or, using `getResult` method, which returns the first result if there are any or `null` otherwise:

```java
QueryResponse response = client.query(frame.bitmap(5));

// check that there's a result and act on it
QueryResult result = response.getResult();
if (result != null) {
    // act on the result
}

// iterate on all results
for (QueryResult result : response.getResults()) {
    // act on the result
}
```

Similarly, a `QueryResponse` object may include a number of profiles (column objects), if `setProfiles(true)` query option was used:

```java
// check that there's a profile and act on it
ProfileItem profile = response.getProfile();
if (profile != null) {
    // act on the profile
}

// iterate on all profiles
for (ProfileItem profile : response.getProfiles()) {
    // act on the profile
}
```

`QueryResult` objects contain

* `getBitmap` method to retrieve a bitmap result,
* `getCountItems` method to retrieve column count per row ID entries returned from `topN` queries,
* `getCount` method to retrieve the number of rows per the given row ID returned from `count` queries.

```java
BitmapResult bitmap = response.getBitmap();
List<Long> bits = bitmap.getBits();
Map<String, Object> attributes = bitmap.getAttributes();

List<CountResultItem> countItems = response.getCountItems();

long count = response.getCount();
```

## Contribution

Please check our [Contributor's Guidelines](https://github.com/pilosa/pilosa/CONTRIBUTING.md).

1. Sign the [Developer Agreement](https://wwww.pilosa.com/developer-agreement) so we can include your contibution in our codebase.
2. Fork this repo and add it as upstream: `git remote add upstream git@github.com:pilosa/java-pilosa.git`.
3. Make sure all tests pass (use `make test-all`) and be sure that the tests cover all statements in your code (we aim for 100% test coverage).
4. Commit your code to a feature branch and send a pull request to the `master` branch of our repo.

The sections below assume your platform has `make`. Otherwise you can view the corresponding steps of the `Makefile`.

### Running tests

You can run unit tests with:
```
make test
```

And both unit and integration tests with:
```
make test-all
```

### Generating protobuf classes

Protobuf classes are already checked in to source control, so this step is only needed when the upstream `public.proto` changes.

Before running the following step, make sure you have the [Protobuf compiler](https://github.com/google/protobuf) installed:

```
make generate-proto
```

## License

**TODO**