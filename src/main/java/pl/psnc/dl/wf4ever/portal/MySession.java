/**
 * 
 */
package pl.psnc.dl.wf4ever.portal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.cookies.CookieUtils;
import org.purl.wf4ever.rosrs.client.common.ROSRService;
import org.purl.wf4ever.rosrs.client.common.Vocab;
import org.scribe.model.Token;

import pl.psnc.dl.wf4ever.portal.model.Creator;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Custom app session.
 * 
 * @author piotrhol
 * 
 */
public class MySession extends AbstractAuthenticatedWebSession {

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

    /** User RODL URI. */
    private URI userURI;

    /** Nice username. */
    private String username;

    /** Cookie key. */
    private static final String DLIBRA_KEY = "dlibra";

    /** Cookie key. */
    private static final String MYEXP_KEY_TOKEN = "myexp1";

    /** Cookie key. */
    private static final String MYEXP_KEY_SECRET = "myexp2";

    /** Usernames cache. */
    private final Map<URI, Creator> usernames = new HashMap<>();


    /**
     * Constructor.
     * 
     * @param request
     *            same as for superclass
     */
    public MySession(Request request) {
        super(request);
        if (new CookieUtils().load(DLIBRA_KEY) != null) {
            setdLibraAccessToken(new Token(new CookieUtils().load(DLIBRA_KEY), null));
        }
        if (new CookieUtils().load(MYEXP_KEY_TOKEN) != null && new CookieUtils().load(MYEXP_KEY_SECRET) != null) {
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
     * RODL access token.
     * 
     * @return the dLibraAccessToken
     */
    public Token getdLibraAccessToken() {
        return dLibraAccessToken;
    }


    /**
     * RODL access token.
     * 
     * @param dLibraAccessToken
     *            the dLibraAccessToken to set
     */
    public void setdLibraAccessToken(Token dLibraAccessToken) {
        this.dLibraAccessToken = dLibraAccessToken;
        fetchUserData();
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
        return isSignedIn() ? new Roles(Roles.USER) : null;
    }


    @Override
    public boolean isSignedIn() {
        return getdLibraAccessToken() != null;
    }


    /**
     * Remove access tokens from memory and cookies.
     */
    public void signOut() {
        dLibraAccessToken = null;
        myExpAccessToken = null;
        username = null;
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


    /**
     * Nice username.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }


    /**
     * Nice username.
     * 
     * @return the userURI
     */
    public URI getUserURI() {
        return userURI;
    }


    /**
     * Nice username or default.
     * 
     * @param defaultValue
     *            the value to use if username is null
     * @return the username
     */
    public String getUsername(String defaultValue) {
        if (username != null) {
            return username;
        }
        return defaultValue;
    }


    /**
     * Load user URI and username using the RODL access token.
     */
    private void fetchUserData() {
        try {
            OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
            userModel.read(ROSRService.getWhoAmi(((PortalApplication) PortalApplication.get()).getRodlURI(),
                getdLibraAccessToken()), null);
            ExtendedIterator<Individual> it = userModel.listIndividuals(Vocab.foafAgent);
            Individual user = it.next();
            if (user != null && user.hasProperty(Vocab.foafName)) {
                userURI = new URI(user.getURI());
                username = user.as(Individual.class).getPropertyValue(Vocab.foafName).asLiteral().getString();
            }
        } catch (Exception e) {
            LOG.error("Error when retrieving user data: " + e.getMessage());
        }
    }


    /**
     * The usernames cache.
     * 
     * @return the usernames
     */
    public Map<URI, Creator> getUsernames() {
        return usernames;
    }

}
