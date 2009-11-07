package net.lagerwey.nzb.net.lagerwey.nzb.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Jos Lagerweij
 */
public class SabnzbdJob {

    private String id;
    private String filename;
    private double mb;
    private double mbleft;
    private String msgid;

    public SabnzbdJob() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public double getMb() {
        return mb;
    }

    public void setMb(double mb) {
        this.mb = mb;
    }

    public double getMbleft() {
        return mbleft;
    }

    public void setMbleft(double mbleft) {
        this.mbleft = mbleft;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

}
