package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON)
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private String LOGOUT_MSG = "Logout successful. See you next time!";
	private String LOG_LOGOUT_MSG = "User logged out.";

	@POST
	public Response doLogout() {
		NewCookie newCookie = new NewCookie("session::apdc", "", "/", null, "comment", 0, false, true);
		LOG.info(LOG_LOGOUT_MSG);
		return Response.ok().cookie(newCookie).entity(LOGOUT_MSG).build();
	}
}
