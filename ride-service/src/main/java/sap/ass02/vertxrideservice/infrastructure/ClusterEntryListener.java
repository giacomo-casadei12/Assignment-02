package sap.ass02.vertxrideservice.infrastructure;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class ClusterEntryListener implements EntryListener<String, JsonObject> {

    final EventBus eventBus;

    public ClusterEntryListener(EventBus eb) {
        this.eventBus = eb;
    }

    @Override
    public void entryAdded(EntryEvent<String, JsonObject> entryEvent) {
        System.out.println(entryEvent.getKey() + ": " + entryEvent.getValue());
        this.eventBus.publish("RideWebControllerConfigurationsChanged", entryEvent.getValue());
    }

    @Override
    public void entryEvicted(EntryEvent<String, JsonObject> entryEvent) {

    }

    @Override
    public void entryExpired(EntryEvent<String, JsonObject> entryEvent) {

    }

    @Override
    public void entryRemoved(EntryEvent<String, JsonObject> entryEvent) {

    }

    @Override
    public void entryUpdated(EntryEvent<String, JsonObject> entryEvent) {
        System.out.println(entryEvent.getKey() + ": " + entryEvent.getValue());
        this.eventBus.publish("RideWebControllerConfigurationsChanged", entryEvent.getValue());
    }

    @Override
    public void mapCleared(MapEvent mapEvent) {

    }

    @Override
    public void mapEvicted(MapEvent mapEvent) {

    }
}
