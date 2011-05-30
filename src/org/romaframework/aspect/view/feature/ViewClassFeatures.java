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

package org.romaframework.aspect.view.feature;

public class ViewClassFeatures extends ViewBaseFeatures {

  public ViewClassFeatures() {
    defineAttribute(EXPLICIT_ELEMENTS, false);
    defineAttribute(COLUMNS, 1);
    defineAttribute(ORDER_AREAS, null);
    defineAttribute(FORM, null);
  }

  public static final String EXPLICIT_ELEMENTS = "explicitElements";
  public static final String COLUMNS           = "columns";
  public static final String ORDER_AREAS       = "orderAreas";
  public static final String FORM              = "form";
}
