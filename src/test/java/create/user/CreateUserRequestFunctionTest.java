package create.user;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class CreateUserRequestFunctionTest {
	@Inject
	CreateUserClient client;

	@Test
	public void testFunction() {
		CreateUserRequest body = new CreateUserRequest();
		body.setUsername("username2");
		assertEquals("Success", client.apply(body).blockingGet().getUsername());
	}
}
