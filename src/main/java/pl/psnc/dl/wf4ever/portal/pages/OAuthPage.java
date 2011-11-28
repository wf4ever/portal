/**
 * 
 */
package pl.psnc.dl.wf4ever.portal.pages;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import pl.psnc.dl.wf4ever.portal.PortalApplication;
import pl.psnc.dl.wf4ever.portal.services.DlibraApi;
import pl.psnc.dl.wf4ever.portal.services.MyExpApi;
import pl.psnc.dl.wf4ever.portal.services.OAuthException;
import pl.psnc.dl.wf4ever.portal.services.OAuthHelpService;
import pl.psnc.dl.wf4ever.portal.utils.MySession;

/**
 * @author Piotr Hołubowicz
 *
 */
public class OAuthPage
	extends WebPage
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3233388849667095897L;

	private static final Logger log = Logger.getLogger(OAuthPage.class);


	public OAuthPage(PageParameters pageParameters)
	{
		super(pageParameters);

		MySession session = MySession.get();
		PortalApplication app = (PortalApplication) getApplication();

		if (session.getdLibraAccessToken() == null) {
			OAuthService service = DlibraApi.getOAuthService(app.getdLibraClientId(), app.getCallbackURL());
			Token token = retrieveDlibraAccessToken(pageParameters, service);
			session.setdLibraAccessToken(token);
			if (token != null) {
				log.info("Successfully received dLibra access token");
			}
		}
		else if (session.getMyExpAccessToken() == null) {
			OAuthService service = MyExpApi.getOAuthService(app.getMyExpConsumerKey(), app.getMyExpConsumerSecret(),
				app.getCallbackURL());
			Token token = retrieveMyExpAccessToken(pageParameters, service);
			session.setMyExpAccessToken(token);
			if (token != null) {
				log.info("Successfully received myExperiment access token");
			}
		}
		continueToOriginalDestination();
		//		String nextUrl = session.getNextUrl() != null ? session.getNextUrl() : urlFor(HomePage.class, null).toString();
		//		getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(nextUrl));
	}


	/**
	 * @param pageParameters
	 * @param service
	 * @return
	 */
	private Token retrieveMyExpAccessToken(PageParameters pageParameters, OAuthService service)
	{
		Token accessToken = null;
		if (!pageParameters.get(MyExpApi.OAUTH_VERIFIER).isEmpty()) {
			Verifier verifier = new Verifier(pageParameters.get(MyExpApi.OAUTH_VERIFIER).toString());
			Token requestToken = MySession.get().getRequestToken();
			log.debug("Request token: " + requestToken.toString() + " verifier: " + verifier.getValue() + " service: "
					+ service.getAuthorizationUrl(requestToken));
			accessToken = service.getAccessToken(requestToken, verifier);
		}
		return accessToken;
	}


	/**
	 * @param pageParameters
	 * @param service
	 * @return
	 */
	private Token retrieveDlibraAccessToken(PageParameters pageParameters, OAuthService service)
	{
		Token accessToken = null;
		//TODO in the OAuth 2.0 implicit grant flow the access token is sent 
		//in URL fragment - how to retrieve it in Wicket?
		if (!pageParameters.get("access_token").isEmpty() && !pageParameters.get("token_type").isEmpty()) {
			if (pageParameters.get("token_type").equals("bearer")) {
				accessToken = new Token(pageParameters.get("access_token").toString(), null);
			}
			else {
				error("Unsupported token type: " + pageParameters.get("token_type").toString());
			}
		}
		else if (!pageParameters.get("code").isEmpty()) {
			String url = new DlibraApi().getAccessTokenEndpoint() + "?grant_type=authorization_code&code="
					+ pageParameters.get("code").toString();
			ObjectMapper mapper = new ObjectMapper();
			String body = null;
			try {
				Response response;
				try {
					response = OAuthHelpService.sendRequest(service, Verb.GET, url);
					body = response.getBody();
					@SuppressWarnings("unchecked")
					Map<String, String> responseData = mapper.readValue(body, Map.class);
					if (responseData.containsKey("access_token") && responseData.containsKey("token_type")) {
						if (responseData.get("token_type").equalsIgnoreCase("bearer")) {
							accessToken = new Token(responseData.get("access_token"), null);
						}
						else {
							error("Unsupported access token type: " + responseData.get("token_type"));
						}
					}
					else {
						error("Missing keys from access token endpoint response");
					}
				}
				catch (OAuthException e) {
					body = e.getResponse().getBody();
					@SuppressWarnings("unchecked")
					Map<String, String> responseData = mapper.readValue(body, Map.class);
					error(String.format("Access token endpoint returned error %s (%s)", responseData.get("error"),
						responseData.get("error_description")));
				}
			}
			catch (JsonParseException e) {
				error("Error in parsing access token endpoint response: " + body);
			}
			catch (JsonMappingException e) {
				error("Error in parsing access token endpoint response: " + body);
			}
			catch (IOException e) {
				error("Error in parsing access token endpoint response: " + body);
			}
		}
		return accessToken;
	}

}