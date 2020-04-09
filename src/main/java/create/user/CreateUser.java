package create.user;

import io.micronaut.core.annotation.*;

@Introspected
public class CreateUser {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

