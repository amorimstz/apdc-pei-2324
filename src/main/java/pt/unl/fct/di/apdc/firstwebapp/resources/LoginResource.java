package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status; // será preciso mudar para  o import cloud.datastore?

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();

	public LoginResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Attempt to login user " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		if (user != null) {
			String hashedPwrd = (String) user.getString("user_pwrd");
			if (hashedPwrd.equals(DigestUtils.sha512Hex(data.pwrd))) {
				user = Entity.newBuilder(user).set("user_login_time", Timestamp.now()).build();
				datastore.update(user);
				LOG.info("User '" + data.username + "' logged in successfully.");
				AuthToken token = new AuthToken(data.username);
				return Response.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).entity(g.toJson(token))
						.build();
			} else {
				LOG.warning("Wrong pwrd for user '" + data.username + "'.");
				return Response.status(Status.FORBIDDEN).build();
			}
		} else {
			LOG.warning("No user found with username '" + data.username + "'.");
			return Response.status(Status.FORBIDDEN).build();
		}
	}

}
