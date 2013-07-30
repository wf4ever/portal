package pl.psnc.dl.wf4ever.portal.components.form;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import pl.psnc.dl.wf4ever.portal.events.AbstractAjaxEvent;
import pl.psnc.dl.wf4ever.portal.events.AbstractClickAjaxEvent;

import com.google.common.eventbus.EventBus;

/**
 * A button the creates an AJAX Event when clicked.
 * 
 * @author piotrekhol
 * 
 */
public class AjaxEventButton extends AjaxButton {

    /** id. */
    private static final long serialVersionUID = -2527416440222820413L;

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(AjaxEventButton.class);

    /** the event bus model to which the event is posted. */
    protected IModel<EventBus> eventBusModel;

    /** the class of the event to post. */
    protected Class<? extends AbstractClickAjaxEvent> eventClass;


    /**
     * Constructor.
     * 
     * @param id
     *            wicket ID
     * @param form
     *            for which will be validated
     * @param eventBusModel
     *            the event bus model to which the event is posted
     * @param eventClass
     *            the class of the event to post
     */
    public AjaxEventButton(String id, Form<?> form, IModel<EventBus> eventBusModel,
            Class<? extends AbstractClickAjaxEvent> eventClass) {
        super(id, form);
        this.eventBusModel = eventBusModel;
        this.eventClass = eventClass;
    }


    /**
     * Constructor.
     * 
     * @param id
     *            wicket ID
     * @param eventBusModel
     *            the event bus model to which the event is posted
     * @param eventClass
     *            the class of the event to post
     */
    public AjaxEventButton(String id, IModel<EventBus> eventBusModel, Class<? extends AbstractClickAjaxEvent> eventClass) {
        super(id);
        this.eventBusModel = eventBusModel;
        this.eventClass = eventClass;
    }


    @Override
    protected final void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
        target.appendJavaScript("hideBusy()");
        AbstractAjaxEvent event = newEvent(target);
        if (event != null) {
            eventBusModel.getObject().post(event);
        }
    }


    /**
     * Create a new event.
     * 
     * @param target
     *            AJAX request target
     * @return an event or null
     */
    protected AbstractAjaxEvent newEvent(AjaxRequestTarget target) {
        try {
            return (AbstractAjaxEvent) eventClass.getConstructor(AjaxRequestTarget.class).newInstance(target);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOG.error("Can't create the default event", e);
            return null;
        }
    }


    @Override
    protected void onError(AjaxRequestTarget target, Form<?> form) {
        target.appendJavaScript("hideBusy()");
        LOG.error("Error when submitting the button");
    }


    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);

        AjaxCallListener myAjaxCallListener = new AjaxCallListener() {

            /** id. */
            private static final long serialVersionUID = -5008615244332637745L;


            @Override
            public CharSequence getBeforeHandler(Component component) {
                return "showBusy();";
            }
        };
        attributes.getAjaxCallListeners().add(myAjaxCallListener);
    }

}
