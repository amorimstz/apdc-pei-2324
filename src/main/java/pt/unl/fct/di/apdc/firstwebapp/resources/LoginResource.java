package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

	@GET
	@Path("/{username}") // PROBABLY WILL REMOVE THIS METHOD
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if (username.equals("jleitao")) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		if (data.username.equals("jleitao") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
		}
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLoginV1(LoginData data) {
		LOG.fine("Attempt to login user " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);
		if (user != null) {
			String hashedPwd = (String) user.getString("user_pwd");
			if (hashedPwd.equals(DigestUtils.sha512Hex(data.password))) {
				user = Entity.newBuilder(user).set("user_login_time", Timestamp.now()).build();
				datastore.update(user);
				LOG.info("User '" + data.username + "' logged in successfully.");
				AuthToken token = new AuthToken(data.username);
				return Response.ok(g.toJson(token)).build();
			} else {
				LOG.warning("Wrong password for user '" + data.username + "'.");
				return Response.status(Status.FORBIDDEN).build();
			}
		} else {
			LOG.warning("Failed to login user '" + data.username + "'.");
			return Response.status(Status.FORBIDDEN).build();
		}

	}

}
