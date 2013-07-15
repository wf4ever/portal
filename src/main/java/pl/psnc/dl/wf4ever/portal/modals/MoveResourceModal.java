package pl.psnc.dl.wf4ever.portal.modals;

import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.purl.wf4ever.rosrs.client.Folder;

import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
import pl.psnc.dl.wf4ever.portal.events.aggregation.AggregationChangedEvent;
import pl.psnc.dl.wf4ever.portal.events.aggregation.ResourceMoveClickedEvent;
import pl.psnc.dl.wf4ever.portal.events.aggregation.ResourceMoveEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * A modal window for moving a resource to a folder.
 * 
 * @author piotrekhol
 * 
 */
public class MoveResourceModal extends AbstractModal {

    /** id. */
    private static final long serialVersionUID = 5398436522469957609L;

    /** Folder selected by the user. */
    private Folder folder = null;


    /**
     * Constructor.
     * 
     * @param id
     *            wicket id
     * @param foldersModel
     *            a list of folders to choose from
     * @param eventBusModel
     *            bus model
     */
    public MoveResourceModal(String id, IModel<List<Folder>> foldersModel, final IModel<EventBus> eventBusModel) {
        super(id, foldersModel, eventBusModel, "move-resource-modal", "Move a resource");
        modal.add(new DropDownChoice<Folder>("folder", new PropertyModel<Folder>(this, "folder"), foldersModel,
                new ChoiceRenderer<Folder>("path", "uri")));
    }


    /**
     * Show itself.
     * 
     * @param event
     *            AJAX event
     */
    @Subscribe
    public void onResourceMoveClicked(ResourceMoveClickedEvent event) {
        show(event.getTarget());
    }


    /**
     * Post an event and hide.
     * 
     * @param event
     *            AJAX event
     */
    @Subscribe
    public void onOk(OkClickedEvent event) {
        if (folder != null) {
            eventBusModel.getObject().post(new ResourceMoveEvent(event.getTarget(), folder));
        }
        hide(event.getTarget());
    }


    /**
     * Hide.
     * 
     * @param event
     *            AJAX event
     */
    @Subscribe
    public void onCancel(CancelClickedEvent event) {
        hide(event.getTarget());
    }


    /**
     * Called when the list of folders may have changed.
     * 
     * @param event
     *            AJAX event
     */
    @Subscribe
    public void onAggregationChanged(AggregationChangedEvent event) {
        event.getTarget().add(this);
    }


    public Folder getFolder() {
        return folder;
    }


    public void setFolder(Folder folder) {
        this.folder = folder;
    }

}
