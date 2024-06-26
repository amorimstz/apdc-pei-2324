package pt.unl.fct.di.apdc.firstwebapp.Servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import java.util.logging.Logger;

@SuppressWarnings("serial")
public class InitServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(InitServlet.class.getName());
	private static final String ROOT_USERNAME = "root";
	private static final String ROOT_ROLE = "Super User";
	private static final String ROOT_STATE = "ACTIVE";
	private static final String PASSWORD = "00000";
	private static final String EMAIL = "root@root.pt";
	private static final String NAME = "root";
	private Datastore datastore;
	private KeyFactory userKeyFactory;

	@Override
	public void init() throws ServletException {
		datastore = DatastoreOptions.getDefaultInstance().getService();
		userKeyFactory = datastore.newKeyFactory().setKind("User");

		setupRootUser();
	}

	private void setupRootUser() {
		Transaction txn = null;
		try {
			txn = datastore.newTransaction();
			Key userKey = userKeyFactory.newKey(ROOT_USERNAME);
			Entity rootUser = txn.get(userKey);

			if (rootUser == null) {
				String hashedPassword = DigestUtils.sha512Hex(PASSWORD);
				rootUser = Entity.newBuilder(userKey).set("user_role", ROOT_ROLE).set("user_state", ROOT_STATE)
						.set("user_pwrd", hashedPassword).set("user_email", EMAIL).set("user_name", NAME)
						.set("user_creation_time", Timestamp.now()).build();
				txn.add(rootUser);
				txn.commit();
				LOG.info("Root user created successfully.");
			} else {
				LOG.info("Root user already exists.");
			}
		} catch (DatastoreException e) {
			LOG.warning("Error creating root user: " + e.getMessage());
			if (txn != null && txn.isActive()) {
				txn.rollback();
			}
		} finally {
			if (txn != null && txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
