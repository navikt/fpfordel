package no.nav.foreldrepenger.fordel.web.app.konfig;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import no.nav.vedtak.sikkerhet.jaxrs.AuthenticationFilterDelegate;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    private ResourceInfo resourceinfo;

    public AuthenticationFilter() {
        // Ingenting
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        AuthenticationFilterDelegate.fjernKontekst();
    }

    @Override
    public void filter(ContainerRequestContext req) {
        AuthenticationFilterDelegate.validerSettKontekst(resourceinfo, req);
    }

}
