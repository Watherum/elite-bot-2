package gg.watherum.elitebot.dao.competitor;

import gg.watherum.elitebot.dao.base.BasicSqlQuery;

public enum SqlCompetitorQuery implements BasicSqlQuery {

    INSERT_COMPETITOR(
            "INSERT INTO ${statsSchema}.competitors ( competitor, subscriber ) " +
                    "VALUES (:competitor, :subscriber)"
    ),

    GET_COMPETITOR(
            "Select * from ${statsSchema}.competitors where competitor ilike :name"
    ),

    UPDATE_SUBSCRIPTION_STATUS(
            "Update ${statsSchema}.competitors set subscriber = :substatus where id = :id"
    ),

    ;

    private String sql;

    SqlCompetitorQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }

}
