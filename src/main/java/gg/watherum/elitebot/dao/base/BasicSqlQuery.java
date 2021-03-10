package gg.watherum.elitebot.dao.base;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import java.util.HashMap;
import java.util.Map;

public interface BasicSqlQuery
{
    /**
     * Return the sql query as defined..
     */
    String getSql();

    default String getSql(String schema) {
        Map<String, String> replaceMap = new HashMap<>();
        replaceMap.put("statsSchema", schema);
        return new StrSubstitutor(replaceMap).replace(getSql());
    }

    /**
     * Return the sql query as is with the given schemas.
     */
    default String getSql(Map<String, String> schemaMap, String sql) {
        StrSubstitutor strSub = new StrSubstitutor(schemaMap);
        return strSub.replace(sql);
    }
}
