package gg.watherum.elitebot.dao.leaderboards;

import gg.watherum.elitebot.dao.base.BasicSqlQuery;

public enum SqlLeaderboardQuery implements BasicSqlQuery {

    GET_LEADERBOARD_DATE_ID(
            "SELECT id from ${statsSchema}.leaderboard_dates where datetime ilike :datetime;"
    ),

    INSERT_LEADERBOARD_DATE(
            "INSERT INTO ${statsSchema}.leaderboard_dates(datetime) values (:datetime)"
    ),

    UPDATE_SEASON_RECORD(
            "update ${statsSchema}.season_leaderboards set points = :points " +
                    "where season_number = :seasonNumber and competitor_id = :id"
    ),

    INSERT_SEASON_RECORD(
            "INSERT INTO ${statsSchema}.season_leaderboards(season_number, competitor_id, points) " +
                    "VALUES (:seasonNumber, :id, :points)"
    ),

    GET_COMPETITOR_SEASON_RECORD(
            "SELECT points from ${statsSchema}.season_leaderboards " +
                    "where season_number = :seasonNumber and competitor_id = :id"
    ),

    GET_SEASON_RECORD(
            "Select c.competitor, l.points from ${statsSchema}.season_leaderboards l, " +
                    "elitebot.stats.competitors c where season_number = :seasonNumber " +
                    "and l.competitor_id = c.id order by points asc"
    ),

    INSERT_STREAK_RECORD(
            "INSERT INTO ${statsSchema}.arena_streak_leaderboards(competitor_id, streak, datetime_id) " +
                    "VALUES (:id,:consecutiveWins,:datetimeID)"
    ),

    INSERT_WIN_RECORD(
            "INSERT INTO ${statsSchema}.arena_wins_leaderboards(competitor_id, wins, datetime_id) " +
                    "VALUES (:id,:wins,:datetimeID)"
    )

    ;

    private String sql;

    SqlLeaderboardQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }

}
