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

package org.romaframework.aspect.view.form;

import org.apache.commons.pool.KeyedObjectPool;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaObject;

/**
 * Deprecated. Now all form management is responsability of ViewAspect.
 * 
 * @deprecated
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
@Deprecated
public class FormPool {

	/**
	 * Set the maximum active forms the pool can manage before to be "exhausted" and wait for a new form instance.
	 * 
	 * @param iMaxActiveFormInstances
	 */
	public void setMaxActiveFormInstances(int iMaxActiveFormInstances) {
	}

	public void init() {
	}

	public ContentForm getForm(SchemaObject iSchema, SchemaField iField, Object iUserObject) {
		return null;
	}

	public void releaseForm(ContentForm iFormInstance) {
	}

	public KeyedObjectPool getPool() {
		return null;
	}
}
