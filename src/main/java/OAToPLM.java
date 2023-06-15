import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OAToPLM {

    public static ArrayList<String> failList = new ArrayList<>();
    public static ArrayList<String> idList = new ArrayList<>();
    public static ArrayList<String> filenameList = new ArrayList<>();
    public static ArrayList<String> filepathList = new ArrayList<>();

    /**
     * 读取excel
     * @param filePath 文件位置
     * @return ids
     */
    public static List<String> getIds(String filePath) {
        try {
            //创建工作簿对象
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(filePath));
            //获取工作簿下sheet的个数
            int sheetNum = xssfWorkbook.getNumberOfSheets();
            System.out.println("该excel文件中总共有：" + sheetNum + "个sheet");
            //读取第i个工作表
            System.out.println("读取第" + (1) + "个sheet");
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            //获取最后一行的num，即总行数。此处从0开始
            int maxRow = sheet.getLastRowNum();
            ArrayList<String> ids = new ArrayList<>();
            for (int row = 1; row <= maxRow; row++) {
                //获取最后单元格num，即总单元格数 ***注意：此处从1开始计数***
                System.out.println("--------第" + row + "行的数据如下--------");
                System.out.println(sheet.getRow(row).getCell(2) + "  ");
                ids.add(String.valueOf(sheet.getRow(row).getCell(2)));
            }
            return ids;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 获取文件下载地址
     * @param url 下载地址
     * @param ids  id列表
     * @param authorization 鉴权
     * @return 下载地址列表
     */
    public static List<String> getFile(String url, List<String> ids, String authorization) {

        ArrayList<String> downloadUrls = new ArrayList<>();

        for (String id : ids) {
            String trueUrl = url + "?fdId=" + id;
            HttpResponse request = HttpRequest.get(trueUrl)
                    .header(Header.AUTHORIZATION, authorization)
                    .timeout(30000).execute();


            System.out.println("请求地址：" + trueUrl);
            System.out.println("请求返回：" + request.body());
            if (request.body() == null || Objects.equals(request.body(), "")) {
                System.out.println("id为：" + id + "请求出错，未获得下载链接");
                failList.add(id);
                continue;
            }
            idList.add(id);
            downloadUrls.add(request.body());
        }

        return downloadUrls;
    }

    /**
     * 导出培训记录excel
     * @param title sheet
     * @return 返回
     */
    public static XSSFWorkbook generateExcel(String title) {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(title);
        Row head = sheet.createRow(0);
        //-----------表头样式----------------
        //字体
        Font font = wb.createFont();
        font.setBoldweight(font.BOLDWEIGHT_BOLD);
        head.setHeightInPoints(30);//行高
        //设置样式
        CellStyle headStyle = wb.createCellStyle();
        headStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        headStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
        headStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);//上边框
        headStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);//下边框
        headStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);//左边框
        headStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);//右边框
        headStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());//上边框颜色
        headStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());//下边框颜色
        headStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());//左边框颜色
        headStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());//右边框颜色
        headStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());//背景色
        headStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headStyle.setFont(font);

        String[] heads = {"文件id", "文件名称", "文件路径"};
        for (int i = 0; i < heads.length; i++) {
            Cell cell = head.createCell(i);
            cell.setCellStyle(headStyle);
            cell.setCellValue(heads[i]);
            sheet.setColumnWidth(i, (int) (35.7 * 200));
        }
        //-----------表头样式 end------------------

        //表体内容填充
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);

        for (int i = 0; i < idList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(25);
            for (int j = 0; j < heads.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                switch (j) {
                    case 0:
                        cell.setCellValue(idList.get(i));
                        break;
                    case 1:
                        cell.setCellValue(filenameList.get(i));
                        break;
                    default:
                        cell.setCellValue(filepathList.get(i));
                        break;
                }
            }
        }
        return wb;
    }

    public static void download(List<String> downloadUrls, String outPath) throws UnsupportedEncodingException {
        for (String url : downloadUrls) {
            UrlBuilder buildUrl = UrlBuilder.of(url, StandardCharsets.UTF_8);
            String query = buildUrl.getQueryStr();
            String fileName = query.substring(query.indexOf("filename") + 9);
            fileName = URLDecoder.decode(fileName, String.valueOf(StandardCharsets.UTF_8));
            System.out.println(fileName);
            filenameList.add(fileName);
            String path = outPath + File.separator + fileName;
            System.out.println(path);
            filepathList.add(path);
            //下载
            long size = HttpUtil.downloadFile(buildUrl.build(), FileUtil.file(path));
            System.out.println("Download size: " + size);
        }
    }


    public static void main(String[] args) throws Exception {

        Scanner in = new Scanner(System.in);
        //创建工作簿对象
        String filePath = "D:\\test\\历史物料RD280_20230213.xlsx";
        String url = "https://portal.zy-ivd.com/api/sys-attachment/sysAttachmentRestService/getDonwloadUrl";
        String outPath = "D:\\test\\out";

//        System.out.println("----------------------请输入OA文件id地址----------------------");
//        String filePath = in.nextLine();
//        System.out.println("----------------------请输入OA文件下载地址请求url----------------------");
//        String url = in.nextLine();
//        System.out.println("----------------------请输入下载地址保存路径----------------------");
//        String outPath = in.nextLine();
//        System.out.println("----------------------请输入登录账号----------------------");
//        String user = in.nextLine();
//        System.out.println("----------------------请输入登录密码----------------------");
//        String password = in.nextLine();


        String user = "WuliaoGuige";
        String password = "WuliaoGuige@*#06#";
        String Authorization = "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        System.out.println("------------------------开始读取excel中的内容---------------------------");
        List<String> ids = OAToPLM.getIds(filePath);
        System.out.println("Authorization为：" + Authorization);

        System.out.println("---------------------------开始获取下载链接------------------------------");
        List<String> downloadUrls = OAToPLM.getFile(url, ids, Authorization);
        OAToPLM.download(downloadUrls, outPath);

        XSSFWorkbook wb = generateExcel("文件列表");
        try {
            FileOutputStream fos = new FileOutputStream(new File(outPath + File.separator + "oa对接文件列表.xlsx"));
            wb.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String resultPath = outPath + "\\result.txt";
        File saveFile = new File(resultPath);
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(saveFile), "GBK");
        BufferedWriter bw = new BufferedWriter(os);
        PrintWriter out = new PrintWriter(bw);
        for (String success : failList) {
            System.out.println(success);
            out.println(success);
        }
        out.close();
        bw.close();
        os.close();
    }
}
