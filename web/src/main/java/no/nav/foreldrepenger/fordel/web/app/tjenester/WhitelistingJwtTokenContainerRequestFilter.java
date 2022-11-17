package no.nav.foreldrepenger.fordel.web.app.tjenester;

import java.lang.reflect.Method;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.security.token.support.jaxrs.JwtTokenContainerRequestFilter;

// TODO: Denne kan egentlig fjernes etter vi er ute med Azure i prod.
public class WhitelistingJwtTokenContainerRequestFilter extends JwtTokenContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(WhitelistingJwtTokenContainerRequestFilter.class);
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext ctx) {
        Method method = resourceInfo.getResourceMethod();
        LOG.trace("{} i klasse {}", method.getName(), method.getDeclaringClass());
        if (method.getDeclaringClass().getName().startsWith("io.swagger")) {
            LOG.trace("{} er whitelisted", method.getName());
        } else {
            super.filter(ctx);
        }
    }

}
