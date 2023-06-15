import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class test {

    public static void main(String[] args) throws Exception {
        String lastStartTime = "2023-05-30 16:00:00";
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(lastStartTime,format);
        String visitTimeFormat = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        Thread.sleep(1000);
        System.out.print(visitTimeFormat);
    }
}
