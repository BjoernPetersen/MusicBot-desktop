package net.bjoernpetersen.deskbot.rest

import io.vertx.core.http.HttpServerResponse
import net.bjoernpetersen.deskbot.rest.model.AuthExpectation

/**
 * An HTTP response status code.
 * @param code the actual HTTP code
 */
enum class Status(val code: Int) {

    // Success
    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NO_CONTENT(204),

    // Client error
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    UNPROCESSABLE_ENTITY(422),

    // Server error
    INTERNAL_SERVER_ERROR(500)
}

/**
 * Fluent convenience method to set the response status with a [Status] object.
 * @param status a HTTP status
 * @return the same response object
 */
fun HttpServerResponse.setStatus(status: Status) = this.apply {
    statusCode = status.code
}

/**
 * An Exception with an associated HTTP status to return.
 */
open class StatusException : Exception {

    val status: Status
    val body: Any?

    /**
     * Creates a new instance.
     * @param status an HTTP status
     * @param body a response body, or null
     */
    constructor(status: Status, body: Any? = null) : super("Failed with status: $status") {
        this.status = status
        this.body = body
    }

    /**
     * Creates a new instance.
     * @param status an HTTP status
     * @param cause an Exception that caused this failure
     * @param body a response body, or null
     */
    constructor(status: Status, cause: Throwable, body: Any? = null) :
        super("Failed with status $status", cause) {
        this.status = status
        this.body = body
    }
}

/**
 * A client error indicating that a non-existent resource was requested.
 */
class NotFoundException : StatusException(Status.NOT_FOUND)

/**
 * A client error indicating that an endpoint was called with insufficient auth.
 * @param status either [Status.UNAUTHORIZED] or [Status.FORBIDDEN]
 * @param authExpectation an instance of [AuthExpectation] indicating the expected
 * authentication/authorization the client failed to meet.
 */
class AuthException(status: Status, authExpectation: AuthExpectation) :
    StatusException(status, authExpectation)

/**
 * A generic internal server error, returns [Status.INTERNAL_SERVER_ERROR].
 */
class InternalServerError : StatusException {

    /**
     * Creates a new instance.
     */
    constructor() : super(Status.INTERNAL_SERVER_ERROR)

    /**
     * Creates a new instance.
     * @param cause an Exception that caused this failure
     */
    constructor(cause: Throwable) : super(Status.INTERNAL_SERVER_ERROR, cause = cause)
}
