package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeStateData;

@Path("/changestate")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeStateResource {

	public static final String SU = "Super User";
	public static final String GA = "App Manager";
	public static final String GBO = "Back Office Manager";
	public static final String U = "User";
	private static final String ACTIVE = "ACTIVE";
	private static final String INACTIVE = "INACTIVE";

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ChangeStateResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeUserState(@CookieParam("session::apdc") Cookie cookie, ChangeStateData data) {
		if (cookie == null) {
			LOG.warning("Cookie is missing.");
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access.").build();
		}

		String username = cookie.getValue().split("\\.")[0];
		String usernameToChange = data.username;

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind(U).newKey(username);
			Entity user = txn.get(userKey);

			Key userToChangeKey = datastore.newKeyFactory().setKind(U).newKey(usernameToChange);
			Entity userToChange = txn.get(userToChangeKey);

			if (userToChange == null) {
				LOG.warning("Targeted user doesn't exist.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Can't change the state of an inexistent user.")
						.build();
			}
			if (!hasPermissionToChangeState(user, userToChange)) {
				LOG.warning("User does not have permission to change states.");
				return Response.status(Status.FORBIDDEN)
						.entity("You don't have permission to change the state of this user.").build();
			}
			if (!data.newState.equals(ACTIVE) && !data.newState.equals(INACTIVE)) {
				LOG.warning("Invalid new role chosen.");
				return Response.status(Status.FORBIDDEN).entity("Please choose a valid role.").build();
			}

			userToChange = Entity.newBuilder(userToChange).set("user_state", data.newState).build();
			txn.update(userToChange);
			LOG.info("User role changed to " + data.newState);
			txn.commit();

			return Response.ok("User " + usernameToChange + "'s state successfully changed to " + data.newState + ".")
					.build();
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	private boolean hasPermissionToChangeState(Entity user, Entity userToChange) {
		String userRole = user.getString("user_role");
		String userToChangeRole = userToChange.getString("user_role");

		if (userRole.equals(SU)) {
			return true;
		} else if (userRole.equals(GA) && (userToChangeRole.equals(GBO) || userToChangeRole.equals(U))) {
			return true;
		}
		return false;

	}
}
