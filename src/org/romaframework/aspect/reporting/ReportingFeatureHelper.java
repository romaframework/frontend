/*
 * Copyright 2006 Giordano Maestro (giordano.maestro--at--assetdata.it)
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
package org.romaframework.aspect.reporting;

import org.romaframework.aspect.i18n.I18NHelper;
import org.romaframework.aspect.reporting.feature.ReportingBaseFeatures;
import org.romaframework.aspect.reporting.feature.ReportingFieldFeatures;
import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.feature.ViewBaseFeatures;
import org.romaframework.aspect.view.feature.ViewElementFeatures;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaFeatures;
import org.romaframework.core.schema.SchemaField;

public class ReportingFeatureHelper {

	public static Boolean isVisibleField(SchemaField iSchemaField) {
		Boolean result = (Boolean) iSchemaField.getFeature(ReportingFieldFeatures.VISIBLE);
		if (result != null) {
			return result;
		} else {
			result = (Boolean) iSchemaField.getFeature(ViewElementFeatures.VISIBLE);
			if (result != null) {
				return result;
			}
		}
		return true;
	}

	public static Boolean isRenderImage(SchemaField iSchemaField) {
		String render = getRender(iSchemaField);
		if (ViewConstants.RENDER_IMAGE.equals(render)) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isRenderHtml(SchemaFeatures iSchemaField) {
		String render = getRender(iSchemaField);
		if (ViewConstants.RENDER_HTML.equals(render)) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isRenderRTF(SchemaFeatures iSchemaField) {
		String render = getRender(iSchemaField);
		if (ViewConstants.RENDER_RICHTEXT.equals(render)) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isRenderChart(SchemaField iSchemaField) {
		String render = getRender(iSchemaField);
		if (ViewConstants.RENDER_CHART.equals(render)) {
			return true;
		} else {
			return false;
		}
	}

	public static String getLayout(SchemaFeatures iFeatures) {
		String fieldLayout = (String) iFeatures.getFeature(ReportingBaseFeatures.LAYOUT);
		if (fieldLayout == null) {
			fieldLayout = (String) iFeatures.getFeature(ViewBaseFeatures.LAYOUT);
		}
		return fieldLayout;
	}

	public static String getRender(SchemaFeatures iFeatures) {
		String fieldRender = (String) iFeatures.getFeature(ReportingBaseFeatures.RENDER);
		if (fieldRender == null) {
			fieldRender = (String) iFeatures.getFeature(ViewBaseFeatures.RENDER);
		}
		return fieldRender;
	}

	public static String getLabel(SchemaFeatures iFeatures) {
		String fieldRender = (String) iFeatures.getFeature(ReportingBaseFeatures.LABEL);
		if (fieldRender == null) {
			fieldRender = (String) iFeatures.getFeature(ViewBaseFeatures.LABEL);
		}
		return fieldRender;
	}

	public static String getI18NLabel(SchemaClassElement schemaField) {
		return I18NHelper.getLabel(schemaField, getLabel(schemaField));
	}

	public static String getI18NLabel(SchemaClassDefinition schemaClass) {
		return I18NHelper.getLabel(schemaClass, getLabel(schemaClass));
	}

}
