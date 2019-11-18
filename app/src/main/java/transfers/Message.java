package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Message implements Transfers, Comparable{

    public int iduser = 0;
    public int idMessage;
    public String login;
    public String password;
    public int id_to;
    public Date date;
    public String text;

    public Message() {
    }

    public Message(String login, String password, int id_to, String text) {
        this.login = login;
        this.password = password;
        this.id_to = id_to;
        this.text = text;
    }

    public Message(int idMessage, int iduser, int id_to, Date date, String text) {
        this.idMessage = idMessage;
        this.iduser = iduser;
        this.id_to = id_to;
        this.date = date;
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return iduser == message.iduser &&
                idMessage == message.idMessage &&
                id_to == message.id_to &&
                date.equals(message.date) &&
                text.equals(message.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMessage);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof Message){
            Message message = (Message) o;
            return this.date.compareTo(message.date);
        }
        return 0;
    }
}
