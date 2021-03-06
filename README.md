# Dummy managed services

This application replicates the Managed services REST API. It aims to help other developers willing to integrate
with this API.

It implements as an in-memory storage and all dependent values will be generated randomly.

It is possible to use an existing Keycloak for authentication and token propagation.

Use the following environment variables to set up authentication:

* `quarkus.oidc.tenant-enabled`: If `false` the `SSO_*` environment variables will not be used and authentication
will be disabled.
* `SSO_URL`: Full URL for Keycloak. e.g. https://myexample.com/auth/realms/myrealm/
* `SSO_CLIENT_ID`: Client name configured in Keycloak
* `SSO_CLIENT_SECRET`: Client secret for the given name


## Supported services

At this moment these are the implemented services:

### ServiceAccount

```shell script
$ curl -X POST -H "Content-type: application/json" -H "Authorization: Bearer $TKN" http://localhost:8080/api/kafkas_mgmt/v1/service_accounts -d '{"name": "foo", "description": "bar"}' | jq
{
  "id": "a3e02fb2-0ede-404f-93ac-6fdd4941256b",
  "kind": null,
  "href": null,
  "name": "foo",
  "description": "bar",
  "client_id": "svc-acc-275792144",
  "client_secret": "9e30213a-d798-4c35-b740-0003453e1bc8",
  "owner": "alice",
  "created_at": "2021-06-01T17:55:46.348782+02:00"
}

```

If you need to provide some static configuration for the `client_id` and `client_secret` it is possible to provide a path containing the static configuration for any service accounts by `name`.
Start the application with the `-Ddms.serviceaccount.static=/path/to/my/config.json` 

An example is provided in the [examples/static-sa.json](./examples/static-sa.json) file.

### KafkaRequests

```shell script
$ curl -H "content-type: application/json" -H "accept: application/json" -H "Authorization: Bearer $TKN" http://localhost:8080/api/kafkas_mgmt/v1/kafkas -d '{"name": "foo", "region": "Teruel", "cloud_provider": "ibm", "multi_az": "true"}' | jq

{
  "id": "1779c504-f6d6-4873-b141-da4ee0b1a960",
  "kind": null,
  "href": null,
  "status": "Provisioning",
  "cloud_provider": "ibm",
  "multi_az": true,
  "region": "Teruel",
  "owner": "bob",
  "name": "foo",
  "bootstrap_server_host": "http://1779c504-f6d6-4873-b141-da4ee0b1a960:9092",
  "created_at": "2021-06-02T11:35:21.028327+02:00",
  "updated_at": null,
  "failed_reason": null,
  "version": null
}

```

## Running the application from a container

A container image already exists in [quay.io/ruben/dummy-managed-services](https://quay.io/repository/ruben/dummy-managed-services)

```shell script
podman run -d -e SSO_URL=http://localhost:8180/auth/realms/myrealm/ \
  -e SSO_CLIENT_ID=managed-services \
  -e SSO_CLIENT_SECRET=secret \
  -p 18080:8080 \
  quay.io/ruben/dummy-managed-services:latest
```

Or with authentication disabled

```shell script
podman run -d -e quarkus.oidc.tenant-enabled=false \
  -p 18080:8080 \
  quay.io/ruben/dummy-managed-services:latest
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it???s not an _??ber-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _??ber-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/dummy-managed-services-1.0.0-SNAPSHOT-runner`
