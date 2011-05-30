/*
 * Copyright 2006-2007 Giordano Maestro (giordano.maestro--at--assetdata.it)
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
package org.romaframework.aspect.geo;

import java.lang.annotation.Annotation;

import org.romaframework.aspect.core.annotation.AnnotationConstants;
import org.romaframework.aspect.geo.annotation.MapField;
import org.romaframework.aspect.geo.feature.GeoFieldFeature;
import org.romaframework.aspect.logging.annotation.LoggingField;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.xmlannotations.XmlAspectAnnotation;
import org.romaframework.core.schema.xmlannotations.XmlFieldAnnotation;
import org.romaframework.core.util.DynaBean;

public abstract class MapAspectAbstract extends SelfRegistrantConfigurableModule<String> implements GeoAspect {

  public static final String ASPECT_NAME = "mapsAspect";

  public String aspectName() {
    return ASPECT_NAME;
  }

  private void readFieldXml(SchemaField iElement, XmlFieldAnnotation iXmlNode) {
    // PROCESS DESCRIPTOR CFG DESCRIPTOR ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT AND ANNOTATION VALUES
    if (iXmlNode == null || iXmlNode.aspect(ASPECT_NAME) == null) {
      return;
    }

    DynaBean features = iElement.getFeatures(ASPECT_NAME);

    XmlAspectAnnotation descriptor = iXmlNode.aspect(ASPECT_NAME);

    configureFieldXml(features, descriptor);
  }

  private void configureFieldXml(DynaBean features, XmlAspectAnnotation descriptor) {
    if (descriptor != null) {
      String type = descriptor.getAttribute(GeoFieldFeature.TYPE);
      if (type!=null) {
        features.setAttribute(GeoFieldFeature.TYPE, type);
      }
      String zoom = descriptor.getAttribute(GeoFieldFeature.ZOOM);
      if (zoom!=null) {
        features.setAttribute(GeoFieldFeature.ZOOM, zoom);
      }

      String otherParameters = descriptor.getAttribute("otherParameters");
      if(otherParameters != null){
	      String[] paramsList = otherParameters.split(",");
	
	      String[] paramArray = new String[paramsList.length];
	
	      int i = 0;
	      for (String param : paramsList) {
	        paramArray[i] = param;
	        i++;
	      }
	
	      if (paramArray.length > 0) {
	        features.setAttribute(GeoFieldFeature.OTHER_PARAMETERS, paramArray);
	      }
      }
    }

  }

  private void configFieldAnnotations(SchemaField field, Annotation annotation, Annotation genericAnnotation) {
    DynaBean features = field.getFeatures(ASPECT_NAME);
    if (features == null) {
      // CREATE EMPTY FEATURES
      features = new GeoFieldFeature();
      field.setFeatures(ASPECT_NAME, features);
    }

    readAnnotation(field, annotation, features);
  }

  private void readAnnotation(SchemaField field, Annotation iAnnotation, DynaBean features) {

    if (iAnnotation instanceof LoggingField) {

      MapField annotation = (MapField) iAnnotation;

      if (annotation != null) {
        // PROCESS ANNOTATIONS
        // ANNOTATION ATTRIBUTES (IF DEFINED) OVERWRITE DEFAULT VALUES
        if (annotation != null) {

          if (!AnnotationConstants.DEF_VALUE.equals(annotation.zoom())) {
            features.setAttribute(GeoFieldFeature.ZOOM, annotation.zoom());
          }
          if (!AnnotationConstants.DEF_VALUE.equals(annotation.type())) {
            features.setAttribute(GeoFieldFeature.TYPE, annotation.type());
          }

        }
      }

    }

  }

  public void configField(SchemaField iField, Annotation iAnnotation, Annotation iGenericAnnotation, Annotation iGetterAnnotation,
      XmlFieldAnnotation iXmlNode) {
    configFieldAnnotations(iField, iAnnotation, iGenericAnnotation);
    readFieldXml(iField, iXmlNode);
  }

}
