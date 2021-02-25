package no.nav.foreldrepenger.fordel.web.server.jetty.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

public final class JndiUtil {

    private JndiUtil() {
        // hidden ctor
    }

    @SuppressWarnings("unchecked")
    public static <T> T lookup(String jndiName) {
        try {
            return ((T) new InitialContext().lookup(jndiName));
        } catch (NamingException e) {
            throw new IllegalArgumentException("Looking up " + jndiName, e);
        }
    }

    public static void register(String jndiName, Object object) {
        try {
            InitialContext ictx = new InitialContext();
            Name name = ictx.getNameParser(jndiName).parse(jndiName);
            Context ctx = ictx;
            for (int i = 0, max = name.size() - 1; i < max; i++) {
                ctx = getOrCreateSubcontext(name, ctx, i);
            }

            ictx.rebind(jndiName, object);
        } catch (NamingException e) {
            throw new IllegalArgumentException("Ugyldig jndiname: " + jndiName, e);
        }
    }

    private static Context getOrCreateSubcontext(Name name, Context ctx, int i) throws NamingException {
        try {
            return ctx.createSubcontext(name.get(i));
        } catch (NameAlreadyBoundException ignoreAndContinue) {
            return (Context) ctx.lookup(name.get(i));
        }
    }

}