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

import java.util.Stack;

import org.romaframework.aspect.flow.FlowAspectAbstract;
import org.romaframework.aspect.flow.feature.FlowActionFeatures;
import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.aspect.view.form.ContentForm;
import org.romaframework.aspect.view.form.ViewComponent;
import org.romaframework.aspect.view.screen.Screen;
import org.romaframework.core.Roma;
import org.romaframework.core.domain.type.Pair;
import org.romaframework.core.flow.Controller;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaClassElement;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaObject;
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
		Stack<Pair<Object, String>> history = getHistory(iSession);

		Pair<Object, String> backObject = history.isEmpty() ? null : history.peek();

		return backObject;
	}

	public void forward(Object iCurrentObject, SchemaClass iNextClass, String iPosition) {
		if (iNextClass == null)
			return;

		// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
		Object nextObj = Controller.getInstance().getObject(iNextClass, null);

		forward(iCurrentObject, nextObj, iPosition);
	}

	public void forward(Object iCurrentObject, Class<? extends Object> iNextClass, String iPosition) {
		if (iNextClass == null)
			return;

		// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
		Object nextObj = Controller.getInstance().getObject(iNextClass, null);

		forward(iCurrentObject, nextObj, iPosition);
	}

	public void forward(Object iNextObject) {
		forward(iNextObject, null);
	}

	public void forward(Object iCurrentObject, Object iNextObject, String iPosition) {
		forward(iNextObject, iPosition);
	}

	public void forward(Object iNextObject, String iPosition) {
		forward(iNextObject, iPosition, null, null, null);
	}

	public void forward(Object iNextObject, String iPosition, Screen iScreen, SessionInfo iSession) {
		forward(iNextObject, iPosition, iScreen, iSession, null);
	}

	public void forward(Object iNextObject, String iPosition, Screen iScreen, SessionInfo iSession, SchemaObject iSchema) {
		forward(iNextObject, iPosition, iScreen, iSession, iSchema, false);
	}

	public void forward(Object iNextObject, String iPosition, Screen iScreen, SessionInfo iSession, SchemaObject iSchema, boolean iPushMode) {
		if (iNextObject instanceof String) {
			SchemaClass cls = Roma.schema().getSchemaClass((String) iNextObject);
			if (cls == null)
				return;

			// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
			iNextObject = Controller.getInstance().getObject(cls, null);
		} else if (iNextObject instanceof Class<?>) {
			SchemaClass cls = Roma.schema().getSchemaClass((Class<?>) iNextObject);
			if (cls == null)
				return;

			// SEARCH THE FORM INSTANCE BETWEEN USER SESSSION FORMS
			iNextObject = Controller.getInstance().getObject(cls, null);
		}

		moveForward(iSession, iNextObject, iPosition);

		// SHOW THE FORM
		viewAspect.show(iNextObject, iPosition, iScreen, iSession, iSchema, iPushMode);
	}

	public void forward(ViewComponent iComponent, String iPosition, Screen iScreen) {
		moveForward(iComponent.getContent(), iPosition);

		// SHOW THE FORM
		viewAspect.showForm((ContentForm) iComponent, iPosition, iScreen);
	}

	public Object back(Object iGoBackUntil) {
		Pair<Object, String> currentObject;

		if (iGoBackUntil == null)
			// GET THE LAST ONE POSITION
			return back();

		// GO BACK UNTIL THE POJO PASSED IS FOUND
		currentObject = null;
		for (Pair<Object, String> history : getHistory()) {
			if (iGoBackUntil.equals(history.getKey())) {
				currentObject = history;
				break;
			}
		}

		if (currentObject == null)
			// NO BACK POJO TO RETURN
			return null;

		// BACK OBJECT FOUND: GO BACK STEP-BY-STEP
		currentObject = current();
		Object content = back();
		while (currentObject != null && content != null && !currentObject.getKey().equals(iGoBackUntil)) {
			content = back();
		}

		// UNKNOWN ERROR
		return content;
	}

	public void clearHistory() {
		clearHistory(null);
	}

	public void clearHistory(SessionInfo iSession) {
		while (!getHistory().isEmpty()) {
			Pair<Object, String> backObject = getHistory(iSession).pop();

			Object currentForm = backObject.getKey();

			ContentForm currentComponent = (ContentForm) viewAspect.getFormByObject(currentForm);
			if (currentComponent != null && currentComponent.isFirstToOpenPopup(currentForm))
				// CLOSE CURRENT OBJECT AS POPUP
				viewAspect.close(currentForm);
		}
	}

	public Object back() {
		return back(null);
	}

	public Object back(SessionInfo iSession) {
		if (iSession == null)
			iSession = Roma.session().getActiveSessionInfo();

		Pair<Object, String> currentObject = current(iSession);
		if (currentObject == null)
			return null;

		Pair<Object, String> backObject = moveBack(iSession);

		Object currentForm = currentObject.getKey();

		ContentForm currentComponent = (ContentForm) viewAspect.getFormByObject(currentForm);
		if (currentComponent != null && currentComponent.isFirstToOpenPopup(currentForm))
			// CLOSE CURRENT OBJECT AS POPUP
			viewAspect.close(currentForm);
		else if (backObject != null)
			// SHOW THE PREVIOUS FORM
			viewAspect.show(backObject.getKey(), backObject.getValue(), null, iSession);

		if (backObject == null)
			return null;

		return backObject.getKey();
	}

	protected void moveForward(Object iNextObject, String iPosition) {
		moveForward(null, iNextObject, iPosition);
	}

	protected void moveForward(SessionInfo iSession, Object iNextObject, String iPosition) {
		if (Controller.getInstance().getContext().getActiveArea() != null && Controller.getInstance().getContext().getActiveArea().startsWith(Screen.POPUP)) {
			if (iPosition != null && (!iPosition.startsWith("screen:" + Screen.POPUP) && !iPosition.startsWith(Screen.POPUP)))
				return;
		}
		Stack<Pair<Object, String>> history = getHistory(iSession);

		if (!history.isEmpty()) {
			Pair<Object, String> last = history.peek();

			if (last.getKey() != null && last.getKey().equals(iNextObject) && (iPosition == null || iPosition != null && iPosition.equals(last.getValue())))
				// SAME OBJECT: JUST A REFRESH, DON'T STORE IN HISTORY
				return;
		}

		history.push(new Pair<Object, String>(iNextObject, iPosition));
	}

	protected Pair<Object, String> moveBack(SessionInfo iSession) {
		getHistory(iSession).pop();
		return current();
	}

	public void onAfterAction(Object iContent, SchemaAction iAction, Object returnedValue) {
		Boolean goBack = (Boolean) iAction.getFeature(FlowActionFeatures.BACK);
		if (goBack != null && goBack) {
			back();
			return;
		}

		SchemaClass nextClass = (SchemaClass) iAction.getFeature(FlowActionFeatures.NEXT);

		if (nextClass != null) {
			String nextPosition = (String) iAction.getFeature(FlowActionFeatures.POSITION);
			forward(iContent, nextClass, nextPosition);
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

	public Stack<Pair<Object, String>> getHistory() {
		return getHistory(null);
	}

	@SuppressWarnings("unchecked")
	public Stack<Pair<Object, String>> getHistory(SessionInfo iSession) {
		Stack<Pair<Object, String>> history = (Stack<Pair<Object, String>>) sessionAspect.getProperty(iSession, SESS_PROPERTY_HISTORY);
		if (history == null) {
			history = new Stack<Pair<Object, String>>();
			sessionAspect.setProperty(SESS_PROPERTY_HISTORY, history);
		}
		return history;
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
