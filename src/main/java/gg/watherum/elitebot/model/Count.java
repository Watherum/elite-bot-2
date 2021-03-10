package gg.watherum.elitebot.model;

public class Count {

    private String information = "";
    private Integer count = 0;

    public Count(String information, Integer count) {
        this.information = information;
        this.count = count;
    }

    public void incrementCount() {
        this.count = this.count + 1;
    }

    public void decrementCount() {
        this.count = this.count - 1;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
