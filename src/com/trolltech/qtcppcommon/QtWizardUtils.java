package com.trolltech.qtcppcommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerGenerator;

public class QtWizardUtils {
	public static boolean addFile(InputStream src, File dest) {
		try {
			FileOutputStream out = new FileOutputStream(dest);

			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(src));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				out.write((line + "\n").getBytes());
			}

			out.close();
			bufferedReader.close();

			return true;
		} catch (IOException e) {
		}
		return false;
	}

	public static boolean addTemplateFile(InputStream src, File dest,
			Map replace) {
		try {
			FileOutputStream out = new FileOutputStream(dest);

			String outstr = patchTemplateFile(src, replace);
			out.write(outstr.getBytes());
			out.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static String patchTemplateFile(InputStream src, Map replace) {
		InputStreamReader fr = new InputStreamReader(src);
		BufferedReader br = new BufferedReader(fr);

		StringBuffer result = new StringBuffer();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				Iterator iter;
				if (replace != null) {
					for (iter = replace.keySet().iterator(); iter.hasNext();) {
						String key = (String) iter.next();
						line = line.replaceAll(key, (String) replace.get(key));
					}
				}
				result.append(line + "\n");
			}

			br.close();
			fr.close();
		} catch (IOException e) {
			return "";
		}

		return result.toString();
	}

	public static IFile createFile(IPath fpath, IProgressMonitor monitor)
			throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFileForLocation(fpath);

		if (file == null) {
			file = root.getFile(fpath);
		}

		if ((file == null) || (file.exists())) {
			monitor.done();
			return file;
		}

		if (fpath.segmentCount() > 1) {
			IPath cpath = fpath.removeLastSegments(1);
			if (root.getContainerForLocation(cpath) == null) {
				ContainerGenerator generator = new ContainerGenerator(cpath);
				generator.generateContainer(monitor);
			}
		}

		return file;
	}

	public static void setupTemplates(Object parent, String templatePath,
			String templateListName, Tree tree) {
		ClassLoader loader = parent.getClass().getClassLoader();

		InputStream stream = loader.getResourceAsStream(templatePath + "/"
				+ templateListName);
		if (stream != null) {
			BufferedReader r = new BufferedReader(new InputStreamReader(stream));
			try {
				String s = null;
				while ((s = r.readLine()) != null) {
					TreeItem item = new TreeItem(tree, 0);

					item.setText(s.replace('_', ' '));
					item.setData(templatePath + "/" + s);

					if (s.equals("Widget"))
						tree.setSelection(new TreeItem[] { item });
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Can't find resource: " + templatePath + "/"
					+ templateListName);
		}
	}
}