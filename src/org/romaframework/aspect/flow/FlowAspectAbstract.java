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

package org.romaframework.aspect.flow;

import org.romaframework.core.flow.Controller;
import org.romaframework.core.flow.SchemaActionListener;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.frontend.domain.message.MessageOk;

/**
 * Abstract implementation for Flow Aspect.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class FlowAspectAbstract extends SelfRegistrantConfigurableModule<String> implements FlowAspect, SchemaActionListener {

	@Override
	public void startup() {
		Controller.getInstance().registerListener(SchemaActionListener.class, this);
	}

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
	}

	public void configAction(SchemaAction iAction) {
	}

	public void configEvent(SchemaEvent iEvent) {
	}

	public String aspectName() {
		return ASPECT_NAME;
	}

	public void forward(Object iNextObject) {
		forward(iNextObject, null);
	}

	public void forward(Object iNextObject, String iPosition) {
		forward(iNextObject, iPosition, null, null);
	}

	public void alert(String iTitle, String iBody) {
		MessageOk msg = new MessageOk("", iTitle, null, iBody);
		forward(msg, "screen:popup");
	}
}
