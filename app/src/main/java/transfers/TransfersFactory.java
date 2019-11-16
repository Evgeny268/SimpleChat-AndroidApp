package transfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class TransfersFactory {

    public static Transfers createTransfers(String data){
        ObjectNode node = null;
        try {
            node = new ObjectMapper().readValue(data,ObjectNode.class);
        } catch (IOException e) {
            return null;
        }
        if (node.has("type")){
            ObjectMapper objectMapper = new ObjectMapper();
            if (node.get("type").asText().equals("."+TransferRequestAnswer.class.getSimpleName())){
                try{
                    TransferRequestAnswer transferRequestAnswer = (TransferRequestAnswer)objectMapper.readValue(data,TransferRequestAnswer.class);
                    return transferRequestAnswer;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+Friends.class.getSimpleName())){
                try {
                    Friends friends = (Friends)objectMapper.readValue(data,Friends.class);
                    return friends;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+Message.class.getSimpleName())){
                try {
                    Message message = (Message)objectMapper.readValue(data,Message.class);
                    return message;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+Messages.class.getSimpleName())){
                try {
                    Messages messages = (Messages)objectMapper.readValue(data,Messages.class);
                    return messages;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+RequestIn.class.getSimpleName())){
                try {
                    RequestIn requestIn = (RequestIn) objectMapper.readValue(data,RequestIn.class);
                    return requestIn;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+User.class.getSimpleName())){
                try {
                    User user = (User) objectMapper.readValue(data,User.class);
                    return user;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }else {
                return null;
            }
        }else{
            return null;
        }
    }
}
