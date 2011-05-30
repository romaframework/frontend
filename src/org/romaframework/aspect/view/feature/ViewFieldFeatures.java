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

package org.romaframework.aspect.view.feature;

public class ViewFieldFeatures extends ViewElementFeatures {
	public ViewFieldFeatures() {
		defineAttribute(SELECTION_FIELD, null);
		defineAttribute(SELECTION_MODE, SELECTION_MODE_VALUE);
		defineAttribute(FORMAT, null);
		defineAttribute(DEPENDS, null);
		defineAttribute(DEPENDS_ON, null);
		defineAttribute(DISPLAY_WITH, null);
	}

	public static final String	DEPENDS_ON						= "dependsOn";
	public static final String	DEPENDS								= "depends";
	public static final String	SELECTION_FIELD				= "selectionField";
	public static final String	SELECTION_MODE				= "selectionMode";
	public static final String	FORMAT								= "format";
	public static final String	DISPLAY_WITH					= "displayWith";

	public static final byte		SELECTION_MODE_VALUE	= 0;
	public static final byte		SELECTION_MODE_INDEX	= 1;
}
