/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.prefs.Preferences;

/**
 * Object for storing preferences.
 * @author Alex Stangl
 */
public class PreferencesStore {

	// JDK object which actually holds preferences
	private Preferences prefs = Preferences.userNodeForPackage(Main.class);

	public String getValue(PreferenceKey key, String def) {
		return prefs.get(key.name(), def);
	}
	
	public void putValue(PreferenceKey key, String value) {
		prefs.put(key.name(), value);
	}
}
