package pl.psnc.dl.wf4ever.portal.components;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.purl.wf4ever.rosrs.client.Folder;

import pl.psnc.dl.wf4ever.portal.events.FolderChangeEvent;

/**
 * Bootstrap breadcrumb that shows the current location given a list of folders. If there are no folders, an information
 * is displayed instead.
 * 
 * @author piotrekhol
 * 
 */
public class FolderBreadcrumbsPanel extends Panel {

    /** id. */
    private static final long serialVersionUID = 6161074268125343983L;


    /**
     * Constructor.
     * 
     * @param id
     *            wicket ID
     * @param model
     *            a list of folders model
     * @param folderModel
     *            the currently selected folder model, used when a user clicks on one of the folders
     */
    public FolderBreadcrumbsPanel(String id, final IModel<List<Folder>> model, final IModel<Folder> folderModel) {
        super(id, model);
        setOutputMarkupId(true);
        AjaxLink<String> home = new AjaxLink<String>("home-link") {

            /** id. */
            private static final long serialVersionUID = 3495158432497901769L;


            @Override
            public void onClick(AjaxRequestTarget target) {
                folderModel.setObject(null);
                send(getPage(), Broadcast.BREADTH, new FolderChangeEvent(target));
            }
        };
        add(home);

        add(new ListView<Folder>("folder-item", model) {

            /** id. */
            private static final long serialVersionUID = 6756293180528006214L;


            @Override
            protected void populateItem(final ListItem<Folder> item) {
                final Folder folder = item.getModelObject();
                String name = folder.getName();
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
                AjaxLink<String> link = new AjaxLink<String>("folder-link", new PropertyModel<String>(folder, "name")) {

                    /** id. */
                    private static final long serialVersionUID = 3495158432497901769L;


                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        folderModel.setObject(folder);
                        send(getPage(), Broadcast.BREADTH, new FolderChangeEvent(target));
                    }
                };
                item.add(link);
                link.add(new Label("folder", name));
                item.add(new WebMarkupContainer("divider"));
            }
        });
    }


    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        if (event.getPayload() instanceof FolderChangeEvent) {
            onFolderChange((FolderChangeEvent) event.getPayload());
        }
    }


    /**
     * Refresh when the current folder changes.
     * 
     * @param event
     *            AJAX event
     */
    private void onFolderChange(FolderChangeEvent event) {
        event.getTarget().add(this);
    }

}
