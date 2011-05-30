/*
 * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.romaframework.aspect.reporting.feature;

import org.romaframework.aspect.reporting.ReportingConstants;

public class ReportingClassFeatures extends ReportingBaseFeatures {

	public ReportingClassFeatures() {
		super();
		defineAttribute(ENTITY, null);
		defineAttribute(EXPLICIT_ELEMENTS, Boolean.FALSE);
		defineAttribute(INHERIT_VIEW_CONFIGURATION, Boolean.TRUE);
		defineAttribute(DOCUMENT_TYPE, ReportingConstants.DOCUMENT_TYPE_PDF);
	}


	public static final String	DOCUMENT_TYPE								= "documentType";

	public static final String	ENTITY											= "entity";

	public static final String	EXPLICIT_ELEMENTS						= "explicitElements";

	public static final String	INHERIT_VIEW_CONFIGURATION	= "inheritViewConfiguration";

	

}
