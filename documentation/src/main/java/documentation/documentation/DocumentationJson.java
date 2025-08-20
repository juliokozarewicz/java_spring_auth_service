package documentation.documentation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentationJson {

    @Value("${APPLICATION_TITLE}")
    private String applicationTitle;

    public String documentationText() {

        String docs = new StringBuilder()

            .append(
                """
                {
                "openapi":"3.0.0",
                "info": {
                    "title": "%s",
                    "version": "1.0",
                    "description": "*** APPLICATION DESCRIPTION *** \\n\\n ## Localization (Translation) \\n\\n Any response containing the \\"message\\" field in the body will have its message translated server-side, based on the language specified in the request header, for the supported languages. \\n\\n ## Common responses from services \\n\\n **Authentication Error (401):** If the user is not authenticated (e.g., missing or invalid token), the response will be: \\n\\n ```json\\n{\\n    \\"status\\" : 401, \\n    \\"statusMessage\\" : \\"error\\", \\n    \\"message\\" : \\"Invalid credentials.\\" \\n }\\n``` \\n\\n **Form field validation error (422):** If there are validation errors in the form fields, the response will include the fields and their respective error messages: \\n\\n ```json\\n{ \\n    \\"statusCode\\": 422, \\n    \\"statusMessage\\": \\"error\\", \\n    \\"fieldErrors\\": [ \\n        { \\n            \\"field\\": \\"field name\\", \\n            \\"message\\": \\"This field is required.\\" \\n        }, \\n        { \\n            \\"field\\": \\"field name\\", \\n            \\"message\\": \\"This field is required.\\" \\n        }\\n    ] \\n }\\n``` \\n\\n **Bad request (400):** If the request is malformed or invalid, the response will be: \\n\\n ```json\\n{ \\n    \\"statusCode\\": 400, \\n    \\"statusMessage\\": \\"error\\", \\n    \\"message\\": \\"The request has an error, check.\\" \\n}\\n``` \\n ## API Gateway Errors (No translation support) \\n\\n **Service Unavailable (503):** The service is temporarily unavailable, often due to maintenance or overload. The response will include the error details: \\n\\n ```json\\n{ \\n    \\"statusCode\\": 503, \\n    \\"statusMessage\\": \\"error\\", \\n    \\"detail\\": \\"Service Unavailable (API Gateway)\\" \\n}\\n``` \\n\\n **Rate limit exceeded (429):** If the user exceeds the allowed number of requests, the response will be: \\n\\n ```json\\n{ \\n    \\"statusCode\\": 429, \\n    \\"statusMessage\\": \\"error\\", \\n    \\"detail\\": \\"Access blocked by rate limiter (API Gateway)\\" \\n}\\n``` \\n\\n **Bad request (400):** If the request is malformed or invalid, the response will be: \\n\\n ```json\\n{ \\n    \\"statusCode\\": 400, \\n    \\"statusMessage\\": \\"error\\", \\n    \\"detail\\": \\"Bad request (API Gateway)\\" \\n}\\n``` \\n\\n **Internal server error (500):** If there's an unexpected condition preventing the server from fulfilling the request, the response will be: \\n\\n ```json\\n{ \\n    \\"statusCode\\": 500, \\n    \\"statusMessage\\": \\"error\\", \\n    \\"detail\\": \\"Server error (API Gateway)\\" \\n}\\n```"
                },
                "components":{
                    "securitySchemes":{
                        "BearerAuth":{
                            "type":"http",
                            "scheme":"bearer",
                            "bearerFormat":"JWT"
                        }
                    }
                },
                "paths":{
                """
            )

            .append(
                """
                # HELLOWORLD
                # ==========================================================
                "/helloworld/helloworld":{
                    "get":{
                        "summary":"Get hello world message",
                        "description":"Retrieves a hello world message. You can optionally provide a custom message via query parameter.",
                        "tags":[
                            "HELLO WORLD"
                        ],
                        "parameters":[
                            {
                                "name":"message",
                                "in":"query",
                                "required":false,
                                "description":"Custom message to be returned. Defaults to 'Hello World!' if not provided.",
                                "schema":{
                                    "type":"string",
                                    "example":"Hello from the API!"
                                }
                            }
                        ],
                        "responses":{
                            "200":{
                                "description":"Successful response with hello world message.",
                                "content":{
                                    "application/json":{
                                        "schema":{
                                            "type":"object",
                                            "properties":{
                                                "statusCode":{
                                                    "type":"integer",
                                                    "example":200
                                                },
                                                "statusMessage":{
                                                    "type":"string",
                                                    "example":"success"
                                                },
                                                "message":{
                                                    "type":"string",
                                                    "example":"Data received successfully. (Hello World!)"
                                                },
                                                "links":{
                                                    "type":"object",
                                                    "properties":{
                                                        "self":{
                                                            "type":"string",
                                                            "example":"/helloworld/helloworld"
                                                        },
                                                        "next":{
                                                            "type":"string",
                                                            "example":"/documentation/swagger"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                """
            )

            .append(
                """
                # ACCOUNTS
                # ==========================================================
                "/accounts/signup": {
                    "post": {
                        "summary": "Create a new user account",
                        "description": "Creates a new user account with the provided details such as name, email, password, and link. An activation email is sent after account creation.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "name": {
                                                "type": "string",
                                                "description": "The name of the user.",
                                                "example": "My Name"
                                            },
                                            "email": {
                                                "type": "string",
                                                "description": "The email address of the user. Must be a valid email format.",
                                                "example": "Email@hotmail.com"
                                            },
                                            "password": {
                                                "type": "string",
                                                "description": "The password for the new account. Must contain at least one uppercase letter, one number, and one special character.",
                                                "example": "Teste1234!"
                                            },
                                            "link": {
                                                "type": "string",
                                                "description": "A valid URL that will be included in the activation email.",
                                                "example": "https://upload.wikimedia.org/wikipedia/commons/b/b1/Loading_icon.gif"
                                            }
                                        },
                                        "required": [
                                            "name",
                                            "email",
                                            "password",
                                            "link"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "201": {
                                "description": "Account successfully created and activation email sent.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 201
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Account created successfully, please activate your account through the link sent to your email."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/signup"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/activate-email"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/activate-account": {
                    "post": {
                        "summary": "Activate a user account",
                        "description": "Activates a user account using an email and token. The token must be valid and match the email provided.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "email": {
                                                "type": "string",
                                                "description": "The email address associated with the account.",
                                                "example": "Email@hotmail.com"
                                            },
                                            "token": {
                                                "type": "string",
                                                "description": "The activation token sent to the user's email.",
                                                "example": "abcd1234efgh5678"
                                            }
                                        },
                                        "required": [
                                            "email",
                                            "token"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Account successfully activated.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Your account has been successfully activated."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/activate-email"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/login"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "404": {
                                "description": "Error occurred while trying to activate the account.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 404
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "An error occurred while trying to activate your account, please try the process again."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/update-password-link": {
                    "post": {
                        "summary": "Send password update link to the user",
                        "description": "Sends an email to the user with a password update link. The email must be valid and the link must be properly formatted.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "email": {
                                                "type": "string",
                                                "description": "The email address of the user requesting the password update.",
                                                "example": "user@example.com"
                                            },
                                            "link": {
                                                "type": "string",
                                                "description": "The URL to which the user will be redirected to update the password.",
                                                "example": "https://example.com/update-password"
                                            }
                                        },
                                        "required": [
                                            "email",
                                            "link"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Password update link sent successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Change your password by clicking the link sent to your email."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/update-password-link"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/update-password"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/update-password": {
                    "patch": {
                        "summary": "Update user password",
                        "description": "This endpoint allows the user to update their password. The user must provide a valid email, token, and a new password. The token should correspond to the one generated for password update purposes. If successful, the password will be updated.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "email": {
                                                "type": "string",
                                                "description": "The email address of the user requesting the password update.",
                                                "example": "user@example.com"
                                            },
                                            "password": {
                                                "type": "string",
                                                "description": "The new password that the user wants to set.",
                                                "example": "NewPassword123!"
                                            },
                                            "token": {
                                                "type": "string",
                                                "description": "The token associated with the password reset process.",
                                                "example": "abc123xyz"
                                            }
                                        },
                                        "required": [
                                            "email",
                                            "password",
                                            "token"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Password updated successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Password changed successfully. Please log in to continue."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/update-password"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/login"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "404": {
                                "description": "An error occurred while changing your password.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 404
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "An error occurred while changing your password. Please try the process again."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/login": {
                    "post": {
                        "summary": "Authenticate user login",
                        "description": "This endpoint allows users to log in using their email and password. If the credentials are valid and the account is active, access and refresh tokens are returned. Otherwise, an appropriate error is returned based on the account status.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "email": {
                                                "type": "string",
                                                "description": "The email address of the user attempting to log in.",
                                                "example": "user@example.com"
                                            },
                                            "password": {
                                                "type": "string",
                                                "description": "The password associated with the user's account.",
                                                "example": "SecurePass123!"
                                            }
                                        },
                                        "required": [
                                            "email",
                                            "password"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "User logged in successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "You are logged in."
                                                },
                                                "data": {
                                                    "type": "object",
                                                    "properties": {
                                                        "access": {
                                                            "type": "string",
                                                            "example": "ACCESS_TOKEN_STRING"
                                                        },
                                                        "refresh": {
                                                            "type": "string",
                                                            "example": "REFRESH_TOKEN_STRING"
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/login"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/get-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "403": {
                                "description": "Account is banned or deactivated.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 403
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "We couldn't complete your login. More information has been sent to your email."
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "404": {
                                "description": "User not found or credentials are incorrect.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 404
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Invalid credentials."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/refresh-login": {
                    "post": {
                        "summary": "Refresh user access credentials",
                        "description": "This endpoint allows users to obtain new access and refresh tokens by providing a valid refresh token. If the token is valid and associated with an active account, the system generates new tokens. Errors are returned if the token is invalid, the account is banned, or the account is deactivated.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "refreshToken": {
                                                "type": "string",
                                                "description": "The base64-encoded refresh token issued during the last login.",
                                                "example": "REFRESH_TOKEN_STRING"
                                            }
                                        },
                                        "required": [
                                            "refreshToken"
                                        ]
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "New tokens issued successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "You are logged in."
                                                },
                                                "data": {
                                                    "type": "object",
                                                    "properties": {
                                                        "access": {
                                                            "type": "string",
                                                            "example": "ACCESS_TOKEN_STRING"
                                                        },
                                                        "refresh": {
                                                            "type": "string",
                                                            "example": "REFRESH_TOKEN_STRING"
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/refresh-login"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/get-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "403": {
                                "description": "Account is banned or deactivated.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 403
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "We couldn't complete your login. More information has been sent to your email."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/get-profile": {
                    "get": {
                        "summary": "Retrieve user profile information",
                        "description": "This endpoint returns the authenticated user's profile information, including personal details and language preferences. The request must include a valid Bearer access token in the Authorization header.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "responses": {
                            "200": {
                                "description": "Profile retrieved successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "data": {
                                                    "type": "object",
                                                    "properties": {
                                                        "profileImage": {
                                                            "type": "string",
                                                            "example": "https://example.com/image.jpg"
                                                        },
                                                        "name": {
                                                            "type": "string",
                                                            "example": "John Doe"
                                                        },
                                                        "email": {
                                                            "type": "string",
                                                            "example": "john.doe@example.com"
                                                        },
                                                        "phone": {
                                                            "type": "string",
                                                            "example": "+123456789"
                                                        },
                                                        "identityDocument": {
                                                            "type": "string",
                                                            "example": "1234567890"
                                                        },
                                                        "gender": {
                                                            "type": "string",
                                                            "example": "male"
                                                        },
                                                        "birthdate": {
                                                            "type": "string",
                                                            "example": "1990-01-01"
                                                        },
                                                        "biography": {
                                                            "type": "string",
                                                            "example": "I'm John Doe. I live everywhere. I am anyone."
                                                        },
                                                        "language": {
                                                            "type": "string",
                                                            "example": "en"
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/get-profile"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/update-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/update-profile": {
                    "put": {
                        "summary": "Update user profile information",
                        "description": "This endpoint allows authenticated users to update their profile details such as name, phone, identity document, gender, birthdate, biography, and language. The fields are not required, but if provided in the request body, they must contain valid non-empty values. A valid Bearer token must be provided in the Authorization header.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "properties": {
                                            "name": {
                                                "type": "string",
                                                "maxLength": 255,
                                                "description": "Full name of the user.",
                                                "example": "John Doe"
                                            },
                                            "phone": {
                                                "type": "string",
                                                "maxLength": 25,
                                                "description": "User's contact number.",
                                                "example": "+1 (123) 456-7890"
                                            },
                                            "identityDocument": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Government-issued identity document number.",
                                                "example": "AB1234567"
                                            },
                                            "gender": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Gender identification.",
                                                "example": "male"
                                            },
                                            "birthdate": {
                                                "type": "string",
                                                "description": "Date of birth in YYYY-MM-DD format.",
                                                "example": "1990-05-15"
                                            },
                                            "biography": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Short biography of the user.",
                                                "example": "A passionate developer who loves open-source."
                                            },
                                            "language": {
                                                "type": "string",
                                                "maxLength": 50,
                                                "description": "Language code in ISO 639-1 format (e.g., en, pt-BR).",
                                                "example": "en"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Profile updated successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Profile updated successfully."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/update-profile"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/get-profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/create-address": {
                    "post": {
                        "summary": "Create a new address for the user",
                        "description": "This endpoint allows authenticated users to create a new address entry. The user can provide various address details including address name, zip code, street, neighborhood, city, state, country, and more. The address will be associated with the user, and if the address is marked as 'primary', any existing primary address will be set to 'secondary'. The user can store up to 5 addresses.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "required": [
                                            "addressName",
                                            "zipCode",
                                            "street",
                                            "number",
                                            "neighborhood",
                                            "city",
                                            "state",
                                            "country",
                                            "addressType",
                                            "isPrimary"
                                        ],
                                        "properties": {
                                            "addressName": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Name of the address (e.g., 'home', 'work').",
                                                "example": "home"
                                            },
                                            "zipCode": {
                                                "type": "string",
                                                "maxLength": 50,
                                                "description": "Zip code of the address.",
                                                "example": "90210"
                                            },
                                            "street": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Street name of the address.",
                                                "example": "Sunset Boulevard"
                                            },
                                            "number": {
                                                "type": "string",
                                                "maxLength": 50,
                                                "description": "Number or identifier for the address (e.g., '123', 'Apt 4B').",
                                                "example": "123"
                                            },
                                            "addressLineTwo": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Additional address line (e.g., 'Apt 4B').",
                                                "example": "Apt 4B"
                                            },
                                            "neighborhood": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Neighborhood or district of the address.",
                                                "example": "Beverly Hills"
                                            },
                                            "city": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "City of the address.",
                                                "example": "Los Angeles"
                                            },
                                            "state": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "State of the address.",
                                                "example": "California"
                                            },
                                            "country": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Country of the address.",
                                                "example": "USA"
                                            },
                                            "addressType": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Type of the address (e.g., 'Home', 'Work').",
                                                "example": "Home"
                                            },
                                            "isPrimary": {
                                                "type": "boolean",
                                                "description": "Flag indicating if the address is the primary one.",
                                                "example": true
                                            },
                                            "landmark": {
                                                "type": "string",
                                                "maxLength": 256,
                                                "description": "Landmark near the address (e.g., 'Near Rodeo Drive').",
                                                "example": "Near Rodeo Drive"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "responses": {
                            "201": {
                                "description": "Address created successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 201
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Address created successfully."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/create-address"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/get-address"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "409": {
                                "description": "Conflict in address creation. The address creation failed because the address already exists.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 409
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "This address has already been registered."
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "422": {
                                "description": "Unprocessable Entity. The user has reached the maximum limit of addresses allowed (5).",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 422
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "You can only keep up to five addresses registered."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/get-address": {
                    "get": {
                        "summary": "Retrieve all addresses associated with the user",
                        "description": "This endpoint allows authenticated users to retrieve a list of their saved addresses. It returns all the addresses associated with the user's account, including information such as street, number, city, state, country, and whether the address is marked as primary. Cached data may be used to improve performance. Users can maintain a maximum of five (5) addresses. It is not possible to edit existing addresses, they must be deleted and recreated if changes are needed.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "responses": {
                            "200": {
                                "description": "Addresses retrieved successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "data": {
                                                    "type": "array",
                                                    "items": {
                                                        "type": "object",
                                                        "properties": {
                                                            "addressId": {
                                                                "type": "string",
                                                                "example": "f53d0ad4-a4fe-456a-b586-c930025233ce"
                                                            },
                                                            "addressName": {
                                                                "type": "string",
                                                                "example": "work"
                                                            },
                                                            "zipCode": {
                                                                "type": "string",
                                                                "example": "90210"
                                                            },
                                                            "street": {
                                                                "type": "string",
                                                                "example": "Sunset Boulevard"
                                                            },
                                                            "number": {
                                                                "type": "string",
                                                                "example": "123"
                                                            },
                                                            "addressLineTwo": {
                                                                "type": "string",
                                                                "example": "Apt 4B"
                                                            },
                                                            "neighborhood": {
                                                                "type": "string",
                                                                "example": "Beverly Hills"
                                                            },
                                                            "city": {
                                                                "type": "string",
                                                                "example": "Los Angeles"
                                                            },
                                                            "state": {
                                                                "type": "string",
                                                                "example": "California"
                                                            },
                                                            "country": {
                                                                "type": "string",
                                                                "example": "USA"
                                                            },
                                                            "addressType": {
                                                                "type": "string",
                                                                "example": "Home"
                                                            },
                                                            "isPrimary": {
                                                                "type": "boolean",
                                                                "example": false
                                                            },
                                                            "landmark": {
                                                                "type": "string",
                                                                "example": "Near Rodeo Drive"
                                                            }
                                                        }
                                                    },
                                                    "example": [
                                                        {
                                                            "addressId": "f53d0ad4-a4fe-456a-b586-c930025233ce",
                                                            "addressName": "home",
                                                            "zipCode": "90210",
                                                            "street": "Sunset Boulevard",
                                                            "number": "123",
                                                            "addressLineTwo": "Apt 4B",
                                                            "neighborhood": "Beverly Hills",
                                                            "city": "Los Angeles",
                                                            "state": "California",
                                                            "country": "USA",
                                                            "addressType": "Home",
                                                            "isPrimary": false,
                                                            "landmark": "Near Rodeo Drive"
                                                        },
                                                        {
                                                            "addressId": "7ab67274-54c5-4625-8e9e-825749290c8e",
                                                            "addressName": "home",
                                                            "zipCode": "90210",
                                                            "street": "Sunset Boulevard",
                                                            "number": "123",
                                                            "addressLineTwo": "Apt 4B",
                                                            "neighborhood": "Beverly Hills",
                                                            "city": "Los Angeles",
                                                            "state": "California",
                                                            "country": "USA",
                                                            "addressType": "Home",
                                                            "isPrimary": true,
                                                            "landmark": "Near Rodeo Drive"
                                                        }
                                                    ]
                                                },
                                                "meta": {
                                                    "type": "object",
                                                    "properties": {
                                                        "totalItems": {
                                                            "type": "integer",
                                                            "example": 2
                                                        }
                                                    }
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/get-address"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/create-address"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/delete-address": {
                    "delete": {
                        "summary": "Delete an address associated with the user",
                        "description": "This endpoint allows authenticated users to delete an address from their account. The user must provide a valid address ID (UUID format). Only addresses that belong to the authenticated user can be deleted.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "required": [
                                            "addressId"
                                        ],
                                        "properties": {
                                            "addressId": {
                                                "type": "string",
                                                "format": "uuid",
                                                "description": "UUID of the address to be deleted.",
                                                "example": "262f621c-01c2-4837-9690-f93765b7a124"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Address deleted successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Address deleted successfully."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/delete-address"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/get-address"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "404": {
                                "description": "Address not found or does not belong to the user.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 404
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "error"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "Address not found."
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/update-email-link": {
                    "post": {
                        "summary": "Send verification link and PIN for email update",
                        "description": "This endpoint allows authenticated users to request an email change. A verification PIN will be sent to the new email address, and a verification link containing a token will be sent to the current email address. Both must be validated in order to complete the email update process.",
                        "tags": [
                            "ACCOUNTS"
                        ],
                        "security": [
                            {
                                "BearerAuth": []
                            }
                        ],
                        "requestBody": {
                            "required": true,
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "type": "object",
                                        "required": [
                                            "newEmail",
                                            "link"
                                        ],
                                        "properties": {
                                            "newEmail": {
                                                "type": "string",
                                                "format": "email",
                                                "description": "The new email address that the user wants to register.",
                                                "example": "email@email.com"
                                            },
                                            "link": {
                                                "type": "string",
                                                "format": "uri",
                                                "description": "Base URL where the user will be redirected to confirm the email update.",
                                                "example": "https://upload.wikimedia.org/wikipedia/commons/b/b1/Loading_icon.gif"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "responses": {
                            "200": {
                                "description": "Verification instructions sent successfully.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 200
                                                },
                                                "statusMessage": {
                                                    "type": "string",
                                                    "example": "success"
                                                },
                                                "message": {
                                                    "type": "string",
                                                    "example": "To change your email, click the link sent to your current address."
                                                },
                                                "links": {
                                                    "type": "object",
                                                    "properties": {
                                                        "self": {
                                                            "type": "string",
                                                            "example": "/accounts/update-email-link"
                                                        },
                                                        "next": {
                                                            "type": "string",
                                                            "example": "/accounts/update-email"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                # ==========================================================
                # ==========================================================
                """
            )

            .append("}}")
            .toString().formatted(applicationTitle);

        return docs;

    }

}