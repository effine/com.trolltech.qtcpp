package com.trolltech.qtcppcommon;

import org.eclipse.swt.widgets.Shell;

public abstract interface JambiCompatible {
	public abstract boolean initializeJambiPlugins(String paramString1,
			String paramString2, String paramString3, String paramString4,
			String paramString5);

	public abstract Shell getShell();

	public abstract void updateCustomWidgetLocation(String paramString);

	public abstract String pluginFailureString();

	public abstract void loadLibrary(String paramString);
}