package gg.watherum.elitebot.model;

public class SeasonRecord {

    private Integer points;
    private String competitorName;

    public SeasonRecord() {

    }

    public SeasonRecord(Integer points, String competitorName) {
        this.points = points;
        this.competitorName = competitorName;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public void setCompetitorName(String competitorName) {
        this.competitorName = competitorName;
    }
}
