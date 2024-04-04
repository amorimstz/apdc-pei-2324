package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;

import pt.unl.fct.di.apdc.firstwebapp.util.RemoveUserData;

@Path("/removeuser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveUserResource {

	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RemoveUserResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response removeUser(RemoveUserData data) {
		LOG.fine("Attempt to remove user " + data.deletingUsername);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.deletingUsername);
		Entity user = datastore.get(userKey);

		if (user != null) {
			datastore.delete(userKey);
			LOG.info("User '" + data.deletingUsername + "' deleted successfully.");
			return Response.ok().entity("User '" + data.deletingUsername + "' deleted successfully.").build();
		} else {
			LOG.warning("Failed to remove user '" + data.deletingUsername + "'.");
			return Response.status(Status.FORBIDDEN).entity("Can't remove inexistent user.").build();
		}
	}
}
