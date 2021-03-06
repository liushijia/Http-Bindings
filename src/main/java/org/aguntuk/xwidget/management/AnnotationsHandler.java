/**
The MIT License (MIT)

Copyright (c) <year> <copyright holders>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package org.aguntuk.xwidget.management;

import static java.lang.System.out;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.aguntuk.xwidget.annotations.RequestMeta;
import org.aguntuk.xwidget.annotations.ServiceInterfaceMeta;
import org.aguntuk.xwidget.exception.InitializationException;
import org.reflections.Reflections;

public enum AnnotationsHandler {
	INSTANCE;
	
	public void scanClassPathForRequestInterfaces(String targetPkg) throws InitializationException {
		try {
		    out.println(targetPkg);
		    Reflections reflections = new Reflections(targetPkg);
	
		     Set<Class<?>> annotated = 
		               reflections.getTypesAnnotatedWith(ServiceInterfaceMeta.class);
		     out.println(annotated.size());
		     
		     for(Iterator<Class<?>> iter = annotated.iterator();iter.hasNext()== true;) {
		    	 Class<?> obj = iter.next();
		    	 out.println("Annotated object found is " + obj);
		    	 Object instance = obj.newInstance();
		    	 String serviceName = obj.getName();
		    	 Service service = ServiceFacade.INSTANCE.getService(serviceName);
		    	 if(service == null) {
			    	 service = new Service(obj, instance);
	    			 ServiceFacade.INSTANCE.addService(serviceName, service);			    	 
		    	 }
		    	 //iterate over the methods to find annotations of BlockRequest type
		    	 AnnotatedElement[] annotationElements = obj.getMethods();
		    	 for(AnnotatedElement annotationElement:annotationElements) {
		    		 if(annotationElement.isAnnotationPresent(RequestMeta.class)) {
		    			 RequestMeta requestMeta = annotationElement.getAnnotation(RequestMeta.class);
		    			 String requestName = requestMeta.requestName();
		    			 out.println("Request type is " + requestName);
		    			 String templateFile = requestMeta.templateFile();
		    			 String outputType = requestMeta.outputType();
		    			 String inputType = requestMeta.inputType();
		    			 //String[] requestClass = requestMeta.requestClass();
		    			 String keys[] = requestMeta.requestKeyName();
		    			 String jsp = requestMeta.jsp();
		    			 RequestKey[] requestKeys = null;
    					 Method method = (Method)annotationElement;
		    			 Class<?>[] classes = method.getParameterTypes();
		    			 int keyNamePosition = 0;
    					 requestKeys = new RequestKey[classes.length];		    			 
		    			 for(int i = 0; i < classes.length; i++) {
		    				 String paramClassName = classes[i].getName();
		    				 if(paramClassName.contains("java.lang")) {//this means the argument is primitive and we will need a keyname
	    						 requestKeys[i] = new RequestKey(keys[keyNamePosition], classes[i].getName(), classes[i].isArray(), false);
			    				 ++keyNamePosition;	    						 
		    				 } else {
	    						 requestKeys[i] = new RequestKey(classes[i].getName(), classes[i].isArray(), true);
		    				 }
		    			 }
		    			 
/*		    			 if(keys != null && keys.length > 0) {
		    					 requestKeys = new RequestKey[keys.length];
		    					 //Class<?>[] classes = method.getParameterTypes();
		    					 for(int i = 0; i < keys.length; i++) {
		    						 requestKeys[i] = new RequestKey(keys[i], classes[i].getName(), classes[i].isArray(), false);
		    					 }	    					 
		    			 } else {
	    					 //Class<?>[] classes = method.getParameterTypes();

	    					 for(int i = 0; i < classes.length; i++) {
	    						 requestKeys[i] = new RequestKey(classes[i].getName(), classes[i].isArray(), true);
	    					 }
		    			 }
*/		    			 
		    			 //create the Request class to hold all the info	    			 
		    			 Request request = new Request((Method)annotationElement, templateFile, outputType);
		    			 //request.setRequestClassName(requestClass);
		    			 request.setRequestKey(requestKeys);
		    			 request.setJsp(jsp);
		    			 request.setInputType(inputType);
		    			 service.addRequestMethod(requestName, request);
		    		 }
		    	 }
		     }
		}catch(InstantiationException ie) {
			throw new InitializationException(ie);
		}catch(IllegalAccessException iae) {
			throw new InitializationException(iae);			
		}
	}

}
