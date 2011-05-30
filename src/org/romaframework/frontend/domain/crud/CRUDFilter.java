package org.romaframework.frontend.domain.crud;

import org.romaframework.aspect.persistence.QueryByFilter;
import org.romaframework.frontend.domain.entity.ComposedEntityInstance;

public class CRUDFilter<T> extends ComposedEntityInstance<T> {

	public CRUDFilter(T iEntity) {
		super(iEntity);
	}

	protected QueryByFilter getAdditionalFilter() {
		return null;
	}

}
