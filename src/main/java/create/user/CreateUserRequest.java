package create.user;

import io.micronaut.core.annotation.*;

@Introspected
public class CreateUserRequest {
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}

