package create.user;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class CreateUserFunctionTest {
	@Inject
	CreateUserClient client;

	@Test
	public void testFunction() {
		CreateUser body = new CreateUser();
		body.setName("create-user");
		assertEquals("create-user", client.apply(body).blockingGet().getName());
	}
}
