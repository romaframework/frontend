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

package org.romaframework.aspect.view;

import java.util.Collection;

/**
 * Callback interface to catch events from the rendering engine at View Aspect level.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public interface ObjectWrapperListener {
	/**
	 * It's called by the rendering engine before the object is displayed. Implementing this method allow to change the object to
	 * display. Usually used to wrap an object instead of original one.
	 */
	public Object getWrapperForObject(Object iObject);

	public Object getObjectFromWrapper(Object iObject);

	public Object findWrapperInCollection(Collection<? extends Object> iCollection, Object iObject);
}