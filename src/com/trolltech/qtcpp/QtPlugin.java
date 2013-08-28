package com.trolltech.qtcpp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;

public class QtPlugin extends Plugin {
	public static final String PLUGIN_ID = "com.trolltech.qtcpp";
	private static boolean alreadyAskedAboutGtkQtEngine = false;

	private static boolean validQt = true;
	private static QtPlugin plugin;

	public QtPlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static QtPlugin getDefault() {
		return plugin;
	}

	public static Vector resolveLinuxLibrary(String libName) {
		File file = new File("/proc/self/maps");

		Vector res = new Vector();
		try {
			BufferedReader dis = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			String line = dis.readLine();
			while (line != null) {
				StringTokenizer st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					String part = st.nextToken();
					if (part.startsWith(File.separator)) {
						File lib = new File(part);
						if (!lib.getName().startsWith(libName))
							break;
						res.add(lib);
						break;
					}

				}

				line = dis.readLine();
			}
			dis.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return res;
	}

	public static boolean checkQtValid() {
		if ((!alreadyAskedAboutGtkQtEngine)
				&& (Platform.getOS().equals("linux"))) {
			Vector libs = resolveLinuxLibrary("libqt-mt.so.3");
			if (!libs.isEmpty()) {
				Display.getDefault().syncExec((Runnable) new QtPlugin());
			}

		}

		return validQt;
	}
}