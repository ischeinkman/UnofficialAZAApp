package org.ramonazaapi.events;


import org.ramonazaapi.interfaces.InfoWrapper;

/**
 * Created by ilanscheinkman on 1/29/15.
 */
public class EventInfoWrapper implements InfoWrapper {

    private String name;
    private String desc;
    private String planner;
    private String meet;
    private String mapsLocation;
    private String bring;
    private String date;
    private int Id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPlanner() {
        return planner;
    }

    public void setPlanner(String planner) {
        this.planner = planner;
    }

    public String getMeet() {
        return meet;
    }

    public void setMeet(String meet) {
        this.meet = meet;
    }

    public String getMapsLocation() {
        return mapsLocation;
    }

    public void setMapsLocation(String mapsLocation) {
        this.mapsLocation = mapsLocation;
    }

    public String getBring() {
        return bring;
    }

    public void setBring(String bring) {
        this.bring = bring;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * date.hashCode() + desc.hashCode() - planner.hashCode();
    }

    @Override
    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
                (obj instanceof EventInfoWrapper
                        && hashCode() == obj.hashCode()
                        && getName().equals(((EventInfoWrapper) obj).getName())
                        && getDesc().equals(((EventInfoWrapper) obj).getDesc())
                        && getDate().equals(((EventInfoWrapper) obj).getDate())
                        && getPlanner().equals(((EventInfoWrapper) obj).getPlanner())
                        && getMeet().equals(((EventInfoWrapper) obj).getMeet())
                        && getMapsLocation().equals(((EventInfoWrapper) obj).getMapsLocation())
                );
    }
}
