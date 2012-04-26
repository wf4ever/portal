/**
 * 
 */
package pl.psnc.dl.wf4ever.portal.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * @author piotrhol
 * 
 */
public class Annotation
	extends AggregatedResource
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8821418401311175036L;

	private URI bodyURI;

	private LoadableDetachableModel<List<Statement>> bodyModel;


	public Annotation(URI uri, Calendar created, List<Creator> creators, String name, URI bodyURI)
	{
		super(uri, created, creators, name, 0, Type.ANNOTATION);
		this.bodyURI = bodyURI;

		bodyModel = new LoadableDetachableModel<List<Statement>>() {

			private static final long serialVersionUID = 4142916952621994965L;


			@Override
			protected List<Statement> load()
			{
				try {
					List<Statement> res = RoFactory.createAnnotationBody(Annotation.this, getBodyURI());
					return res != null ? res : new ArrayList<Statement>();
				}
				catch (URISyntaxException e) {
					return new ArrayList<Statement>();
				}
			}
		};
	}


	public Annotation()
	{
		super();
	}


	/**
	 * @return the bodyURI
	 */
	public URI getBodyURI()
	{
		return bodyURI;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.psnc.dl.wf4ever.portal.model.AggregatedResource#getDownloadURI()
	 */
	@Override
	public URI getDownloadURI()
	{
		return getURI();
	}


	@Override
	public String getSizeFormatted()
	{
		return "--";
	}


	public List<Statement> getBody()
	{
		return bodyModel.getObject();
	}
}
