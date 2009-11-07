package net.lagerwey.nzb.net.lagerwey.nzb.domain;

/**
 * @author Jos Lagerweij
 */
public class SabnzbdAlias {
    private String name;
    private Class<?> clazz;

    public SabnzbdAlias(String name, Class<?> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
}
