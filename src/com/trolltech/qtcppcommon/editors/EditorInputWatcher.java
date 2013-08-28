package com.trolltech.qtcppcommon.editors;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.InfoForm;

public class EditorInputWatcher implements IPartListener, IWindowListener,
		IResourceChangeListener {
	private IQtEditor part;
	private long timestamp;
	private IResource resource;

	public EditorInputWatcher(IQtEditor part) {
		this.part = part;
		this.resource = ((IResource) part.getEditorInput().getAdapter(
				IResource.class));
		this.part.getSite().getWorkbenchWindow().getPartService()
				.addPartListener(this);
		PlatformUI.getWorkbench().addWindowListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		updateTimeStamp();
	}

	public void dispose() {
		this.part.getSite().getWorkbenchWindow().getPartService()
				.removePartListener(this);
		PlatformUI.getWorkbench().removeWindowListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	private String fileName() {
		FileEditorInput fin = (FileEditorInput) this.part.getEditorInput();
		return fin.getFile().getLocation().toOSString();
	}

	private long getCurrentTimeStamp() {
		return new File(fileName()).lastModified();
	}

	private boolean readable() {
		File file = new File(fileName());
		return (file.exists()) && (file.canRead());
	}

	public void updateTimeStamp() {
		if (!readable())
			this.timestamp = -1L;
		else
			this.timestamp = getCurrentTimeStamp();
	}

	public void partActivated(IWorkbenchPart partArg) {
		if ((partArg == this.part)
				&& (this.timestamp > -1L)
				&& ((this.resource == null) || (this.resource.exists()) || (this.part
						.isDirty()))) {
			if (!readable()) {
				String[] buttons = { "Save", "Close" };
				MessageDialog dialog = new MessageDialog(
						this.part.getSite().getShell(),
						"File not accessible",
						null,
						"The file has been deleted or is not accessible. Do you want to save your changes or close the editor without saving?",
						3, buttons, 0);

				if (dialog.open() == 0) {
					this.part.doSave(new NullProgressMonitor());
				}
				this.part
						.getSite()
						.getShell()
						.getDisplay()
						.asyncExec(
								(Runnable) new EditorInputWatcher(
										(IQtEditor) this));
			} else {
				long newtimestamp = getCurrentTimeStamp();
				if ((newtimestamp != this.timestamp)
						&& (MessageDialog
								.openQuestion(
										this.part.getSite().getShell(),
										"File changed",
										"The file has been changed on the file system. Do you want to replace the editor contents with these changes?"))) {
					this.part.reload();
				}
			}

			updateTimeStamp();
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	public void windowActivated(IWorkbenchWindow window) {
		if (window == this.part.getEditorSite().getWorkbenchWindow()) {
			window.getShell().getDisplay()
					.asyncExec((Runnable) new EditorInputWatcher(part));
		}
	}

	public void windowClosed(IWorkbenchWindow window) {
	}

	public void windowDeactivated(IWorkbenchWindow window) {
	}

	public void windowOpened(IWorkbenchWindow window) {
	}

	public void resourceChanged(IResourceChangeEvent event) {
		handleResourceDelta(event.getDelta());
	}

	public static Control createMissingFileInfo(Composite parent,
			String fileName) {
		parent.setLayout(new FillLayout(512));
		InfoForm info = new InfoForm(parent);
		info.setBannerText("");
		info.setHeaderText("");
		info.setInfo("File " + fileName + " does not exist or is not readable.");
		return info.getControl();
	}

	private boolean handleResourceDelta(IResourceDelta delta) {
		if (delta == null)
			return false;
		if ((delta.getKind() == 2)
				&& (delta.getResource().equals(this.resource))) {
			this.part.getSite().getShell().getDisplay()
					.syncExec((Runnable) new EditorInputWatcher(part));

			return true;
		}

		IResourceDelta[] deltas = delta.getAffectedChildren();
		for (int i = 0; (deltas != null) && (i < deltas.length); i++) {
			if (handleResourceDelta(deltas[i]))
				return true;
		}
		return false;
	}
}