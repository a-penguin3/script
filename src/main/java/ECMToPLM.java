import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.handler.EntityListHandler;
import cn.hutool.db.sql.SqlExecutor;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ECMToPLM {
    public static List<String> getIds(String filePath) {
        try {
            //创建工作簿对象
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(Files.newInputStream(Paths.get(filePath)));
            //获取工作簿下sheet的个数
//            int sheetNum = xssfWorkbook.getNumberOfSheets();
//            System.out.println("该excel文件中总共有：" + sheetNum + "个sheet");
            //读取第i个工作表
//            System.out.println("读取第" + (0) + "个sheet");
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            //获取最后一行的num，即总行数。此处从0开始
            int maxRow = sheet.getLastRowNum();
            ArrayList<String> ids = new ArrayList<>();
            for (int row = 0; row <= maxRow; row++) {
                //获取最后单元格num，即总单元格数 ***注意：此处从1开始计数***
//                System.out.println("--------第" + row + "行的数据如下--------");
//                System.out.println(sheet.getRow(row).getCell(0) + "  ");
                ids.add(String.valueOf(sheet.getRow(row).getCell(0)));
            }
            return ids;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static List<Entity> getFile() throws SQLException {
        DataSource ds = DSFactory.get();
        Connection conn = ds.getConnection();
//        String sql = "select file_id,file_name from dms_file where (file_createOperatorName = '向林凤' or 'RD182') and file_name like '%" + fileName + "%'";
//        System.out.println("文件名称为：" + fileName);
        List<Entity> qr = SqlExecutor.query(conn, "select file_id,file_name from dms_file where (file_createOperatorName = '向林凤' or 'RD182') and file_name like '%.pdf'", new EntityListHandler());
//        List<Entity> qr = SqlExecutor.query(conn, sql, new EntityListHandler());
        System.out.println(qr.size());
        return qr;
    }

    public static List<Entity> getFiles(String sql) throws SQLException{
        DataSource ds = DSFactory.get();
        Connection conn = ds.getConnection();
        List<Entity> qr = SqlExecutor.query(conn, sql, new EntityListHandler());
        System.out.println(qr.size());
        return qr;
    }

    public static String getHash(String id, String token) {
        String url = "https://ecm.zy-ivd.com/DownLoad/DownLoadCheck";
        String trueUrl = url + "?fileids=" + id + "&token=" + token;
        HttpResponse response = HttpRequest.get(trueUrl)
                .timeout(30000)
                .execute();
        Map<String, Object> res = JSONObject.parseObject(response.body());
        return String.valueOf(res.get("RegionHash"));
    }

    public static String login(String ip) {
        String url = "https://ecm.zy-ivd.com/api/services/Org/UserLoginIntegrationByUserLoginName";
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("LoginName", "admin");
        body.put("IPAddress", ip);
        body.put("IntegrationKey", "46aa92ec-66af-4818-b7c1-8495a9bd7f17");
        String bodyStr = JSONObject.toJSONString(body);
        HttpResponse response = HttpRequest.post(url).body(bodyStr).execute();
        String resStr = response.body();
        Map<String, Object> res = JSONObject.parseObject(resStr);
        return String.valueOf(res.get("data"));
    }

    public static void download(String filePath, String id, String token, String hash, String fileName) {
        String url = "https://ecm.zy-ivd.com/downLoad/index" + "?fileids=" + id + "&token=" + token + "&regionHash=" + hash + "&async=" + true;

        UrlBuilder buildUrl = UrlBuilder.of(url, StandardCharsets.UTF_8);

        String path = filePath + File.separator + fileName;
        //下载
        long size = HttpUtil.downloadFile(buildUrl.build(), FileUtil.file(path));
    }

    public static void main(String[] args) throws SQLException {

        Scanner in = new Scanner(System.in);
        System.out.println("++++++++++++++++ 请输入excel文件地址 ++++++++++++++++++++++");
        String excelPath = in.nextLine();
        System.out.println("++++++++++++++++++ 请输入ip地址 ++++++++++++++++++++");
        String ip = in.nextLine();
        System.out.println("+++++++++++++++++++++ 请输入存入文件夹地址 ++++++++++++++++++++++++");
        String filepath = in.nextLine();

        List<Entity> dataList = getFile();
        List<String> files = getIds(excelPath);
        String token = login(ip);

        // -----------------  方案一  -------------------
        AtomicInteger count = new AtomicInteger();
        dataList.forEach(it -> {
            String fileName = String.valueOf(it.get("file_name"));
            for (String file: files){
                String temp = file.substring(0,file.length()-4);
                if (fileName.contains(temp)){
                    String id = (String.valueOf(it.get("file_id")));
                    String regionHash = getHash(id, token);
                    download(filepath, id, token, regionHash, fileName);
                    count.getAndIncrement();
                }
            }
        });
        System.out.println(count);

        //---------------   方案二   ---------------------
//        StringBuilder sb = new StringBuilder();
//
//        for (String file: files){
//            sb.append("'");
//            sb.append(file);
//            sb.append("'");
//            sb.append(",");
//        }
//
//        sb.delete(sb.length()-1,sb.length());
//
//        String sql = "select file_id,file_name from dms_file where (file_createOperatorName = '向林凤' or 'RD182') and file_name in (" + sb +");";
//
//        List<Entity> dataList = getFiles(sql);


    }
}
