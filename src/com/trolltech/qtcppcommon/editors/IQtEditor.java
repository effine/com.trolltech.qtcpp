package com.trolltech.qtcppcommon.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;

public abstract interface IQtEditor extends IEditorPart, ISaveablePart {
	public abstract void reload();
}