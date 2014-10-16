package com.wowfly.wowyun.wowyun_mobile;

/**
 * Created by user on 9/23/14.
 */
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import java.security.MessageDigest;

class MD5 {
    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

    /** 把inputString加密 */
    public static String md5(byte[] inputStr) {
        return encodeByMD5(inputStr);
    }

    /** 对字符串进行MD5编码 */
    private static String encodeByMD5(byte[] inputStr) {
        if (inputStr != null) {
            try {
                // 创建具有指定算法名称的信息摘要
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                byte[] results = md5.digest(inputStr);
                // 将得到的字节数组变成字符串返回
                String result = byteArrayToHexString(results);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 轮换字节数组为十六进制字符串
     *
     * @param b
     *            字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    // 将一个字节转化成十六进制形式的字符串
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}

public class QzoneProto {
    private static final String TAG = "QzoneProto";
    public String qq, password, vcode;// QQ号，密码，验证码
    private byte[] uin;// 16进制表示的QQ号
    private HashMap<String, String> cookies;

    public QzoneProto(String qq, String password) {
        this.qq = qq;
        this.password = password;
        cookies = new HashMap<String, String>();
    }

    public String getQzoneProfile() {
        String url = "http://blog60.z.qq.com/mood/mood_list_my.jsp?B_UID=8065388";

        String text = connectURL(url);
        return text;
    }

    /** 登录 */
    public String login(String vc) {
        String url;// 访问网址
        String text;// 提示文本
        // 验证码
        if(vc.length() > 0) {
            vcode = vc;
            Log.i(TAG, " login qzone with vcode " + vcode);
        }

        if (vcode == null) {
            vcode = ptui_checkVC();
            if (vcode == "1") {//需要验证码
                Log.i(TAG, " vcode needed" + vcode);
                saveVC();
                return "1";
            }
        }
        // 登录
        System.out.println(vcode);
        url = "http://ptlogin2.qq.com/login?u="
                + this.qq
                + "&p="
                + getEncryption()
                + "&verifycode="
                + this.vcode
                + "&aid=1006102&u1=http%3A%2F%2Fid.qq.com%2Findex.html%23myfriends&h=1&ptredirect=1&ptlang=2052&from_ui=1&dumy=&fp=loginerroralert&action=8-57-411578&mibao_css=&t=5&g=1&js_type=0&js_ver=10015&login_sig=M68RroVE7d9cWVGLMysPechIltwu1GWLDkOrMwJ1O2VISYLTKwX6t3*qLIwl1DIa";
        text = connectURL(url);
        return text;
    }

    /** 加密，QQ号，密码，验证码 */
    private String getEncryption() {
        byte[] str1;
        String str2, str3;
        str1 = hexchar2bin(MD5.md5(password.getBytes()));
        str2 = MD5.md5(addByte(str1, this.uin));
        str3 = MD5.md5((str2 + this.vcode).getBytes());
        return str3;

    }

    /** 将返回的uin解析为byte[] */
    private byte[] uinToByte(String haxqq) {
        byte[] uin = new byte[8];
        String haxtext = haxqq.replaceAll("\\\\x", "");
        uin = hexchar2bin(haxtext);
        return uin;
    }

    /** 检查是否需要验证码，并返回uin */
    private String ptui_checkVC() {
        String text = "";// 返回的文本
        Response res = null;
        String url = "http://check.ptlogin2.qq.com/check?uin="
                + this.qq
                + "&appid=1006102&js_ver=10015&js_type=0&login_sig=y9izLTQDUx-VRJ*tu9aAnzzd3Th5R5d3-LSQ-R-DgQmZx7cRXxodffTGfDUzJtox&u1=http%3A%2F%2Fid.qq.com%2Findex.html&r="
                + getRandom(15);
        text = connectURL(url);
        // 解析返回信息
        if(text.equals("")) {
            return text;
        }

        this.uin = uinToByte(getTextInfo(text, "\\x", "');"));// 其实是\x，但java要转义符号));
        if (getTextInfo(text, "'", "','").equals("1")) {// 需要验证码
            return ("1");
        }
        return getTextInfo(text, "','", "','");

    }

    /**
     * 取随机数
     *
     * @param length
     *            随机数长度，如果为0则为17
     * @return 随机数文本
     */
    private String getRandom(int length) {
        String text = "";// 返回的随机数
        Random random = new Random();
        for (int i = 0; i < length;) {
            int num = random.nextInt(10);
            if (i != 0 || num != 0) {
                text += num + "";
                i++;
            }
        }
        return text;

    }

    /**
     * 取文本中间
     *
     * @param text
     *            被取的文本
     * @param start
     *            开始的文本(不包括)
     * @param end
     *            结束的文本(不包括)
     * @return 文本的中间
     */
    private String getTextInfo(String text, String start, String end) {
        int startIndex;
        int endIndex;
        startIndex = text.indexOf(start) + start.length();
        endIndex = text.indexOf(end, startIndex);
        text = text.substring(startIndex, endIndex);
        return text;

    }

    /** 访问网络 */
    private String connectURL(String url) {
        String text = "";
        try {
            Response res = Jsoup.connect(url).cookies(cookies)
                    .ignoreContentType(true).execute();
            Log.i(TAG, " res = " + res.cookies());
            cookies.putAll(res.cookies());
            text = new String(res.bodyAsBytes(), "utf-8");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return text;

    }

    /** 获取并保存验证码图片 */
    public void saveVC() {
        Response res;
        File file;
        byte[] b = null;// 返回值
        String url = "http://captcha.qq.com/getimage?aid=1006102&r=0."
                + getRandom(17) + "&uin=" + this.qq;
        try {
            res = Jsoup.connect(url).cookies(cookies).ignoreContentType(true)
                    .execute();
            b = res.bodyAsBytes();
            cookies.putAll(res.cookies());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 写入文件
        try {

            file = new File("/sdcard/qzone-vc.jpg");
            // 安卓的写法：
            // file=new File(this.getCacheDir(),"VC.jpg");
            // 如果不写在一个Activity内，则this写成getContext
            FileOutputStream out = new FileOutputStream(file);
            out.write(b);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** 将md5值转为byte[] */
    private byte[] hexchar2bin(String hax) {
        Log.i(TAG, " hax " + hax);
        int sep = hax.indexOf("'");
        if(sep > 0) {
            hax = hax.substring(0, sep);
        }
        Log.i(TAG, " hax " + hax);
        hax = hax.toUpperCase();// 只是看着舒服点，可以去掉
        byte[] b = new byte[hax.length() / 2];
        for (int i = 0; i < hax.length() - 1; i = i + 2) {
            b[i / 2] = (byte) Integer.parseInt(hax.substring(i, i + 2), 16);
        }
        return b;
    }

    /**
     * byte增加，JAVA中没找到直接合并的方法
     *
     * @param b1
     *            byte[]数据1
     * @param b2
     *            byte[]数据2
     * @return b1和b2的集合
     */
    private byte[] addByte(byte[] b1, byte[] b2) {
        byte[] by = new byte[b1.length + b2.length];
        for (int i = 0; i < b1.length; i++) {
            by[i] = b1[i];
        }
        for (int i = 0; i < b2.length; i++) {
            by[b1.length + i] = b2[i];
        }
        return by;
    }
}
