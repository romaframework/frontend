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

package org.romaframework.frontend.domain.page;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.flow.FlowAspect;
import org.romaframework.aspect.view.annotation.ViewAction;
import org.romaframework.core.Roma;

/**
 * Base class to build page with "back" action.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public abstract class Page {
	public Page() {
	}

	@ViewAction(bind = AnnotationConstants.FALSE)
	protected void back() {
		Roma.aspect(FlowAspect.class).back();
	}
}
