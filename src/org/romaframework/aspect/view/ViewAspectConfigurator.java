package org.romaframework.aspect.view;

import org.romaframework.aspect.core.feature.CoreFieldFeatures;
import org.romaframework.aspect.view.event.SchemaEventAdd;
import org.romaframework.aspect.view.event.SchemaEventAddInline;
import org.romaframework.aspect.view.event.SchemaEventDown;
import org.romaframework.aspect.view.event.SchemaEventEdit;
import org.romaframework.aspect.view.event.SchemaEventOpen;
import org.romaframework.aspect.view.event.SchemaEventRemove;
import org.romaframework.aspect.view.event.SchemaEventReset;
import org.romaframework.aspect.view.event.SchemaEventSearch;
import org.romaframework.aspect.view.event.SchemaEventUp;
import org.romaframework.aspect.view.event.SchemaEventView;
import org.romaframework.aspect.view.feature.ViewActionFeatures;
import org.romaframework.aspect.view.feature.ViewClassFeatures;
import org.romaframework.aspect.view.feature.ViewFieldFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.aspect.AspectConfigurator;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaHelper;
import org.romaframework.core.schema.config.SchemaConfiguration;
import org.romaframework.core.schema.reflection.SchemaClassReflection;
import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlClassAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlFormAnnotation;

public class ViewAspectConfigurator implements AspectConfigurator {

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void configClass(SchemaClassDefinition iClass) {
		XmlClassAnnotation xmlNode = null;
		if (iClass instanceof SchemaClassReflection) {
			SchemaConfiguration conf = ((SchemaClassReflection) iClass).getDescriptor();
			if (conf != null)
				xmlNode = conf.getType();
		}
		if (xmlNode == null || xmlNode.aspect(ViewAspect.ASPECT_NAME) == null)
			return;

		XmlAspectAnnotation featureDescriptor = xmlNode.aspect(ViewAspect.ASPECT_NAME);

		if (featureDescriptor != null) {
			XmlFormAnnotation layout = featureDescriptor.getForm();
			if (layout != null && layout.getRootArea() != null)
				iClass.setFeature(ViewClassFeatures.FORM, layout.getRootArea());

		}
	}

	public void configField(SchemaField iField) {

		if (iField.getFeature(CoreFieldFeatures.EXPAND)) {
			iField.setFeature(ViewFieldFeatures.VISIBLE, false);
		}

		if (iField.getEntity().getFeature(ViewClassFeatures.EXPLICIT_ELEMENTS)) {
			if (!iField.isSetFeature(ViewFieldFeatures.VISIBLE) && iField.getDescriptorInfo() == null) {
				iField.setFeature(ViewFieldFeatures.VISIBLE, false);
			}
		}

		if (iField.getFeature(CoreFieldFeatures.EMBEDDED)) {
			if (iField.getFeature(ViewFieldFeatures.RENDER) == null && !SchemaHelper.isMultiValueObject(iField))
				// IF THE FIELD IS EMBEDDED, THEN THE DEFAULT RENDER IS OBJECTEMBEDDED
				iField.setFeature(ViewFieldFeatures.RENDER, ViewConstants.RENDER_OBJECTEMBEDDED);
		}

		String classRender = iField.getEntity().getFeature(ViewClassFeatures.RENDER);
		if (classRender != null)
			if (classRender.equals(ViewConstants.RENDER_MENU)) {
				// INSIDE A MENU: FORCE MENU RENDERING AND LAYOUT
				iField.setFeature(ViewFieldFeatures.RENDER, ViewConstants.RENDER_MENU);
			} else if (classRender.equals(ViewConstants.RENDER_ACCORDION)) {
				// INSIDE AN ACCORDITION: FORCE ACCORDITION LAYOUT
				iField.setFeature(ViewFieldFeatures.RENDER, ViewConstants.RENDER_ACCORDION);
			}

		if (SchemaHelper.isMultiValueObject(iField)) {
			iField.setEvent(new SchemaEventAddInline(iField));
			iField.setEvent(new SchemaEventAdd(iField));
			iField.setEvent(new SchemaEventView(iField));
			iField.setEvent(new SchemaEventEdit(iField));
			iField.setEvent(new SchemaEventRemove(iField));
			iField.setEvent(new SchemaEventUp(iField));
			iField.setEvent(new SchemaEventDown(iField));
		} else if (iField.getType() != null && !SchemaHelper.isJavaType(iField.getType().getName())) {
			iField.setEvent(new SchemaEventOpen(iField));
			iField.setEvent(new SchemaEventReset(iField));
			iField.setEvent(new SchemaEventSearch(iField));
		}
	}

	public void configAction(SchemaAction iAction) {

		if (((SchemaAction) iAction).getParameterNumber() > 0)
			iAction.setFeature(ViewActionFeatures.VISIBLE, Boolean.FALSE);
		iAction.toString();

		if (iAction.getEntity().getFeature(ViewClassFeatures.EXPLICIT_ELEMENTS)) {
			if (!iAction.isSetFeature(ViewActionFeatures.VISIBLE) && iAction.getDescriptorInfo() == null) {
				iAction.setFeature(ViewActionFeatures.VISIBLE, false);
			}
		}
		// CHECK RENDER AND LAYOUT MODES
		String classRender = iAction.getEntity().getFeature(ViewClassFeatures.RENDER);

		if (classRender != null)
			if (classRender.equals(ViewConstants.RENDER_MENU)) {
				// INSIDE A MENU: FORCE MENU RENDERING AND LAYOUT
				iAction.setFeature(ViewActionFeatures.RENDER, ViewConstants.RENDER_MENU);
			}
	}

	public void configEvent(SchemaEvent iEvent) {

	}

	public void endConfigClass(SchemaClassDefinition iClass) {
		((ViewAspectAbstract) Roma.component(ViewAspect.class)).updateFieldDependencies(iClass);
	}

}
