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

package org.romaframework.aspect.flow.feature;

import org.romaframework.core.util.DynaBean;

public class FlowActionFeatures extends DynaBean {
	public FlowActionFeatures() {
		defineAttribute(NEXT, null);
		defineAttribute(POSITION, null);
		defineAttribute(ERROR, null);
		defineAttribute(BACK, null);
		defineAttribute(CONFIRM_REQUIRED, null);
		defineAttribute(CONFIRM_MESSAGE, null);
	}

	public static final String	NEXT			= "next";
	public static final String	POSITION	= "position";
	public static final String	ERROR			= "error";
	public static final String	BACK			= "back";
	
	public static final String	CONFIRM_REQUIRED = "confirmRequired";
	public static final String	CONFIRM_MESSAGE = "confirmMessage";
}
