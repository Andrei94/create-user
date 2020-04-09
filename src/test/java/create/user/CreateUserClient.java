package create.user;

import io.micronaut.function.client.FunctionClient;
import io.micronaut.http.annotation.Body;
import io.reactivex.Single;

import javax.inject.Named;

@FunctionClient
public interface CreateUserClient {
	@Named("create-user")
	Single<CreateUser> apply(@Body CreateUser body);
}
