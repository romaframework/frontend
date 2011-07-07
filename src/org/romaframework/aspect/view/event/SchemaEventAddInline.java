package org.romaframework.aspect.view.event;

import java.lang.reflect.InvocationTargetException;

import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaHelper;

public class SchemaEventAddInline extends SchemaEvent {

	private static final long	serialVersionUID	= -4441941839016286308L;

	public SchemaEventAddInline(SchemaField field) {
		super(field, COLLECTION_ADD_INLINE_EVENT, null);
	}

	@Override
	public Object invokeFinal(Object iContent, Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object obj;
		try {
			obj = SchemaHelper.createObject(field.getEmbeddedType());
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
		SchemaHelper.insertElements(field, iContent, new Object[] { obj });
		return obj;
	}

}
