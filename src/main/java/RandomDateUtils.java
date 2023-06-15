import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.handler.EntityListHandler;
import cn.hutool.db.sql.SqlExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @program: ReservedInstance
 * @description: FastoneDateUtil
 * @author: sunyuhua
 * @create: 2021-09-18 15:16
 **/
public class RandomDateUtils {

    /**
     * 指定开始时间和结束时间，生成指定范围内的时间
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static LocalTime randomTime(LocalTime startTime, LocalTime endTime) {
        int startSeconds = startTime.toSecondOfDay();
        int endSeconds = endTime.toSecondOfDay();
        int randomTime = ThreadLocalRandom
                .current()
                .nextInt(startSeconds, endSeconds);
        return LocalTime.ofSecondOfDay(randomTime);
    }

    /**
     * 生成随机日期
     *
     * @return
     */
    public static LocalDate randomDay(LocalDate startDate, LocalDate endDate) {
        Random random = new Random();
        int minDay = (int) startDate.toEpochDay();
        int maxDay = (int) endDate.toEpochDay();
        long randomDay = minDay + random.nextInt(maxDay - minDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    /**
     * 返回随机的日期和时间
     *
     * @return
     */
    public static LocalDateTime randomDateTime(LocalDate startDay, LocalDate endDay, LocalTime startTime, LocalTime endTime) {
        return randomDay(startDay,endDay).atTime(randomTime(startTime,endTime));
    }

    public static void main(String[] args) throws SQLException {

        DataSource ds = DSFactory.get();
        Connection conn = ds.getConnection();
        List<Entity> qr = SqlExecutor.query(conn, "select * from read_day_voice where status = 0", new EntityListHandler());
        qr.forEach(it ->{
            it.set("voice_time",randomDateTime(LocalDate.of(2023,5,1),LocalDate.now(),LocalTime.of(6,0,59),LocalTime.of(23,59,59)));
            it.set("status",1);
            try {
                Db.use().update(it,Entity.create("read_day_voice").set("id",it.get("id")));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

}


