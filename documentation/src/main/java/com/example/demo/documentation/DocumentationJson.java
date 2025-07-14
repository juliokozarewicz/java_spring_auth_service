package com.example.demo.documentation;

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
                "info":{
                    "title":"%s",
                    "version":"1.0",
                    "description":"*** APPLICATION DESCRIPTION ***"
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
                            },
                            "422": {
                                "description": "Unprocessable Entity. Validation errors in the provided data.",
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
                                                "fieldErrors": {
                                                    "type": "array",
                                                    "items": {
                                                        "type": "object",
                                                        "properties": {
                                                            "field": {
                                                                "type": "string",
                                                                "example": "name"
                                                            },
                                                            "message": {
                                                                "type": "string",
                                                                "example": "This field is required."
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            "example": {
                                                "statusCode": 422,
                                                "statusMessage": "error",
                                                "fieldErrors": [
                                                    {
                                                        "field": "name",
                                                        "message": "This field is required."
                                                    },
                                                    {
                                                        "field": "email",
                                                        "message": "This field is required."
                                                    },
                                                    {
                                                        "field": "password",
                                                        "message": "This field is required."
                                                    },
                                                    {
                                                        "field": "link",
                                                        "message": "This field is required."
                                                    }
                                                ]
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
                                                            "example": "/accounts/profile"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "401": {
                                "description": "Invalid credentials provided.",
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "properties": {
                                                "statusCode": {
                                                    "type": "integer",
                                                    "example": 401
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
                                        "example": {
                                            "statusCode": 200,
                                            "statusMessage": "success",
                                            "message": "You are logged in.",
                                            "data": {
                                                "access": "ACCESS_TOKEN_STRING",
                                                "refresh": "REFRESH_TOKEN_STRING"
                                            },
                                            "links": {
                                                "self": "/accounts/refresh-login",
                                                "next": "/accounts/profile"
                                            }
                                        }
                                    }
                                }
                            },
                            "422": {
                                "description": "Unprocessable Entity. Validation error due to bad or missing refreshToken field.",
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
                                                "fieldErrors": {
                                                    "type": "array",
                                                    "items": {
                                                        "type": "object",
                                                        "properties": {
                                                            "field": {
                                                                "type": "string",
                                                                "example": "refreshToken"
                                                            },
                                                            "message": {
                                                                "type": "string",
                                                                "example": "This field is required."
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            "example": {
                                                "statusCode": 422,
                                                "statusMessage": "error",
                                                "fieldErrors": [
                                                    {
                                                        "field": "refreshToken",
                                                        "message": "This field is required."
                                                    }
                                                ]
                                            }
                                        }
                                    }
                                }
                            },
                            "403": {
                                "description": "Account is banned or deactivated.",
                                "content": {
                                    "application/json": {
                                        "example": {
                                            "statusCode": 403,
                                            "statusMessage": "error",
                                            "message": "We couldn't complete your login. More information has been sent to your email."
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                "/accounts/profile": {
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
                                        "example": {
                                            "statusCode": 200,
                                            "statusMessage": "success",
                                            "data": {
                                                "profileImage": "https://example.com/image.jpg",
                                                "name": "John Doe",
                                                "email": "john.doe@example.com",
                                                "phone": "+123456789",
                                                "identityDocument": "1234567890",
                                                "gender": "male",
                                                "birthdate": "1990-01-01",
                                                "biography": "I'm John Doe. I live everywhere. I am anyone.",
                                                "language": "en",
                                            },
                                            "links": {
                                                "self": "/accounts/profile",
                                                "next": "/accounts/profile-update"
                                            }
                                        }
                                    }
                                }
                            },
                            "401": {
                                "description": "Unauthorized - Missing or invalid access token.",
                                "content": {
                                    "application/json": {
                                        "example": {
                                            "statusCode": 401,
                                            "statusMessage": "error",
                                            "message": "Invalid credentials."
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                # ==========================================================
                    "/accounts/profile-update": {
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
                                            "example": {
                                                "statusCode": 200,
                                                "statusMessage": "success",
                                                "message": "Profile updated successfully.",
                                                "links": {
                                                    "self": "/accounts/profile-update",
                                                    "next": "/accounts/profile"
                                                }
                                            }
                                        }
                                    }
                                },
                                "422": {
                                    "description": "Unprocessable Entity. One or more fields failed validation.",
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
                                                    "fieldErrors": {
                                                        "type": "array",
                                                        "items": {
                                                            "type": "object",
                                                            "properties": {
                                                                "field": {
                                                                    "type": "string",
                                                                    "example": "biography"
                                                                },
                                                                "message": {
                                                                    "type": "string",
                                                                    "example": "This field cannot be empty."
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                "example": {
                                                    "statusCode": 422,
                                                    "statusMessage": "error",
                                                    "fieldErrors": [
                                                        {
                                                            "field": "biography",
                                                            "message": "This field cannot be empty."
                                                        },
                                                        {
                                                            "field": "language",
                                                            "message": "This field cannot be empty."
                                                        }
                                                    ]
                                                }
                                            }
                                        }
                                    }
                                },
                                "401": {
                                    "description": "Unauthorized - Missing or invalid access token.",
                                    "content": {
                                        "application/json": {
                                            "example": {
                                                "statusCode": 401,
                                                "statusMessage": "error",
                                                "message": "Invalid credentials."
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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