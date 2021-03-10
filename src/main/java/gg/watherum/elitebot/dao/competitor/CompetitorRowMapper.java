package gg.watherum.elitebot.dao.competitor;

import gg.watherum.elitebot.dao.base.BaseRowMapper;
import gg.watherum.elitebot.model.Competitor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CompetitorRowMapper extends BaseRowMapper<Competitor> {

    public CompetitorRowMapper() {
    }

    @Override
    public Competitor mapRow(ResultSet rs, int rowNum) throws SQLException {
        Competitor competitor = new Competitor();
        competitor.setId(rs.getInt("id"));
        competitor.setName(rs.getString("competitor"));
        competitor.setSubscriber(rs.getBoolean("subscriber"));
        return competitor;
    }
}
