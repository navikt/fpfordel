package no.nav.foreldrepenger.mottak.felles.kafka;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvironmentAlternative {

    String value() default DEFAULT;

    public static class Literal extends AnnotationLiteral<EnvironmentAlternative> implements EnvironmentAlternative {
        private final String value;

        @Override
        public String value() {
            return value;
        }

        public Literal(String value) {
            this.value = value;
        }

    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD })
    @Documented
    public @interface ContainerOfEnvironmentAlternative {
        EnvironmentAlternative[] value();
    }

    public static final String DEFAULT = "*";
}
