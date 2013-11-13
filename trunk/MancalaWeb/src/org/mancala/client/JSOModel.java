package org.mancala.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * Java overlay of a JavaScriptObject.
 */
public abstract class JSOModel extends JavaScriptObject {

    // Overlay types always have protected, zero-arg constructors
    protected JSOModel() {
    }

   
    /**
     * Create an empty instance.
     * 
     * @return new Object
     */
    public static native JSOModel create() /*-{
        return new Object();
    }-*/;

    /**
     * Convert a JSON encoded string into a JSOModel instance.
     * <p/>
     * Expects a JSON string structured like '{"foo":"bar","number":123}'
     *
     * @return a populated JSOModel object
     */
    public static native JSOModel fromJson(String jsonString) /*-{
        return eval('(' + jsonString + ')');
    }-*/;

    /**
     * Convert a JSON encoded string into an array of JSOModel instance.
     * <p/>
     * Expects a JSON string structured like '[{"foo":"bar","number":123}, {...}]'
     *
     * @return a populated JsArray
     */
    public static native JsArray<JSOModel> arrayFromJson(String jsonString) /*-{
        return eval('(' + jsonString + ')');
    }-*/;

    /**
     * Check if the object has key
     */
    public final native boolean hasKey(String key) /*-{
        return this[key] != undefined;
    }-*/;

    
    /**
     * Get keys
     */
    public final native JsArrayString keys() /*-{
        var a = new Array();
        for (var p in this) { a.push(p); }
        return a;
    }-*/;

    @Deprecated
    public final Set<String> keySet() {
        JsArrayString array = keys();
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < array.length(); i++) {
            set.add(array.get(i));
        }
        return set;
    }

    /**
     * Get string value
     */
    public final native String get(String key) /*-{
        return "" + this[key];
    }-*/;

    /**
     * Get string with default value
     */
    public final native String get(String key, String defaultValue) /*-{
        return this[key] ? ("" + this[key]) : defaultValue;
    }-*/;

    /**
     * Set value
     */
    public final native void set(String key, String value) /*-{
        this[key] = value;
    }-*/;

    /**
     * Set int value
     */
    public final native void set(String key, int value ) /*-{
    	this[key] = value;
    }-*/;

    /**
     * Get int value
     */
    public final int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Get boolean value
     */
    public final boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Get object
     */
    public final native JSOModel getObject(String key) /*-{
    	return (this[key] == null) ? null : this[key];
	}-*/; 

    /**
     * Get array
     */
    public final native JsArray<JSOModel> getArray(String key) /*-{
        return this[key] ? this[key] : new Array();
    }-*/;
}
