package net.lagerwey.nzb.net.lagerwey.nzb.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

/**
 * @author Jos Lagerweij
 */
public class SabnzbdStatus {
    private String have_warnings;
    private String timeleft;
    private String mb;
    private String noofslots;
    private Boolean paused;
    private String mbleft;
    private String diskspace1;
    private String diskspace2;
    private String kbpersec;
    private List<SabnzbdJob> jobs;

    public SabnzbdStatus() {
        System.out.println();
    }

    public String getHave_warnings() {
        return have_warnings;
    }

    public void setHave_warnings(String have_warnings) {
        this.have_warnings = have_warnings;
    }

    public String getTimeleft() {
        return timeleft;
    }

    public void setTimeleft(String timeleft) {
        this.timeleft = timeleft;
    }

    public String getMb() {
        return mb;
    }

    public void setMb(String mb) {
        this.mb = mb;
    }

    public String getNoofslots() {
        return noofslots;
    }

    public void setNoofslots(String noofslots) {
        this.noofslots = noofslots;
    }

    public Boolean getPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    public String getMbleft() {
        return mbleft;
    }

    public void setMbleft(String mbleft) {
        this.mbleft = mbleft;
    }

    public String getDiskspace1() {
        return diskspace1;
    }

    public void setDiskspace1(String diskspace1) {
        this.diskspace1 = diskspace1;
    }

    public String getDiskspace2() {
        return diskspace2;
    }

    public void setDiskspace2(String diskspace2) {
        this.diskspace2 = diskspace2;
    }

    public String getKbpersec() {
        return kbpersec;
    }

    public void setKbpersec(String kbpersec) {
        this.kbpersec = kbpersec;
    }

    public List<SabnzbdJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<SabnzbdJob> jobs) {
        this.jobs = jobs;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

}
