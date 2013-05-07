package org.romaframework.frontend.domain.crud;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.core.annotation.CoreField;

public class GenericFilter<T> extends CRUDFilter<T> {

	public GenericFilter(T iEntity) {
		super(iEntity);
	}

	@CoreField(useRuntimeType = AnnotationConstants.TRUE)
	public T getEntity() {
		return super.getEntity();
	}

	
}
