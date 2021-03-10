package gg.watherum.elitebot.dao.leaderboards;

import gg.watherum.elitebot.dao.base.BaseRowMapper;
import gg.watherum.elitebot.model.SeasonRecord;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SeasonRecordRowMapper extends BaseRowMapper<SeasonRecord> {

    public SeasonRecordRowMapper() {
    }

    @Override
    public SeasonRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        SeasonRecord seasonRecord = new SeasonRecord();
        seasonRecord.setPoints(rs.getInt("points"));
        seasonRecord.setCompetitorName(rs.getString("competitor"));
        return seasonRecord;
    }
}
