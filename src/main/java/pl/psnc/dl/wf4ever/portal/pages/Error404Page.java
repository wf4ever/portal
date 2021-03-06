package pl.psnc.dl.wf4ever.portal.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A static page for displaying errors.
 * 
 * TODO: should it really exist, or can it be replaced by feedback panels?
 * 
 * @author Piotr Hołubowicz
 * 
 */
public class Error404Page extends BasePage {

    /** id. */
    private static final long serialVersionUID = -3233388849667095897L;

    /** The query param to pass the error message. */
    public static final String MESSAGE = "message";


    /**
     * Constructor.
     * 
     * @param pageParameters
     *            page params, should contain the MESSAGE query param
     */
    public Error404Page(PageParameters pageParameters) {
        super(pageParameters);

        if (pageParameters.get(MESSAGE) != null) {
            add(new Label("message", pageParameters.get(MESSAGE).toString()));
        } else {
            add(new Label("message").setVisible(false));
        }
    }
}
