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

package org.romaframework.frontend.domain.message;

import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.aspect.view.annotation.ViewClass;

@ViewClass(label = "Attention!")
public class MessageYesNo extends MessageText {
	public MessageYesNo() {
	}

	public MessageYesNo(String iId, String iTitle, MessageResponseListener iListener) {
		super(iId, iTitle, iListener);
		setIcon("question.gif");
	}

	public MessageYesNo(String iId, String iTitle, MessageResponseListener iListener, String iMessage) {
		super(iId, iTitle, iListener, iMessage);
	}

	public MessageYesNo(String iId, String iTitle) {
		super(iId, iTitle);
	}

	@ViewAction(render=ViewConstants.RENDER_BUTTON)
	public void yes() {
		close();
		setResponse(true);
	}

	@ViewAction(render=ViewConstants.RENDER_BUTTON)
	public void no() {
		close();
		setResponse(false);
	}
}
