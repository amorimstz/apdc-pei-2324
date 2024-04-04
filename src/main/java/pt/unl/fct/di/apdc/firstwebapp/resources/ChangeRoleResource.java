package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;

@Path("/changerole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeRoleResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();

	public ChangeRoleResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeUserRole(@HeaderParam(HttpHeaders.AUTHORIZATION) String authTokenHeader,
			ChangeRoleData data) {

		if (authTokenHeader == null || authTokenHeader.isEmpty()) {
			LOG.warning("Authorization token is missing.");
			return Response.status(Status.UNAUTHORIZED)
					.entity("Authorization token is missing. (" + authTokenHeader + ")").build();
		}

		String authToken = authTokenHeader.substring("Bearer".length()).trim();

		// Validate and parse token
		AuthToken token = validateToken(authToken);
		if (token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
		}

		// Extract user information from token
		String username = token.username;
		String usernameToChange = data.username;

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = datastore.get(userKey);

			Key userToChangeKey = datastore.newKeyFactory().setKind("User").newKey(usernameToChange);
			Entity userToChange = datastore.get(userToChangeKey);

			if (userToChange == null) {
				LOG.warning("Targeted user doesn't exist.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Can't change the role of an inexistent user.")
						.build();
			}
			// Check if the user has the necessary permissions to change roles
			if (!hasPermissionToChangeRole(user, userToChange)) {
				LOG.warning("User does not have permission to change roles.");
				return Response.status(Status.FORBIDDEN).entity("You don't have permission to change roles.").build();
			}

			user = Entity.newBuilder(userKey).set("user_role", data.newRole).build();
			txn.update(user);
			LOG.info("User role changed to " + data.username);
			txn.commit();

			return Response.ok("User " + usernameToChange + "'s role successfully changed to " + data.newRole + ".")
					.build();

		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	private AuthToken validateToken(String authToken) {
		try {
			// Parse the token string into an AuthToken object
			AuthToken token = g.fromJson(authToken, AuthToken.class);
			// Check if the token has expired
			if (System.currentTimeMillis() > token.expirationDate) {
				// Token has expired
				LOG.warning("Token has expired.");
				return null;
			}
			// Token is valid
			return token;
		} catch (Exception e) {
			// Token parsing failed
			LOG.warning("Token parsing failed: " + e.getMessage());
			return null;
		}
	}

	private boolean hasPermissionToChangeRole(Entity user, Entity userToChange) {
		String userRole = user.getBlob("user_role").toString();
		String userToChangeRole = userToChange.getBlob("user_role").toString();

		if (userRole.equals("SU")) {
			return true;
		} else if (userRole.equals("GA") && (userToChangeRole.equals("GBO") || userToChangeRole.equals("USER"))) {
			return true;
		}
		return false;

	}
}
