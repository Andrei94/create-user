package create.user;

import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.function.FunctionBean;

import javax.inject.*;
import java.io.IOException;
import java.util.function.Function;

@FunctionBean("create-user")
public class CreateUserFunction extends FunctionInitializer implements Function<CreateUser, CreateUser> {
	@Override
	public CreateUser apply(CreateUser msg) {
		return msg;
	}

	/**
	 * This main method allows running the function as a CLI application using: echo '{}' | java -jar function.jar
	 * where the argument to echo is the JSON to be parsed.
	 */
	public static void main(String... args) throws IOException {
		CreateUserFunction function = new CreateUserFunction();
		function.run(args, (context) -> function.apply(context.get(CreateUser.class)));
	}
}

