package no.nav.foreldrepenger.fordel.web.app.rest;

final class ThreadLocalTimer extends ThreadLocal<Long> {
    public long start() {
        long value = System.currentTimeMillis();
        this.set(value);
        return value;
    }

    public long stop() {
        return System.currentTimeMillis() - get();
    }

    @Override
    protected Long initialValue() {
        return System.currentTimeMillis();
    }
}
