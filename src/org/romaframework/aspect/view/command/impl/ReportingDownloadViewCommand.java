package org.romaframework.aspect.view.command.impl;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.romaframework.aspect.view.command.ViewCommand;
import org.romaframework.aspect.view.form.ViewComponent;
import org.romaframework.core.Roma;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaObject;
import org.romaframework.frontend.RomaFrontend;

public class ReportingDownloadViewCommand implements ViewCommand {

	private String	fileName;
	private String	contentType;
	private Object	toRender;

	public ReportingDownloadViewCommand(String fileName, String contentType, Object toRender) {
		super();
		this.fileName = fileName;
		this.contentType = contentType;
		this.toRender = toRender;
		if (this.toRender == null)
			throw new NullPointerException();
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public Object getToRender() {
		return toRender;
	}

	public void write(OutputStream outputStream) {
		if (getToRender() instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) getToRender();
			if (!collection.isEmpty()) {
				Iterator<?> it = collection.iterator();
				Object o = it.next();
				SchemaClass schema = Roma.schema().getSchemaClass(o.getClass());
				ViewComponent formComponent = RomaFrontend.view().createForm(new SchemaObject(schema, getToRender()), null, null);
				RomaFrontend.reporting().renderCollection((Collection<?>) getToRender(), getContentType(), formComponent.getSchemaObject(),
						outputStream);
			}
		} else {
			SchemaClass schema = Roma.schema().getSchemaClass(getToRender().getClass());
			ViewComponent formComponent = RomaFrontend.view().createForm(new SchemaObject(schema, getToRender()), null, null);
			RomaFrontend.reporting().render(getToRender(), getContentType(), formComponent.getSchemaObject(), outputStream);
		}
	}

}
