package org.ramonazaapi.events;

import org.ramonazaapi.interfaces.InfoWrapperJsonHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ilan on 12/19/15.
 */
public class EventJsonHandler extends InfoWrapperJsonHandler<EventInfoWrapper> {
    @Override
    protected Map<String, String> getAttributes(EventInfoWrapper toGetAttributes) {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("id", "" + toGetAttributes.getId());
        attrs.put("name", "" + toGetAttributes.getName());
        attrs.put("description", toGetAttributes.getDesc());
        attrs.put("date", toGetAttributes.getDate());
        attrs.put("planner", toGetAttributes.getPlanner());
        attrs.put("location", toGetAttributes.getMeet());
        attrs.put("mapslocation", toGetAttributes.getMapsLocation());
        attrs.put("bring", toGetAttributes.getBring());
        return attrs;
    }

    @Override
    protected EventInfoWrapper createFromAttributes(Map<String, String> attributes) {
        EventInfoWrapper fromAttrs = new EventInfoWrapper();
        fromAttrs.setId(Integer.parseInt(attributes.get("id")));
        fromAttrs.setName(attributes.get("name"));
        fromAttrs.setDesc(attributes.get("desc"));
        fromAttrs.setDate(attributes.get("date"));
        fromAttrs.setPlanner(attributes.get("planner"));
        fromAttrs.setMeet(attributes.get("location"));
        fromAttrs.setMapsLocation(attributes.get("mapslocation"));
        fromAttrs.setBring(attributes.get("bring"));
        return fromAttrs;
    }
}
