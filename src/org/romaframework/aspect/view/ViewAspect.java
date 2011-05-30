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

package org.romaframework.aspect.view;

import java.util.List;
import java.util.Map;

import org.romaframework.aspect.session.SessionInfo;
import org.romaframework.aspect.view.command.ViewCommand;
import org.romaframework.aspect.view.form.ContentForm;
import org.romaframework.aspect.view.form.ViewComponent;
import org.romaframework.aspect.view.screen.Screen;
import org.romaframework.core.aspect.Aspect;
import org.romaframework.core.exception.ConfigurationNotFoundException;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaObject;

/**
 * View Aspect behavior interface.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public interface ViewAspect extends Aspect {

	public static final String	ASPECT_NAME	= "view";

	/**
	 * Display the form reading information from POJO received in the current desktop, in default position.
	 * 
	 * @param iContent
	 *          Object instance to display
	 */
	public void show(Object iContent) throws ViewException;

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the current desktop.
	 * 
	 * @param iContent
	 *          Object instance to display
	 * @param iPosition
	 *          Position where to display the object
	 * @throws ViewException
	 */
	public void show(Object iContent, String iPosition) throws ViewException;

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the current desktop.
	 * 
	 * @param iContent
	 *          Object instance to display
	 * @param iPosition
	 *          Position where to display the object
	 * @param iScreen
	 *          Screen to use
	 * @param iSession
	 *          User session to use (null for the current user session)
	 * @throws ViewException
	 */
	public void show(Object iContent, String iPosition, Screen iScreen, SessionInfo iSession) throws ViewException;

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the desktop received as the argument iDesktop.
	 * 
	 * @param iContent
	 *          Object instance to display
	 * @param iPosition
	 *          Desktop position where render the object
	 * @param iScreen
	 *          Desktop instance to use
	 * @param iSession
	 *          User session to use (null for the current user session)
	 * @param iSchema
	 *          SchemaObject to use
	 * @throws Exception
	 */
	public void show(Object iContent, String iPosition, Screen iScreen, SessionInfo iSession, SchemaObject iSchema)
			throws ViewException;

	/**
	 * Display the form reading information from POJO received following the layout rules. Display the object on iWhere position in
	 * the desktop received as the argument iDesktop.
	 * 
	 * @param iContent
	 *          Object instance to display
	 * @param iPosition
	 *          Desktop position where render the object
	 * @param iScreen
	 *          Desktop instance to use
	 * @param iSession
	 *          User session to use (null for the current user session)
	 * @param iSchema
	 *          SchemaObject to use
	 * @param iPushMode
	 *          Force push mode (enqueue the request to the session)
	 * @throws Exception
	 */
	public void show(Object iContent, String iPosition, Screen iScreen, SessionInfo iSession, SchemaObject iSchema, boolean iPushMode)
			throws ViewException;

	/**
	 * Return the desktop for the current user.
	 * 
	 * @return Screen instance
	 */
	public Screen getScreen();

	/**
	 * Return the screen for the user.
	 * 
	 * @param iUserSession
	 *          User session
	 * @return Screen instance
	 */
	public Screen getScreen(Object iUserSession);

	/**
	 * Set the current screen.
	 * 
	 * @param iScreen
	 *          Screen instance to set for the current user.
	 */
	public void setScreen(Screen iScreen);

	/**
	 * Set the screen in a specified session.
	 * 
	 * @param iScreen
	 *          Screen instance to set for the current user.
	 * @param iSession
	 *          User session to use (null for the current user session)
	 */
	public void setScreen(Screen screen, SessionInfo currentSession);

	/**
	 * Close the current form displayed. It applies only on Pop-up windows.
	 * 
	 * @param iUserObject
	 *          User Object to close
	 */
	public void close(Object iUserObject);

	public void showComponent(Object iComponent, String iArea);

	public void createObjectFormAssociation(Object iUserObject, ViewComponent iForm, SessionInfo iSession);

	public void removeObjectFormAssociation(Object iUserObject, SessionInfo iSession);

	/**
	 * Return the form associated to a User Object.
	 * 
	 * @param iUserObject
	 * @return ContentComponent instance if any, otherwise null
	 */
	public ViewComponent getFormByObject(Object iUserObject);

	/**
	 * Return the form associated to a User Object.
	 * 
	 * @param iSession
	 *          User session, null to get the current active
	 * @param iUserObject
	 * @return ContentComponent instance if any, otherwise null
	 */
	public ViewComponent getFormByObject(Object iSession, Object iUserObject);

	/**
	 * Return the all the forms of the declaring Class iClassOfObject.
	 * 
	 * @param iSession
	 *          User session, null to get the current active
	 * @param iClassOfObject
	 *          The Class of the object
	 * @return Set of ContentComponent instances if any, otherwise null
	 */
	public List<ViewComponent> getFormsByClass(Object iSession, SchemaClass iClassOfObject);

	/**
	 * Return all the forms for all the active session that render POJOs of class iClass.
	 * 
	 * @param iClass
	 *          SchemaClass to search
	 * @return Map<SessionInfo, ContentComponent> with all entries that are handling POJOs of class iClass
	 */
	public Map<SessionInfo, ViewComponent> getFormsByClass(SchemaClass iClass);

	/**
	 * Create a form instance
	 * 
	 * @param iSchemaClass
	 *          the schema class for the form creation
	 * @param iSchemaField
	 *          the schema field of the object, null if the form is not part of another form
	 * @param iParent
	 *          the parent form
	 * @return
	 */
	public ContentForm createForm(SchemaObject iSchemaClass, SchemaField iSchemaField, ViewComponent iParent);

	/**
	 * Remove the form form the memory
	 * 
	 * @param iFormInstance
	 */
	public void releaseForm(ContentForm iFormInstance);

	/**
	 * Shows a form component
	 * 
	 * @param iForm
	 *          The form to be showed
	 * @param iWhere
	 *          The area where the form must be showed
	 * @param iDesktop
	 *          The Screen where to show the form
	 * @return
	 */
	public String showForm(ContentForm iForm, String iWhere, Screen iDesktop);

	/**
	 * Push a command that must be executed by the view aspect
	 * 
	 * @param iCommand
	 */
	public void pushCommand(ViewCommand iCommand);

	/**
	 * Get the schema object associated to the current displayed POJO.
	 * 
	 * @param iUserObject
	 *          User POJO
	 * @return SchemaObject instance
	 * @throws ConfigurationNotFoundException
	 */
	public SchemaObject getSchemaObject(Object iUserObject) throws ConfigurationNotFoundException;
	
	/**
	 *  Retrieve the context where the application was deployed.
	 *  
	 * @return the context where the application was deployed.
	 */
	public String getContextPath();
}
