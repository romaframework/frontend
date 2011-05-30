package org.romaframework.aspect.view.event;

import java.lang.reflect.InvocationTargetException;

import org.romaframework.core.Roma;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;

public class SchemaEventReset extends SchemaEvent {

	private static final long	serialVersionUID	= -9052552383144883723L;

	public SchemaEventReset(SchemaField field) {
		super(field, "reset",null);
	}

	@Override
	public Object invokeFinal(Object iContent, Object[] params) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if (this.field == null) {
			return false;
		}
		this.field.setValue(iContent, null);
		Roma.fieldChanged(iContent, this.field.getName());
		return true;
	}

}
