package org.romaframework.frontend.domain.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.core.CoreAspect;
import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.core.annotation.CoreClass;
import org.romaframework.aspect.core.annotation.CoreField;
import org.romaframework.aspect.core.feature.CoreFieldFeatures;
import org.romaframework.aspect.validation.CustomValidation;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.ViewCallback;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.aspect.view.feature.ViewFieldFeatures;
import org.romaframework.aspect.view.form.ViewComponent;
import org.romaframework.core.Roma;
import org.romaframework.core.domain.entity.ComposedEntity;
import org.romaframework.core.entity.EntityHelper;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaHelper;

@CoreClass(orderActions = "add remove")
public abstract class CollectionWrapper<T> implements ViewCallback, CustomValidation {
	protected Object													object;
	protected String													selectionFieldName;
	protected boolean													lazy	= false;

	@CoreField(useRuntimeType = AnnotationConstants.TRUE)
	protected List<? extends ComposedEntity>	elements;
	protected Collection<T>										domainElements;
	@ViewField(visible = AnnotationConstants.FALSE)
	protected ComposedEntity<T>[]							selection;
	protected SchemaClass											listClass;
	protected Class<T>												clazz;

	protected static Log											log		= LogFactory.getLog(CollectionWrapper.class);

	public void onShow() {
		SchemaClass embType = (SchemaClass) Roma.getFieldFeature(this, CoreAspect.ASPECT_NAME, "elements",
				CoreFieldFeatures.EMBEDDED_TYPE);

		if (embType == null || embType.getLanguageType().equals(Object.class))
			Roma.setFieldFeature(this, CoreAspect.ASPECT_NAME, "elements", CoreFieldFeatures.EMBEDDED_TYPE, listClass);

		Roma.fieldChanged(this, "elements");

		if (object != null && selectionFieldName != null) {
			Roma.setFieldFeature(object, ViewAspect.ASPECT_NAME, selectionFieldName, ViewFieldFeatures.VISIBLE, Boolean.FALSE);
		}

		if (lazy)
			load();

		if (elements != null && elements.size() > 0)
			setSelection(elements.get(0));
	}

	public void add() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ViewComponent component = Roma.aspect(ViewAspect.class).getFormByObject(this);
		SchemaHelper.invokeEvent(component.getContainerComponent(), component.getSchemaField().getName(),
				SchemaEvent.COLLECTION_ADD_EVENT);
	}

	public void remove() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ViewComponent component = Roma.aspect(ViewAspect.class).getFormByObject(this);
		SchemaHelper.invokeEvent(component, component.getSchemaField().getName(), SchemaEvent.COLLECTION_REMOVE_EVENT);
	}

	public ComposedEntity<T>[] getSelection() {
		return selection;
	}

	public void setSelection(ComposedEntity<T> iSelection) {
		setSelection(new ComposedEntity[] { iSelection });
	}

	public void setSelection(ComposedEntity<T>[] iSelection) {
		validate();

		this.selection = iSelection;
	}

	public void onDispose() {
	}

	/**
	 * Overwrite this method to use custom filters on search query.
	 */
	@SuppressWarnings("unchecked")
	@ViewAction(visible = AnnotationConstants.FALSE)
	public void load() {
		if (object != null && selectionFieldName != null) {
			setDomainElements((Collection<T>) SchemaHelper.getFieldValue(Roma.schema().getSchemaClass(object.getClass()),
					selectionFieldName, object));
		}
	}

	public void setDomainElements(Collection<T> iElements) {
		domainElements = iElements;

		if (iElements == null)
			elements = null;
		else
			try {
				elements = EntityHelper.createComposedEntityList(iElements, listClass);
			} catch (Exception e) {
				log.error("[CollectionWrapper.setDomainElements] Error on creating wrapper class for result. Class: " + listClass, e);
			}
		Roma.fieldChanged(this, "elements");
	}
}
