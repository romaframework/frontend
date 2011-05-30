/*
 * Copyright 2006-2009 Luca Garulli (luca.garulli--at--assetdata.it)
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
package org.romaframework.frontend;

import org.romaframework.aspect.flow.FlowAspect;
import org.romaframework.aspect.reporting.ReportingAspect;
import org.romaframework.aspect.view.ViewAspect;
import org.romaframework.core.Roma;

/**
 * Utility class to access directly to the most common used component in applications that use any kind of front-end.
 */
public class RomaFrontend extends Roma {

	protected static FlowAspect				flowAspect			= null;
	protected static ViewAspect				viewAspect			= null;
	protected static ReportingAspect	reportingAspect	= null;

	static {
		singleton = new RomaFrontend();
	}

	public static FlowAspect flow() {
		if (flowAspect == null) {
			synchronized (RomaFrontend.class) {
				if (flowAspect == null) {
					flowAspect = Roma.aspect(FlowAspect.class);
				}
			}
		}
		return flowAspect;
	}

	public static ViewAspect view() {
		if (viewAspect == null) {
			synchronized (RomaFrontend.class) {
				if (viewAspect == null) {
					viewAspect = Roma.aspect(ViewAspect.class);
				}
			}
		}
		return viewAspect;
	}

	public static ReportingAspect reporting() {
		if (reportingAspect == null) {
			synchronized (RomaFrontend.class) {
				if (reportingAspect == null) {
					reportingAspect = Roma.aspect(ReportingAspect.class);
				}
			}
		}
		return reportingAspect;
	}
}
