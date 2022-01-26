package gg.watherum.elitebot.model;

public class CompetitiveSet {

    private String competitorOneName = "";
    private Integer competitorOneWins = 0;
    private String competitorTwoName = "";
    private Integer competitorTwoWins = 0;
    private Integer winCondition = 0;
    private Integer bestOf = 0;
    private Integer firstTo = 0;
    private Integer gameNumber = 0;
    private String winner = "";
    private String setRound = "";
    private boolean setComplete = false;

    public CompetitiveSet() {}

    public void setUpFirstTo(Integer firstTo) {
        this.firstTo = firstTo;
        this.winCondition = firstTo;
        calculateGameNumber();
    }

    public void setUpBestOf(Integer bestOf) {
        if (isEven(bestOf)) {
            this.bestOf = bestOf + 1;
        }
        else {
            this.bestOf = bestOf;
        }
        this.winCondition = (int) Math.ceil( this.bestOf.doubleValue() / 2 );
        calculateGameNumber();
    }

    public void calculateGameNumber() {
        this.gameNumber = this.competitorOneWins + this.competitorTwoWins + 1;
    }

    /**
     * This function checks the wins of the competitors to see if the set is over.
     *
     * @returns {boolean}
     */
    public boolean compareWinsToWinCondition() {
        if (this.competitorOneWins >= this.winCondition ) {
            //ensure over clicks don't cause bad data
            this.competitorOneWins = this.winCondition;
            this.winner = this.competitorOneName;
            this.setComplete = true;
        }
        else if (this.competitorTwoWins >= this.winCondition ) {
            //ensure over clicks don't cause bad data
            this.competitorTwoWins = this.winCondition;
            this.winner = this.competitorTwoName;
            this.setComplete = true;
        }

        if (!this.setComplete) {
            this.calculateGameNumber();
        }

        return this.setComplete;
    };

    /**
     * Increments the wins of the first competitor
     * @returns {boolean}
     */
    public boolean incrementCompOneWins() {
        this.competitorOneWins = this.competitorOneWins + 1;
        return this.compareWinsToWinCondition();
    };

    /**
     * Increments the wins of the second competitor
     * @returns {boolean}
     */
    public boolean incrementCompTwoWins() {
        this.competitorTwoWins = this.competitorTwoWins + 1;
        return this.compareWinsToWinCondition();
    };

    /**
     * Decrements the wins of the first competitor
     * @returns {boolean}
     */
    public boolean decrementCompOneWins() {
        this.competitorOneWins = this.competitorOneWins - 1;
        return this.compareWinsToWinCondition();
    };

    /**
     * Decrements the wins of the second competitor
     * @returns {boolean}
     */
    public boolean decrementCompTwoWins() {
        this.competitorTwoWins = this.competitorTwoWins - 1;
        return this.compareWinsToWinCondition();
    };

    /**
     * Helper method to calculate how many wins are required to win the set
     * @param bestOf
     * @returns {number}
     */
    public Double calculateWinCondition(Integer bestOf) {
        return Math.ceil(bestOf / 2 );
    };

    /**
     * Helper method to determine if a number is odd
     * @param num
     * @returns {number}
     */
    public boolean isEven(Integer num) { return num.intValue() % 2 == 0;}

    public String getCompetitorOneName() {
        return competitorOneName;
    }

    public void setCompetitorOneName(String competitorOneName) {
        this.competitorOneName = competitorOneName;
    }

    public Integer getCompetitorOneWins() {
        return competitorOneWins;
    }

    public void setCompetitorOneWins(Integer competitorOneWins) {
        this.competitorOneWins = competitorOneWins;
    }

    public String getCompetitorTwoName() {
        return competitorTwoName;
    }

    public void setCompetitorTwoName(String competitorTwoName) {
        this.competitorTwoName = competitorTwoName;
    }

    public Integer getCompetitorTwoWins() {
        return competitorTwoWins;
    }

    public void setCompetitorTwoWins(Integer competitorTwoWins) {
        this.competitorTwoWins = competitorTwoWins;
    }

    public Integer getWinCondition() {
        return winCondition;
    }

    public void setWinCondition(Integer winCondition) {
        this.winCondition = winCondition;
    }

    public Integer getBestOf() {
        return bestOf;
    }

    public void setBestOf(Integer bestOf) {
        this.bestOf = bestOf;
    }

    public Integer getFirstTo() {
        return firstTo;
    }

    public void setFirstTo(Integer firstTo) {
        this.firstTo = firstTo;
    }

    public Integer getGameNumber() {
        return gameNumber;
    }

    public void setGameNumber(Integer gameNumber) {
        this.gameNumber = gameNumber;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isSetComplete() {
        return setComplete;
    }

    public void setSetComplete(boolean setComplete) {
        this.setComplete = setComplete;
    }

    public String getSetRound() {
        return setRound;
    }

    public void setSetRound(String setRound) {
        this.setRound = setRound;
    }
}
