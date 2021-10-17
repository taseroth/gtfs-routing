package org.faboo.example.routing;

import org.neo4j.graphdb.Node;
import org.neo4j.values.storable.DurationValue;

import java.util.HashSet;
import java.util.Set;

/**
 * Keeps track of data relevant to a branch during expansion.
 */
public class JourneyBranchState {

    private DurationValue lastArrivalTime;
    private SeenStopsCache stopsCache;

    public JourneyBranchState() {
        stopsCache = new SeenStopsCache();
    }

    public JourneyBranchState copy() {
        JourneyBranchState copy = new JourneyBranchState();
        copy.setLastArrivalTime(lastArrivalTime);
        copy.stopsCache = new SeenStopsCache(stopsCache);
        return copy;
    }

    public DurationValue getLastArrivalTime() {
        return lastArrivalTime;
    }

    public void setLastArrivalTime(DurationValue lastArrivalTime) {
        this.lastArrivalTime = lastArrivalTime;
    }

    /**
     *
     * @return true if this is not a double back
     */
    public boolean registerStop(Node stop) {
        return stopsCache.registerStop(stop);
    }

    @Override
    public String toString() {
        return "JourneyBranchState{" +
                "lastArrivalTime=" + lastArrivalTime +
                ", stopsCache=" + stopsCache +
                '}';
    }



    private static class SeenStopsCache {

        private final Set<Long> cache;

        private SeenStopsCache() {
            cache = new HashSet<>();
        }

        private SeenStopsCache(SeenStopsCache seenStopsCache) {
            cache = new HashSet<>(seenStopsCache.cache);
        }

        boolean registerStop(Node stop) {
            return cache.add(stop.getId());
        }

        boolean contains(Node stop) {
            return cache.contains(stop.getId());
        }

        @Override
        public String toString() {
            return cache.toString();
        }
    }

}
