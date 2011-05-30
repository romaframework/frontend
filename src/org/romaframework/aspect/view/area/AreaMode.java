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
package org.romaframework.aspect.view.area;

/**
 * Interface to define area component factories.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public interface AreaMode {
	public Object createComponent(int iSize);

	public Object createComponentContainer(Object iComponentContainer, Object iSubComponentContainer, String iAreaAlign);

	/**
	 * Place a component removing all the others prevously added
	 * @param iComponentContainer the container of the component
	 * @param iComponentToPlace the component to place
	 * @return
	 */
	public Object placeComponent(Object iComponentContainer, Object iComponentToPlace);

	
	public boolean isChildrenAllowed();
}
