package fr.arcep;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.Explode;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
    tags = {
      @Tag(
          name = "Trouble Ticket API",
          description =
              """
                This API allows to create and manage trouble tickets.

                The API is secured by a client ID. You must provide the client ID in the `X-Client-Id` header.
                For a production environment, this client ID should be a JWT token.

                This API is only allowed for internal use. It is not exposed to the public.
                """),
      @Tag(
          name = "Attachment API",
          description =
              """
                The Attachment API provides a standardized client interfaces to send files by a customer or another system.

                An Attachment is a file that can be:

                * Attached to a specific entity (e.g. a customer, a product, a service, etc.).
                * A document, a picture, a video, a sound, a zip file, etc.


                The API support the ability to:

                * Upload a new Attachment for a specific
                    [relatedEntity](https://raw.githubusercontent.com/tmforum-rand/schemas/candidates/Common/EntityRef.schema.json)
                * Download an Attachment
                * Update an Attachment metadata
                * Delete an Attachment
                * Search Attachments with a filters and pagination or stream

                The API is designed to be used by a machine-to-machine interface,
                and is not intended to be used by a human user interface.
                """),
      @Tag(
          name = "Anomalie Adresse Proxy",
          description =
              """
                Proxy for the Trouble Ticket API
                which can manage trouble tickets of type `anomalieAdresse`.
                """),
      @Tag(
          name = "Malfacon Proxy",
          description =
              """
                Proxy for the Trouble Ticket API
                which can manage trouble tickets of type `malfacon`.
                """),
      @Tag(
          name = "Admin Proxy",
          description =
              """
                Proxy for the Trouble Ticket API which is allowed to:

                * list all trouble tickets for any type
                * create and delete trouble tickets of type `admin`
                """),
      @Tag(
          name = "Admin ReadOnly Proxy",
          description =
              """
            Proxy for the Trouble Ticket API which is allowed to only list all trouble tickets for any type
            """),
    },
    components =
        @Components(
            parameters = {
              @Parameter(
                  in = ParameterIn.QUERY,
                  name = "filters",
                  style = ParameterStyle.FORM,
                  explode = Explode.TRUE,
                  schema = @Schema(type = SchemaType.OBJECT),
                  description =
                      """
                        The filter criteria.

                        key: the name of the property to filter on.
                        value: the value of the property to filter on.

                        You can filter on nested properties using the dot notation.

                        You can also add complex filters using the following syntax:

                        `"{key}[{operator}]": "{value}"`

                        <br />where:

                        * `key`: the name of the property to filter on.
                        * `operator`: the operator to use for the filter.
                        * `value`: the value to be processed by the operator.

                        List of operators:

                        * `==`: equals (default)

                        * `!=`: not equals

                        * `>`: greater than

                        * `>=`: greater than or equals

                        * `<`: less than

                        * `<=`: less than or equals

                        * `=~`: regex match

                        * `!~`: regex not match

                        * `in`: check if the value is in the list.

                          <br />The value must be a comma-separated list of values.

                        * `nin`: not in

                        * `is`: Check the nature of a property.

                          <br />Allowed values are:

                            * `null`: the property must be null
                            * `nnull`: the property must not be null
                            * `empty`: the property must be empty
                            * `nempty`: the property must not be empty
                            * `exists`: the property must exist
                            * `nexists`: the property must not exist
                            * `int`: the property must be an integer
                            * `array`: the property must be an array
                            * `object`: the property must be an object
                            * `string`: the property must be a string
                            * `boolean`: the property must be a boolean

                        * `nis`: Inverse of `is` operator

                        ---

                        Example:

                        ```
                        {
                            "@type": "MyType",
                            "status[in]": "OPEN,CLOSED",
                            "myarray[nis]": "empty",
                            "relatedEntity.@referredType": "EntityType",
                            "relatedEntity.id": "31546546"
                        }
                        ```

                        The generated Url for the previews example will generate the following query string:

                        `%40type=MyType&status%5Bin%5D=OPEN%2CCLOSED&myarray%5Bnis%5D=empty&relatedEntity.%40referredType=EntityType&relatedEntity.id=31546546`

                        <br><br>
                        """),
              @Parameter(
                  in = ParameterIn.QUERY,
                  name = "sort",
                  schema = @Schema(example = "-createdDate"),
                  description =
                      """
                        The sort criteria.

                        The default sort order is ascending.
                        To specify descending order for a field, a prefix of `-` should be used.

                        Multiple sort criteria can be passed.
                        The sort criteria will be applied in the order they are passed.
                        """),
              @Parameter(
                  in = ParameterIn.QUERY,
                  name = "limit",
                  schema =
                      @Schema(
                          type = SchemaType.INTEGER,
                          format = "int32",
                          minimum = "0",
                          maximum = "100",
                          defaultValue = "100"),
                  description = "The maximum number of elements to return."),
              @Parameter(
                  in = ParameterIn.QUERY,
                  name = "offset",
                  schema = @Schema(type = SchemaType.INTEGER, format = "int32", defaultValue = "0"),
                  description = "The offset of the first element to return."),
              @Parameter(
                  in = ParameterIn.QUERY,
                  name = "fields",
                  schema = @Schema(type = SchemaType.ARRAY, implementation = String.class),
                  description =
                      """
                        Comma-separated list of fields to return.

                        * If not specified, all fields will be returned.
                        * If specified, only the specified fields will be returned.
                        * If specified, you can use 2 ways to specify the fields:
                            * `fields=id,name,@type`: the fields must be separated by a comma.
                            * `fields=id&fields=name&fields=@type`: the fields must be specified in separate query parameters.

                            Note: You can combine both ways.
                        """),
              @Parameter(
                  name = "Range",
                  in = ParameterIn.HEADER,
                  description = "The range of bytes to download.",
                  schema = @Schema(type = SchemaType.STRING),
                  example = "bytes=0-100"),
              @Parameter(
                  name = "If-Match",
                  in = ParameterIn.HEADER,
                  schema = @Schema(type = SchemaType.STRING)),
              @Parameter(
                  name = "If-None-Match",
                  in = ParameterIn.HEADER,
                  schema = @Schema(type = SchemaType.STRING)),
              @Parameter(
                  name = "If-Modified-Since",
                  in = ParameterIn.HEADER,
                  schema = @Schema(type = SchemaType.STRING)),
              @Parameter(
                  name = "If-Unmodified-Since",
                  in = ParameterIn.HEADER,
                  schema = @Schema(type = SchemaType.STRING)),
              @Parameter(
                  name = "X-Client-Id",
                  in = ParameterIn.HEADER,
                  schema = @Schema(type = SchemaType.STRING),
                  required = true,
                  description =
                      """
                        The client id of the application making the request.

                        In production mode, this header must be replaced by a valid JWT token.
                        """),
              @Parameter(
                  name = "X-Allowed-MimeType",
                  in = ParameterIn.HEADER,
                  schema = @Schema(type = SchemaType.STRING),
                  description =
                      """
                        The regular expression of the mime types allowed to be saved in the repository.

                        Multiple regular expressions can be passed by separating them with a semicolon.

                        e.g. `image/.*;application/pdf`
                        """),
            },
            headers = {
              @Header(
                  name = "X-Total-Count",
                  description = "Total number of items matching criteria",
                  schema = @Schema(type = SchemaType.INTEGER, format = "int32", example = "42")),
              @Header(
                  name = "X-Result-Count",
                  description = "Actual number of items returned",
                  schema = @Schema(type = SchemaType.INTEGER, format = "int32", example = "42")),
            },
            responses = {
              @APIResponse(
                  name = "error-400",
                  responseCode = "400",
                  description = "Bad Request",
                  content =
                      @Content(
                          schema = @Schema(ref = "Error"),
                          mediaType = MediaType.APPLICATION_JSON)),
              @APIResponse(
                  name = "error-404",
                  responseCode = "404",
                  description = "Not Found",
                  content =
                      @Content(
                          schema = @Schema(ref = "Error"),
                          mediaType = MediaType.APPLICATION_JSON)),
              @APIResponse(
                  name = "error-500",
                  responseCode = "500",
                  description = "Internal server error",
                  content =
                      @Content(
                          schema = @Schema(ref = "Error"),
                          mediaType = MediaType.APPLICATION_JSON)),
              @APIResponse(
                  name = "error-501",
                  responseCode = "501",
                  description = "Not Implemented",
                  content =
                      @Content(
                          schema = @Schema(ref = "Error"),
                          mediaType = MediaType.APPLICATION_JSON)),
              @APIResponse(
                  name = "error-502",
                  responseCode = "502",
                  description = "Bad Gateway",
                  content =
                      @Content(
                          schema = @Schema(ref = "Error"),
                          mediaType = MediaType.APPLICATION_JSON)),
              @APIResponse(
                  name = "error-503",
                  responseCode = "503",
                  description = "Service Unavailable",
                  content =
                      @Content(
                          schema = @Schema(ref = "Error"),
                          mediaType = MediaType.APPLICATION_JSON)),
            }),
    info =
        @Info(
            title = "POC TMF in a proxified architecture",
            version = "1.0.0",
            description =
                """
                This POC demonstrates how to use the TMF API like *Trouble Ticket* and *Attachment* in a proxified architecture.

                All ARCEP applications are designed to be used in a proxified architecture
                and all data are stored in a TMF repository.

                For this POC, we have implemented a proxy for the *Malfaçon* and *Anomalie Adresse* TroubleTicket types.

                > **Note:** This API is a POC and is not intended to be used in production.

                ## Target

                This APIs should prove that the TroubleTicket proxy is working as expected with the following features:

                * TroubleTicket creation with standard fields
                * Validation of TroubleTicket standard fields
                * Allow full customization of the the TroubleTicket API by accepting custom fields
                * TroubleTicket creation with custom fields for specific TroubleTicket types
                * Validation of TroubleTicket custom fields for specific TroubleTicket types
                * Secure access to the data limited to the TroubleTicket type
                * Allow access in read-only mode to the data for all TroubleTicket types for an administrator
                * Allow filtering of the data on custom fields

                #### TODO

                * Implement the following methods to manage the TroubleTickets:
                  * **PUT** to update a TroubleTicket
                  * **PATCH** to update a TroubleTicket partially
                * Replace the `X-Client-Id` header by a JWT token

                ## Architecture schema

                In the following schema:

                * The *Malfacon Proxy* and the *Annomalie Adresse Proxy*
                  are two examples of proxies that can be used to manage the TroubleTickets with a custom business logic.
                * The *Admin Proxy* is an example of proxy that can be used to manage the TroubleTickets with a read-only access.
                  It can be used by an administrator to read all the TroubleTickets without limitations of type.
                * All the data are stored in a centralized *MongoDB* database.
                * The security logic is based on 2 levels:
                  * A user is allowed to access the data of a TroubleTicket type
                    if he has the right to access the proxy that manages the TroubleTickets of this type.
                  * The data of a TroubleTicket type are separated by domain which is defined by the `X-Client-Id` header.
                    A user is allowed to access the data of a TroubleTicket type if he has the right to access the domain
                    defined by the `X-Client-Id` header.

                ![Schema Architecture](/schema/architecture.drawio.svg)

                ## Event Processing

                Each proxy can send to *Zbus* a message that contains all the information needed to read a TroubleTicket.

                But the *TroubleTicket API* can send to *Zbus* a message that contains only the minimal information
                needed to read a TroubleTicket to ensure the security of the data:

                ```json
                {
                    "id": "a8151c29-0390-4ef0-b754-6715b7e49b5a",
                    "creationDate": "2021-01-01T00:00:00Z",
                    "lastUpdate": "2021-01-01T00:00:00Z",
                    "@baseType": "TroubleTicket",
                    "@type": "AdminTroubleTicket"
                }
                ```

                **Note:** *Zbus* can be replaced by another message broker.

                #### Example of event processing from the *Malfacon Proxy*:

                This architecture is useful to:

                * Reduce Input/Output operations
                * Reduce the load average of the *TroubleTicket API*
                * External client with specific business logic based on the parent TroubleTicket

                ![Schema Architecture](/schema/event-proxy.drawio.svg)

                ### Example of event processing from the *TroubleTicket API*:

                This architecture is useful to:

                * External client with multiple business logic based on TroubleTicket

                ![Schema Architecture](/schema/event-tool.drawio.svg)

                **Warning:** In the preview example, we can see that
                if an external application use an *Admin Proxy* to read the data, you will lost the confidentiality of the data.

                """))
public class OpenAPI extends Application {

  public static final String DOC_OPERATION_LIST_DESC =
      """
            * If you use the `application/json` media type, the response will be a paginated list.
            * If you use the `text/event-stream` media type, the response will be a stream.

            Note that the stream way does not support pagination. So the `limit` and `offset` query parameters will be ignored.

            > **TIP**:
            >
            >   You can set the query parameter `limit` to `0` to disable pagination and retrieve easily the number of entity.
            >
            >   But you can also use the `HEAD` method to retrieve the number of entity.

            <br>

            <hr>The following diagram shows the process to retrieve the list of elements:

            ![schema list](/schema/tt-list.drawio.svg)

            """;

  public static final String DOC_OPERATION_COUNT_DESC =
      """
            The following diagram shows the process to count the number of elements:

            ![schema list](/schema/tt-list.drawio.svg)

            """;

  public static final String DOC_OPERATION_POST_DESC =
      """
            This endpoint will validate all the custom fields.

            If there is no validation error, the request will be forwarded to the TMF API.

            <br>

            <hr>The following diagram shows the sequence diagram of the creation of a element:

            ![schema post](/schema/tt-post.drawio.svg)
            """;

  public static final String DOC_OPERATION_GET_DESC =
      """
            This endpoint will retrieve an element from the TMF API.

            <br>

            <hr>The following diagram shows the process to retrieve the an element:

            ![schema list](/schema/tt-list.drawio.svg)
            """;

  public static final String DOC_OPERATION_DELETE_DESC =
      """
            This endpoint will delete an element from the TMF API.

            <br>

            <hr>The following diagram shows the process to delete an element:

            ![schema list](/schema/tt-list.drawio.svg)
            """;

  public static final String DOC_EXAMPLE_STREAM =
      """
            data:{"id":"987b8913-f7df-4af0-9bcd-c593d1d43497","creationDate":"2023-04-10T07:22:24.510+00:00","lastUpdate":null,"resolutionDate":null,"expectedResolutionDate":null,"name":"My new ticket","description":"string","priority":"string","severity":"string","externalId":"string","domain":"malfacon","@baseType":null,"@type":"MalfaconTroubleTicket","@schemaLocation":null,"closed":false,"mafalcon":null}
            data:{"id":"c6ee50cd-e733-4e0d-bcde-17f049ca8241","creationDate":"2023-04-10T07:22:31.142+00:00","lastUpdate":null,"resolutionDate":null,"expectedResolutionDate":null,"name":"My new ticket","description":"string","priority":"string","severity":"string","externalId":"string","domain":"malfacon","@baseType":null,"@type":"MalfaconTroubleTicket","@schemaLocation":null,"closed":false,"mafalcon":null}
            data:{"id":"6134b79d-d453-4352-a28e-94b7defa8469","creationDate":"2023-04-10T07:23:38.652+00:00","lastUpdate":null,"resolutionDate":null,"expectedResolutionDate":null,"name":"My new ticket","description":"string","priority":"string","severity":"string","externalId":"string","domain":"malfacon","@baseType":null,"@type":"MalfaconTroubleTicket","@schemaLocation":null,"closed":false,"mafalcon":{"volumetrie":3,"localisation":"string","defaut":"string","quotePart":0}}
            """;
}
