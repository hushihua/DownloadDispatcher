package android.library.downloadcenter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class Files {

	private static String sd_card = "/sdcard/";
	private static String path = "guoyuan/images/";

	/**
	 * 创建文件夹
	 * 
	 * @param context
	 */
	public static void mkdir(Context context) {
		File file;
		file = new File(sd_card + path);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	/**
	 * 保存图片到SD卡
	 * 
	 * @param URL
	 * @param data
	 * @throws IOException
	 */
	public static void saveImage(String URL, byte[] data) throws IOException {
		String name = URL;
		saveData(sd_card + path, name, data);
	}

	/**
	 * 读取图片
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] readImage(String filename) throws IOException {
		String name = filename;
		byte[] tmp = readData(sd_card + path, name);
		return tmp;
	}

	/**
	 * 读取图片工具
	 * 
	 * @param path
	 * @param name
	 * @return
	 * @throws IOException
	 */
	private static byte[] readData(String path, String name) throws IOException {
		// String name = MyHash.mixHashStr(url);
		ByteArrayBuffer buffer = null;
		String paths = path + name;
		File file = new File(paths);
		if (!file.exists()) {
			return null;
		}
		InputStream inputstream = new FileInputStream(file);
		buffer = new ByteArrayBuffer(1024);
		byte[] tmp = new byte[1024];
		int len;
		while (((len = inputstream.read(tmp)) != -1)) {
			buffer.append(tmp, 0, len);
		}
		inputstream.close();
		return buffer.toByteArray();
	}

	/**
	 * 图片保存工具类
	 * 
	 * @param path
	 * @param fileName
	 * @param data
	 * @throws IOException
	 */
	private static void saveData(String path, String fileName, byte[] data)
			throws IOException {
		// String name = MyHash.mixHashStr(AdName);
		File file = new File(path + fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(data);
		outStream.close();
	}

	/**
	 * 判断文件是否存在 true存在 false不存在
	 * 
	 * @param url
	 * @return
	 */
	public static boolean compare(String url) {
		String name = url;
		String paths = sd_card + path + name;
		File file = new File(paths);
		if (!file.exists()) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public static boolean hasSdcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	   /**
     * 得到sd卡剩余大�?
     * @return
     */
    public static  long getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks/1024;
    }
	
	public static String getRootFilePath() {
		if (hasSdcard()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";// filePath:/sdcard/
		} else {
			return Environment.getDataDirectory().getAbsolutePath() + "/data/"; // filePath: /data/data/
		}
	}

}
