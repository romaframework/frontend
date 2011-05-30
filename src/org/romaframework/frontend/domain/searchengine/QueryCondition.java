/*
 *
 * Copyright 2009 Luca Molino (luca.molino--AT--assetdata.it)
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
package org.romaframework.frontend.domain.searchengine;

import java.util.Calendar;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.aspect.view.feature.ViewActionFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.frontend.domain.searchengine.filter.BaseFilter;

/**
 * @author molino
 * 
 */
public class QueryCondition implements QueryItem {

	@ViewField(render = ViewConstants.RENDER_OBJECTEMBEDDED)
	protected BaseFilter<?>		filter;

	protected Long						timestamp	= Calendar.getInstance().getTimeInMillis();

	@ViewField(visible = AnnotationConstants.FALSE)
	protected QueryOperation	operation;

	public QueryCondition(BaseFilter<?> iEntity, QueryOperation iOperation) {
		filter = iEntity;
		operation = iOperation;
	}

	@ViewField(visible = AnnotationConstants.FALSE)
	public BaseFilter<?> getEntity() {
		return filter;
	}

	public void setEntity(BaseFilter<?> entity) {
		this.filter = entity;
	}

	public QueryOperation getOperation() {
		return operation;
	}

	public void setOperation(QueryOperation operation) {
		this.operation = operation;
	}

	@ViewField(render = ViewConstants.RENDER_BUTTON, label = "")
	public String getText() {
		StringBuffer text = new StringBuffer(filter.toString() + ": ");
		Object entity = filter.getEntity();
		SchemaClass schema = Roma.schema().getSchemaClass(filter.getEntityClass());
		for (String fieldName : schema.getFields().keySet()) {
			Object value = schema.getField(fieldName).getValue(entity);
			if (value != null)
				text.append(fieldName + "=" + value);
		}
		if (text.length() > 20) {
			text.delete(18, text.length());
			text.append("...");
		}
		return text.toString();
	}

	public void onText() {
		showHideActions();
	}

	@ViewField(layout = "form://itemActions", style = "searchEngineAddRightFilter")
	public void addRight() {
		operation.showFilters();
		operation.setPositionToAdd(getPosition() + 1);
		hideActions();
	}

	@ViewField(layout = "form://itemActions", style = "searchEngineAddLeftFilter")
	public void addLeft() {
		operation.showFilters();
		Integer position = getPosition();
		if (position > 2 && operation.getOperation().get(position - 1) instanceof QuerySubOperationDelimiter)
			position++;
		operation.setPositionToAdd(position - 1);
		hideActions();
	}

	@ViewField(layout = "form://itemActions", style = "searchEngineRemoveFilter")
	public void remove() {
		operation.removeFilter(getPosition());
		hideActions();
	}

	@ViewField(layout = "form://itemActions", style = "searchEngineEditFilter")
	public void edit() {
		operation.setEditCondition(filter);
		operation.showEditFilter();
		hideActions();
	}

	public void onDispose() {
	}

	public void onShow() {
		hideActions();
	}

	@Override
	public String toString() {
		return getText();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0!=null && getClass().equals(arg0.getClass())) {
			QueryCondition other = (QueryCondition) arg0;
			return filter.equals(other.getEntity()) && timestamp == other.timestamp;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return filter.hashCode() + timestamp.hashCode();
	}

	private void hideActions() {
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "addRight", ViewActionFeatures.VISIBLE, Boolean.FALSE);
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "addLeft", ViewActionFeatures.VISIBLE, Boolean.FALSE);
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "remove", ViewActionFeatures.VISIBLE, Boolean.FALSE);
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "edit", ViewActionFeatures.VISIBLE, Boolean.FALSE);
	}

	private void showHideActions() {
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "addRight", ViewActionFeatures.VISIBLE,
				!(Boolean) Roma.getActionFeature(this, ViewAspect.ASPECT_NAME, "addRight", ViewActionFeatures.VISIBLE));
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "addLeft", ViewActionFeatures.VISIBLE,
				!(Boolean) Roma.getActionFeature(this, ViewAspect.ASPECT_NAME, "addLeft", ViewActionFeatures.VISIBLE));
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "remove", ViewActionFeatures.VISIBLE,
				!(Boolean) Roma.getActionFeature(this, ViewAspect.ASPECT_NAME, "remove", ViewActionFeatures.VISIBLE));
		Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "edit", ViewActionFeatures.VISIBLE,
				!(Boolean) Roma.getActionFeature(this, ViewAspect.ASPECT_NAME, "edit", ViewActionFeatures.VISIBLE));
	}

	private Integer getPosition() {
		return operation.getOperation().indexOf(this);
	}

}
