package create.user;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.function.FunctionBean;
import okhttp3.*;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionBean("create-user")
public class CreateUserFunction extends FunctionInitializer implements Function<CreateUserRequest, CreateUserResponse> {
	private final Logger logger = LoggerFactory.getLogger(CreateUserFunction.class);
	private OkHttpClient httpClient;

	@Override
	public CreateUserResponse apply(CreateUserRequest request) {
		HandshakeCertificates certificates = new HandshakeCertificates.Builder()
				.addTrustedCertificate(getBackedupCertificateAuthority())
				.build();

		httpClient = new OkHttpClient.Builder()
				.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
				.build();
		List<String> ipAddresses = getIpAddresses();
		String leastUsedInstanceIp = findLeastUsedInstanceIp(getFreeDevicesCountInEndpoints(ipAddresses));
		CreateUserResponse createUserResponse = new CreateUserResponse();
		try {
			String url = "https://" + leastUsedInstanceIp + ":8443" + "/volume/createUser/" + request.getUsername();
			Response response = httpClient.newCall(new Request.Builder().url(url)
					.put(RequestBody.create("", MediaType.parse("application/json; charset=utf-8"))).build())
					.execute();
			String token = Objects.requireNonNull(response.body()).string();
			createUserResponse.setToken(token);
		} catch(IOException e) {
			logger.error("An error occurred", e);
		}
		return createUserResponse;
	}

	private X509Certificate getBackedupCertificateAuthority() {
		try {
			return Certificates.decodeCertificatePem(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("backedup.pem").toURI()))));
		} catch(IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Failed to load certificate");
	}

	private List<String> getIpAddresses() {
		AmazonEC2 client = AmazonEC2ClientBuilder.defaultClient();
		DescribeInstancesResult backedupInstances = client.describeInstances(new DescribeInstancesRequest().
				withFilters(new Filter("tag:project", Collections.singletonList("backedup")),
						new Filter("instance-state-name", Collections.singletonList("running"))));
		client.shutdown();
		return backedupInstances.getReservations().stream()
				.flatMap(reservation -> reservation.getInstances().stream()).collect(Collectors.toList())
				.stream().map(Instance::getPublicIpAddress).collect(Collectors.toList());
	}

	private ConcurrentMap<String, Long> getFreeDevicesCountInEndpoints(List<String> endpoints) {
		return endpoints.parallelStream().collect(Collectors.toConcurrentMap(endpoint -> endpoint, o -> {
			try {
				Response response = httpClient.newCall(new Request.Builder().url("https://" + o + ":8443/freeDevicesCount").get().build()).execute();
				return Long.parseUnsignedLong(Objects.requireNonNull(response.body()).string());
			} catch(IOException e) {
				logger.error("An error occurred", e);
			}
			return 0L;
		}));
	}

	private String findLeastUsedInstanceIp(ConcurrentMap<String, Long> freeDevicesCount) {
		Optional<Map.Entry<String, Long>> max = freeDevicesCount.entrySet().parallelStream().max(Comparator.comparingLong(Map.Entry::getValue));
		if(max.isPresent())
			return max.get().getKey();
		return "";
	}

	/**
	 * This main method allows running the function as a CLI application using: echo '{}' | java -jar function.jar
	 * where the argument to echo is the JSON to be parsed.
	 */
	public static void main(String... args) throws IOException {
		CreateUserFunction function = new CreateUserFunction();
		function.run(args, (context) -> function.apply(context.get(CreateUserRequest.class)));
	}
}

