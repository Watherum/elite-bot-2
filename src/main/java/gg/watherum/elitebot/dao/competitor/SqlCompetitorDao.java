package gg.watherum.elitebot.dao.competitor;

import gg.watherum.elitebot.dao.base.SqlBaseDao;
import gg.watherum.elitebot.model.Competitor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import static gg.watherum.elitebot.dao.competitor.SqlCompetitorQuery.*;

@Repository
public class SqlCompetitorDao extends SqlBaseDao {

    public Competitor insertCompetitor(Competitor competitor) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("competitor", competitor.getName())
                .addValue("subscriber", competitor.isSubscriber());

        localNamedJdbc.update(INSERT_COMPETITOR.getSql("stats"), params);
        return getCompetitor(competitor.getName());
    }

    public Competitor getCompetitor(String name) {

        try {
            MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("name", name);
            return localNamedJdbc.queryForObject(GET_COMPETITOR.getSql("stats") , param, new CompetitorRowMapper());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    public void updateSubscriptionStatus(Integer id, boolean subStatus) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("substatus", subStatus)
                .addValue("id", id);

        localNamedJdbc.update(UPDATE_SUBSCRIPTION_STATUS.getSql("stats"), params);
    }

}
