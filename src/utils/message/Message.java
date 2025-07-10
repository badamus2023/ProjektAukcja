package utils.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message<T> {
    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    private MessageType type;
    @JsonProperty("data")
    private T message;

    public Message(MessageType type, T message) {
        this.type = type;
        this.message = message;
    }

    public Message() {
    }

    public String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(this);
    }

    public static <T> Message<T> deserialize(String serializedMessage, TypeReference<Message<T>> typeRef) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(serializedMessage, typeRef);
    }
}