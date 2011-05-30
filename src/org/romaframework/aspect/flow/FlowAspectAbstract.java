/*
 * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.romaframework.aspect.flow;

import java.lang.annotation.Annotation;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.flow.annotation.FlowAction;
import org.romaframework.aspect.flow.feature.FlowActionFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.flow.Controller;
import org.romaframework.core.flow.SchemaActionListener;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlEventAnnotation;

/**
 * Abstract implementation for Flow Aspect.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class FlowAspectAbstract extends SelfRegistrantConfigurableModule<String> implements FlowAspect, SchemaActionListener {

	@Override
	public void startup() {
		Controller.getInstance().registerListener(SchemaActionListener.class, this);
	}

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
	}

	public void configAction(SchemaClassElement iAction, Annotation iActionAnnotation, Annotation iGenericAnnotation, XmlActionAnnotation iXmlNode) {
		readActionAnnotation(iAction, iActionAnnotation);
		readActionXml(iAction, iXmlNode);
		setActionDefaults(iAction);
	}

	private void readActionAnnotation(SchemaClassElement iAction, Annotation iAnnotation) {
		FlowAction annotation = (FlowAction) iAnnotation;

		if (annotation != null) {
			// PROCESS ANNOTATIONS
			// ANNOTATION ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT VALUES
			if (annotation != null) {
				if (!annotation.next().equals(Object.class))
					iAction.setFeature(FlowActionFeatures.NEXT, Roma.schema().getSchemaClass(annotation.next()));
				if (!annotation.position().equals(AnnotationConstants.DEF_VALUE))
					iAction.setFeature(FlowActionFeatures.POSITION, annotation.position());
				if (!annotation.error().equals(AnnotationConstants.DEF_VALUE))
					iAction.setFeature(FlowActionFeatures.ERROR, annotation.error());
				if (annotation.back() != AnnotationConstants.UNSETTED)
					iAction.setFeature(FlowActionFeatures.BACK, annotation.back() == AnnotationConstants.TRUE);
				if (annotation.confirmRequired() != AnnotationConstants.UNSETTED)
					iAction.setFeature(FlowActionFeatures.CONFIRM_REQUIRED, annotation.confirmRequired() == AnnotationConstants.TRUE);
				if (!annotation.confirmMessage().equals(AnnotationConstants.DEF_VALUE))
					iAction.setFeature(FlowActionFeatures.CONFIRM_MESSAGE, annotation.confirmMessage());
			}
		}
	}

	private void readActionXml(SchemaClassElement iAction, XmlActionAnnotation iXmlNode) {
		// PROCESS DESCRIPTOR CFG
		// DESCRIPTOR ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT AND ANNOTATION
		// VALUES
		if (iXmlNode == null || iXmlNode.aspect(ASPECT_NAME) == null)
			return;

		XmlAspectAnnotation descriptor = iXmlNode.aspect(ASPECT_NAME);

		if (descriptor != null) {
			String next = descriptor.getAttribute(FlowActionFeatures.NEXT.getName());
			if (next != null) {
				SchemaClass clazz = Roma.schema().getSchemaClass(next);
				if (clazz != null && clazz.getSchemaClass() != null) {
					iAction.setFeature(FlowActionFeatures.NEXT, clazz);
				}
			}
			String position = descriptor.getAttribute(FlowActionFeatures.POSITION.getName());
			if (position != null)
				iAction.setFeature(FlowActionFeatures.POSITION, position);

			String error = descriptor.getAttribute(FlowActionFeatures.ERROR.getName());
			if (error != null)
				iAction.setFeature(FlowActionFeatures.ERROR, error);

			String back = descriptor.getAttribute(FlowActionFeatures.BACK.getName());
			if (back != null)
				iAction.setFeature(FlowActionFeatures.BACK, new Boolean(back));

			String confirmRequired = descriptor.getAttribute(FlowActionFeatures.CONFIRM_REQUIRED.getName());
			if (confirmRequired != null)
				iAction.setFeature(FlowActionFeatures.CONFIRM_REQUIRED, new Boolean(confirmRequired));

			String confirmMessage = descriptor.getAttribute(FlowActionFeatures.CONFIRM_MESSAGE.getName());
			if (confirmMessage != null)
				iAction.setFeature(FlowActionFeatures.CONFIRM_MESSAGE, confirmMessage);
		}
	}

	public void configEvent(SchemaEvent iEvent, Annotation iEventAnnotation, Annotation iGenericAnnotation, XmlEventAnnotation iXmlNode) {
		readActionAnnotation(iEvent, iEventAnnotation);
		readActionXml(iEvent, iXmlNode);
		setActionDefaults(iEvent);
	}

	private void setActionDefaults(SchemaClassElement iAction) {
	}

	public String aspectName() {
		return ASPECT_NAME;
	}
}
