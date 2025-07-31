package email_management_service.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StandardResponseService {

    private int statusCode;
    private String statusMessage;
    private String field;
    private String message;
    private Object data;
    private Map<String, Object> meta;
    private Map<String, String> links;

    private StandardResponseService(Builder builder) {
        this.statusCode = builder.statusCode;
        this.statusMessage = builder.statusMessage;
        this.field = builder.field.isEmpty() ? null :
            builder.field;
        this.message = builder.message.isEmpty() ? null :
            builder.message;
        this.data = builder.data;
        this.meta = builder.meta.isEmpty() ? null : builder.meta;
        this.links = builder.links.isEmpty() ? null : builder.links;
    }

    public static class Builder {

        private int statusCode;
        private String statusMessage;
        private String field = "";
        private String message = "";
        private Object data;
        private Map<String, Object> meta = new HashMap<>();
        private Map<String, String> links = new HashMap<>();

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder meta(Map<String, Object> meta) {
            this.meta = meta;
            return this;
        }

        public Builder links(Map<String, String> links) {
            this.links = links;
            return this;
        }

        public StandardResponseService build() {
            return new StandardResponseService(this);
        }
    }
}