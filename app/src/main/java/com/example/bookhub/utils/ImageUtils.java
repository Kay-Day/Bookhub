package com.example.bookhub.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int COMPRESSION_QUALITY = 80;

    public static byte[] convertImageToByteArray(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                return null;
            }

            // Đọc metadata để xác định hướng (orientation)
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(context.getContentResolver().openInputStream(imageUri));
            } catch (IOException e) {
                Log.e(TAG, "Error reading EXIF data", e);
            }

            // Đọc bitmap từ Uri
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Xoay hình ảnh nếu cần thiết
            if (exif != null) {
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                    Matrix matrix = new Matrix();

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                    originalBitmap = Bitmap.createBitmap(
                            originalBitmap, 0, 0,
                            originalBitmap.getWidth(), originalBitmap.getHeight(),
                            matrix, true);
                }
            }

            // Resize hình ảnh nếu nó quá lớn
            Bitmap resizedBitmap = resizeBitmap(originalBitmap);

            // Chuyển Bitmap thành byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);

            // Giải phóng bộ nhớ
            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            resizedBitmap.recycle();

            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error converting image to byte array", e);
            return null;
        }
    }

    public static Bitmap convertByteArrayToBitmap(byte[] imageBytes) {
        if (imageBytes == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    private static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap; // Không cần resize
        }

        float scaleFactor;
        if (width > height) {
            scaleFactor = (float) MAX_IMAGE_DIMENSION / width;
        } else {
            scaleFactor = (float) MAX_IMAGE_DIMENSION / height;
        }

        int newWidth = Math.round(width * scaleFactor);
        int newHeight = Math.round(height * scaleFactor);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}