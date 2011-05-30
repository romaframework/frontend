/*
 * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.romaframework.aspect.reporting;

import java.lang.annotation.Annotation;

import org.romaframework.aspect.core.CoreAspect;
import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.core.feature.CoreFieldFeatures;
import org.romaframework.aspect.reporting.annotation.ReportingClass;
import org.romaframework.aspect.reporting.annotation.ReportingField;
import org.romaframework.aspect.reporting.feature.ReportingClassFeatures;
import org.romaframework.aspect.reporting.feature.ReportingFieldFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassResolver;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlClassAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlFieldAnnotation;
import org.romaframework.core.util.DynaBean;

/**
 * The abstract class for the reporting aspect
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class ReportingAspectAbstract extends SelfRegistrantConfigurableModule<String> implements ReportingAspect {

	public static final String	ASPECT_NAME	= "reporting";

	@Override
	public void startup() {
		super.startup();
		// REGISTER THE VIEW DOMAIN TO SCHEMA CLASS RESOLVER
		Roma.component(SchemaClassResolver.class).addDomainPackage(Utility.getApplicationAspectPackage(aspectName()));
		Roma.component(SchemaClassResolver.class).addDomainPackage(Utility.getRomaAspectPackage(aspectName()));
	}

	protected abstract void refresh(SchemaClassDefinition updatedClass);

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
	}

	public void configClass(SchemaClassDefinition iClass, Annotation iAnnotation, XmlClassAnnotation iXmlNode) {

		DynaBean features = iClass.getFeatures(ReportingAspect.ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new ReportingClassFeatures();
			iClass.setFeatures(ReportingAspect.ASPECT_NAME, features);
		}

		readClassAnnotation(iAnnotation, features);
		readClassXml(iClass, iXmlNode);
		refresh(iClass);
	}

	public void configField(SchemaField iField, Annotation iFieldAnnotation, Annotation iGenericAnnotation,
			Annotation iGetterAnnotation, XmlFieldAnnotation iXmlNode) {
		DynaBean features = iField.getFeatures(ReportingAspect.ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new ReportingFieldFeatures();
			iField.setFeatures(ReportingAspect.ASPECT_NAME, features);
		}

		readFieldAnnotation(iFieldAnnotation, features);
		readFieldAnnotation(iGetterAnnotation, features);
		readFieldXml(iField, iXmlNode);
		setFieldDefaults(iField);
	}

	public void configAction(SchemaClassElement iAction, Annotation iActionAnnotation, Annotation iGenericAnnotation,
			XmlActionAnnotation iNode) {
		// Reporting cannot be used on actions
	}

	public String aspectName() {
		return ASPECT_NAME;
	}

	private void readClassAnnotation(Annotation iAnnotation, DynaBean features) {
		ReportingClass annotation = (ReportingClass) iAnnotation;
		if (annotation != null) {
			// PROCESS ANNOTATIONS
			if (!annotation.entity().equals(Object.class)) {
				features.setAttribute(ReportingClassFeatures.ENTITY, annotation.entity());
			}
			if (!annotation.render().equals(AnnotationConstants.DEF_VALUE)) {
				features.setAttribute(ReportingClassFeatures.RENDER, annotation.render());
			}
			if (!annotation.layout().equals(AnnotationConstants.DEF_VALUE)) {
				features.setAttribute(ReportingClassFeatures.LAYOUT, annotation.layout());
			}
			if (annotation.explicitElements() != AnnotationConstants.UNSETTED) {
				features.setAttribute(ReportingClassFeatures.EXPLICIT_ELEMENTS, annotation.explicitElements());
			}
			if (!annotation.label().equals(AnnotationConstants.DEF_VALUE)) {
				features.setAttribute(ReportingClassFeatures.LABEL, annotation.label());
			}

			if (annotation.inheritViewConfiguration() != AnnotationConstants.UNSETTED) {
				features.setAttribute(ReportingClassFeatures.INHERIT_VIEW_CONFIGURATION, Boolean.FALSE);
			}

			if (!annotation.documentType().equals(AnnotationConstants.DEF_VALUE)) {
				features.setAttribute(ReportingClassFeatures.DOCUMENT_TYPE, annotation.documentType());
			}

		}
	}

	private void readClassXml(SchemaClassDefinition iClass, XmlClassAnnotation iXmlNode) {
		if (iXmlNode == null || iXmlNode.aspect(ASPECT_NAME) == null)
			return;

		DynaBean features = iClass.getFeatures(ReportingAspect.ASPECT_NAME);

		XmlAspectAnnotation featureDescriptor = iXmlNode.aspect(ASPECT_NAME);

		if (featureDescriptor != null) {

			// PROCESS DESCRIPTOR CFG
			if (featureDescriptor != null) {

				String label = featureDescriptor.getAttribute(ReportingClassFeatures.LABEL);
				if (label != null) {
					features.setAttribute(ReportingClassFeatures.LABEL, label);
				}
				String entity = featureDescriptor.getAttribute(ReportingClassFeatures.ENTITY);
				if (entity != null) {
					features.setAttribute(ReportingClassFeatures.ENTITY, Roma.schema().getSchemaClass(entity));
				}
				String render = featureDescriptor.getAttribute(ReportingClassFeatures.RENDER);
				if (render != null) {
					features.setAttribute(ReportingClassFeatures.RENDER, render);
				}
				String layout = featureDescriptor.getAttribute(ReportingClassFeatures.LAYOUT);
				if (layout != null) {
					features.setAttribute(ReportingClassFeatures.LAYOUT, layout);
				}
				String explicitElements = featureDescriptor.getAttribute(ReportingClassFeatures.EXPLICIT_ELEMENTS);
				if (explicitElements != null) {
					features.setAttribute(ReportingClassFeatures.EXPLICIT_ELEMENTS, new Boolean(explicitElements));
				}

				String inheritViewConfig = featureDescriptor.getAttribute(ReportingClassFeatures.INHERIT_VIEW_CONFIGURATION);
				if (inheritViewConfig != null) {
					features.setAttribute(ReportingClassFeatures.INHERIT_VIEW_CONFIGURATION, new Boolean(inheritViewConfig));
				}

				String documentType = featureDescriptor.getAttribute(ReportingClassFeatures.DOCUMENT_TYPE);
				if (documentType != null) {
					features.setAttribute(ReportingClassFeatures.DOCUMENT_TYPE, documentType);
				}

			}
		}
	}

	private void readFieldAnnotation(Annotation iAnnotation, DynaBean iFeatures) {
		ReportingField annotation = (ReportingField) iAnnotation;

		if (annotation != null) {
			if (!annotation.label().equals(AnnotationConstants.DEF_VALUE)) {
				iFeatures.setAttribute(ReportingFieldFeatures.LABEL, annotation.label());
			}

			if (annotation.visible() != AnnotationConstants.UNSETTED) {
				iFeatures.setAttribute(ReportingFieldFeatures.VISIBLE, annotation.visible() == AnnotationConstants.TRUE);
			}

			if (!annotation.render().equals(AnnotationConstants.DEF_VALUE)) {
				iFeatures.setAttribute(ReportingFieldFeatures.RENDER, annotation.render());
			}
			if (!annotation.layout().equals(AnnotationConstants.DEF_VALUE)) {
				iFeatures.setAttribute(ReportingFieldFeatures.LAYOUT, annotation.layout());
			}
		}
	}

	private void readFieldXml(SchemaField iField, XmlFieldAnnotation iXmlNode) {
		// PROCESS DESCRIPTOR CFG
		// DESCRIPTOR ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT AND ANNOTATION
		// VALUES
		if (iXmlNode == null)
			return;

		// FIELD FOUND IN DESCRIPTOR: ASSUME ITS VISIBILITY
		// features.setAttribute(ReportingFieldFeatures.VISIBLE, true);

		XmlAspectAnnotation descriptor = iXmlNode.aspect(ASPECT_NAME);

		if (descriptor == null)
			return;

		DynaBean features = iField.getFeatures(ASPECT_NAME);

		String label = descriptor.getAttribute(ReportingFieldFeatures.LABEL);
		if (label != null) {
			features.setAttribute(ReportingFieldFeatures.LABEL, label);
		}

		String visible = descriptor.getAttribute(ReportingFieldFeatures.VISIBLE);
		if (visible != null) {
			features.setAttribute(ReportingFieldFeatures.VISIBLE, new Boolean(visible));
		}

		String render = descriptor.getAttribute(ReportingFieldFeatures.RENDER);
		if (render != null) {
			features.setAttribute(ReportingFieldFeatures.RENDER, render);
		}
		String layout = descriptor.getAttribute(ReportingFieldFeatures.LAYOUT);
		if (layout != null) {
			features.setAttribute(ReportingFieldFeatures.LAYOUT, layout);
		}

	}

	public void setFieldDefaults(SchemaField iField) {
		if ((Boolean) iField.getFeature(CoreAspect.ASPECT_NAME, CoreFieldFeatures.EMBEDDED)) {
			if (iField.getFeature(ASPECT_NAME, ReportingFieldFeatures.RENDER) == null)
				// IF THE FIELD IS EMBEDDED, THEN THE DEFAULT RENDER IS OBJECTEMBEDDED
				iField.setFeature(ASPECT_NAME, ReportingFieldFeatures.RENDER, ReportingConstants.RENDER_OBJECTEMBEDDED);
		}
	}

	public void configEvent(SchemaEvent iEvent, Annotation iAnnotation, XmlActionAnnotation iNode) {
		// TODO Auto-generated method stub

	}
}