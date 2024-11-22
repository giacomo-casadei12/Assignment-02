package sap.ass02.configurationserver.infrastructure;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.vertx.core.json.JsonObject;

public class ClusterEntryListener implements EntryListener<String, JsonObject> {
    @Override
    public void entryAdded(EntryEvent<String, JsonObject> entryEvent) {
        System.out.println("groda " + entryEvent.getKey() + ": " + entryEvent.getValue());
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
        System.out.println("grodantelo " + entryEvent.getKey() + ": " + entryEvent.getValue());
    }

    @Override
    public void mapCleared(MapEvent mapEvent) {

    }

    @Override
    public void mapEvicted(MapEvent mapEvent) {

    }
}
