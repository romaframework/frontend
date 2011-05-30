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

package org.romaframework.aspect.view.screen;

import org.romaframework.aspect.view.area.AreaComponent;

/**
 * Represents the screen where to render forms and components. Each desktop is
 * divided in areas. Area names and behavior are screen dependent.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public interface Screen {

	public static final String BODY = "//body";
	public static final String POPUP = "popup";
	public static final String NULL = "null";

	/**
	 * Display a component in the specified area
	 * 
	 * @return area name where the component was displayed
	 */
	public String view(String iArea, Object iComponent);

	
	/**
	 * Return a representation of the object contained in the area
	 * @param iArea
	 * @return
	 */
	public Object getComponentInArea(String iArea);

	/**
	 * Close a popup opened
	 * @param iWindow
	 */
	public void close(Object iWindow);

	/**
	 * Return the area component
	 * @param iAreaName The name of the sceeen area
	 * @return
	 */
	public AreaComponent getArea(String iAreaName);

	/**
	 * Set the visibility of the area
	 * @param iArea
	 * @param iValue
	 */
	public void setVisibleArea(String iArea, boolean iValue);
}
