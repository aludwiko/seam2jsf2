package org.jboss.seam.debug.jsf;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.view.facelets.FaceletCache;
import javax.faces.view.facelets.ResourceResolver;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.navigation.Pages;

import com.sun.faces.facelets.Facelet;
import com.sun.facelets.StateWriterControl;
import com.sun.faces.facelets.compiler.SAXCompiler;
import com.sun.faces.facelets.impl.DefaultFaceletFactory;
import com.sun.faces.facelets.impl.DefaultResourceResolver;
/**
 * Intercepts any request for a view-id like /debug.xxx and renders
 * the Seam debug page using facelets.
 * 
 * @author Gavin King
 */
public class SeamDebugPhaseListener implements PhaseListener
{
   private static final String STATE_KEY = "~facelets.VIEW_STATE~";
   
   public void beforePhase(PhaseEvent event)
   {
      FacesLifecycle.setPhaseId( event.getPhaseId() ); //since this gets called before SeamPhaseListener!
      
      if ( Pages.isDebugPage() )
      {
         try
         {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            URL url = SeamDebugPhaseListener.class.getClassLoader().getResource("META-INF/debug.xhtml");
            ResourceResolver resroler=new ResourceResolver(){
               @Override
               public URL resolveUrl(String path)
               {
                  return SeamDebugPhaseListener.class.getClassLoader().getResource(path);
               }};
            Facelet f = new DefaultFaceletFactory( new SAXCompiler(),resroler).getFacelet(url);
            UIViewRoot viewRoot = facesContext.getViewRoot();
            f.apply(facesContext, viewRoot);
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; UTF-8"); 
            ResponseWriter originalWriter = facesContext.getRenderKit().createResponseWriter( response.getWriter(), "text/html", "UTF-8" );
            StateWriterControl.initialize(originalWriter);
            ResponseWriter writer = StateWriterControl.createClone(originalWriter);
            facesContext.setResponseWriter(writer);
            writer.startDocument();
            viewRoot.encodeAll(facesContext);
            writer.endDocument();
            writer.close();
            writeState(facesContext, originalWriter);
            originalWriter.flush();
            facesContext.responseComplete();
         }
         catch (IOException ioe)
         {
            throw new RuntimeException(ioe);
         }
      }      
   }

   public void afterPhase(PhaseEvent event) {}

   public PhaseId getPhaseId()
   {
      return PhaseId.RENDER_RESPONSE;
   }
   
   private void writeState(FacesContext facesContext, Writer writer) throws IOException {
      try
      {
         if (StateWriterControl.isStateWritten())
         {
            String content = StateWriterControl.getAndResetBuffer();
            int end = content.indexOf(STATE_KEY);
            if (end >= 0)
            {
               StateManager stateMgr = facesContext.getApplication().getStateManager();
               Object stateObj = stateMgr.saveView(facesContext);
               String stateStr;
               if (stateObj == null)
               {
                  stateStr = null;
               }
               else
               {
                  stateMgr.writeState(facesContext, stateObj);
                  stateStr = StateWriterControl.getAndResetBuffer();
               }

               int start = 0;

               while (end != -1)
               {
                  writer.write(content, start, end - start);
                  if (stateStr != null)
                  {
                     writer.write(stateStr);
                  }
                  start = end + STATE_KEY.length();
                  end = content.indexOf(STATE_KEY, start);
               }
               writer.write(content, start, content.length() - start);
            }
            else
            {
               writer.write(content);
            }
         }
      }
      finally
      {
         StateWriterControl.release();
      }
   }
   
}
