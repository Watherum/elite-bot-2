package gg.watherum.elitebot.model;

public class Streak {

    private String victor = "No Victor";
    private Integer consecutiveWins = 0;

    public Streak() {}

    public Streak(String competitor, Integer consecutiveWins) {
        this.victor = competitor;
        this.consecutiveWins = consecutiveWins;
    }

    public String getVictor() {
        return victor;
    }

    public void setVictor(String victor) {
        this.victor = victor;
    }

    public Integer getConsecutiveWins() {
        return consecutiveWins;
    }

    public void setConsecutiveWins(Integer consecutiveWins) {
        this.consecutiveWins = consecutiveWins;
    }

    public void incrementWins() {
        this.consecutiveWins = this.consecutiveWins + 1;
    }

    public void decrementWins() {
        this.consecutiveWins = this.consecutiveWins - 1;
    }
}
