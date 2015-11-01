/*
 * Copyright (c) Smals
 */
package org.jboss.resteasy.client.jaxrs.internal.proxy.processors;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.lang.reflect.Method;
import java.util.Map;

import javax.ws.rs.Encoded;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.SlashInPath;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.client.jaxrs.internal.proxy.processors.webtarget.PathParamProcessor;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author Thomas De Smedt
 *
 */
public class PathParamProcessorSlashInPathSupportTest {
    
    public static final String PATH_TEMPLATE = "/test/{p1}/{p2}/{p3}";

    public static interface RestClient {
        public Map<String,Object> supported(
            @PathParam("p1") String p1,
            @PathParam("p2") @Encoded String p2,
            @PathParam("p3") @SlashInPath String p3            
        );       
        public Map<String,Object> unsupported(
            @PathParam("p1") String p1,
            @PathParam("p2") @Encoded String p2,
            @PathParam("p3") @Encoded @SlashInPath String p3           
        );
    }

    private static Method methodSupported;
    private static Method methodUnSupported;   
   
    @BeforeClass
    public static final void init() throws NoSuchMethodException, SecurityException {
                
        methodSupported = RestClient.class.getDeclaredMethod("supported", String.class,String.class,String.class);
        methodUnSupported = RestClient.class.getDeclaredMethod("unsupported", String.class,String.class,String.class);
         
    }
   
    private String buildPath(Method method, String... path) {
        ResteasyClient client = new ResteasyClientBuilder().build();
        Object[]  processors = ProcessorFactory.createProcessors(method.getDeclaringClass(), method, (ClientConfiguration) client.getConfiguration(),MediaType.APPLICATION_JSON_TYPE);    
        WebTarget target= client.target(PATH_TEMPLATE) ;
        
        for (int i = 0; i < path.length; i++) {
            target  = ((PathParamProcessor)processors[i]).build(target, path[i]);
        }
        return target.getUri().toString();
    }
    
    @Test
    public void testBasicCase() throws NoSuchMethodException, SecurityException {
        assertEquals("/test/abc/def/ghi", buildPath(methodSupported,"abc","def","ghi") );
    }
    @Test
    public void testEncoded() throws NoSuchMethodException, SecurityException {       
        assertEquals("/test/ab%20c/de%20f/gh%20i", buildPath(methodSupported,"ab c","de%20f","gh i") );
    }
    @Test
    public void testSlashes() throws NoSuchMethodException, SecurityException {
        assertEquals("/test/a%2Fb%20c/d%2Fe%20f/g/h%20i", buildPath(methodSupported,"a/b c","d%2Fe%20f","g/h i") );
    }
    
    @Test
    public void testEncodedSlash() throws NoSuchMethodException, SecurityException {
        assertEquals("/test/a%2Fb%20c/d%2Fe%20f/g/h%20i", buildPath(methodSupported,"a/b c","d/e%20f","g/h i") );
    }
    
    @Test    
    public void testUnsupported() throws NoSuchMethodException, SecurityException {
        // limitation of the WebTarget jaxrs API
        try {
            String path = buildPath(methodUnSupported,"a/b c","d/e%20f","g/h i");
            fail("combination of @Encode and @SlashInPath should be unsupported");
            assertEquals("/test/a%2Fb%20c/d/e%20f/g/h%20i", path );
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("combination of @Encoded and @SlashInPath"));
        }
    }
}
