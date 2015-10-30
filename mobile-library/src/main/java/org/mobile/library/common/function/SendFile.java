package org.mobile.library.common.function;
/**
 * Created by 超悟空 on 2015/6/4.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * 将文件发送给其他应用打开
 *
 * @author 超悟空
 * @version 1.0 2015/6/4
 * @since 1.0
 */
public class SendFile {

    private Context context = null;

    public SendFile(Context context) {
        this.context = context;
    }

    /**
     * 用第三方应用打开文件
     *
     * @param file 要打开的文件
     */
    public void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        // 获取文件file的MIME类型
        String type = getMIMEType(file);
        // 设置intent的data和Type属性。
        intent.setDataAndType(Uri.fromFile(file), type);
        // 跳转
        context.startActivity(intent);
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file 指定的文件
     */
    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        // 获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        // 获得后缀
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if ("".equals(end)) {
            return type;
        }

        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (String[] aMIME_MapTable : MIME_MapTable) {

            if (end.equals(aMIME_MapTable[0]))
                type = aMIME_MapTable[1];
        }
        return type;
    }

    /**
     * MIME匹配表
     */
    private final String[][] MIME_MapTable = {
            //{后缀名， MIME类型}
            {".3gp" , "video/3gpp"} , {".apk" , "application/vnd.android.package-archive"} , {".asf" , "video/x-ms-asf"} , {".avi" , "video/x-msvideo"} , {".bin" , "application/octet-stream"} , {".bmp" , "image/bmp"} , {".c" , "text/plain"} , {".class" , "application/octet-stream"} , {".conf" , "text/plain"} , {".cpp" , "text/plain"} , {".doc" , "application/msword"} , {".docx" , "application/vnd.openxmlformats-officedocument.wordprocessingml.document"} , {".xls" , "application/vnd.ms-excel"} , {".xlsx" , "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"} , {".exe" , "application/octet-stream"} , {".gif" , "image/gif"} , {".gtar" , "application/x-gtar"} , {".gz" , "application/x-gzip"} , {".h" , "text/plain"} , {".htm" , "text/html"} , {".html" , "text/html"} , {".jar" , "application/java-archive"} , {".java" , "text/plain"} , {".jpeg" , "image/jpeg"} , {".jpg" , "image/jpeg"} , {".js" , "application/x-javascript"} , {".log" , "text/plain"} , {".m3u" , "audio/x-mpegurl"} , {".m4a" , "audio/mp4a-latm"} , {".m4b" , "audio/mp4a-latm"} , {".m4p" , "audio/mp4a-latm"} , {".m4u" , "video/vnd.mpegurl"} , {".m4v" , "video/x-m4v"} , {".mov" , "video/quicktime"} , {".mp2" , "audio/x-mpeg"} , {".mp3" , "audio/x-mpeg"} , {".mp4" , "video/mp4"} , {".mpc" , "application/vnd.mpohun.certificate"} , {".mpe" , "video/mpeg"} , {".mpeg" , "video/mpeg"} , {".mpg" , "video/mpeg"} , {".mpg4" , "video/mp4"} , {".mpga" , "audio/mpeg"} , {".msg" , "application/vnd.ms-outlook"} , {".ogg" , "audio/ogg"} , {".pdf" , "application/pdf"} , {".png" , "image/png"} , {".pps" , "application/vnd.ms-powerpoint"} , {".ppt" , "application/vnd.ms-powerpoint"} , {".pptx" , "application/vnd.openxmlformats-officedocument.presentationml.presentation"} , {".prop" , "text/plain"} , {".rc" , "text/plain"} , {".rmvb" , "audio/x-pn-realaudio"} , {".rtf" , "application/rtf"} , {".sh" , "text/plain"} , {".tar" , "application/x-tar"} , {".tgz" , "application/x-compressed"} , {".txt" , "text/plain"} , {".wav" , "audio/x-wav"} , {".wma" , "audio/x-ms-wma"} , {".wmv" , "audio/x-ms-wmv"} , {".wps" , "application/vnd.ms-works"} , {".xml" , "text/plain"} , {".z" , "application/x-compress"} , {".zip" , "application/x-zip-compressed"} , {"" , "*/*"}};
}
