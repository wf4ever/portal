/**
 * 
 */
package pl.psnc.dl.wf4ever.portal.services;

import org.apache.log4j.Logger;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.oauth.OAuthService;

import pl.psnc.dl.wf4ever.portal.PortalApplication;
import pl.psnc.dl.wf4ever.portal.utils.OAuth20ServiceImpl;

/**
 * @author Piotr Hołubowicz
 * 
 */
public class DlibraApi
	extends DefaultApi20
{

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DlibraApi.class);


	public static OAuthService getOAuthService(String clientId, String callbackURL)
	{
		//		return new ServiceBuilder().provider(DlibraApi.class)
		//				.apiKey(DlibraApi.CONSUMER_KEY)
		//				.apiSecret(DlibraApi.SHARED_SECRET).build();
		return new OAuth20ServiceImpl(new DlibraApi(), new OAuthConfig(clientId, "foobar", callbackURL, null, null));
	}


	/* (non-Javadoc)
	 * @see org.scribe.builder.api.DefaultApi10a#getAccessTokenEndpoint()
	 */
	@Override
	public String getAccessTokenEndpoint()
	{
		return ((PortalApplication) PortalApplication.get()).getUserAccessTokenEndpointURL().toString();
	}


	@Override
	public String getAuthorizationUrl(OAuthConfig config)
	{
		// note: in response type "token" it is required to add redirection URI
		return ((PortalApplication) PortalApplication.get()).getUserAuthorizationEndpointURL().toString()
				+ String.format("?client_id=%s&response_type=%s", config.getApiKey(), "code");
	}
}
