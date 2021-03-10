package gg.watherum.elitebot.dao.leaderboards;

import gg.watherum.elitebot.dao.base.SqlBaseDao;
import gg.watherum.elitebot.model.SeasonRecord;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static gg.watherum.elitebot.dao.leaderboards.SqlLeaderboardQuery.*;

@Repository
public class SqlLeaderboardDao extends SqlBaseDao {

    public Integer getLeaderboardDateID(LocalDate leaderboardDate) {
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("datetime", leaderboardDate.toString());
        return localNamedJdbc.queryForObject(GET_LEADERBOARD_DATE_ID.getSql("stats"), param, Integer.class);
    }

    public Integer insertLeaderboardDate(LocalDate leaderboardDate) {
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("datetime", leaderboardDate.toString());

        localNamedJdbc.update(INSERT_LEADERBOARD_DATE.getSql("stats"), param);
        return getLeaderboardDateID(leaderboardDate);
    }

    public void updateSeasonRecord(Integer points, Integer seasonNumber, Integer competitorID) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("points", points)
                .addValue("seasonNumber", seasonNumber)
                .addValue("id", competitorID);

        localNamedJdbc.update(UPDATE_SEASON_RECORD.getSql("stats"), params);
    }

    public void insertSeasonRecord(Integer points, Integer seasonNumber, Integer competitorID) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("points", points)
                .addValue("seasonNumber", seasonNumber)
                .addValue("id", competitorID);

        localNamedJdbc.update(INSERT_SEASON_RECORD.getSql("stats"), params);
    }

    public Integer getCompetitorSeasonRecord(Integer seasonNumber, Integer competitorID) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("seasonNumber", seasonNumber)
                .addValue("id", competitorID);

        try {
            return localNamedJdbc.queryForObject(GET_COMPETITOR_SEASON_RECORD.getSql("stats"), params, Integer.class);
        }
        catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public List<SeasonRecord> getSeasonRecordData(Integer seasonNumber) {
        MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("seasonNumber", seasonNumber);

        return localNamedJdbc.query(GET_SEASON_RECORD.getSql("stats"), param, new SeasonRecordRowMapper());
    }

    public void insertStreakRecord(Integer competitorID, Integer consecutiveWins, Integer datetimeID) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", competitorID)
                .addValue("consecutiveWins", consecutiveWins)
                .addValue("datetimeID", datetimeID);
        localNamedJdbc.update(INSERT_STREAK_RECORD.getSql("stats"), params);
    }

    public void insertWinRecord(Integer competitorID, Integer wins, Integer datetimeID) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", competitorID)
                .addValue("wins", wins)
                .addValue("datetimeID", datetimeID);
        localNamedJdbc.update(INSERT_WIN_RECORD.getSql("stats"), params);
    }

}
