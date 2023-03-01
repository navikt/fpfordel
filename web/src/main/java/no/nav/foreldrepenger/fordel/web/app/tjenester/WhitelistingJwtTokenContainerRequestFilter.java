package no.nav.foreldrepenger.fordel.web.app.tjenester;

import java.lang.reflect.Method;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// TODO: Denne er et eksmepel på et auth-filter som implements ContainerRequestFilter - token fra getHeaders + getCookies //NOSONAR
public class WhitelistingJwtTokenContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistingJwtTokenContainerRequestFilter.class);
    //@Context
    private ResourceInfo resourceInfo;

    //@Override
    public void filter(ContainerRequestContext ctx) { //NOSONAR
        Method method = resourceInfo.getResourceMethod();
        LOG.trace("{} i klasse {}", method.getName(), method.getDeclaringClass());
        if (method.getDeclaringClass().getName().startsWith("io.swagger")) {
            LOG.trace("{} er whitelisted", method.getName());
        } else {
            //super.filter(ctx); //NOSONAR
        }
    }

}
