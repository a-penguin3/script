import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileStream {

    static ArrayList<String> successFile = new ArrayList<>();

    static ArrayList<String> failFile = new ArrayList<>();

    static final byte[] buffer = new byte[2 * 2048];

    /**
     * @param srcDir           压缩文件夹路径
     * @param outDir           压缩文件输出流
     * @param KeepDirStructure 是否保留原来的目录结构,
     *                         true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(File srcDir, String outDir,
                             boolean KeepDirStructure) throws RuntimeException, Exception {

        OutputStream out = new FileOutputStream(outDir);

        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            compress(srcDir, zos, KeepDirStructure);
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils:" + e.getMessage(), e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param KeepDirStructure 是否保留原来的目录结构,
     *                         true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos,
                                 String name, boolean KeepDirStructure) throws Exception {
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                if (KeepDirStructure) {
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    zos.closeEntry();
                }

            } else {
                for (File file : listFiles) {
                    if (KeepDirStructure) {
                        compress(file, zos, name + "/" + file.getName(),
                                KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }

                }
            }
        }
    }

    private static void compress(File sourceFile,
                                 ZipOutputStream zos, boolean KeepDirStructure) throws Exception {

        String name = sourceFile.getName();
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                if (KeepDirStructure) {
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    if (KeepDirStructure) {
                        compress(file, zos, name + "/" + file.getName(),
                                true);
                    } else {
                        compress(file, zos, file.getName(),
                                false);
                    }
                }
            }
        }

    }

    public static void getFile(String filePath) throws Exception {

        File socDir = new File(filePath);
        File[] files1 = socDir.listFiles();
        assert files1 != null;
        for (File pFile : files1) {
            if (pFile.isFile()) {
                continue;
            }
            File[] files = pFile.listFiles();
            assert files != null;
            try {
                for (File file : files) {
                    String fileName = file.getName();
                    if (!fileName.contains("C-02-27")) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        System.out.println("************************开始扫描一级路径" + fileName + "****************************");
                        File[] filesSeco = file.listFiles();
                        assert filesSeco != null;
                        for (File fileSeco : filesSeco) {
                            boolean successful = true;
                            String fileNameSeco = fileSeco.getName();
                            System.out.println("------------------------开始扫描二级路径" + fileNameSeco + "---------------------------");
                            String fileNameSecoPre = fileNameSeco.substring(0, 1);
                            String flag = fileName.substring(0, fileName.indexOf(']') + 1);
                            switch (fileNameSecoPre) {
                                case "I":
                                    if (fileSeco.isFile()) {
                                        successful = false;
                                        failFile.add("I系列文件需要集中放在文件夹内: " + fileSeco.getPath() + "     \r\n");
                                    } else {
                                        FileStream.toZip(fileSeco, fileSeco.getPath() + ".zip", true);
                                    }
                                    break;
                                case "M":
                                    if (fileSeco.isFile()) {
                                        successful = false;
                                        failFile.add("M系列文件需要集中放在文件夹内: " + fileSeco.getPath() + "        \r\n");
                                    } else {
                                        FileStream.toZip(fileSeco, fileSeco.getPath() + ".zip", true);
                                    }
                                    break;
                                case "N":
                                    if (fileSeco.isFile()) {
                                        successful = false;
                                        failFile.add("N系列文件需要集中放在文件夹内: " + fileSeco.getPath() + "        \r\n");
                                    } else {
                                        FileStream.toZip(fileSeco, fileSeco.getPath() + ".zip", true);
                                    }
                                    break;
                                case "B":
                                    successful = false;
                                    if (fileSeco.isFile()) {
                                        successFile.add(flag + "      $" + fileName + "    $" + fileSeco.getPath() + "        \r\n");
                                        System.out.println("------------------------B系列文件地址为：" + fileSeco.getPath() + "---------------------------");
                                    } else {
                                        failFile.add("B系列文件只能是单一文件: " + fileSeco.getPath() + "      \r\n");
                                    }
                                    break;
                                case "A":
                                    if (fileSeco.isFile()) {
                                        successful = false;
                                        failFile.add("A系列文件需要集中放在文件夹内: " + fileSeco.getPath());
                                    }else {
                                        FileStream.toZip(fileSeco, fileSeco.getPath() + ".zip", true);
                                    }
                                    break;
                                default:
                                    successful = false;
                                    failFile.add(fileName + "下,文件命名不合规:  " + fileSeco.getPath());
                                    break;
                            }
                            if (successful) {
                                successFile.add(flag + "      $" + fileName + "    $" + fileSeco.getPath() + ".zip" + "        \r\n");
                            }
                        }

                    }
                }
            } catch (Exception e) {
                failFile.add(e.getMessage());
                throw new Exception("压缩文件发生错误:" + e.getMessage(), e);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        System.out.println("--------------------------请输入需要扫描的文件夹地址-----------------------");
        String filePath = in.nextLine();
        System.out.println("-----------------------------输入压缩结果输出路劲--------------------------");
        String outPath = in.nextLine();
//        String filePath = "D:\\zhongyuan\\test\\ECM归档图纸";
//        String outPath = "D:\\test\\out";
        successFile.add("文件头名                      文件名                                文件路劲                                                               \r\n");
        FileStream.getFile(filePath);
        System.out.println("开始打印成功文件");
        String resultPath = outPath + File.separator + "success.txt";
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(resultPath), "GBK");
        BufferedWriter bw = new BufferedWriter(os);
        PrintWriter out = new PrintWriter(bw);
        for (String success : successFile) {
            System.out.println(success);
            out.println(success);
        }
        resultPath = outPath + File.separator + "fail.txt";
        OutputStreamWriter os1 = new OutputStreamWriter(new FileOutputStream(resultPath), "GBK");
        BufferedWriter bw1 = new BufferedWriter(os1);
        PrintWriter out1 = new PrintWriter(bw1);
        for (String fail : failFile) {
            out1.println(fail);
        }
        bw.close();
        os.close();
        out.close();
        bw1.close();
        os1.close();
        out1.close();
    }
}
