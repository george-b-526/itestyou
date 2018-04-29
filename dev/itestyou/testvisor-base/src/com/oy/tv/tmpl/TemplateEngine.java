package com.oy.tv.tmpl;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class TemplateEngine {

	public static void init() throws Exception {
		
		Properties props = new Properties();  
		props.put("resource.loader", "jar");
		props.put("jar.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		Velocity.init(props);
		
	}
	
	public static void renderTemplate(Object host, String name, Writer out, Map<String, Object> bindings) throws IOException {
		// bind  
		VelocityContext vctx = new VelocityContext();
		Iterator<String> iter = bindings.keySet().iterator();
		while(iter.hasNext()){
			String key = (String) iter.next();
			Object value = bindings.get(key);
			  
			vctx.put(key, value);			
		}  

		// render
		Template tmpl;
		try {    
			String base = host.getClass().getName();
			base = base.replaceAll("[.]", "/");
			base += name + ".vm.html";    
			tmpl = Velocity.getTemplate(base);
		} catch (Exception e){
			throw new RuntimeException(e);
		}

		tmpl.merge(vctx, out);
	}
	
}
