package org.ramonazaapi.interfaces;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for interacting with JSON representations of InfoWrappers.
 * InfoWrappers can be loaded directly from the {@link #loadWrappers(InfoWrapper[])} or {@link #loadWrappers(Collection)}
 * methods, or created from JSON using the {@link #loadJson(String)} method. They can then be dumped from the handler
 * into either JSON using {@link #dumpJson()} or to InfoWrapper objects using {@link #dumpWrappers()}. After dumping, the
 * internal storage is cleared.
 * Created by ilan on 12/19/15.
 */
public abstract class InfoWrapperJsonHandler<T extends InfoWrapper> {

    private List<T> currentWrappers;

    public InfoWrapperJsonHandler() {
        currentWrappers = new ArrayList<>();
    }

    /**
     * Converts an InfoWrapper into a map of attribute names to values.
     *
     * @param toGetAttributes the InfoWrapper to extract values from
     * @return the map of attributes to values
     */
    protected abstract Map<String, String> getAttributes(T toGetAttributes);

    /**
     * Creates an InfoWrapper from its attribute map.
     *
     * @param attributes the map of attributes
     * @return the new InfoWrapper
     */
    protected abstract T createFromAttributes(Map<String, String> attributes);

    /**
     * Loads infowrappers into the json handler.
     *
     * @param infoWrappers the infowrappers to load
     * @return this
     */
    public InfoWrapperJsonHandler loadWrappers(T... infoWrappers) {
        for (T toLoad : infoWrappers) {
            currentWrappers.add(toLoad);
        }
        return this;
    }

    /**
     * Loads infowrappers into the json handler.
     *
     * @param infoWrappers the infowrappers to load
     * @return this
     */
    public InfoWrapperJsonHandler loadWrappers(Collection<T> infoWrappers) {
        currentWrappers.addAll(infoWrappers);
        return this;
    }

    /**
     * Loads infowrappers into the json handler.
     *
     * @param json the infowrappers to load, in JSON format
     * @return this
     */
    public InfoWrapperJsonHandler loadJson(String json) {
        if (json.charAt(0) == '[') {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                Map<String, String> attrs = new HashMap<>();
                for (String key : object.keySet()) {
                    attrs.put(key, object.get(key).toString());
                }
                currentWrappers.add(createFromAttributes(attrs));
            }
        } else if (json.charAt(0) == '{') {
            JSONObject object = new JSONObject(json);
            Map<String, String> attrs = new HashMap<>();
            for (String key : object.keySet()) {
                attrs.put(key, object.get(key).toString());
            }
            currentWrappers.add(createFromAttributes(attrs));
        }
        return this;
    }


    /**
     * Dumps the currently loaded InfoWrappers into object form.
     * This clears the internal InfoWrapper buffer.
     *
     * @return a list of the dumped wrappers
     */
    public List<T> dumpWrappers() {
        List<T> toDump = new ArrayList<>(currentWrappers);
        currentWrappers.clear();
        return toDump;
    }

    /**
     * Dumps the currently loaded InfoWrappers into JSON form.
     * This clears the internal InfoWrapper buffer.
     *
     * @return a JSON string representing the internal InfoWrapper array
     */
    public String dumpJson() {
        JSONArray array = new JSONArray();
        for (T a : currentWrappers) {
            Map<String, String> currAttrs = getAttributes(a);
            array.put(currAttrs);
        }
        StringWriter writer = new StringWriter();
        array.write(writer);
        currentWrappers.clear();
        return writer.toString();
    }

}
