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

            .append("}}")
            .toString().formatted(applicationTitle);

        return docs;

    }

}