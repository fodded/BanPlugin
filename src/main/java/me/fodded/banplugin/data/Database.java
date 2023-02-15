package me.fodded.banplugin.data;

import lombok.Getter;
import me.fodded.banplugin.Main;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class Database {

    public @Getter File file;

    public Database() {

    }

    public void createTables(String sql_statement) {
        try {
            Main.getDatabaseConnection().prepareStatement(
                    sql_statement
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement prepareStatement(String query, Object... vars) {
        try {
            PreparedStatement ps = Main.getPlugin().getConnection().prepareStatement(query);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
            }
            return ps;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void prepareStatement(String query) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                PreparedStatement ps = Main.getPlugin().getConnection().prepareStatement(query);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CachedRowSet query(String query, Object... vars) {
        CachedRowSet rowSet = null;

        try {
            Future<CachedRowSet> future = ForkJoinPool.commonPool().submit(new Callable<CachedRowSet>() {

                @Override
                public CachedRowSet call() {
                    try {
                        PreparedStatement ps = prepareStatement(query, vars);

                        ResultSet rs = ps.executeQuery();
                        CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                        crs.populate(rs);
                        rs.close();
                        ps.close();

                        if (crs.next()) {
                            return crs;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            if (future.get() != null) {
                rowSet = future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rowSet;
    }

    public void execute(String sql, Object... vars) {
        ForkJoinPool.commonPool().submit(() -> {
            update(sql, vars);
        });
    }

    private void update(String sql, Object... vars) {
        try {
            PreparedStatement ps = prepareStatement(sql, vars);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getCurrentSeason() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date());

        long monthsBetween = ChronoUnit.MONTHS.between(
                LocalDate.parse("2023-01-13").withDayOfMonth(1),
                LocalDate.parse(date).withDayOfMonth(1));

        return (int) (monthsBetween + 1);
    }

    public int getBanAmount(UUID banned_uuid) {
        String request = "SELECT * FROM `bans` WHERE `uuid` = ?";
        int amount = 0;

        try {
            ResultSet resultSet = query(request, banned_uuid.toString());
            if (resultSet == null) {
                return 0;
            }

            resultSet.last();
            amount = resultSet.getRow();
            resultSet.beforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return amount;
    }

    public boolean isPlayerBanned(UUID uuid) {
        String request = "SELECT `ban_time` FROM `bans` WHERE `uuid` = ?";
        try {
            ResultSet resultSet = query(request, uuid.toString());
            if(resultSet != null) {
                resultSet.last();
                long time = resultSet.getLong("ban_time");
                if(System.currentTimeMillis() + time > System.currentTimeMillis()) {
                    return true;
                }
            }
        } catch (SQLException e) {}

        return false;
    }

    public long getBanDate(UUID uuid) {
        String request = "SELECT `ban_date` FROM `bans` WHERE `uuid` = ?";

        try {
            ResultSet resultSet = query(request, uuid.toString());
            if(resultSet != null) {
                resultSet.last();
                long time = resultSet.getLong("ban_date");
                return time;
            }
        } catch (SQLException e) {}

        return -1;
    }

    public String getBanReason(UUID uuid) {
        String request = "SELECT `reason` FROM `bans` WHERE `uuid` = ?";
        try {
            ResultSet resultSet = query(request, uuid.toString());
            if(resultSet != null) {
                resultSet.last();
                return resultSet.getString("reason");
            }
        } catch (SQLException e) {}

        return "";
    }

    public long getBanDuration(UUID uuid) {
        String request = "SELECT `ban_time` FROM `bans` WHERE `uuid` = ?";
        try {
            ResultSet resultSet = query(request, uuid.toString());
            if(resultSet != null) {
                resultSet.last();
                long time = resultSet.getLong("ban_time");
                return time;
            }
        } catch (SQLException e) {}

        return -1;
    }

    public int getRating(final UUID uuid) {
        final String request = "SELECT `rating` FROM `season_" + getCurrentSeason() + "` WHERE `uuid` = ?";
        try {
            final ResultSet resultSet = query(request, uuid.toString());
            if (resultSet != null) {
                return resultSet.getInt("rating");
            }
        }
        catch (SQLException ex) {}
        return -1;
    }

    public int getPosition(final UUID uuid) {
        int pos = 0;
        final String request = "SELECT * FROM `season_" + getCurrentSeason() + "` ORDER BY `rating` DESC LIMIT 1000000;";
        try {
            final CachedRowSet cachedRowSet = query(request, new Object[0]);
            cachedRowSet.beforeFirst();
            while (cachedRowSet.next()) {
                ++pos;
                if (cachedRowSet.getString("uuid").equalsIgnoreCase(uuid.toString())) {
                    return pos;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return pos + 1;
    }
}
