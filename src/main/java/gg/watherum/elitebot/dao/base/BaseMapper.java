package gg.watherum.elitebot.dao.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class BaseMapper
{
    /**
     * Read the 'column' date value from the result set and cast it to a LocalDate.
     */
    public static LocalDate getLocalDateFromRs(ResultSet rs, String column) throws SQLException {
        if (rs.getDate(column) == null) return null;
        return rs.getDate(column).toLocalDate();
    }

    /**
     * Read the 'column' date value from the result set and cast it to a LocalDateTime.
     */
    public static LocalDateTime getLocalDateTimeFromRs(ResultSet rs, String column) throws SQLException {
        if (rs.getTimestamp(column) == null) return null;
        return rs.getTimestamp(column).toLocalDateTime();
    }
}
