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
import org.romaframework.core.flow.UserObjectEventListener;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlEventAnnotation;
import org.romaframework.core.util.DynaBean;

/**
 * Abstract implementation for Flow Aspect.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class FlowAspectAbstract extends SelfRegistrantConfigurableModule<String> implements FlowAspect,
		UserObjectEventListener {

	@Override
	public void startup() {
		Controller.getInstance().registerListener(UserObjectEventListener.class, this);
	}

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
	}

	public void configAction(SchemaClassElement iAction, Annotation iActionAnnotation, Annotation iGenericAnnotation,
			XmlActionAnnotation iXmlNode) {
		DynaBean features = iAction.getFeatures(ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new FlowActionFeatures();
			iAction.setFeatures(ASPECT_NAME, features);
		}

		readActionAnnotation(iAction, iActionAnnotation, features);
		readActionXml(iAction, iXmlNode);
		setActionDefaults(iAction);
	}

	private void readActionAnnotation(SchemaClassElement iAction, Annotation iAnnotation, DynaBean features) {
		FlowAction annotation = (FlowAction) iAnnotation;

		if (annotation != null) {
			// PROCESS ANNOTATIONS
			// ANNOTATION ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT VALUES
			if (annotation != null) {
				if (!annotation.next().equals(Object.class))
					features.setAttribute(FlowActionFeatures.NEXT, Roma.schema().getSchemaClass(annotation.next()));
				if (!annotation.position().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(FlowActionFeatures.POSITION, annotation.position());
				if (!annotation.error().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(FlowActionFeatures.ERROR, annotation.error());
				if (annotation.back() != AnnotationConstants.UNSETTED)
					features.setAttribute(FlowActionFeatures.BACK, annotation.back() == AnnotationConstants.TRUE);
				if (annotation.confirmRequired() != AnnotationConstants.UNSETTED)
					features.setAttribute(FlowActionFeatures.CONFIRM_REQUIRED, annotation.confirmRequired() == AnnotationConstants.TRUE);
				if (!annotation.confirmMessage().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(FlowActionFeatures.CONFIRM_MESSAGE, annotation.confirmMessage());
			}
		}
	}

	private void readActionXml(SchemaClassElement iAction, XmlActionAnnotation iXmlNode) {
		// PROCESS DESCRIPTOR CFG
		// DESCRIPTOR ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT AND ANNOTATION
		// VALUES
		if (iXmlNode == null || iXmlNode.aspect(ASPECT_NAME) == null)
			return;

		DynaBean features = iAction.getFeatures(ASPECT_NAME);

		XmlAspectAnnotation descriptor = iXmlNode.aspect(ASPECT_NAME);

		if (descriptor != null) {
			String next = descriptor.getAttribute(FlowActionFeatures.NEXT);
			if (next != null) {
				SchemaClass clazz = Roma.schema().getSchemaClass(next);
				if (clazz != null && clazz.getSchemaClass() != null) {
					features.setAttribute(FlowActionFeatures.NEXT, clazz);
				}
			}
			String position = descriptor.getAttribute(FlowActionFeatures.POSITION);
			if (position != null) {
				features.setAttribute(FlowActionFeatures.POSITION, position);
			}
			String error = descriptor.getAttribute(FlowActionFeatures.ERROR);
			if (error != null)
				features.setAttribute(FlowActionFeatures.ERROR, error);

			String back = descriptor.getAttribute(FlowActionFeatures.BACK);
			if (back != null)
				features.setAttribute(FlowActionFeatures.BACK, new Boolean(back));

			String confirmRequired = descriptor.getAttribute(FlowActionFeatures.CONFIRM_REQUIRED);
			if (confirmRequired != null) {
				features.setAttribute(FlowActionFeatures.CONFIRM_REQUIRED, new Boolean(confirmRequired));
			}

			String confirmMessage = descriptor.getAttribute(FlowActionFeatures.CONFIRM_MESSAGE);
			if (confirmMessage != null)
				features.setAttribute(FlowActionFeatures.CONFIRM_MESSAGE, confirmMessage);
		}
	}

	public void configEvent(SchemaEvent iEvent, Annotation iEventAnnotation, Annotation iGenericAnnotation,
			XmlEventAnnotation iXmlNode) {
		DynaBean features = iEvent.getFeatures(ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new FlowActionFeatures();
			iEvent.setFeatures(ASPECT_NAME, features);
		}

		readActionAnnotation(iEvent, iEventAnnotation, features);
		readActionXml(iEvent, iXmlNode);
		setActionDefaults(iEvent);
	}

	private void setActionDefaults(SchemaClassElement iAction) {
	}

	public String aspectName() {
		return ASPECT_NAME;
	}
}
