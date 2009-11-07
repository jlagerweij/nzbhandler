package net.lagerwey.nzb.net.lagerwey.nzb.domain;

/**
 * @author Jos Lagerweij
 */
public class SabnzbdCategory {
    private String category;

    public SabnzbdCategory() {
        System.out.println();
    }

    public SabnzbdCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
