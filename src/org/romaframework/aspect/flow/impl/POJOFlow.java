/*
 * Copyright 2006-2007 Luca Garulli (luca.garulli--at--assetdata.it)
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

package org.romaframework.aspect.flow.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.romaframework.aspect.flow.FlowAspectAbstract;
import org.romaframework.aspect.flow.feature.FlowActionFeatures;
import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.screen.Screen;
import org.romaframework.core.Roma;
import org.romaframework.core.domain.type.Pair;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.frontend.domain.message.Message;
import org.romaframework.frontend.domain.message.MessageResponseListener;
import org.romaframework.frontend.domain.message.MessageYesNo;

/**
 * POJO based implementation of Flow Aspect behavior interface.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public class POJOFlow extends FlowAspectAbstract {
	private static final String	INHIBIT_CONFIRM_ACTION	= "pojoflow_inhibitConfirm";
	public static final String	SESS_PROPERTY_HISTORY		= "_HISTORY_";
	protected SessionAspect			sessionAspect;
	protected ViewAspect				viewAspect;

	public Pair<Object, String> current() {
		return current(null);
	}

	public Pair<Object, String> current(SessionInfo iSession) {
		String activeArea = Roma.view().getScreen().getActiveArea();
		Pair<Object, String> backObject = null;
		Stack<Object> stack = getAreaHistory(iSession, activeArea);
		if (stack != null && !stack.isEmpty()) {
			backObject = new Pair<Object, String>(stack.peek(), activeArea);
		}
		return backObject;
	}

	public void forward(SchemaClass iNextClass, String iPosition) {
		forward(iNextClass, iPosition);
	}

	public void forward(Object iCurrentObject, Class<? extends Object> iNextClass, String iPosition) {
		forward(iNextClass, iPosition);
	}

	public void forward(Object iNextObject, String iPosition, Screen iScreen, SessionInfo iSession) {
		if (iNextObject == null)
			return;

		if (iNextObject instanceof String) {
			SchemaClass cls = Roma.schema().getSchemaClass((String) iNextObject);
			if (cls == null)
				return;

			// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
			iNextObject = Roma.session().getObject(cls);
		} else if (iNextObject instanceof Class<?>) {
			SchemaClass cls = Roma.schema().getSchemaClass((Class<?>) iNextObject);
			if (cls == null)
				return;

			// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
			iNextObject = Roma.session().getObject(cls);
		} else if (iNextObject instanceof SchemaClass) {
			SchemaClass cls = ((SchemaClass) iNextObject);

			// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
			iNextObject = Roma.session().getObject(cls);
		}

		moveForward(iSession, iNextObject, iPosition);

		// SHOW THE FORM
		viewAspect.show(iNextObject, iPosition, iScreen, iSession);
	}

	public Object back(Object iGoBackUntil) {

		if (iGoBackUntil == null)
			// GET THE LAST ONE POSITION
			return back();

		if (!getAreaHistory(null, Roma.view().getScreen().getActiveArea()).contains(iGoBackUntil)) {
			return null;
		}

		Object content = back();
		while (!iGoBackUntil.equals(content)) {
			content = back();
		}

		// UNKNOWN ERROR
		return content;
	}

	public void clearHistory() {
		clearHistory(null);
	}

	public void clearHistory(SessionInfo iSession) {
		getHistory(iSession).clear();
	}

	public Object back() {
		return back((SessionInfo) null);
	}

	public Object back(SessionInfo iSession) {
		if (iSession == null)
			iSession = Roma.session().getActiveSessionInfo();

		Pair<Object, String> currentObject = current(iSession);
		if (currentObject == null)
			return null;

		Pair<Object, String> backObject = moveBack(iSession);

		if (backObject == null)
			return null;
		viewAspect.show(backObject.getKey(), backObject.getValue(), null, iSession);

		return backObject.getKey();
	}

	protected void moveForward(Object iNextObject, String iPosition) {
		moveForward(null, iNextObject, iPosition);
	}

	protected void moveForward(SessionInfo iSession, Object iNextObject, String iPosition) {
		Stack<Object> history = getAreaHistory(iSession, iPosition);

		if (!history.isEmpty()) {
			Object last = history.peek();

			if (last.equals(iNextObject))
				// SAME OBJECT: JUST A REFRESH, DON'T STORE IN HISTORY
				return;
		}

		history.push(iNextObject);
	}

	protected Pair<Object, String> moveBack(SessionInfo iSession) {
		getAreaHistory(iSession, Roma.view().getScreen().getActiveArea()).pop();
		return current();
	}

	public void onAfterAction(Object iContent, SchemaAction iAction, Object returnedValue) {
		Boolean goBack = (Boolean) iAction.getFeature(FlowActionFeatures.BACK);
		if (goBack != null && goBack) {
			back();
			return;
		}

		if (iAction.isSettedFeature(FlowActionFeatures.NEXT)) {
			SchemaClass nextClass = (SchemaClass) iAction.getFeature(FlowActionFeatures.NEXT);
			if (nextClass != null) {
				String nextPosition = (String) iAction.getFeature(FlowActionFeatures.POSITION);
				forward(nextClass, nextPosition);
			}
		}
	}

	public boolean onBeforeAction(Object iContent, SchemaAction iAction) {
		if (!(Boolean.TRUE.equals(iAction.getFeature(FlowActionFeatures.CONFIRM_REQUIRED)))) {
			return true;
		}
		if (Boolean.TRUE.equals(Roma.context().component(INHIBIT_CONFIRM_ACTION))) {
			return true;
		}

		String confirmMessage = (String) iAction.getFeature(FlowActionFeatures.CONFIRM_MESSAGE);
		if (confirmMessage == null) {
			try {
				confirmMessage = Roma.i18n().resolveString(iAction.getEntity(), "$" + iAction.getName() + ".confirmMessage");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (confirmMessage.startsWith("$")) {
			try {
				confirmMessage = Roma.i18n().resolveString(confirmMessage);
			} catch (Exception e) {
			}
		}
		MessageYesNo msg = new MessageYesNo("confirm", "", new ConfirmListener(iAction, iContent), confirmMessage);
		forward(msg, "screen:popup");
		return false;
	}

	public void onExceptionAction(Object iContent, SchemaAction iAction, Exception exception) {
	}

	static class ConfirmListener implements MessageResponseListener {

		protected SchemaClassElement	originalAction;
		protected Object							content;

		protected ConfirmListener(SchemaClassElement iOriginalAction, Object iContent) {
			this.originalAction = iOriginalAction;
			this.content = iContent;
		}

		public void responseMessage(Message iMessage, Object iResponse) {
			if (Boolean.TRUE.equals(iResponse)) {
				try {
					Roma.context().setComponent(INHIBIT_CONFIRM_ACTION, true);
					((SchemaAction) originalAction).invoke(content);
					Roma.context().setComponent(INHIBIT_CONFIRM_ACTION, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Map<String, Stack<Object>> getHistory() {
		return getHistory(null);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Stack<Object>> getHistory(SessionInfo iSession) {
		Map<String, Stack<Object>> history = (Map<String, Stack<Object>>) sessionAspect.getProperty(iSession, SESS_PROPERTY_HISTORY);
		if (history == null) {
			history = new HashMap<String, Stack<Object>>();
			sessionAspect.setProperty(SESS_PROPERTY_HISTORY, history);
		}
		return history;
	}

	public Stack<Object> getAreaHistory(SessionInfo iSession, String area) {
		Map<String, Stack<Object>> areas = getHistory(iSession);
		Stack<Object> stack = areas.get(area);
		if (stack == null) {
			stack = new Stack<Object>();
			areas.put(area, stack);
		}
		return stack;
	}

	@Override
	public void startup() {
		super.startup();
		sessionAspect = Roma.aspect(SessionAspect.ASPECT_NAME);
		viewAspect = Roma.aspect(ViewAspect.ASPECT_NAME);
	}

	@Override
	public void shutdown() {
		super.shutdown();
		sessionAspect = null;
	}

	public void configClass(SchemaClassDefinition class1) {
	}

	public void configField(SchemaField field) {
	}

	public Object getUnderlyingComponent() {
		return null;
	}
}
