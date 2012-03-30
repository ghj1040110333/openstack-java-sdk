package org.openstack.ui.client.view.identity.tenant;

import java.util.Collection;

import org.openstack.model.identity.Endpoint;
import org.openstack.model.identity.EndpointList;
import org.openstack.model.identity.Service;
import org.openstack.model.identity.ServiceList;
import org.openstack.ui.client.OpenStackPlace;
import org.openstack.ui.client.UI;
import org.openstack.ui.client.api.DefaultAsyncCallback;
import org.openstack.ui.client.api.OpenStackClient;
import org.openstack.ui.client.api.RefreshableDataProvider;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;

public class ServicesActivity extends AbstractActivity implements ServicesView.Presenter {
	
	private static final ServicesView VIEW = new ServicesView();
	
	private OpenStackPlace place;
	
	private RefreshableDataProvider<Service> dataProvider;

	private MultiSelectionModel<Service> selectionModel = new MultiSelectionModel<Service>();

	private DefaultSelectionEventManager<Service> selectionManager = DefaultSelectionEventManager
			.<Service> createCheckboxManager(0);

	public ServicesActivity(OpenStackPlace place) {
		this.place = place;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		VIEW.setPresenter(this);
		panel.setWidget(VIEW);
		VIEW.grid.setSelectionModel(selectionModel, selectionManager);
		dataProvider = new RefreshableDataProvider<Service>(VIEW.grid) {

			@Override
			protected void onRangeChanged(HasData<Service> display) {
				OpenStackClient.IDENTITY.listServices(new DefaultAsyncCallback<ServiceList>() {

					@Override
					public void onSuccess(ServiceList result) {
						updateRowCount(result.getList().size(), true);
						updateRowData(0, result.getList());

					}
				});
			}

		};
	}

	@Override
	public void refresh() {
		dataProvider.refresh();
		
	}

	@Override
	public void onCreateService() {
		CreateServiceActivity activity = new CreateServiceActivity();
		activity.start(UI.MODAL, null);
		UI.MODAL.center();
		
	}

	@Override
	public void onDeleteService() {
		for(Service u : selectionModel.getSelectedSet()) {
			OpenStackClient.IDENTITY.deleteService(u.getId(), new DefaultAsyncCallback<Void>() {

				@Override
				public void onSuccess(Void result) {
					refresh();
					
				}
			});
		}
		
	}

	
	
	@Override
	public void onShowService(final Service service) {
		OpenStackClient.IDENTITY.listEndpoints(new DefaultAsyncCallback<EndpointList>() {

			@Override
			public void onSuccess(EndpointList result) {
				Collection<Endpoint> sEndpoints = Collections2.filter(result.getList(), new Predicate<Endpoint>() {

					@Override
					public boolean apply(Endpoint input) {
						return service.getId().equals(input.getServiceId());
					}
				});
				VIEW.detail.setWidget(new Label(sEndpoints.toString()));
				
			}
		});
		
	}

}