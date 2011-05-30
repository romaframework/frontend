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

package org.romaframework.aspect.view.form;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.session.SessionListener;
import org.romaframework.aspect.view.screen.Screen;
import org.romaframework.aspect.view.screen.ScreenContainer;
import org.romaframework.core.Roma;
import org.romaframework.core.flow.Controller;
import org.romaframework.frontend.RomaFrontend;

/**
 * Manage the current user screen and invoke the custom aspect renderer
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public class FormViewer implements SessionListener {

	/**
	 * Contains a map of SessionInfo,
	 */
	private Map<Object, ScreenContainer>	userView;
	private SessionAspect									sessionManager;

	private static FormViewer							instance	= new FormViewer();
	private static Log										log				= LogFactory.getLog(FormViewer.class);

	protected FormViewer() {
		sessionManager = Roma.session();
		userView = new HashMap<Object, ScreenContainer>();

		Controller.getInstance().registerListener(SessionListener.class, this);
	}

	public Screen getScreen() {
		return getScreen(sessionManager.getActiveSessionInfo());
	}

	public Screen getScreen(Object iSession) {
		ScreenContainer container = userView.get(iSession);

		if (container == null)
			return null;

		return container.getScreen();
	}

	/**
	 * Change the current screen.
	 * 
	 * @param iScreen
	 */
	public void setScreen(Screen iScreen) {
		setScreen(iScreen, sessionManager.getActiveSessionInfo());
	}

	/**
	 * Change the current screen.
	 * 
	 * @param iScreen
	 */
	public void setScreen(Screen iScreen, SessionInfo iSession) {
		ScreenContainer current = userView.get(iSession);
		if (current != null && iScreen != current.getScreen())
			// SCREEN CHANGED: SET IT
			current.setScreen(iScreen);
	}

	public void setScreenContainer(ScreenContainer iScreenContainer) {
		userView.put(sessionManager.getActiveSessionInfo(), iScreenContainer);
	}

	/**
	 * Render the an object on the defined area
	 * 
	 * @param iArea
	 *          The area where to render the object
	 * @param iForm
	 *          The Object to render
	 */
	public void display(String iArea, Object iForm) {
		Object session = sessionManager.getActiveSessionInfo();

		if (session != null) {
			ScreenContainer screenCont = userView.get(session);

			if (screenCont != null) {
				Screen currentDesktop = screenCont.getScreen();
				display(iArea, iForm, currentDesktop);
			}
		}
	}

	public String display(String iArea, Object iForm, Screen iScreen) {
		if (iScreen == null || Roma.session().getActiveSessionInfo() == null) {
			// SCREEN NOT YET SETTED: PUSH THE FORMS IN THE SESSION TO BE DISPLAYED WHEN THE SCREEN WILL BE SETTED
			LinkedHashMap<String, Object> queuedForms = (LinkedHashMap<String, Object>) Roma.session().getProperty("formQueue");
			if (queuedForms == null) {
				// FIRST TIME: CREATE IT AND PUT IN THE SESSION
				queuedForms = new LinkedHashMap<String, Object>();
				Roma.session().setProperty("formQueue", queuedForms);
			}
			queuedForms.put(iArea, iForm);
			return iArea;
		} else {
			sync(iScreen);
			return iScreen.view(iArea, iForm);
		}
	}

	public void sync() {
		sync(RomaFrontend.view().getScreen());
	}

	public void sync(Screen iScreen) {
		LinkedHashMap<String, Object> queuedForms = (LinkedHashMap<String, Object>) Roma.session().getProperty("formQueue");
		if (queuedForms != null) {
			for (Map.Entry<String, Object> entry : queuedForms.entrySet()) {
				iScreen.view(entry.getKey(), entry.getValue());
			}
			queuedForms.clear();
			Roma.session().setProperty("formQueue", null);
		}
	}

	public void onSessionCreating(SessionInfo iSession) {
	}

	/**
	 * Remove user session screen if any
	 */
	public void onSessionDestroying(SessionInfo iSession) {
		ScreenContainer screen = userView.remove(iSession);
		if (screen != null)
			screen.destroy();

		if (log.isDebugEnabled())
			log.debug("[FormViewer.onSessionDestroying] Removed screen container: " + screen);

	}

	/**
	 * Get the singleton instance.
	 * 
	 * @return The singleton instance.
	 */
	public static FormViewer getInstance() {
		return instance;
	}
}
