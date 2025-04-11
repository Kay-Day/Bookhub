package com.example.bookhub.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * Đọc nội dung từ file URI
     * @param context Context để mở file
     * @param uri URI của file
     * @return Nội dung file dưới dạng chuỗi
     */
    public static String readTextFromUri(Context context, Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Cannot open input stream for URI: " + uri);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }

            reader.close();
            inputStream.close();

            Log.d(TAG, "File read successfully. Content length: " + stringBuilder.length());
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + uri, e);
            throw e;
        }

        return stringBuilder.toString();
    }

    /**
     * Lấy tên file từ URI
     * @param context Context để truy vấn
     * @param uri URI của file
     * @return Tên file
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                        Log.d(TAG, "File name from content resolver: " + result);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name from cursor", e);
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
            Log.d(TAG, "File name from path: " + result);
        }

        return result;
    }
}