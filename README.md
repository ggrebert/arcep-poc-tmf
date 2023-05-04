# POC TMF Proxy

This project should prove that the TroubleTicket proxy is working as expected with the following features:

* TroubleTicket creation with standard fields
* Validation of TroubleTicket standard fields
* Allow full customization of the the TroubleTicket API by accepting custom fields
* TroubleTicket creation with custom fields for specific TroubleTicket types
* Validation of TroubleTicket custom fields for specific TroubleTicket types
* Secure access to the data limited to the TroubleTicket type
* Allow access in read-only mode to the data for all TroubleTicket types for an administrator

> **Note:**
>
> This project is a POC and is not intended to be used in production.

## Running the application in dev mode

Requirements:

* JDK 17 or later
* Docker

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**
>
>  Quarkus now ships with a Dev UI, which is available in dev mode only
>  at http://localhost:8080/q/dev/.
>
> You can use the Swagger UI to test the REST API
> at http://localhost:8080/q/swagger-ui/.

### Launched containers

* MongoDB
* Kafka
* Localstack (S3)
