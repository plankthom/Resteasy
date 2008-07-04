package org.jboss.resteasy.test.finegrain.resource;

import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ResourceMethodRegistry;
import org.jboss.resteasy.specimpl.HttpHeadersImpl;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.HttpRequestImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AcceptTest
{
   @BeforeClass
   public static void start()
   {
      ResteasyProviderFactory.initializeInstance();
   }

   @Path("/")
   public static class WebResource
   {
      @ProduceMime("application/foo")
      @GET
      public String doGetFoo()
      {
         return "foo";
      }

      @ProduceMime("application/bar")
      @GET
      public String doGetBar()
      {
         return "bar";
      }

      @ProduceMime("application/baz")
      @GET
      public String doGetBaz()
      {
         return "baz";
      }

      @ProduceMime("*/*")
      @GET
      public Response doGetWildCard()
      {
         return Response.ok("wildcard", "application/wildcard").build();
      }
   }

   private HttpRequest createRequest(String httpMethod, List<PathSegment> pathSegments, MediaType contentType, List<MediaType> accepts)
   {
      UriInfoImpl uriInfo = new UriInfoImpl(pathSegments);
      HttpHeadersImpl headers = new HttpHeadersImpl();
      headers.setAcceptableMediaTypes(accepts);
      headers.setMediaType(contentType);
      return new HttpRequestImpl(null, headers, httpMethod, uriInfo);
   }

   @Test
   public void testAcceptGet() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(WebResource.class);

      MediaType contentType = new MediaType("text", "plain");
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/");

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/foo"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(WebResource.class.getMethod("doGetFoo"), method.getMethod());
      }

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/foo;q=0.1"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(WebResource.class.getMethod("doGetFoo"), method.getMethod());
      }

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/foo"));
         accepts.add(MediaType.valueOf("application/bar;q=0.4"));
         accepts.add(MediaType.valueOf("application/baz;q=0.2"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(WebResource.class.getMethod("doGetFoo"), method.getMethod());
      }

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/foo;q=0.4"));
         accepts.add(MediaType.valueOf("application/bar"));
         accepts.add(MediaType.valueOf("application/baz;q=0.2"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(WebResource.class.getMethod("doGetBar"), method.getMethod());
      }

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/foo;q=0.4"));
         accepts.add(MediaType.valueOf("application/bar;q=0.2"));
         accepts.add(MediaType.valueOf("application/baz"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(WebResource.class.getMethod("doGetBaz"), method.getMethod());
      }
   }

   @Path("/xml")
   public static class XmlResource
   {
      @ConsumeMime("application/xml;schema=foo")
      @PUT
      public void putFoo(String foo)
      {
      }

      @ConsumeMime("application/xml")
      @PUT
      public void put(String foo)
      {
      }

      @ConsumeMime("application/xml;schema=bar")
      @PUT
      public void putBar(String foo)
      {
      }


   }

   @Test
   public void testConsume() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(XmlResource.class);

      MediaType contentType = MediaType.valueOf("application/xml;schema=bar");
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/xml");

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("PUT", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(XmlResource.class.getMethod("putBar", String.class), method.getMethod());
      }
   }

   @Path("/xml")
   public static class XmlResource2
   {
      @ConsumeMime("application/xml;schema=foo")
      @ProduceMime("application/xml;schema=junk")
      @PUT
      public String putFoo(String foo)
      {
         return "hello";
      }

      @ConsumeMime("application/xml;schema=bar")
      @ProduceMime("application/xml;schema=stuff")
      @PUT
      public String putBar(String foo)
      {
         return "hello";
      }

      @ConsumeMime("application/xml")
      @ProduceMime("application/xml;schema=stuff")
      @PUT
      public String put(String foo)
      {
         return "hello";
      }

   }

   @Test
   public void testConsume2() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(XmlResource2.class);

      MediaType contentType = MediaType.valueOf("application/xml;schema=bar");
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/xml");

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/xml;schema=junk;q=1.0"));
         accepts.add(MediaType.valueOf("application/xml;schema=stuff;q=0.5"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("PUT", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(XmlResource2.class.getMethod("putBar", String.class), method.getMethod());
      }
   }

   @Test
   public void testConsume3() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(XmlResource2.class);

      MediaType contentType = MediaType.valueOf("application/xml;schema=blah");
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/xml");

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/xml;schema=junk;q=1.0"));
         accepts.add(MediaType.valueOf("application/xml;schema=stuff;q=0.5"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("PUT", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(XmlResource2.class.getMethod("put", String.class), method.getMethod());
      }
   }

   @Test
   public void testAcceptGetWildCard() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(WebResource.class);

      MediaType contentType = new MediaType("text", "plain");
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/");

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/wildcard"));
         accepts.add(MediaType.valueOf("application/foo;q=0.6"));
         accepts.add(MediaType.valueOf("application/bar;q=0.4"));
         accepts.add(MediaType.valueOf("application/baz;q=0.2"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(WebResource.class.getMethod("doGetWildCard"), method.getMethod());
      }
   }

   @Path("/")
   public static class MultipleResource
   {
      @ProduceMime({"application/foo", "application/bar"})
      @GET
      public String get()
      {
         return "GET";
      }
   }

   @Test
   public void testAcceptMultiple() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(MultipleResource.class);

      MediaType contentType = new MediaType("text", "plain");
      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/");

      MediaType foo = MediaType.valueOf("application/foo");
      MediaType bar = MediaType.valueOf("application/bar");

      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(foo);
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(MultipleResource.class.getMethod("get"), method.getMethod());
      }
      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(bar);
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(MultipleResource.class.getMethod("get"), method.getMethod());
      }
      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("*/*"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(MultipleResource.class.getMethod("get"), method.getMethod());
      }
      {
         ArrayList<MediaType> accepts = new ArrayList<MediaType>();
         accepts.add(MediaType.valueOf("application/*"));
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(MultipleResource.class.getMethod("get"), method.getMethod());
      }
   }

   @Path("/")
   public static class ConsumeResource
   {
      @ConsumeMime("application/foo")
      @GET
      public String doGetFoo()
      {
         return "foo";
      }

      @ConsumeMime("application/bar")
      @GET
      public String doGetBar()
      {
         return "bar";
      }

      @ConsumeMime("application/baz")
      @GET
      public String doGetBaz()
      {
         return "baz";
      }

      @ConsumeMime("*/*")
      @GET
      public Response doGetWildCard()
      {
         return Response.ok("wildcard", "application/wildcard").build();
      }
   }


   @Test
   public void testContentTypeMatching() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(ConsumeResource.class);

      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/");
      ArrayList<MediaType> accepts = new ArrayList<MediaType>();

      {
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, MediaType.valueOf("text/plain"), accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(ConsumeResource.class.getMethod("doGetWildCard"), method.getMethod());
      }
      {
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, MediaType.valueOf("application/foo"), accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(ConsumeResource.class.getMethod("doGetFoo"), method.getMethod());
      }
      {
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, MediaType.valueOf("application/bar"), accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(ConsumeResource.class.getMethod("doGetBar"), method.getMethod());
      }
      {
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, MediaType.valueOf("application/baz"), accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(ConsumeResource.class.getMethod("doGetBaz"), method.getMethod());
      }
   }

   @Path("/")
   public static class ComplexResource
   {
      @ConsumeMime("text/*")
      @ProduceMime("text/html")
      @GET
      public String method1()
      {
         return null;
      }

      @ConsumeMime("text/xml")
      @ProduceMime("text/json")
      @GET
      public String method2()
      {
         return null;
      }
   }

   @Test
   public void testComplex() throws Exception
   {
      ResourceMethodRegistry registry = new ResourceMethodRegistry(ResteasyProviderFactory.getInstance());
      registry.addPerRequestResource(ComplexResource.class);

      List<PathSegment> pathSegments = PathSegmentImpl.parseSegments("/");
      MediaType contentType = new MediaType("text", "xml");

      ArrayList<MediaType> accepts = new ArrayList<MediaType>();
      accepts.add(new MediaType("*", "*"));
      accepts.add(new MediaType("text", "html"));

      {
         ResourceMethod method = (ResourceMethod) registry.getResourceInvoker(createRequest("GET", pathSegments, contentType, accepts), null);
         Assert.assertNotNull(method);
         Assert.assertEquals(ComplexResource.class.getMethod("method2"), method.getMethod());
      }
   }


}
