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

import pt.unl.fct.di.apdc.firstwebapp.util.RemoveUserData;

@Path("/removeuser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveUserResource {

	public static final String SU = "Super User";
	public static final String GA = "App Manager";
	public static final String GBO = "Back Office Manager";
	public static final String U = "User";

	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RemoveUserResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeUser(@CookieParam("session::apdc") Cookie cookie, RemoveUserData data) {

		LOG.fine("Attempt to remove user " + data.deletingUsername);

		if (cookie == null) {
			LOG.warning("Cookie is missing.");
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access.").build();
		}

		String username = cookie.getValue().split("\\.")[0];
		String deletingUsername = data.deletingUsername;

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind(U).newKey(username);
			Entity user = txn.get(userKey);

			Key deletingUserKey = datastore.newKeyFactory().setKind(U).newKey(deletingUsername);
			Entity deletingUser = txn.get(deletingUserKey);

			if (deletingUser == null) {
				LOG.warning("Targeted user doesn't exist.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Can't delete an inexistent user.").build();
			}
			if (!hasPermissionToDelete(user, deletingUser)) {
				LOG.warning("User does not have permission to delete this user.");
				return Response.status(Status.FORBIDDEN).entity("You don't have permission to delete this user.")
						.build();
			}

			txn.delete(deletingUserKey);
			LOG.info("User deleted.");
			txn.commit();

			return Response.ok("User " + deletingUsername + " successfully deleted.").build();

		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	private boolean hasPermissionToDelete(Entity user, Entity deletingUser) {
		String userRole = user.getString("user_role");
		String deletingUserRole = deletingUser.getString("user_role");

		if (userRole.equals(SU)) {
			return true;
		} else if (userRole.equals(GA) && (deletingUserRole.equals(GBO) || deletingUserRole.equals(U))) {
			return true;
		}
		return false;
	}
}
