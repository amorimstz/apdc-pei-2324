package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.Authentication.SignatureUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.common.hash.Hashing;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	// Settings that must be in the database
	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";
	private static final String INACTIVE = "INACTIVE";

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		if (user == null || !checkPassword(data, user)) {
			return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
		}

		if (user.getString("user_state").equals(INACTIVE)) {
			return Response.status(Status.FORBIDDEN).entity("User currently inactive.").build();
		}

		String id = UUID.randomUUID().toString();
		long currentTime = System.currentTimeMillis();

		String fields = data.username + "." + id + "." + currentTime + "." + 1000 * 60 * 60 * 2;

		String signature = SignatureUtils.calculateHMac(key, fields);

		if (signature == null) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error while signing token. See logs.").build();
		}

		String value = fields + "." + signature;
		NewCookie cookie = new NewCookie("session::apdc", value, "/", null, "comment", 1000 * 60 * 60 * 2, false, true);

		return Response.ok().entity("Login successful. Welcome!").cookie(cookie).build();
	}

	public boolean checkPassword(LoginData data, Entity user) {
		String hashedPWD = user.getString("user_pwrd");
		String hashedInputPWD = Hashing.sha512().hashString(data.pwrd, StandardCharsets.UTF_8).toString();
		return (hashedPWD.equals(hashedInputPWD));
	}
}
