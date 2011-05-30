/*
 * Copyright 2006-2007 Luca Garulli (luca.garulli--at--assetdata.it)
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

package org.romaframework.aspect.view;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.core.CoreAspect;
import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.core.feature.CoreFieldFeatures;
import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.session.SessionListener;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.aspect.view.annotation.ViewClass;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.aspect.view.command.impl.ChangeScreenViewCommand;
import org.romaframework.aspect.view.command.impl.RefreshViewCommand;
import org.romaframework.aspect.view.command.impl.ShowViewCommand;
import org.romaframework.aspect.view.event.SchemaEventAdd;
import org.romaframework.aspect.view.event.SchemaEventDown;
import org.romaframework.aspect.view.event.SchemaEventEdit;
import org.romaframework.aspect.view.event.SchemaEventOpen;
import org.romaframework.aspect.view.event.SchemaEventRemove;
import org.romaframework.aspect.view.event.SchemaEventReset;
import org.romaframework.aspect.view.event.SchemaEventSearch;
import org.romaframework.aspect.view.event.SchemaEventUp;
import org.romaframework.aspect.view.event.SchemaEventView;
import org.romaframework.aspect.view.feature.ViewActionFeatures;
import org.romaframework.aspect.view.feature.ViewBaseFeatures;
import org.romaframework.aspect.view.feature.ViewClassFeatures;
import org.romaframework.aspect.view.feature.ViewElementFeatures;
import org.romaframework.aspect.view.feature.ViewFieldFeatures;
import org.romaframework.aspect.view.form.ContentForm;
import org.romaframework.aspect.view.form.FormViewer;
import org.romaframework.aspect.view.form.ViewComponent;
import org.romaframework.aspect.view.screen.Screen;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.binding.Bindable;
import org.romaframework.core.exception.ConfigurationNotFoundException;
import org.romaframework.core.exception.UserException;
import org.romaframework.core.flow.Controller;
import org.romaframework.core.handler.RomaObjectHandler;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaClassResolver;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaHelper;
import org.romaframework.core.schema.SchemaObject;
import org.romaframework.core.schema.SchemaReloadListener;
import org.romaframework.core.schema.xmlannotations.XmlActionAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlClassAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlFieldAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlFormAnnotation;
import org.romaframework.core.util.DynaBean;

/**
 * View Aspect abstract implementation. It configures the ViewAspect from Java5 and XML annotations. This is a good starting point
 * for all View aspect implementations by extending this.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public abstract class ViewAspectAbstract extends SelfRegistrantConfigurableModule<String> implements ViewAspect, SessionListener,
		SchemaReloadListener {
	protected Map<SessionInfo, IdentityHashMap<Object, ViewComponent>>	objectsForms;

	private static Log																									log	= LogFactory.getLog(ViewAspectAbstract.class);

	public ViewAspectAbstract() {
		Controller.getInstance().registerListener(SessionListener.class, this);
		Controller.getInstance().registerListener(SchemaReloadListener.class, this);

		objectsForms = new WeakHashMap<SessionInfo, IdentityHashMap<Object, ViewComponent>>();
	}

	@Override
	public void startup() {
		// REGISTER THE VIEW DOMAIN TO SCHEMA CLASS RESOLVER
		Roma.component(SchemaClassResolver.class).addDomainPackage(Utility.getApplicationAspectPackage(aspectName()));
	}

	@Override
	public void shutdown() {
		objectsForms.clear();
	}

	/**
	 * Return the ObjectHandler as ViewComponent.
	 */
	@Override
	public RomaObjectHandler getObjectHandler(Object iUserObject) {
		return getFormByObject(iUserObject);
	}

	/**
	 * Return the ObjectHandler as ViewComponent.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<RomaObjectHandler> getObjectHandlers(SchemaClass iUserClass) {
		return (List) getFormsByClass(null, iUserClass);
	}

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
		updateFieldDependencies(iClass);
	}

	@SuppressWarnings("unchecked")
	protected void updateFieldDependencies(SchemaClassDefinition iClass) {
		Iterator<SchemaField> iterator = iClass.getFieldIterator();
		while (iterator.hasNext()) {
			SchemaField iField = iterator.next();
			String[] dependsOnList = (String[]) iField.getFeature(ASPECT_NAME, ViewFieldFeatures.DEPENDS_ON);
			if (dependsOnList != null) {
				for (String fieldName : dependsOnList) {
					SchemaField dependsFieldSchema = iClass.getField(fieldName);
					if (dependsFieldSchema == null)
						continue;
					Set<String> fieldDependsList = (Set<String>) dependsFieldSchema.getFeature(ASPECT_NAME, ViewFieldFeatures.DEPENDS);
					if (fieldDependsList == null)
						fieldDependsList = new HashSet<String>();
					fieldDependsList.add(iField.getName());
					dependsFieldSchema.setFeature(ASPECT_NAME, ViewFieldFeatures.DEPENDS, fieldDependsList);
				}
			}
		}
	}

	public void configClass(SchemaClassDefinition iClass, Annotation iAnnotation, XmlClassAnnotation iXmlNode) {
		DynaBean features = iClass.getFeatures(ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new ViewClassFeatures();
			iClass.setFeatures(ASPECT_NAME, features);
		}

		readClassAnnotation(iAnnotation, features);
		readClassXml(iClass, iXmlNode);
		setClassDefaults(iClass);
	}

	public void configField(SchemaField iField, Annotation iFieldAnnotation, Annotation iGenericAnnotation,
			Annotation iGetterAnnotation, XmlFieldAnnotation iXmlNode) {
		DynaBean features = iField.getFeatures(ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new ViewFieldFeatures();
			iField.setFeatures(ASPECT_NAME, features);
		}

		readFieldAnnotation(iGenericAnnotation, features);
		readFieldAnnotation(iFieldAnnotation, features);
		readFieldAnnotation(iGetterAnnotation, features);
		readFieldXml(iField, iXmlNode);
		setFieldDefaults(iField);
		if (SchemaHelper.isMultiValueObject(iField)) {
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

	public void configAction(SchemaClassElement iAction, Annotation iActionAnnotation, Annotation iGenericAnnotation,
			XmlActionAnnotation iXmlNode) {
		DynaBean features = iAction.getFeatures(ASPECT_NAME);
		if (features == null) {
			// CREATE EMPTY FEATURES
			features = new ViewActionFeatures();
			iAction.setFeatures(ASPECT_NAME, features);
		}

		if (((SchemaAction) iAction).getParameterNumber() > 0 || ((SchemaAction) iAction).getReturnType() != null)
			features.setAttribute(ViewActionFeatures.VISIBLE, Boolean.FALSE);

		readActionAnnotation(iAction, iActionAnnotation, features);
		readActionXml(iAction, iXmlNode);
		setActionDefaults(iAction);
	}

	private void readClassAnnotation(Annotation iAnnotation, DynaBean features) {
		ViewClass annotation = (ViewClass) iAnnotation;

		if (annotation != null) {
			// PROCESS ANNOTATIONS
			if (!annotation.description().equals(AnnotationConstants.DEF_VALUE))
				features.setAttribute(ViewBaseFeatures.DESCRIPTION, annotation.description());
			if (!annotation.label().equals(AnnotationConstants.DEF_VALUE))
				features.setAttribute(ViewBaseFeatures.LABEL, annotation.label());
			if (!annotation.style().equals(AnnotationConstants.DEF_VALUE))
				features.setAttribute(ViewBaseFeatures.STYLE, annotation.style());
			if (!annotation.render().equals(AnnotationConstants.DEF_VALUE))
				features.setAttribute(ViewBaseFeatures.RENDER, annotation.render());
			if (!annotation.layout().equals(AnnotationConstants.DEF_VALUE))
				features.setAttribute(ViewBaseFeatures.LAYOUT, annotation.layout());
			if (annotation.explicitElements() != AnnotationConstants.UNSETTED)
				features.setAttribute(ViewClassFeatures.EXPLICIT_ELEMENTS, annotation.explicitElements());
		}
	}

	private void readClassXml(SchemaClassDefinition iClass, XmlClassAnnotation iXmlNode) {
		if (iXmlNode == null || iXmlNode.aspect(ASPECT_NAME) == null)
			return;

		DynaBean features = iClass.getFeatures(ASPECT_NAME);

		XmlAspectAnnotation featureDescriptor = iXmlNode.aspect(ASPECT_NAME);

		if (featureDescriptor != null) {
			XmlFormAnnotation layout = featureDescriptor.getForm();
			if (layout != null && layout.getRootArea() != null)
				features.setAttribute(ViewClassFeatures.FORM, layout.getRootArea());

			// PROCESS DESCRIPTOR CFG
			if (featureDescriptor != null) {
				String description = featureDescriptor.getAttribute(ViewBaseFeatures.DESCRIPTION);
				if (description != null) {
					features.setAttribute(ViewBaseFeatures.DESCRIPTION, description);
				}
				String label = featureDescriptor.getAttribute(ViewBaseFeatures.LABEL);
				if (label != null) {
					features.setAttribute(ViewBaseFeatures.LABEL, label);
				}
				String style = featureDescriptor.getAttribute(ViewBaseFeatures.STYLE);
				if (style != null) {
					features.setAttribute(ViewBaseFeatures.STYLE, style);
				}
				String render = featureDescriptor.getAttribute(ViewBaseFeatures.RENDER);
				if (render != null) {
					features.setAttribute(ViewBaseFeatures.RENDER, render);
				}
				String layoutFeature = featureDescriptor.getAttribute(ViewBaseFeatures.LAYOUT);
				if (layoutFeature != null) {
					features.setAttribute(ViewBaseFeatures.LAYOUT, layoutFeature);
				}
				String explicitElements = featureDescriptor.getAttribute(ViewClassFeatures.EXPLICIT_ELEMENTS);
				if (explicitElements != null) {
					features.setAttribute(ViewClassFeatures.EXPLICIT_ELEMENTS, new Boolean(explicitElements));// boolean
				}
				String columns = featureDescriptor.getAttribute(ViewClassFeatures.COLUMNS);
				if (columns != null) {
					features.setAttribute(ViewClassFeatures.COLUMNS, new Integer(columns));
				}
			}
		}
	}

	public void setClassDefaults(SchemaClassDefinition iClass) {
		DynaBean features = iClass.getFeatures(ASPECT_NAME);

		if ((Boolean) features.getAttribute(ViewClassFeatures.EXPLICIT_ELEMENTS)) {
			// HIDE ALL INHERITED ELEMENTS
			for (Iterator<SchemaField> itField = iClass.getFieldIterator(); itField.hasNext();) {
				itField.next().setFeature(ASPECT_NAME, ViewElementFeatures.VISIBLE, false);
			}
			for (Iterator<SchemaAction> itAction = iClass.getActionIterator(); itAction.hasNext();) {
				itAction.next().setFeature(ASPECT_NAME, ViewElementFeatures.VISIBLE, false);
			}

			// AVOID THE CLASS INHERIT IT TO RECYCLE ALL AGAIN
			features.setAttribute(ViewClassFeatures.EXPLICIT_ELEMENTS, Boolean.FALSE);
		}
	}

	@SuppressWarnings("unchecked")
	private void readFieldAnnotation(Annotation iAnnotation, DynaBean iFeatures) {
		ViewField annotation = (ViewField) iAnnotation;

		if (annotation != null) {
			if (!annotation.label().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewBaseFeatures.LABEL, annotation.label());
			if (!annotation.description().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewBaseFeatures.DESCRIPTION, annotation.description());
			if (annotation.visible() != AnnotationConstants.UNSETTED)
				iFeatures.setAttribute(ViewElementFeatures.VISIBLE, annotation.visible() == AnnotationConstants.TRUE);
			if (annotation.enabled() != AnnotationConstants.UNSETTED)
				iFeatures.setAttribute(ViewElementFeatures.ENABLED, annotation.enabled() == AnnotationConstants.TRUE);
			if (!annotation.style().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewBaseFeatures.STYLE, annotation.style());
			if (!annotation.render().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewBaseFeatures.RENDER, annotation.render());
			if (!annotation.layout().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewBaseFeatures.LAYOUT, annotation.layout());
			if (!annotation.selectionField().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewFieldFeatures.SELECTION_FIELD, annotation.selectionField());
			if (annotation.selectionMode() != AnnotationConstants.UNSETTED)
				iFeatures.setAttribute(ViewFieldFeatures.SELECTION_MODE, annotation.selectionMode());
			if (annotation.depends().length > 0) {
				Set<String> dependsFields = (Set<String>) iFeatures.getAttribute(ViewFieldFeatures.DEPENDS);
				if (dependsFields == null)
					dependsFields = new HashSet<String>();
				for (String dependsField : annotation.depends()) {
					dependsFields.add(dependsField);
				}
				iFeatures.setAttribute(ViewFieldFeatures.DEPENDS, dependsFields);
			}
			if (annotation.dependsOn().length > 0) {
				iFeatures.setAttribute(ViewFieldFeatures.DEPENDS_ON, annotation.dependsOn());
			}
			if (!annotation.format().equals(AnnotationConstants.DEF_VALUE))
				iFeatures.setAttribute(ViewFieldFeatures.FORMAT, annotation.format());
			if (!Bindable.class.equals(annotation.displayWith())) {
				iFeatures.setAttribute(ViewFieldFeatures.DISPLAY_WITH, annotation.displayWith());
				iFeatures.setAttribute(ViewFieldFeatures.RENDER, ViewConstants.RENDER_OBJECTEMBEDDED);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readFieldXml(SchemaField iField, XmlFieldAnnotation iXmlNode) {
		// PROCESS DESCRIPTOR CFG
		// DESCRIPTOR ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT AND ANNOTATION
		// VALUES
		if (iXmlNode == null)
			return;

		DynaBean features = iField.getFeatures(ASPECT_NAME);

		// FIELD FOUND IN DESCRIPTOR: ASSUME ITS VISIBILITY
		features.setAttribute(ViewElementFeatures.VISIBLE, true);

		XmlAspectAnnotation descriptor = iXmlNode.aspect(ASPECT_NAME);

		if (descriptor == null) {
			return;
		}

		String embeddedType = descriptor.getAttribute("embeddedType");
		if (embeddedType != null) {
			iField.setEmbeddedLanguageType((Roma.component(SchemaClassResolver.class).getLanguageClass(embeddedType)));
		}

		String label = descriptor.getAttribute(ViewBaseFeatures.LABEL);
		if (label != null) {
			features.setAttribute(ViewBaseFeatures.LABEL, label);
		}
		String description = descriptor.getAttribute(ViewBaseFeatures.DESCRIPTION);
		if (description != null) {
			features.setAttribute(ViewBaseFeatures.DESCRIPTION, description);
		}
		String visible = descriptor.getAttribute(ViewElementFeatures.VISIBLE);
		if (visible != null) {
			features.setAttribute(ViewElementFeatures.VISIBLE, new Boolean(visible));
		}
		String enabled = descriptor.getAttribute(ViewElementFeatures.ENABLED);
		if (enabled != null) {
			features.setAttribute(ViewElementFeatures.ENABLED, new Boolean(enabled));
		}
		String style = descriptor.getAttribute(ViewBaseFeatures.STYLE);
		if (style != null) {
			features.setAttribute(ViewBaseFeatures.STYLE, style);
		}
		String render = descriptor.getAttribute(ViewBaseFeatures.RENDER);
		if (render != null) {
			features.setAttribute(ViewBaseFeatures.RENDER, render);
		}
		String layout = descriptor.getAttribute(ViewBaseFeatures.LAYOUT);
		if (layout != null) {
			features.setAttribute(ViewBaseFeatures.LAYOUT, layout);
		}
		String selectionField = descriptor.getAttribute(ViewFieldFeatures.SELECTION_FIELD);
		if (selectionField != null) {
			features.setAttribute(ViewFieldFeatures.SELECTION_FIELD, selectionField);
		}
		String selectionMode = descriptor.getAttribute(ViewFieldFeatures.SELECTION_MODE);
		if (selectionMode != null) {
			String mode = selectionMode;
			features.setAttribute(ViewFieldFeatures.SELECTION_MODE, mode.equals("index") ? ViewFieldFeatures.SELECTION_MODE_INDEX
					: ViewFieldFeatures.SELECTION_MODE_VALUE);
		}
		String format = descriptor.getAttribute(ViewFieldFeatures.FORMAT);
		if (format != null) {
			features.setAttribute(ViewFieldFeatures.FORMAT, format);
		}
		String dependsOn = descriptor.getAttribute(ViewFieldFeatures.DEPENDS_ON);
		if (dependsOn != null && dependsOn.length() > 0) {
			features.setAttribute(ViewFieldFeatures.DEPENDS_ON, dependsOn.split(","));
		}
		String depends = descriptor.getAttribute(ViewFieldFeatures.DEPENDS);
		if (depends != null && depends.length() > 0) {
			Set<String> fieldDependsList = (Set<String>) features.getAttribute(ViewFieldFeatures.DEPENDS);
			if (fieldDependsList == null)
				fieldDependsList = new HashSet<String>();
			for (String fieldName : depends.split(",")) {
				fieldDependsList.add(fieldName);
			}
		}
		String displayWith = descriptor.getAttribute(ViewFieldFeatures.DISPLAY_WITH);
		if (displayWith != null && displayWith.length() > 0) {
			features
					.setAttribute(ViewFieldFeatures.DISPLAY_WITH, Roma.component(SchemaClassResolver.class).getLanguageClass(displayWith));
			features.setAttribute(ViewFieldFeatures.RENDER, ViewConstants.RENDER_OBJECTEMBEDDED);
		}
	}

	public void setFieldDefaults(SchemaField iField) {
		DynaBean features = iField.getFeatures(ASPECT_NAME);

		// CHECK RENDER AND LAYOUT MODES
		String classRender = (String) iField.getEntity().getFeature(ASPECT_NAME, ViewBaseFeatures.RENDER);

		if ((Boolean) iField.getFeature(CoreAspect.ASPECT_NAME, CoreFieldFeatures.EMBEDDED)) {
			if (iField.getFeature(ViewAspectAbstract.ASPECT_NAME, ViewBaseFeatures.RENDER) == null
					&& !SchemaHelper.isMultiValueObject(iField))
				// IF THE FIELD IS EMBEDDED, THEN THE DEFAULT RENDER IS OBJECTEMBEDDED
				iField.setFeature(ViewAspectAbstract.ASPECT_NAME, ViewBaseFeatures.RENDER, ViewConstants.RENDER_OBJECTEMBEDDED);
		}

		String layoutMode = (String) iField.getFeature(ViewAspect.ASPECT_NAME, ViewFieldFeatures.LAYOUT);

		if (ViewConstants.LAYOUT_EXPAND.equals(layoutMode))
			// IF THE FIELD HAS LAYOUT EXPAND, FORCE THE RENDER=OBJECT EMBEDDED
			iField.setFeature(ViewAspectAbstract.ASPECT_NAME, ViewBaseFeatures.RENDER, ViewConstants.RENDER_OBJECTEMBEDDED);

		if (classRender != null)
			if (classRender.equals(ViewConstants.RENDER_MENU)) {
				// INSIDE A MENU: FORCE MENU RENDERING AND LAYOUT
				features.setAttribute(ViewBaseFeatures.RENDER, ViewConstants.RENDER_MENU);
				features.setAttribute(ViewBaseFeatures.LAYOUT, ViewConstants.LAYOUT_MENU);
			} else if (classRender.equals(ViewConstants.RENDER_ACCORDION)) {
				// INSIDE AN ACCORDITION: FORCE ACCORDITION LAYOUT
				features.setAttribute(ViewBaseFeatures.RENDER, ViewConstants.RENDER_ACCORDION);
				features.setAttribute(ViewBaseFeatures.LAYOUT, ViewConstants.LAYOUT_ACCORDION);
			}
	}

	@SuppressWarnings("deprecation")
	private void readActionAnnotation(SchemaClassElement iAction, Annotation iAnnotation, DynaBean features) {
		ViewAction annotation = (ViewAction) iAnnotation;

		if (annotation != null) {
			// PROCESS ANNOTATIONS
			// ANNOTATION ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT VALUES
			if (annotation != null) {
				if (!annotation.label().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(ViewBaseFeatures.LABEL, annotation.label());
				if (!annotation.description().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(ViewBaseFeatures.DESCRIPTION, annotation.description());
				if (!annotation.actionName().equals(AnnotationConstants.DEF_VALUE)) {
				}
				if (annotation.visible() != AnnotationConstants.UNSETTED)
					features.setAttribute(ViewElementFeatures.VISIBLE, annotation.visible() == AnnotationConstants.TRUE);
				if (!annotation.style().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(ViewBaseFeatures.STYLE, annotation.style());
				if (!annotation.render().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(ViewBaseFeatures.RENDER, annotation.render());
				if (!annotation.layout().equals(AnnotationConstants.DEF_VALUE))
					features.setAttribute(ViewBaseFeatures.LAYOUT, annotation.layout());
				if (annotation.bind() != AnnotationConstants.UNSETTED)
					features.setAttribute(ViewActionFeatures.BIND, annotation.bind() == AnnotationConstants.TRUE);
				if (annotation.enabled() != AnnotationConstants.UNSETTED)
					features.setAttribute(ViewElementFeatures.ENABLED, annotation.enabled() == AnnotationConstants.TRUE);
				if (annotation.submit() != AnnotationConstants.UNSETTED)
					features.setAttribute(ViewActionFeatures.SUBMIT, annotation.submit() == AnnotationConstants.TRUE);
			}
		}
	}

	private void readActionXml(SchemaClassElement iAction, XmlActionAnnotation iXmlNode) {
		// PROCESS DESCRIPTOR CFG
		// DESCRIPTOR ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT AND ANNOTATION
		// VALUES
		if (iXmlNode == null)
			return;

		DynaBean features = iAction.getFeatures(ASPECT_NAME);

		// ACTION FOUND IN DESCRIPTOR: ASSUME ITS VISIBILITY
		features.setAttribute(ViewElementFeatures.VISIBLE, true);

		if (iXmlNode.aspect(ASPECT_NAME) == null)
			return;

		XmlAspectAnnotation descriptor = iXmlNode.aspect(ASPECT_NAME);

		if (descriptor != null) {
			String label = descriptor.getAttribute(ViewBaseFeatures.LABEL);
			if (label != null) {
				features.setAttribute(ViewBaseFeatures.LABEL, label);
			}
			String description = descriptor.getAttribute(ViewBaseFeatures.DESCRIPTION);
			if (description != null) {
				features.setAttribute(ViewBaseFeatures.DESCRIPTION, description);
			}
			String visible = descriptor.getAttribute(ViewElementFeatures.VISIBLE);
			if (visible != null) {
				features.setAttribute(ViewElementFeatures.VISIBLE, new Boolean(visible));
			}
			String style = descriptor.getAttribute(ViewBaseFeatures.STYLE);
			if (style != null) {
				features.setAttribute(ViewBaseFeatures.STYLE, style);
			}
			String render = descriptor.getAttribute(ViewBaseFeatures.RENDER);
			if (render != null) {
				features.setAttribute(ViewBaseFeatures.RENDER, render);
			}
			String layout = descriptor.getAttribute(ViewBaseFeatures.LAYOUT);
			if (layout != null) {
				features.setAttribute(ViewBaseFeatures.LAYOUT, layout);
			}
			String bind = descriptor.getAttribute(ViewActionFeatures.BIND);
			if (bind != null) {
				features.setAttribute(ViewActionFeatures.BIND, new Boolean(bind));
			}
			String enabled = descriptor.getAttribute(ViewElementFeatures.ENABLED);
			if (enabled != null) {
				features.setAttribute(ViewElementFeatures.ENABLED, new Boolean(enabled));
			}
			String submit = descriptor.getAttribute(ViewActionFeatures.SUBMIT);
			if (submit != null) {
				features.setAttribute(ViewActionFeatures.SUBMIT, new Boolean(submit));
			}
		}
	}

	public void setActionDefaults(SchemaClassElement iAction) {
		DynaBean features = iAction.getFeatures(ASPECT_NAME);

		// CHECK RENDER AND LAYOUT MODES
		String classRender = (String) iAction.getEntity().getFeature(ASPECT_NAME, ViewBaseFeatures.RENDER);

		if (classRender != null)
			if (classRender.equals(ViewConstants.RENDER_MENU)) {
				// INSIDE A MENU: FORCE MENU RENDERING AND LAYOUT
				features.setAttribute(ViewBaseFeatures.RENDER, ViewConstants.RENDER_MENU);
				features.setAttribute(ViewBaseFeatures.LAYOUT, ViewConstants.LAYOUT_MENU);
			} else if (classRender.equals(ViewConstants.RENDER_ACCORDION))
				features.setAttribute(ViewBaseFeatures.LAYOUT, ViewConstants.LAYOUT_ACCORDION);
	}

	/**
	 * Display the form reading information from POJO received in the current desktop, in default position.
	 * 
	 * @param iContent
	 *          Object instance to display
	 */
	public void show(Object iContent) throws ViewException {
		show(iContent, null);
	}

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the current desktop.
	 * 
	 * @param iContent
	 * @param iPosition
	 * @throws ViewException
	 */
	public void show(Object iContent, String iPosition) throws ViewException {
		show(iContent, iPosition, null, null);
	}

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the current desktop.
	 * 
	 * @param iContent
	 * @param iPosition
	 * @throws ViewException
	 */
	public void show(Object iContent, String iPosition, Screen iScreen, SessionInfo iSession) throws ViewException {
		show(iContent, iPosition, iScreen, iSession, null);
	}

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the desktop received as the argument iDesktop.
	 * 
	 * @param iContent
	 *          Object instance to display
	 * @param iPosition
	 *          Desktop position where render the object
	 * @param iScreen
	 *          Desktop instance to use
	 * @throws Exception
	 */
	public void show(Object iContent, String iPosition, Screen iScreen, SessionInfo iSession, SchemaObject iSchema)
			throws ViewException {
		show(iContent, iPosition, iScreen, iSession, iSchema, false);
	}

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the desktop received as the argument iDesktop.
	 * 
	 * @param iContent
	 *          Object instance to display
	 * @param iPosition
	 *          Desktop position where render the object
	 * @param iScreen
	 *          Desktop instance to use
	 * @throws Exception
	 */
	public void show(Object iContent, String iPosition, Screen iScreen, SessionInfo iSession, SchemaObject iSchema, boolean iPushMode)
			throws ViewException {

		if (iScreen == null)
			// GET THE CURRENT ONE
			iScreen = FormViewer.getInstance().getScreen();

		List<ObjectWrapperListener> listeners = Controller.getInstance().getListeners(ObjectWrapperListener.class);
		if (listeners != null)
			for (ObjectWrapperListener l : listeners)
				iContent = l.getWrapperForObject(iContent);

		if (iContent == null) {
			if (iPosition == null)
				// GET CURRENT AREA FOR OBJECT
				iPosition = Controller.getInstance().getContext().getActiveArea();
			FormViewer.getInstance().display(iPosition, iContent, iScreen);
			return;
		}

		boolean currentSession = iPushMode ? false : iSession == null || iSession.equals(Roma.session().getActiveSessionInfo());

		if (iSchema == null && iContent != null) {
			SchemaClass cls = Roma.schema().getSchemaClass(iContent);
			iSchema = new SchemaObject(cls, iContent);
		}

		// SEARCH THE FORM TO VIEW BY ENTITY
		boolean hasToRenderTheForm = false;
		ContentForm form = (ContentForm) getFormByObject(iSession, iContent);
		if (form == null) {
			// CREATE IT
			form = ViewHelper.createForm(iSchema, null, iContent, iSession);
			if (currentSession)
				hasToRenderTheForm = true;
		} else {
			ViewHelper.invokeOnShow(iContent);
			iPosition = form.getScreenArea();
		}

		if (iPosition == null) {
			iPosition = (String) form.getSchemaObject().getFeature(ViewAspect.ASPECT_NAME, ViewBaseFeatures.LAYOUT);
		}

		if (iPosition == null && Controller.getInstance().getContext() != null) {
			iPosition = Controller.getInstance().getContext().getActiveArea();
		}

		if (iPosition == null)
			iPosition = ViewConstants.LAYOUT_DEFAULT;

		if (currentSession)
			// DISPLAY NOW
			Roma.component(ViewAspect.class).showForm(form, iPosition, iScreen);
		else
			// PUSH CHANGES
			Roma.aspect(ViewAspect.class).pushCommand(new ShowViewCommand(iSession, iScreen, form, iPosition));

		if (hasToRenderTheForm)
			form.renderContent();
	}

	/**
	 * Return the desktop for the current user.
	 * 
	 * @return Screen instance
	 */
	public Screen getScreen() {
		return FormViewer.getInstance().getScreen();
	}

	/**
	 * Return the screen for the user.
	 * 
	 * @param iUserSession
	 *          User session
	 * @return Screen instance
	 */
	public Screen getScreen(Object iUserSession) {
		return FormViewer.getInstance().getScreen(iUserSession);
	}

	/**
	 * Set the current screen.
	 * 
	 * @param iScreen
	 *          Screen instance to set for the current user.
	 */
	public void setScreen(Screen iScreen) {
		if (iScreen != null)
			FormViewer.getInstance().setScreen(iScreen);
	}

	/**
	 * Set the current screen.
	 * 
	 * @param iScreen
	 *          Screen instance to set for the current user.
	 */
	public void setScreen(Screen iScreen, SessionInfo iSession) {
		if (iScreen != null)
			pushCommand(new ChangeScreenViewCommand(iSession, iScreen));
	}

	/**
	 * Close the current form displayed. It applies only on Pop-up windows.
	 * 
	 * @param iUserObject
	 *          User Object to close
	 */
	public void close(Object iUserObject) {
		ViewComponent form = getFormByObject(iUserObject);
		if (form != null)
			form.close();
	}

	public void showComponent(Object iComponent, String iArea) {
		FormViewer.getInstance().display(iArea, iComponent);
	}

	public void onSessionCreating(SessionInfo iSession) {
		objectsForms.put(iSession, new IdentityHashMap<Object, ViewComponent>());
	}

	public void onSessionDestroying(SessionInfo iSession) {
		// REMOVE OBJECTS-AREA/COMPONENTS ASSOCIATION FOR CURRENT SESSION
		IdentityHashMap<Object, ViewComponent> forms = objectsForms.remove(iSession);
		if (forms != null) {
			if (log.isDebugEnabled())
				log.debug("[ObjectContext.onSessionDestroying] Removing components " + forms.values().size());

			for (ViewComponent c : forms.values()) {
				c.destroy();
			}

			if (log.isDebugEnabled())
				log.debug("[ObjectContext.onSessionDestroying] Removed " + forms.size() + " forms for session=" + iSession);
		}
	}

	/**
	 * Create an association between a User Object and a ContentForm. This association is useful to gather custom form information.
	 * 
	 * @param iUserObject
	 * @param iForm
	 */
	public void createObjectFormAssociation(Object iUserObject, ViewComponent iForm, SessionInfo iSession) {
		if (iSession == null)
			iSession = Roma.session().getActiveSessionInfo();

		if (iSession == null)
			throw new UserException(iForm.getContent(), "Cannot display the form since there is no active session");

		IdentityHashMap<Object, ViewComponent> userForms = objectsForms.get(iSession);
		userForms.put(iUserObject, iForm);
	}

	public void removeObjectFormAssociation(Object iUserObject, SessionInfo iSession) {
		if (iSession == null)
			if (iSession == null)
				iSession = Roma.session().getActiveSessionInfo();

		// REMOVE OBJECT-FORM ASSOCIATION
		IdentityHashMap<Object, ViewComponent> userForms = objectsForms.get(iSession);
		if (userForms != null) {
			if (log.isDebugEnabled())
				log.debug("[ViewAspectAbstract.removeObjectFormAssociation] Flushing form: " + iUserObject);
			userForms.remove(iUserObject);
		}
	}

	/**
	 * Return the form associated to a User Object.
	 * 
	 * @param iUserObject
	 * @return ContentComponent instance if any, otherwise null
	 */
	public ViewComponent getFormByObject(Object iUserObject) {
		return getFormByObject(null, iUserObject);
	}

	/**
	 * Return the form associated to a User Object.
	 * 
	 * @param iSession
	 *          User session, null to get the current active
	 * @param iUserObject
	 * @return ContentComponent instance if any, otherwise null
	 */
	public ViewComponent getFormByObject(Object iSession, Object iUserObject) {
		if (iSession == null)
			iSession = Roma.component(SessionAspect.class).getActiveSessionInfo();

		IdentityHashMap<Object, ViewComponent> userForms = objectsForms.get(iSession);

		if (userForms == null)
			return null;

		List<ObjectWrapperListener> listeners = Controller.getInstance().getListeners(ObjectWrapperListener.class);
		if (listeners != null) {
			ViewComponent o;
			for (ObjectWrapperListener l : listeners) {
				o = (ViewComponent) l.findWrapperInCollection(userForms.values(), iUserObject);
				if (o != null)
					// FOUND: RETURN IT
					return o;

				// NOT FOUND: CONTINUE WITH THE NEXT LISTENER (IF ANY)
			}
		}

		return userForms.get(iUserObject);
	}

	/**
	 * Return the first form of the declaring Class iClassOfObject.
	 * 
	 * @param iSession
	 *          User session, null to get the current active
	 * @param iClassOfObject
	 *          The Class of the object
	 * @return ContentComponent instance if any, otherwise null
	 */
	public List<ViewComponent> getFormsByClass(Object iSession, SchemaClass iClassOfObject) {
		if (iSession == null)
			iSession = Roma.component(SessionAspect.class).getActiveSessionInfo();

		IdentityHashMap<Object, ViewComponent> userForms = objectsForms.get(iSession);

		if (userForms == null)
			return null;

		List<ViewComponent> result = new ArrayList<ViewComponent>();
		for (Map.Entry<Object, ViewComponent> entry : userForms.entrySet()) {
			if (entry.getValue().getSchemaObject().getSchemaClass().equals(iClassOfObject))
				result.add(entry.getValue());
		}
		return result;
	}

	/**
	 * Return all the forms for all the active session that render POJOs of class iClass.
	 * 
	 * @param iClass
	 *          Class to search
	 * @return Map<SessionInfo, ContentComponent> with all entries that are handling POJOs of class iClass
	 */
	public Map<SessionInfo, ViewComponent> getFormsByClass(SchemaClass iClass) {
		Map<SessionInfo, ViewComponent> result = new HashMap<SessionInfo, ViewComponent>();

		Map<Object, ViewComponent> perSessionObjects;

		SchemaClass cls;
		for (Map.Entry<SessionInfo, IdentityHashMap<Object, ViewComponent>> entry : objectsForms.entrySet()) {
			perSessionObjects = entry.getValue();
			for (Map.Entry<Object, ViewComponent> formEntry : perSessionObjects.entrySet()) {
				if (formEntry.getKey() == null)
					continue;

				try {
					cls = Roma.schema().getSchemaClass(formEntry.getKey());

					if (cls.extendsClass(iClass))
						result.put(entry.getKey(), formEntry.getValue());
				} catch (Exception e) {
				}
			}
		}
		return result;
	}

	/**
	 * Refresh the changed objects.
	 */
	public void signalUpdatedClass(SchemaClass iSchemaClass, File iFile) {
		// OVERWRITE LIVING OBJECT SCHEMA OBJECTS BY COPYING NEW DEFINITION AND REFRESH ITS
		Map<SessionInfo, ViewComponent> forms = getFormsByClass(iSchemaClass);
		for (Map.Entry<SessionInfo, ViewComponent> entry : forms.entrySet()) {
			// entry.getValue().getSchemaObject().copyDefinition(iSchemaClass);

			Roma.aspect(ViewAspect.class).pushCommand(new RefreshViewCommand(entry.getKey(), (ViewComponent) entry.getValue()));
		}
	}

	/**
	 * Get the schema object associated to the current POJO.
	 * 
	 * @param iUserObject
	 *          User POJO
	 * @return SchemaObject instance
	 * @throws ConfigurationNotFoundException
	 */
	public SchemaObject getSchemaObject(Object iUserObject) throws ConfigurationNotFoundException {
		ViewComponent form = getFormByObject(iUserObject);
		if (form == null)
			return null;

		return form.getSchemaObject();
	}

	public String aspectName() {
		return ASPECT_NAME;
	}
}
