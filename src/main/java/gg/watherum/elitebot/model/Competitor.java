package gg.watherum.elitebot.model;

public class Competitor {

    private String name = "";
    private Integer id = 0;
    private boolean subscriber = false;
    private Integer seasonPoints = 0;
    private Integer estimatedPoints = 0;
    private Integer losses = 0;
    private String leaveMessage = " when someone needs to join the arena, you must leave to let them join";

    public Competitor() {

    }

    public Competitor(String name, boolean subscriber) {
        this.name = name;
        this.subscriber = subscriber;
    }

    public Competitor(String name, Integer id, boolean subscriber) {
        this.name = name;
        this.id = id;
        this.subscriber = subscriber;
    }

    public boolean incrementLoses() {
        if (this.id != 1) {
            this.losses = this.losses + 1;

            if (isSubscriber() && this.losses == 5) {
                return true;
            }
            else if (this.losses == 3) {
                return true;
            }
            return false;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isSubscriber() {
        return subscriber;
    }

    public void setSubscriber(boolean subscriber) {
        this.subscriber = subscriber;
    }

    public Integer getSeasonPoints() {
        return seasonPoints;
    }

    public void setSeasonPoints(Integer seasonPoints) {
        this.seasonPoints = seasonPoints;
    }

    public Integer getLosses() {
        return losses;
    }

    public void setLosses(Integer losses) {
        this.losses = losses;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    public Integer getEstimatedPoints() {
        return estimatedPoints;
    }

    public void setEstimatedPoints(Integer estimatedPoints) {
        this.estimatedPoints = estimatedPoints;
    }
}
