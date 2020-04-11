package create.user;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class CreateUserResponse {
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
