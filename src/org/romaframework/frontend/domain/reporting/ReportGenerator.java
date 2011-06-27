package org.romaframework.frontend.domain.reporting;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.reporting.ReportingAspect;
import org.romaframework.aspect.view.ViewCallback;
import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.aspect.view.annotation.ViewClass;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.aspect.view.command.impl.DownloadStreamViewCommand;
import org.romaframework.aspect.view.form.ViewComponent;
import org.romaframework.core.Roma;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaObject;
import org.romaframework.frontend.RomaFrontend;

@ViewClass(layout = ViewConstants.LAYOUT_POPUP)
public class ReportGenerator implements ViewCallback {
	private Object	content;

	// @ViewField(selectionField = "type", render = ViewConstants.RENDER_RADIO)
	// @ViewField(visible = AnnotationConstants.FALSE)
	// private String[] types;

	// private List<IconButton> availableTypes;

	@ViewField(visible = AnnotationConstants.FALSE)
	private String	type;

	private String	fileName;

	public ReportGenerator(Object iContent, String fileName) {
		this(iContent, fileName, null);
		String[] types = getSupportedTypes();
		this.type = (types != null && types.length > 0) ? types[0] : null;

	}

	private static String[] getSupportedTypes() {
		if (RomaFrontend.reporting() == null) {
			return new String[] {};
		}
		return RomaFrontend.reporting().getSupportedTypes();
	}

	public ReportGenerator(Object iContent, String fileName, String type) {
		this.content = iContent;
		this.fileName = fileName;
		this.type = type;

	}

	public void onShow() {
		String[] supportedTypes = getSupportedTypes();
		if (supportedTypes != null && supportedTypes.length > 0) {
			setType(supportedTypes[0]);

		}
		Roma.fieldChanged(this, "types");
		Roma.fieldChanged(this, "availableTypes");

	}

	public void onDispose() {
	}

	@ViewAction(visible = AnnotationConstants.FALSE)
	public void report() throws FileNotFoundException, IOException {
		if (type == null)
			return;

		ReportingAspect reporting = RomaFrontend.reporting();
		String fileType = type.toLowerCase();

		byte[] report = reporting.render(content, type, Roma.session().getSchemaObject(content));
		RomaFrontend.view().pushCommand(new DownloadStreamViewCommand(new ByteArrayInputStream(report), fileName + "." + fileType, fileType));
	}

	public void close() {
		RomaFrontend.flow().back();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@ViewField(visible = AnnotationConstants.FALSE)
	public String[] getTypes() {
		return getSupportedTypes();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@ViewField(label = "Generate", render = ViewConstants.RENDER_COLSET)
	public List<IconButton> getAvailableTypes() {
		ArrayList<IconButton> availableTypes = new ArrayList<IconButton>();

		if (getSupportedTypes() != null)
			for (String tmpType : getSupportedTypes()) {
				availableTypes.add(new IconButton(this, tmpType));
			}
		return availableTypes;
	}
}