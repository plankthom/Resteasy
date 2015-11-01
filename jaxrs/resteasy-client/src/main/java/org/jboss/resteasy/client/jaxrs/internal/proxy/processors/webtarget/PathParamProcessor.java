package org.jboss.resteasy.client.jaxrs.internal.proxy.processors.webtarget;

import org.jboss.resteasy.client.jaxrs.internal.proxy.processors.WebTargetProcessor;

import javax.ws.rs.client.WebTarget;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PathParamProcessor implements WebTargetProcessor
{
   private final String paramName;
   private final boolean encoded;
   private final boolean slashInPath;

   public PathParamProcessor(String paramName)
   {
      this(paramName,false,true);
   }


   public PathParamProcessor(String paramName, boolean encoded, boolean slashInPath) 
   {
       this.paramName = paramName;
       this.encoded = encoded;
       this.slashInPath = slashInPath;
       if( this.encoded && this.slashInPath ) {
           throw new UnsupportedOperationException("Unsupported combination of @Encoded and @SlashInPath");
       }
   }

   @Override
   public WebTarget build(WebTarget target, Object param)
   {
       if (slashInPath) {
           return target.resolveTemplate(paramName, param,false);
       } else if (encoded) {
           return target.resolveTemplateFromEncoded(paramName, param);
       } else {
           return target.resolveTemplate(paramName, param);
       }
   }
}
