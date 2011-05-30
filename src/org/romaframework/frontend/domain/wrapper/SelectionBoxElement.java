/*
 *
 * Copyright 2010 Luca Molino (luca.molino--AT--assetdata.it)
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
package org.romaframework.frontend.domain.wrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.ConfigurationException;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.persistence.PersistenceAspect;
import org.romaframework.aspect.persistence.QueryByFilter;
import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.core.Roma;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaHelper;

/**
 * Wrapper view class that implements a List selection between available
 * elements and selected elements
 * 
 * @author luca.molino
 * 
 */
public class SelectionBoxElement<T> {

	protected Object instance;

	protected String fieldName;

	@ViewField(visible = AnnotationConstants.FALSE)
	protected T entity;

	@ViewField(visible = AnnotationConstants.FALSE)
	protected Collection<T> allElements = null;

	@ViewField(render = ViewConstants.RENDER_LIST, selectionField = "availableElementSelected", enabled = AnnotationConstants.FALSE)
	protected Collection<T> availableElements = new ArrayList<T>();

	@ViewField(visible = AnnotationConstants.FALSE)
	protected T availableElementSelected;

	@ViewField(render = ViewConstants.RENDER_LIST, selectionField = "selectedElementSelected", enabled = AnnotationConstants.FALSE)
	protected List<T> selectedElements = new ArrayList<T>();

	@ViewField(visible = AnnotationConstants.FALSE)
	protected T selectedElementSelected;

	public SelectionBoxElement(Object iInstance, String iFieldName,
			List<T> sourceList) throws IllegalArgumentException {
		allElements = sourceList;
		init(iInstance, iFieldName);
	}

	protected SelectionBoxElement(Object iInstance, String iFieldName)
			throws IllegalArgumentException {
		SchemaClass genericClass = SchemaHelper.getSuperclassGenericType(Roma
				.schema().getSchemaClass(this.getClass()));
		allElements = loadElements((Class<T>) genericClass.getLanguageType());
		init(iInstance, iFieldName);
	}

	protected SelectionBoxElement(Object iInstance, String iFieldName,
			Class<T> iClass) throws IllegalArgumentException {
		allElements = loadElements(iClass);
		init(iInstance, iFieldName);
	}

	protected List<T> loadElements(Class<T> iClass) {
		PersistenceAspect db = Roma.context().persistence();
		QueryByFilter filter = new QueryByFilter(iClass);
		filter.setMode(PersistenceAspect.FULL_MODE_LOADING);
		filter.setStrategy(PersistenceAspect.STRATEGY_DETACHING);
		return db.query(filter);
	}

	public Collection<T> getAvailableElements() {
		return availableElements;

	}

	public void setAvailableElements(List<T> elemntiDisponibili) {
		this.availableElements = elemntiDisponibili;
	}

	public T getAvailableElementSelected() {
		return availableElementSelected;
	}

	public void setAvailableElementSelected(T availableElementSelected)
			throws ConfigurationException {
		this.availableElementSelected = availableElementSelected;
		selectElement();
	}

	public List<T> getSelectedElements() {
		return selectedElements;
	}

	public void setSelectedElements(List<T> elementiSelezionate) {
		this.selectedElements = elementiSelezionate;
	}

	public T getSelectedElementSelected() {
		return selectedElementSelected;
	}

	public void setSelectedElementSelected(T selectedElementSelected)
			throws ConfigurationException {
		this.selectedElementSelected = selectedElementSelected;
		unselectElement();
	}

	public void addAll() throws IllegalArgumentException {
		List<T> toAdd = new ArrayList<T>(availableElements);
		for (T element : toAdd) {
			availableElementSelected = element;
			selectElement();
		}
	}

	public void removeAll() throws IllegalArgumentException {
		List<T> selected = new ArrayList<T>(selectedElements);
		for (T element : selected) {
			selectedElementSelected = element;
			unselectElement();
		}
	}

	@ViewAction(visible = AnnotationConstants.FALSE)
	public void selectElement() throws IllegalArgumentException {
		if (availableElementSelected == null) {
			return;
		}

		boolean added = false;

		if (!selectedElements.contains(availableElementSelected)) {
			selectedElements.add(availableElementSelected);
			availableElements.remove(availableElementSelected);
			added = true;
		}

		if (added) {
			Object value = SchemaHelper.getFieldValue(instance, fieldName);
			if (value instanceof Collection<?>) {
				((Collection<T>) value).add(availableElementSelected);
				SchemaHelper.setFieldValue(instance, fieldName, value);
			} else {
				throw new IllegalArgumentException("Field " + fieldName
						+ " isn't a collection.");
			}
		}
		availableElementSelected = null;
		Roma.fieldChanged(this, "selectedElements");
		Roma.fieldChanged(this, "availableElements");

	}

	@ViewAction(visible = AnnotationConstants.FALSE)
	public void unselectElement() throws IllegalArgumentException {
		if (selectedElementSelected == null) {
			return;
		}

		selectedElements.remove(selectedElementSelected);
		if (!availableElements.contains(selectedElementSelected)) {
			availableElements.add(selectedElementSelected);
		}

		Object value = SchemaHelper.getFieldValue(instance, fieldName);
		if (value instanceof Collection<?>) {
			((Collection<T>) value).remove(selectedElementSelected);
			SchemaHelper.setFieldValue(instance, fieldName, value);
		} else {
			throw new IllegalArgumentException("Field " + fieldName
					+ " isn't a collection.");
		}
		selectedElementSelected = null;
		Roma.fieldChanged(this, "selectedElements");
		Roma.fieldChanged(this, "availableElements");

	}

	private void init(Object iInstance, String iFieldName)
			throws IllegalArgumentException {
		availableElements.addAll(allElements);
		instance = iInstance;
		fieldName = iFieldName;
		Object value = SchemaHelper.getFieldValue(instance, fieldName);
		if (value instanceof Collection<?>) {
			for (T elem : ((Collection<T>) value)) {
				selectedElements.add(elem);
				availableElements.remove(elem);
			}
		} else {
			throw new IllegalArgumentException("Field " + fieldName
					+ " isn't a collection.");
		}
	}

}
