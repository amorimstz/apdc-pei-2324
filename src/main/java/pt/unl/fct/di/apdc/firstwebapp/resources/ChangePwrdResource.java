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

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.ChangePwrdData;

@Path("/changepwrd")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePwrdResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ChangePwrdResource() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePwrd(@CookieParam("session::apdc") Cookie cookie, ChangePwrdData data) {

		if (cookie == null) {
			LOG.warning("Cookie is missing.");
			return Response.status(Status.UNAUTHORIZED).entity("Unauthorized access.").build();
		}

		String username = cookie.getValue().split("\\.")[0];

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = txn.get(userKey);

			String currentPwrd = DigestUtils.sha512Hex(data.currentPwrd);
			if (!currentPwrd.equals(user.getString("user_pwrd"))) {
				LOG.warning("Wrong current password");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Wrong current password.").build();
			}

			if (!data.isValidConfirm()) {
				LOG.warning("New passwords mismatch.");
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("New passwords must match.").build();
			}

			if (!data.isValidPwrd()) {
				return Response.status(Status.BAD_REQUEST).entity("Password must have at least 5 characters.").build();
			}

			String newPwrd = DigestUtils.sha512Hex(data.newPwrd);
			user = Entity.newBuilder(user).set("user_pwrd", newPwrd).build();
			txn.update(user);
			LOG.info("User pwrd successfuly changed.");
			txn.commit();
			return Response.ok("Your password was successfully changed!").build();

		} finally {
			if (txn.isActive())
				txn.rollback();
		}
	}

}
