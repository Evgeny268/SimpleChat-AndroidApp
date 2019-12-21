package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class TokenAccess implements Transfers {
    public String login;
    public String password;
    public String cloud;

    public TokenAccess() {
    }

    public TokenAccess(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public TokenAccess(String login, String password, String cloud) {
        this.login = login;
        this.password = password;
        this.cloud = cloud;
    }

    public TokenAccess(String cloud) {
        this.cloud = cloud;
    }
}
