package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	/**
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RegisterResource() {
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegistration(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.isValidPwrd()) {
			return Response.status(Status.BAD_REQUEST).entity("Password must have at least 5 characters.").build();
		}
		if (!data.isValidEmail()) {
			return Response.status(Status.BAD_REQUEST).entity("Please use a valid email format.").build();
		}
		if (!data.isValidName()) {
			return Response.status(Status.BAD_REQUEST).entity("Please use a valid name.").build();
		}
		if (!data.isValidUsername()) {
			return Response.status(Status.BAD_REQUEST).entity("Username must have at least 3 characters.").build();
		}
		if (!data.isValidPhone()) {
			return Response.status(Status.BAD_REQUEST).entity("Please use a valid phone number.").build();
		}
		if (!data.isValidRegistry()) { // probs not needed
			return Response.status(Status.BAD_REQUEST).entity("Please fill all the mandatory fields.").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			if (checkUsernameAvailability(data, userKey)) {
				if (!data.pwrd.equals(data.confirmPwrd)) {
					LOG.warning("Passwords mismatch");
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Passwords must match.").build();
				}
				Entity user = Entity.newBuilder(userKey).set("user_pwrd", DigestUtils.sha512Hex(data.pwrd))
						.set("user_creation_time", Timestamp.now()).set("user_name", data.name)
						.set("user_email", data.email).set("user_phone", data.phone).set("user_role", "User")
						.set("user_state", "INACTIVE").set("user_address", data.address)
						.set("user_occupation", data.occupation).set("user_profileVisibilit", data.profileVisibility)
						.set("user_workplace", data.workplace).set("user_postalCode", data.postalCode)
						.set("user_NIF", data.NIF).build();

				txn.add(user);
				LOG.info("User registered " + data.username);
				txn.commit();
				return Response.ok("User successfully created.").build();
			} else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Username already taken.").build();
			}
		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

	private boolean checkUsernameAvailability(RegisterData data, Key userKey) {
		if (datastore.get(userKey) != null)
			return false;
		return true;
	}
}
