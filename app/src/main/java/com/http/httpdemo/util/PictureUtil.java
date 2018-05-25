package com.http.httpdemo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PictureUtil {
	public static final long SIZE_200K = 200 * 1024L;
	public static final long SIZE_30K = 30 * 1024L;
	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/**
	 * 根据文件名称获取文件的后缀字符串
	 * 
	 * @param filename		文件的名称,可能带路径
	 * @return 				文件的后缀字符串
	 */
	public static String getFileExtensionFromUrl(String filename) {
		if (!TextUtils.isEmpty(filename)) {
			int dotPos = filename.lastIndexOf('.');
			if (0 <= dotPos) {
				return filename.substring(dotPos + 1);
			}
		}
		return "";
	}

	public static String getFileName(String filename) {
		if (!TextUtils.isEmpty(filename)) {
			int pos = filename.lastIndexOf('/');
			if (0 <= pos) {
				return filename.substring(pos + 1);
			}
		}
		return filename;
	}

	/**
	 * 根据路径删除图片
	 * 
	 * @param path			要删除的文件路径
	 */
	public static void deleteTempFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

	// 按图片大小(字节大小)缩放图片
	public static Bitmap fitSizeImg(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (file.length() < 20480) { // 0-20k
			opts.inSampleSize = 1;
		} else if (file.length() < 51200) { // 20-50k
			opts.inSampleSize = 2;
		} else if (file.length() < 307200) { // 50-300k
			opts.inSampleSize = 4;
		} else if (file.length() < 819200) { // 300-800k
			opts.inSampleSize = 6;
		} else if (file.length() < 1048576) { // 800-1024k
			opts.inSampleSize = 8;
		} else {
			opts.inSampleSize = 10;
		}
		resizeBmp = BitmapFactory.decodeFile(file.getPath(), opts);
		return resizeBmp;
	}

	public static Bitmap getImageByUrl(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 5;// 图片宽高都为原来的二分之一，即图片为原来的四分之一
		Bitmap b = BitmapFactory.decodeFile(path, options);
		if (b != null) {
			return b;
		}
		return null;
	}

	/**
	 * 拷贝文件
	 * 
	 * @param sourceFile	源文件
	 * @param destFile		目标文件
	 * @return 				是否拷贝成功
	 */
	public static boolean copyFile(File sourceFile, File destFile) {
		boolean isCopyOk = false;
		byte[] buffer = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 如果此时没有文件夹目录就创建
		String canonicalPath = "";
		try {
			canonicalPath = destFile.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!destFile.exists()) {
			if (canonicalPath.lastIndexOf(File.separator) >= 0) {
				canonicalPath = canonicalPath.substring(0,
						canonicalPath.lastIndexOf(File.separator));
				File dir = new File(canonicalPath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
		}

		try {
			bis = new BufferedInputStream(new FileInputStream(sourceFile),
					DEFAULT_BUFFER_SIZE);
			bos = new BufferedOutputStream(new FileOutputStream(destFile),
					DEFAULT_BUFFER_SIZE);
			buffer = new byte[DEFAULT_BUFFER_SIZE];
			int len = 0;
			while ((len = bis.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
				bos.write(buffer, 0, len);
			}
			bos.flush();
			isCopyOk = sourceFile.length() == destFile.length();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
					bos = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
				buffer = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.i("wh", "copyFile.sourceFile = " + sourceFile + ", destFile = "
				+ destFile + ", isCopyOk = " + isCopyOk);
		return isCopyOk;
	}

	/**
	 * 判断是否是PNG图片
	 * 
	 * @param fileName		要判断的文件名称
	 */
	public static boolean isPNG(String fileName) {
		if (!TextUtils.isEmpty(fileName)) {
			String extension = getFileExtensionFromUrl(fileName);
			return "png".equalsIgnoreCase(extension);
		}
		return false;
	}

	/**
	 * 判断是否是JPG图片
	 * 
	 * @param fileName		要判断的文件名称
	 */
	public static boolean isJPG(String fileName) {
		if (!TextUtils.isEmpty(fileName)) {
			String extension = getFileExtensionFromUrl(fileName);
			return "jpg".equalsIgnoreCase(extension)
					|| "jpeg".equalsIgnoreCase(extension)
					|| "jpe".equalsIgnoreCase(extension);
		}
		return false;
	}

	/**
	 * 保存Bitmap到本地文件
	 * 
	 * @param bitmap	要保存的bitmap
	 * @param format	压缩格式
	 * @param quality	1-100，只对JPG格式有效，PNG格式忽略该参数
	 * @param file		保存的文件
	 */
	public static boolean writeBitmapToFile(Bitmap bitmap,
                                            Bitmap.CompressFormat format, int quality, File file) {
		boolean isSave = false;
		if (file != null && bitmap != null && !bitmap.isRecycled()) {
			FileOutputStream fos = null;
			try {
				if (!file.exists()) {
					String canonicalPath = file.getCanonicalPath();
					if (canonicalPath.lastIndexOf(File.separator) >= 0) {
						canonicalPath = canonicalPath.substring(0,
								canonicalPath.lastIndexOf(File.separator));
						File dir = new File(canonicalPath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
					}
					file.createNewFile();
				}
				if (file.exists()) {
					fos = new FileOutputStream(file);
					isSave = bitmap.compress(format, quality, fos);
					fos.flush();
					// bitmap.recycle();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSave;
	}

	/**
	 * 压缩并且向SD卡里写入一张图片
	 * 
	 * @param bitmap	要保存的bitmap
	 */

	public static String writeImages(Context context, Bitmap bitmap,
									 Bitmap.CompressFormat format, int quality) {
		String path = "";
		String name = String.valueOf(System.currentTimeMillis() + ".jpg");
		File file = new File(FileUtil.getSDCardRootPath(), name);

		if (bitmap != null && !bitmap.isRecycled()) {
			FileOutputStream fos = null;
			try {
				if (!file.exists()) {
					String canonicalPath = file.getCanonicalPath();
					if (canonicalPath.lastIndexOf(File.separator) >= 0) {
						canonicalPath = canonicalPath.substring(0,
								canonicalPath.lastIndexOf(File.separator));
						File dir = new File(canonicalPath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
					}
					file.createNewFile();
				}
				if (file.exists()) {
					fos = new FileOutputStream(file);
					bitmap.compress(format, quality, fos);
					fos.flush();
					if (!bitmap.isRecycled()) {
						Log.i("wh", "释放当前bitmap");
						bitmap.recycle();
						System.gc();
					}
					path = file.getAbsolutePath();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return path;
	}

	/**
	 * 用于压缩时旋转图片
	 * 
	 * @param srcFilePath		要操作的文件路径
	 * @param bitmap			旋转后的bitmap
	 */
	private static Bitmap rotateBitMap(String srcFilePath, Bitmap bitmap)
			throws IOException, OutOfMemoryError {
		ExifInterface exif = null;
		exif = new ExifInterface(srcFilePath);
		float degree = 0F;
		switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_UNDEFINED)) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90F;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180F;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270F;
				break;
			default:
				break;
		}
		Matrix matrix = new Matrix();
		matrix.setRotate(degree, bitmap.getWidth(), bitmap.getHeight());
		Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		if (bitmap != b2) {
			bitmap.recycle();
			bitmap = b2;
		}
		return bitmap;
	}

	/**
	 * Bitmap转换成byte数组
	 *
	 * @param bitmap	要转换的bitmap
	 * @param format	转换格式
	 */
	public static byte[] bitmapToBytes(Bitmap bitmap,
			Bitmap.CompressFormat format) {
		return bitmapToBytes(bitmap, format, 100);
	}

	/**
	 * Bitmap转换成byte数组
	 * 
	 * @param bitmap	要转换的bitmap
	 * @param format	转换格式
	 * @param quality	1-100，只对JPG格式有效，PNG格式忽略该参数
	 */
	public static byte[] bitmapToBytes(Bitmap bitmap,
                                       Bitmap.CompressFormat format, int quality) {
		byte[] bytes = null;
		if (bitmap != null) {
			ByteArrayOutputStream baos = null;
			try {
				baos = new ByteArrayOutputStream();
				bitmap.compress(format, quality, baos);
				baos.flush();
				bytes = baos.toByteArray();
				 bitmap.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (baos != null) {
						baos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}

	/**
	 * 这是一个把一个bitmap保存为一个file的方法
	 */
	public static String savePng(Bitmap bm, String path, String fileName) throws IOException {
		File file = new File(path, fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out;
		try{
			out = new FileOutputStream(file);
			if(bm.compress(Bitmap.CompressFormat.PNG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}

	/**
	 * 这是一个把一个bitmap保存为一个file的方法
	 *
	 * @param bitmap	要保存的bitmap
	 */
	public static String saveBitmapFile(Context context, Bitmap bitmap) {
		String sr = FileUtil.getSDCardRootPath()  + System.currentTimeMillis() + ".jpg";
		File file = new File(sr);// 将要保存图片的路径
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
		//	if(getBitmapsize(bitmap) > 1024 * 150){
				bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
//			}else{
//				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			}
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		bitmap.recycle();
//		System.gc();
		return sr;
	}

	public static long getBitmapSize(Bitmap bitmap){
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();

	}

	/**
	 * 压缩存储图片到200KB左右，只支持jpg和png格式图片
	 * 
	 * @param srcFile	要压缩的文件
	 */
	public static String compressImageTo200KB(Context context, File srcFile) {
		String path = null;
		if (srcFile != null && srcFile.exists()) {
			FileInputStream fis = null;

			try {
				if (SIZE_200K >= srcFile.length()
						&& (isJPG(srcFile.getAbsolutePath()) || isPNG(srcFile
								.getAbsolutePath()))) {
					Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG; // 默认压缩成png格式图片

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;

					fis = new FileInputStream(srcFile.getAbsolutePath());
					FileDescriptor fd = fis.getFD();
					BitmapFactory.decodeFileDescriptor(fd, null, options);
					options.inJustDecodeBounds = false;

					int reqHeight = 800;
					int reqWidth = 480;
					options.inSampleSize = calculateInSampleSize(options,
							reqWidth, reqHeight);
					Log.i("wh", "options.inSampleSize == "
							+ options.inSampleSize);

					int quality = 1; // 压缩比例，只在jpg格式有效，png格式忽略
					// if (isJPG(srcFile.getAbsolutePath())) {
					// compressFormat = Bitmap.CompressFormat.JPEG;
					// quality =
					// getImageOptions(BitmapFactory.decodeFileDescriptor(fd,
					// null, options)); // jpg格式压缩率，和上面的比例系数搭配使用
					// }
					Log.i("wh", "quality == " + quality);
					path = writeImages(context,
							rotateBitMap(srcFile.getAbsolutePath(),
									BitmapFactory.decodeFileDescriptor(fd,
											null, options)), compressFormat,
							quality);

				} else {

					Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG; // 默认压缩成png格式图片

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;

					fis = new FileInputStream(srcFile.getAbsolutePath());
					FileDescriptor fd = fis.getFD();
					BitmapFactory.decodeFileDescriptor(fd, null, options);
					options.inJustDecodeBounds = false;

					int reqHeight = 800;
					int reqWidth = 480;
					options.inSampleSize = calculateInSampleSize(options,
							reqWidth, reqHeight);

					int quality = 0; // 压缩比例，只在jpg格式有效，png格式忽略
					// if (isJPG(srcFile.getAbsolutePath())) {
					// compressFormat = Bitmap.CompressFormat.JPEG;
					// quality =
					// getImageOptions(BitmapFactory.decodeFileDescriptor(fd,
					// null, options)); // jpg格式压缩率，和上面的比例系数搭配使用
					// }
					Log.i("wh", "quality == " + quality);
					path = writeImages(context,
							rotateBitMap(srcFile.getAbsolutePath(),
									BitmapFactory.decodeFileDescriptor(fd,
											null, options)), compressFormat,
							quality);
				}

			} catch (IOException e) {
				Log.i("wh", "compressImageTo200KB方法 - IOException异常");
				return path;
				// e.printStackTrace();
			} catch (OutOfMemoryError e) {
				Log.i("wh", "compressImageTo200KB方法 - OutOfMemoryError异常");
				System.gc();
				return path;
				// e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return path;
	}

	/**
	 * 图片按质量压缩
	 */
	private static int getImageOptions(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int options = 100;
		while (baos.toByteArray().length / 1024 > 150) {
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
			options -= 5;
		}
		return options;
	}

	/**
	 * 计算按图片大小压缩比
	 * 
	 * @param options		配置
	 * @param reqWidth		期望宽度
	 * @param reqHeight		期望高度
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (width > height && width > reqWidth) {// 如果宽度大的话根据宽度固定大小缩放
			inSampleSize = (width / reqWidth);
		} else if (width < height && height > reqHeight) {// 如果高度高的话根据宽度固定大小缩放
			inSampleSize = (height / reqHeight);
		}
		if (inSampleSize < 1) {
			inSampleSize = 1;
		}

		return inSampleSize;
	}

	/**
	 * 压缩存储图片到200KB左右，只支持jpg和png格式图片
	 *
	 * @param context		上下文
	 * @param path 			要压缩的图片路径
	 */
	public static String compressImageTo200KB(Context context, String path) {
		File srcFile = new File(path);
		if (!PictureUtil.smallerThan200k(srcFile)) {
			if (srcFile.exists()) {
				FileInputStream fis = null;
				try {
					Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG; // 默认压缩成png格式图片
					int sizeRatio = 180; // 180 默认png格式图片使用这个比例系数，压缩后大约200KB
					int quality = 0; // 压缩比例，只在jpg格式有效，png格式忽略
					if (isJPG(srcFile.getAbsolutePath())) {
						Log.i("wh", "jpg的图片");
						compressFormat = Bitmap.CompressFormat.JPEG;
						sizeRatio = 300; // 384 jpg格式图片使用这个比例系数，压缩后大约200KB
						quality = 100; // 70jpg格式压缩率，和上面的比例系数搭配使用
					}

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;

					fis = new FileInputStream(srcFile.getAbsolutePath());
					FileDescriptor fd = fis.getFD();
					BitmapFactory.decodeFileDescriptor(fd, null, options);
					options.inJustDecodeBounds = false;
					int sizeOut = Math.max(options.outWidth, options.outHeight);
					int scale = (int) Math.rint(sizeOut / (double) sizeRatio);
					// int scale = (int) (srcFile.length() / (30* 1024));
					if (scale <= 1) {
						scale = 1;
					}
					options.inSampleSize = scale;
					path = writeImages(context,
							rotateBitMap(srcFile.getAbsolutePath(),
									BitmapFactory.decodeFileDescriptor(fd,
											null, options)), compressFormat,
							quality);
				} catch (IOException e) {
					Log.i("wh", "compressImageTo200KB方法 - IOException异常");
					return path;
					// e.printStackTrace();
				} catch (OutOfMemoryError e) {
					Log.i("wh", "compressImageTo200KB方法 - OutOfMemoryError异常");
					System.gc();
					return path;
					// e.printStackTrace();
				} finally {
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException e) {
						Log.i("wh",
								"compressImageTo200KB方法 finally - OutOfMemoryError异常");
					}
				}
			}
			return path;
		} else {
			return srcFile.getPath();
		}

	}

	public static boolean smallerThan200k(String path) {
		if (!TextUtils.isEmpty(path)) {
			return true;
		}
		return smallerThan200k(new File(path));
	}

	/**
	 * 判断文件大小是否小于200k
	 */
	public static boolean smallerThan200k(File f) {
		if (f.exists()) {
			long length = f.length();
			Log.i("wh", "文件大小==" + length / 1024);
			if (SIZE_200K > length) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

}
