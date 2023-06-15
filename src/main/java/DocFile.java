import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocFile {


    public static ArrayList<String> prodLine = new ArrayList<>();
    public static ArrayList<String> prod = new ArrayList<>();
    public static ArrayList<String> folder = new ArrayList<>();
    public static ArrayList<String> FILENAME = new ArrayList<>();
    public static ArrayList<String> fileNumber = new ArrayList<>();
    public static ArrayList<String> fileVersion = new ArrayList<>();
    public static ArrayList<String> primaryPath = new ArrayList<>();
    public static ArrayList<String> accessory = new ArrayList<>();

    public static HashMap<String,ArrayList<String>> FILE_PART = new HashMap<>();
    public static ArrayList<String> failPath = new ArrayList<>();

    public static String preFileName = "";
    public static String preFilePath = "";

    public static boolean first = true;

    public static void orderByName(File[] files) {
        List<File> fileList = Arrays.asList(files);
        fileList.sort((o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return -1;
            if (o1.isFile() && o2.isDirectory())
                return 1;
            return o2.getName().compareTo(o1.getName());
        });
    }

    public void generateData(String filePath) {
        File sourceFile = new File(filePath);
        File[] files = sourceFile.listFiles();
        assert files != null : "产品线为空";
        for (File file : files) {
            if (file.isFile()) {
                continue;
            }
            String lineName = file.getName();
            System.out.println("-----------------------------开始进行 *" + lineName + "* 产线遍历-----------------------------");
            File[] proFiles = file.listFiles();
            assert proFiles != null : "项目为空";
            for (File proFile : proFiles) {
                if (proFile.isFile()) {
                    continue;
                }
                //项目
                String prodName = proFile.getName();
                System.out.println("-----------------------------开始进行 *" + prodName + "* 产品遍历-----------------------------");
                File[] secProds = proFile.listFiles();
                assert secProds != null : "项目分层为空";
                //项目分层
                for (File secProd : secProds) {
                    if (secProd.isFile()) {
                        continue;
                    }
                    File[] defFiles = secProd.listFiles();
                    assert defFiles != null : "试剂文档项为空";

                    //具体文件夹
                    for (File defFile : defFiles) {
                        if (defFile.isFile()) {
                            continue;
                        }
                        String proName = defFile.getName();
                        File[] desFiles = defFile.listFiles();
                        assert desFiles != null : "试剂具体文档为空";
                        //具体文件
                        DocFile.orderByName(desFiles);
                        preFileName = "";
                        preFilePath = "";
                        for (File desFile : desFiles) {
                            if (desFile.isDirectory()) {
                                continue;
                            }
                            nameCheck(desFile, proName, lineName, prodName);
                        }
                    }
                }
            }
        }
    }

    /**
     * 导出培训记录excel
     *
     * @param title sheet
     * @return 返回
     */
    public XSSFWorkbook generateExcel(String title) {
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

        String[] heads = {"产品线", "项目", "folder", "name", "number", "zybio_DataRevison", "主文档路径", "附件路径"};
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

        for (int i = 0; i < primaryPath.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.setHeightInPoints(25);
            for (int j = 0; j < heads.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                switch (j) {
                    case 0:
                        cell.setCellValue(prodLine.get(i));
                        break;
                    case 1:
                        cell.setCellValue(prod.get(i));
                        break;
                    case 2:
                        cell.setCellValue(folder.get(i));
                        break;
                    case 3:
                        cell.setCellValue(FILENAME.get(i));
                        break;
                    case 4:
                        cell.setCellValue(fileNumber.get(i));
                        break;
                    case 5:
                        cell.setCellValue(fileVersion.get(i));
                        break;
                    case 6:
                        cell.setCellValue(primaryPath.get(i));
                        break;
                    default:
                        ArrayList<String> primary = FILE_PART.get(primaryPath.get(i));
                        if (primary == null){
                            throw new RuntimeException("未找到相关主文件:" + primaryPath.get(i));
                        }
                        if (primary.isEmpty()){
                            cell.setCellValue("空");
                        }else {
                            StringBuilder sb = new StringBuilder();
                            for (String part: primary){
                                sb.append(part);
                                sb.append("; ");
                            }
                            cell.setCellValue(sb.toString());
                        }
                        break;
                }
            }
        }
        return wb;
    }

    public void nameCheck(File file, String folderName, String lineName, String prodName) {
        String pattern = "^(.*\\[[0-9]{2}\\]_.*_)(签字版).*";
        String patternF = "^(.*\\[[0-9]{2}\\]_.*_)(未签字版).*";

        String filename = file.getName();
        Pattern r = Pattern.compile(pattern);
        Pattern f = Pattern.compile(patternF);
        Matcher m = r.matcher(filename);
        Matcher m1 = f.matcher(filename);
        String[] fileParts = filename.split("_");
        //一定要用这个  因为有未签字和签字的区别
        String cur = fileParts[0] + fileParts[1];
        first = preFileName.equals("") || !cur.equals(preFileName);
        if (m.matches()) {
            if (first) {
                preFileName = cur;
                preFilePath = file.getPath();
            } else {
                System.out.println("-----------------------------出现重复文件：" + filename + "-----------------------------");
                failPath.add(file.getPath());
                return;
            }
            System.out.println("----------------------------- 找到主文件：" + filename + "-----------------------------");
            prodLine.add(lineName);
            prod.add(prodName);
            FILENAME.add(fileParts[1]);
            folder.add(folderName);
            String numberTemp = fileParts[0];
            String version = numberTemp.substring(numberTemp.indexOf("[") + 1, numberTemp.indexOf("]"));
            fileVersion.add(version);
            String number = numberTemp.substring(0, numberTemp.indexOf("["));
            fileNumber.add(number);
            primaryPath.add(file.getPath());
            ArrayList<String> primaryFile = new ArrayList<>();
            FILE_PART.put(file.getPath(),primaryFile);
        } else if (m1.matches()) {
            if (first) {
                System.out.println("-----------------------------该文件不存在主文件：" + file.getPath() + "-----------------------------");
                failPath.add(file.getPath());
                return;
            }
            System.out.println("-----------------------------找到附件文件:" + filename + "-----------------------------");
            accessory.add(file.getPath());
            ArrayList<String> primary = FILE_PART.get(preFilePath);
            primary.add(file.getPath());
            FILE_PART.replace(preFilePath,primary);
        } else {
            failPath.add(file.getPath());
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.println("-----------------------------请输入初始文件地址-----------------------------");
        String filePath = in.nextLine();
        System.out.println("-----------------------------请输入输出文件地址-----------------------------");
        String outPath = in.nextLine();

//        String filePath = "D:\\test\\新建文件夹 (2)\\ShiJi";
//        String outPath = "D:\\test\\out";
        DocFile doc = new DocFile();
        doc.generateData(filePath);
        XSSFWorkbook wb = doc.generateExcel("文件列表");
        try {
            FileOutputStream fos = new FileOutputStream(new File(outPath + File.separator + "下载测试.xlsx"));
            wb.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String resultPath = outPath + "\\fail.txt";
        File saveFile = new File(resultPath);
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(saveFile), "GBK");
        BufferedWriter bw = new BufferedWriter(os);
        PrintWriter out = new PrintWriter(bw);
        for (String fail : failPath) {
            System.out.println(fail);
            out.println(fail);
        }
        out.close();
        bw.close();
        os.close();
        System.out.println("执行完毕");
    }
}
