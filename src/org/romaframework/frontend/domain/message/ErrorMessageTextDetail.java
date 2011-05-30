/*
 * Copyright 2009 Emanuele Tagliaferri (emanuele.tagliaferri--at--assetdata.it)
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
package org.romaframework.frontend.domain.message;

import org.romaframework.aspect.core.annotation.CoreClass;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.aspect.view.feature.ViewActionFeatures;
import org.romaframework.core.Roma;
import org.romaframework.core.config.ApplicationConfiguration;
import org.romaframework.frontend.RomaFrontend;

/**
 * 
 * @author Emanuele Tagliaferri (emanuele.tagliaferri--at--assetdata.it)
 * 
 */
@CoreClass(orderFields = "icon message customMessage detail")
public class ErrorMessageTextDetail extends MessageTextDetail {

	private Throwable	exception;

	@ViewField(render = ViewConstants.RENDER_TEXTAREA)
	private String		customMessage;

	public ErrorMessageTextDetail(String iId, String iTitle, Throwable exception) {
		this(iId, iTitle, null, null, exception);
	}

	public ErrorMessageTextDetail(String iId, String iTitle, MessageResponseListener iListener, Throwable exception) {
		this(iId, iTitle, iListener, null, exception);
	}

	public ErrorMessageTextDetail(String iId, String iTitle, MessageResponseListener iListener, String iMessage, Throwable exception) {
		super(iId, iTitle, iListener, iMessage);
		this.exception = exception;
	}

	@Override
	public void onShow() {
		super.onShow();
		if (!Roma.existComponent(ErrorReporter.class)) {
			Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "sendReport", ViewActionFeatures.VISIBLE, Boolean.FALSE);
			Roma.setFieldFeature(this, ViewAspect.ASPECT_NAME, "customMessage", ViewActionFeatures.VISIBLE, Boolean.FALSE);
		} else {
			Roma.setActionFeature(this, ViewAspect.ASPECT_NAME, "sendReport", ViewActionFeatures.VISIBLE, Boolean.TRUE);
			Roma.setFieldFeature(this, ViewAspect.ASPECT_NAME, "customMessage", ViewActionFeatures.VISIBLE, Boolean.TRUE);
		}
		if(Boolean.FALSE.equals(Roma.component(ApplicationConfiguration.class).isApplicationDevelopment())){
			Roma.setFieldFeature(this, ViewAspect.ASPECT_NAME, "detail", ViewActionFeatures.VISIBLE, Boolean.FALSE);
		}
	}

	public void sendReport() {
		try {
			Roma.component(ErrorReporter.class).reportError(getCustomMessage(), exception);
			String msg = Roma.i18n().resolveString("$ErrorMessageTextDetail.errorReported");
			MessageOk message = new MessageOk("Error reported", msg, null, msg);
			RomaFrontend.flow().forward(message, "screen:popup:sendOk");
		} catch (Exception e) {
			if (Roma.component(ApplicationConfiguration.class).isApplicationDevelopment()) {
				MessageOk message = new MessageOk("Impossible to report error", e.getMessage());
				message.setMessage(e.getMessage());
				RomaFrontend.flow().forward(message, "screen:popup:sendError");
			}
			e.printStackTrace();
		}
	}

	public String getCustomMessage() {
		return customMessage;
	}

	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}

}
