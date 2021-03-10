package gg.watherum.elitebot.dao.base;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public abstract class SqlBaseDao
{
    @Resource(name = "localJdbcTemplate")
    protected JdbcTemplate localJdbc;

    @Resource(name = "localNamedJdbcTemplate")
    protected NamedParameterJdbcTemplate localNamedJdbc;

    @Resource(name = "schemaMap")
    protected Map<String, String> schemaMap;

    protected Map<String, String> schemaMap() {
        return schemaMap;
    }

    /**
     * Convert a LocalDate to a Date. Returns null on null input.
     */
    public static java.sql.Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return java.sql.Date.valueOf(localDate);
    }

    /**
     * Convert a LocalDateTime to a Date. Returns null on null input.
     */
    public static Timestamp toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Timestamp.valueOf(localDateTime);
    }

    public static Time toTime(LocalTime localTime) {
        if (localTime == null) return null;
        return Time.valueOf(localTime);
    }

}
