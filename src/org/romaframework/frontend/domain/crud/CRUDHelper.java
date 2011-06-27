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

package org.romaframework.frontend.domain.crud;

import org.romaframework.aspect.flow.FlowAspect;
import org.romaframework.core.Roma;
import org.romaframework.core.binding.Bindable;
import org.romaframework.core.domain.entity.ComposedEntity;
import org.romaframework.core.factory.GenericFactory;
import org.romaframework.core.flow.ObjectContext;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.reflection.SchemaClassReflection;

/**
 * Helper class to resolve common CRUD tasks.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public class CRUDHelper {

	/**
	 * Show the form specified.
	 * 
	 * @param iSelectObj
	 * @param iSourceObject
	 * @param iSourceFieldName
	 * @return
	 */

	private static long	popupIndex	= 0;

	/**
	 * Show the form of the class specified.
	 * 
	 * @param <T>
	 *          Selection form
	 * @param iClass
	 * @param iEntityInstance
	 * @return
	 */
	public static <T extends ComposedEntity<?>> T show(Class<T> iClass, Object iEntityInstance) {
		// GET THE FORM
		T formObject = ObjectContext.getInstance().getObject(iClass, iEntityInstance);

		// SHOW THE FORM
		Roma.aspect(FlowAspect.class).forward(formObject);

		// RETURN IT TO THE CALLER TO SET ADDITIONAL INFO
		return formObject;
	}

	/**
	 * Show the form of the class specified.
	 * 
	 * @param <T>
	 *          Selection form
	 * @param iClass
	 * @param iSourceObject
	 * @param iSourceFieldName
	 * @return
	 */
	public static <T extends Bindable> T show(Class<T> iClass, Object iSourceObject, String iSourceFieldName) {
		// GET THE FORM
		return show(iClass, iSourceObject, iSourceFieldName, null);
	}

	/**
	 * Show the form of the class specified.
	 * 
	 * @param <T>
	 *          Selection form
	 * @param iClass
	 * @param iSourceObject
	 * @param iSourceFieldName
	 * @param iCaller
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Bindable> T show(Class<T> iClass, Object iSourceObject, String iSourceFieldName, Object iCaller) {
		// GET THE FORM
		T formObject = ObjectContext.getInstance().getObject(iClass);
		return (T) show(formObject, iSourceObject, iSourceFieldName);
	}

	public static Bindable show(Bindable iSelectObj, Object iSourceObject, String iSourceFieldName) {
		// GET SOURCE INFO
		iSelectObj.setSource(iSourceObject, iSourceFieldName);

		// SHOW THE FORM
		Roma.aspect(FlowAspect.class).forward(iSelectObj, "screen:popup:" + "openedByBinder" + popupIndex);
		popupIndex++;

		// RETURN IT TO THE CALLER TO SET ADDITIONAL INFO
		return iSelectObj;
	}

	public static SchemaClass getCRUDSchemaClass(SchemaClass iClass, String iSuffix) {
		// TRY TO DISPLAY CRUD CLASS BY SUFFIX
		return getCRUDSchemaClass(iClass.getName(), iSuffix);
	}

	public static SchemaClass getCRUDSchemaClass(Class<?> iClass, String iSuffix) {
		return getCRUDSchemaClass(iClass.getSimpleName(), iSuffix);
	}

	public static Class<?> getCRUDClass(Class<?> iClass, String iSuffix) {
		SchemaClassReflection cls = (SchemaClassReflection) getCRUDSchemaClass(iClass.getSimpleName(), iSuffix);
		if (cls != null)
			return cls.getLanguageType();

		return null;
	}

	public static SchemaClass getCRUDSchemaClass(String iClassName, String iSuffix) {
		String entityName = iClassName;
		SchemaClass entity = null;
		GenericFactory<?> gf = Roma.factory(entityName);
		if (gf != null) {
			entity = gf.getEntitySchemaClass();
		}

		SchemaClass lastChecked = null;
		do {
			if (entity != null) {
				entityName = entity.getName();
				lastChecked = entity;
				entity = entity.getSuperClass();
			}
			entityName += iSuffix;
			if(Roma.schema().existsSchemaClass(entityName)){
				return Roma.schema().getSchemaClass(entityName);
			}
		} while (entity != null && lastChecked!=null && !lastChecked.getName().equals(iClassName));

		// NOT FOUND
		return null;
	}

	public static SchemaClass getCRUDSelect(SchemaClass iClass) {
		return getCRUDSchemaClass(iClass, CRUDConstants.SELECT_EXTENSION);
	}

	public static SchemaClass getCRUDSelect(Class<?> iClass) {
		return getCRUDSelect(Roma.schema().getSchemaClass(iClass));
	}

	public static SchemaClass getCRUDMain(Class<?> iClass) {
		return getCRUDMain(Roma.schema().getSchemaClass(iClass));
	}

	public static SchemaClass getCRUDMain(SchemaClass iClass) {
		return getCRUDSchemaClass(iClass, CRUDConstants.MAIN_EXTENSION);
	}

	public static SchemaClass getCRUDInstance(SchemaClass iClass) {
		return getCRUDSchemaClass(iClass, CRUDConstants.INSTANCE_EXTENSION);
	}

	public static SchemaClass getCRUDInstance(Class<?> iClass) {
		return getCRUDInstance(Roma.schema().getSchemaClass(iClass));
	}

	public static SchemaClass getCRUDListable(SchemaClass iClass) {
		return getCRUDSchemaClass(iClass, CRUDConstants.LISTABLE_EXTENSION);
	}

	public static SchemaClass getCRUDListable(Class<?> iClass) {
		return getCRUDListable(Roma.schema().getSchemaClass(iClass));
	}

	public static SchemaClass getCRUDFilter(SchemaClass iClass) {
		return getCRUDSchemaClass(iClass, CRUDConstants.FILTER_EXTENSION);
	}

	public static SchemaClass getCRUDFilter(Class<?> iClass) {
		return getCRUDFilter(Roma.schema().getSchemaClass(iClass));
	}
}