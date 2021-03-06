/**
 * 
 */
package pl.psnc.dl.wf4ever.portal;

import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.StringValue;
import org.openid4java.discovery.DiscoveryInformation;
import org.purl.wf4ever.rosrs.client.ROSRService;
import org.purl.wf4ever.rosrs.client.accesscontrol.AccessControlService;
import org.purl.wf4ever.rosrs.client.accesscontrol.AccessMode;
import org.purl.wf4ever.rosrs.client.accesscontrol.Mode;
import org.purl.wf4ever.rosrs.client.accesscontrol.Permission;
import org.purl.wf4ever.rosrs.client.accesscontrol.Role;
import org.purl.wf4ever.rosrs.client.users.User;
import org.purl.wf4ever.rosrs.client.users.UserManagementService;
import org.purl.wf4ever.wf2ro.Wf2ROService;
import org.scribe.model.Token;

import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Custom app session.
 * 
 * @author piotrhol
 * 
 */
public class MySession extends AbstractAuthenticatedWebSession {

	/**
	 * A simple model that searches for the object with a given key. The
	 * returned value will be the same even if the calling page is
	 * serialized/deserialized.
	 * 
	 * @author piotrekhol
	 * 
	 */
	private static class SessionStoreModel<T> extends AbstractReadOnlyModel<T> {

		/** id. */
		private static final long serialVersionUID = 8741109057544006402L;

		/** key. */
		private int key;

		/**
		 * Constructor.
		 * 
		 * @param key
		 *            key
		 */
		public SessionStoreModel(int key) {
			this.key = key;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T getObject() {
			return (T) MySession.get().getStoredObject(key);
		}

	}

	/** Id. */
	private static final long serialVersionUID = -4113134277706549806L;

	/** Logger. */
	private static final Logger LOG = Logger.getLogger(MySession.class);

	/** RODL access token. */
	private Token dLibraAccessToken;

	/** Should RODL tokens be flushed to cookies. */
	private boolean dirtydLibra = false;

	/** myExperiment access token. */
	private Token myExpAccessToken;

	/** Should myExperiment tokens be flushed to cookies. */
	private boolean dirtyMyExp = false;

	/** Temporary token used for OAuth 1.0 with myExperiment. */
	private Token requestToken;

	/** Cookie key. */
	private static final String DLIBRA_KEY = "dlibra";

	/** Cookie key. */
	private static final String MYEXP_KEY_TOKEN = "myexp1";

	/** Cookie key. */
	private static final String MYEXP_KEY_SECRET = "myexp2";

	/** OpenID discovery information. */
	private DiscoveryInformation discoveryInformation;

	/** OpenID request token. */
	private String rodlRequestToken;

	/** Callback to the application's OpenID endpoint. */
	private URI openIDCallbackURI;

	/** RODL user. */
	private User user;

	/** ROSRS client. */
	private ROSRService rosrs;

	/** Access control client. */
	private AccessControlService accessControlService;

	/** UMS client. */
	private UserManagementService ums;

	/** Wf-RO transformation service. */
	private Wf2ROService wf2ro;
	
	/** Last visited ro .*/
	private String lastVisitedRO = "";
	
	/** Last given roles. */
	private Roles lastRoles;
	/**
	 * Keep futures here so that they are not dropped between subsequent page
	 * refreshes.
	 */
	private transient Map<Integer, SoftReference<?>> storedObjects;

	/**
	 * Constructor.
	 * 
	 * @param request
	 *            same as for superclass
	 */
	public MySession(Request request) {
		super(request);
		PortalApplication app = (PortalApplication) getApplication();
		this.rosrs = new ROSRService(app.getRodlURI().resolve("ROs/"), null);
		accessControlService = (new org.purl.wf4ever.rosrs.client.accesscontrol.AccessControlService(
				app.getRodlURI(), null));
		this.ums = new UserManagementService(app.getRodlURI(), app.getAdminToken());
		if (new CookieUtils().load(DLIBRA_KEY) != null) {
			signIn(new CookieUtils().load(DLIBRA_KEY));
		}
		if (new CookieUtils().load(MYEXP_KEY_TOKEN) != null
				&& new CookieUtils().load(MYEXP_KEY_SECRET) != null) {
			myExpAccessToken = new Token(new CookieUtils().load(MYEXP_KEY_TOKEN),
					new CookieUtils().load(MYEXP_KEY_SECRET));
		}
	}

	/**
	 * Singleton.
	 * 
	 * @return the only instance
	 */
	public static MySession get() {
		return (MySession) Session.get();
	}

	/**
	 * Sign in the user.
	 * 
	 * @param userToken
	 *            the access token
	 */
	public void signIn(String userToken) {
		try {
			PortalApplication app = (PortalApplication) getApplication();
			this.rosrs = new ROSRService(app.getRodlURI().resolve("ROs/"), userToken);
			accessControlService = (new AccessControlService(app.getRodlURI(), userToken));
			this.user = getUms().getWhoAmi(userToken);
			this.wf2ro = new Wf2ROService(app.getWf2ROService(), userToken);
		} catch (Exception e) {
			LOG.error("Error when retrieving user data: " + e.getMessage());
		}
		dirtydLibra = true;
	}

	/**
	 * myExperiment access token.
	 * 
	 * @return the myExpAccessToken
	 */
	public Token getMyExpAccessToken() {
		return myExpAccessToken;
	}

	/**
	 * myExperiment access token.
	 * 
	 * @param myExpAccessToken
	 *            the myExpAccessToken to set
	 */
	public void setMyExpAccessToken(Token myExpAccessToken) {
		this.myExpAccessToken = myExpAccessToken;
		dirtyMyExp = true;
	}

	/**
	 * myExperiment temporary access token.
	 * 
	 * @return the requestToken
	 */
	public Token getRequestToken() {
		return requestToken;
	}

	/**
	 * myExperiment temporary access token.
	 * 
	 * @param requestToken
	 *            the requestToken to set
	 */
	public void setRequestToken(Token requestToken) {
		this.requestToken = requestToken;
	}

	@Override
	public Roles getRoles() {
		//if anonymous
		if (!isSignedIn()) {
			return new Roles();
		}
		
		//check if there is there is an ro in context
		Request req = RequestCycle.get().getRequest();
		IRequestParameters params = req.getRequestParameters();
		StringValue roContext = params.getParameterValue("ro");
		if(roContext.isEmpty()){
			return isSignedIn() ? new Roles(Roles.USER) : new Roles();
		} else {
			if (roContext.toString().equals(lastVisitedRO)){
				return lastRoles;
			}	
			lastVisitedRO = roContext.toString();
			List<Permission> permissions = accessControlService.getPermissions(URI.create(roContext.toString()));
			for(Permission p : permissions) {
				if(p.getUserLogin().equals(user.getURI().toString())) {
					if(p.getRole().equals(Role.OWNER) || p.getRole().equals(Role.EDITOR)) {
						if(p.getRole().equals(Role.OWNER)){
							lastRoles = new Roles(Roles.USER + "," + "editor" + "," + "owner");
							return lastRoles;
						}
						lastRoles = new Roles(Roles.USER + "," + "editor");
						return lastRoles;
					}
				}
			}
			//check if it perhaps isn't open
			try {
				AccessMode mode  = accessControlService.getMode(URI.create(roContext.toString()));
				if(mode != null && mode.getMode().equals(Mode.OPEN)) {
					lastRoles = new Roles(Roles.USER + "," + "editor" );
					return lastRoles;

				}
			} catch (UniformInterfaceException e) {
				; //it just say the mode isn't open
			}
			
		}
		lastRoles = new Roles(Roles.USER);
		return lastRoles;
	}

	@Override
	public boolean isSignedIn() {
		return user != null;
	}

	/**
	 * Remove access tokens from memory and cookies.
	 */
	public void signOut() {
		dLibraAccessToken = null;
		myExpAccessToken = null;
		user = null;
		rosrs = new ROSRService(rosrs.getRosrsURI(), null);
		new CookieUtils().remove(DLIBRA_KEY);
		new CookieUtils().remove(MYEXP_KEY_TOKEN);
		new CookieUtils().remove(MYEXP_KEY_SECRET);
	}

	/**
	 * Flush access tokens to cookies.
	 */
	public void persist() {
		if (dirtydLibra) {
			if (dLibraAccessToken != null) {
				new CookieUtils().save(DLIBRA_KEY, dLibraAccessToken.getToken());
			}
			dirtydLibra = false;
		}
		if (dirtyMyExp) {
			if (myExpAccessToken != null) {
				new CookieUtils().save(MYEXP_KEY_TOKEN, myExpAccessToken.getToken());
				new CookieUtils().save(MYEXP_KEY_SECRET, myExpAccessToken.getSecret());
			}
			dirtyMyExp = false;
		}
	}

	public User getUser() {
		return user;
	}

	public DiscoveryInformation getDiscoveryInformation() {
		return discoveryInformation;
	}

	public void setDiscoveryInformation(DiscoveryInformation discoveryInformation) {
		this.discoveryInformation = discoveryInformation;
	}

	public String getRodlRequestToken() {
		return rodlRequestToken;
	}

	public void setRodlRequestToken(String rodlRequestToken) {
		this.rodlRequestToken = rodlRequestToken;
	}

	public URI getOpenIDCallbackURI() {
		return openIDCallbackURI;
	}

	public void setOpenIDCallbackURI(URI openIDCallbackURI) {
		this.openIDCallbackURI = openIDCallbackURI;
	}

	public ROSRService getRosrs() {
		return rosrs;
	}

	public UserManagementService getUms() {
		return ums;
	}

	public Wf2ROService getWf2ROService() {
		return wf2ro;
	}

	/**
	 * Get or create the transient store.
	 * 
	 * @return a map
	 */
	private synchronized Map<Integer, SoftReference<?>> getStoredObjects() {
		if (storedObjects == null) {
			storedObjects = new HashMap<>();
		}
		return storedObjects;
	}

	/**
	 * Store an object such as background job.
	 * 
	 * @param object
	 *            the object to store
	 * @param <T>
	 *            type of the object
	 * @return a read only model to retrieve the object
	 */
	public <T> IModel<T> storeObject(T object) {
		int key;
		do {
			key = new Random().nextInt();
		} while (getStoredObjects().containsKey(key));
		getStoredObjects().put(key, new SoftReference<T>(object));
		return new SessionStoreModel<T>(key);
	}

	/**
	 * Return the value. SoftReference is used so that the values can be deleted
	 * if they take too much memory.
	 * 
	 * @param key
	 *            key
	 * @return value or null
	 */
	private Object getStoredObject(int key) {
		SoftReference<?> value = getStoredObjects().get(key);
		return value != null ? value.get() : null;
	}

	public AccessControlService getAccessControlService() {
		return accessControlService;
	}

	public void clearAccessControlCache(){
		lastRoles = null;
		lastVisitedRO = "";
	}
}
