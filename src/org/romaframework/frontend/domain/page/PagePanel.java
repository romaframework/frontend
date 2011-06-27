/*
 * Copyright 2006-2010 Luca Garulli (luca.garulli--at--assetdata.it)
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
package org.romaframework.frontend.domain.page;

import org.romaframework.aspect.view.ViewConstants;
import org.romaframework.aspect.view.annotation.ViewField;
import org.romaframework.frontend.domain.link.AbstractDynaLink;

public interface PagePanel {

	@ViewField(render = ViewConstants.RENDER_IMAGEBUTTON, label = "")
	public abstract String getIcon();

	@ViewField(label = "")
	public abstract void onIcon();

	@ViewField(render = ViewConstants.RENDER_ROWSET, label = "")
	public abstract AbstractDynaLink[] getLinks();
}