import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.jcraft.jsch.*;
import consts.SftpConst;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

public class ZybioSFTPUtil implements SftpConst {

    private ChannelSftp sftp;

    private Session session;
    /**
     * SFTP 登录用户名
     */
    private String username;
    /**
     * SFTP 登录密码
     */
    private String password;
    /**
     * 私钥
     */
    private String privateKey;
    /**
     * SFTP 服务器地址IP地址
     */
    private String host;
    /**
     * SFTP 端口
     */
    private int port;


    /**
     * 构造基于密码认证的sftp对象
     */
    public ZybioSFTPUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 构造基于秘钥认证的sftp对象
     */
    public ZybioSFTPUtil(String username, String host, int port, String privateKey) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
    }

    public ZybioSFTPUtil() {
        this.username = USERNAME;
        this.password = PASSWORD;
        this.host = HOST;
        this.port = 22;
    }

    public ChannelSftp getChannelSftp() {
        return this.sftp;
    }

    /**
     * 连接sftp服务器
     */
    public void login() {
        try {
            JSch jsch = new JSch();
            if (privateKey != null) {
                jsch.addIdentity(privateKey);// 设置私钥
            }

            session = jsch.getSession(username, host, port);

            if (password != null) {
                session.setPassword(password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();

            sftp = (ChannelSftp) channel;
//            log.info("登陆 Sftp Server Success");
        } catch (JSchException e) {
            e.printStackTrace();
//            log.error(e.getMessage());
        }
    }

    /**
     * 关闭连接 server
     */
    public void logout() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory
     *
     * @param basePath     服务器的基础路径
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     */
    public void upload(String basePath, String directory, String sftpFileName, InputStream input) throws SftpException {
        try {
            sftp.cd(basePath);
            sftp.cd(directory);
        } catch (SftpException e) {
            //目录不存在，则创建文件夹
            String[] dirs = directory.split("/");
            String tempPath = basePath;
            for (String dir : dirs) {
                if (null == dir || "".equals(dir)) continue;
                tempPath += "/" + dir;
                try {
                    sftp.cd(tempPath);
                } catch (SftpException ex) {
                    sftp.mkdir(tempPath);
                    sftp.cd(tempPath);
                }
            }
        }
        sftp.put(input, sftpFileName);  //上传文件
    }


    /**
     * 下载文件。
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的路径
     */
    public void download(String directory, String downloadFile, String saveFile) throws SftpException, IOException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        File file = new File(saveFile);
        sftp.get(downloadFile, Files.newOutputStream(file.toPath()));
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件名
     * @return 字节数组
     */
    public InputStream download(String directory, String downloadFile) throws SftpException, IOException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        return sftp.get(downloadFile);
    }


    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws SftpException {
        sftp.cd(directory);
        sftp.rm(deleteFile);
    }


    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     */
    public Vector<?> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    public ArrayList<String> listFiles1(String dir) throws SftpException {
        ArrayList<String> files = new ArrayList<>();
        sftp.cd(dir);
        Vector<?> lss = sftp.ls("*");
        for (int i = 0; i < lss.size(); i++) {
            Object obj = lss.elementAt(i);
            if (obj instanceof ChannelSftp.LsEntry) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                if (!entry.getAttrs().isDir()) {
                    files.add(entry.getFilename());
                }
                if (entry.getAttrs().isDir()) {
                    if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                        files.add(entry.getFilename());
                    }
                }
            }
        }
        return files;
    }

    public void listDir(StringBuilder virtualPath, StringBuilder obsPath, ArrayList<String> dirs, String code) throws SftpException {
        sftp.cd(obsPath.toString());
        Vector<?> lss = sftp.ls("*");
        for (Object ls : lss) {
            assert ls instanceof ChannelSftp.LsEntry : "error ls is not ChannelSftp.LsEntry";
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) ls;
            if (entry.getAttrs().isDir()) {
                if (entry.getFilename().equals("@Recycle") || entry.getFilename().equals("@Recently-Snapshot") || entry.getFilename().equals("scripts")) {
                    continue;
                }
                virtualPath.append("/").append(entry.getFilename());
                obsPath.append("/").append(entry.getFilename());
                listDir(virtualPath, obsPath, dirs, code);
                virtualPath.delete(virtualPath.lastIndexOf("/"), virtualPath.length());
                obsPath.delete(obsPath.lastIndexOf("/"), obsPath.length());
            } else {
                if (entry.getFilename().contains(code)) {
                    dirs.add(virtualPath.toString() + "/" + entry.getFilename());
                }
            }
        }
    }

    /**
     * 将InputStream写入本地文件
     *
     * @param destination 写入本地目录
     * @param input       输入流
     * @throws IOException
     */
    private static void writeToLocal(String destination, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }


    public static void main(String[] args) {
        ZybioSFTPUtil sftp = new ZybioSFTPUtil();

        sftp.login();
        try {

            StringBuilder virtualPath = new StringBuilder(VIRTUAL);
            StringBuilder obsPath = new StringBuilder(URI);
            ArrayList<String> dirs = new ArrayList<>();
//            Vector<?> vector = sftp.listFiles("/share/CACHEDEV2_DATA/homes/DOMAIN=ZY-IVD/zhangzihao");
            sftp.listDir(virtualPath, obsPath, dirs,"02-55-55-5555-55");
            System.out.println("size :" + dirs.size());
            for (Object v : dirs) {
                System.out.println(v);
                String value = String.valueOf(v);
                value = value.substring(8);
                int index = value.lastIndexOf("/");
                String fileName = value.substring(index + 1);
                String filePath = value.substring(0,index);
                System.out.println("fileName:" + fileName + " filePath:" + filePath );
//                InputStream file = sftp.download(URI + filePath,fileName);
             }
//            System.out.println(obsPath);

//            System.out.println("下载成功！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sftp.logout();
        }
    }

}
