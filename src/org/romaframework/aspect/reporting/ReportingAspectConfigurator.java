package org.romaframework.aspect.reporting;

import org.romaframework.aspect.core.feature.CoreFieldFeatures;
import org.romaframework.aspect.reporting.feature.ReportingFieldFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.aspect.AspectConfigurator;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;

public class ReportingAspectConfigurator implements AspectConfigurator {

	public void beginConfigClass(SchemaClassDefinition iClass) {

	}

	public void configClass(SchemaClassDefinition iClass) {
		ReportingAspect asp;
		if ((asp = Roma.aspect(ReportingAspect.class)) != null)
			((ReportingAspectAbstract) asp).refresh(iClass);
	}

	public void configField(SchemaField iField) {
		setFieldDefaults(iField);
	}

	public void configAction(SchemaAction iAction) {
	}

	public void configEvent(SchemaEvent iEvent) {

	}

	public void endConfigClass(SchemaClassDefinition iClass) {

	}

	public void setFieldDefaults(SchemaField iField) {
		if ((Boolean) iField.getFeature(CoreFieldFeatures.EMBEDDED)) {
			if (iField.getFeature(ReportingFieldFeatures.RENDER) == null)
				// IF THE FIELD IS EMBEDDED, THEN THE DEFAULT RENDER IS OBJECTEMBEDDED
				iField.setFeature(ReportingFieldFeatures.RENDER, ReportingConstants.RENDER_OBJECTEMBEDDED);
		}
	}

}
