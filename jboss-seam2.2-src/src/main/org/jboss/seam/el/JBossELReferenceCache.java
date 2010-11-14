package org.jboss.seam.el;

import org.jboss.el.util.ReflectionUtil;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import static org.jboss.seam.annotations.Install.BUILT_IN;

@Name("org.jboss.seam.el.referenceCache")
@Scope(ScopeType.APPLICATION)
@Install(precedence = BUILT_IN, classDependencies="org.jboss.el.util.ReflectionUtil")
@Startup
public class JBossELReferenceCache {
    private int version=1;
    @Create
    public void start() {
       try{
          ReflectionUtil.startup();
       }catch(NoSuchMethodError e)
       {
          version=2;
       }
    }
    
    @Destroy 
    public void stop() {
       if(version==1)
          ReflectionUtil.shutdown();
    }
}
