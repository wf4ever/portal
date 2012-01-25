/**
 * 
 */
package pl.psnc.dl.wf4ever.portal.model;

import java.net.URI;
import java.util.Calendar;

/**
 * @author piotrhol
 * 
 */
public class ResearchObject
	extends AggregatedResource
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6525552866849376681L;


	public ResearchObject(URI researchObjectURI, Calendar created, String creator)
	{
		super(researchObjectURI, created, creator, "RO", 0);
	}


	public ResearchObject()
	{
		super();
	}


	@Override
	public boolean isWorkflow()
	{
		return false;
	}


	@Override
	public String getSizeFormatted()
	{
		return "--";
	}


	@Override
	public URI getDownloadURI()
	{
		return getURI();
	}


	@Override
	public String getName()
	{
		return getURI().resolve("..").relativize(getURI()).toString();
	}
}
