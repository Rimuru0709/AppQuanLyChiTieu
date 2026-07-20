package com.quanlychitieu.doan.setting;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountStorage {

    private static final String PREF_ACCOUNT =
            "AccountPreferences";

    private static final String KEY_FULL_NAME =
            "full_name";

    private static final String KEY_EMAIL =
            "email";

    private static final String KEY_PHONE =
            "phone";

    private static final String KEY_BIRTHDAY =
            "birthday";

    private static final String KEY_AVATAR_URI =
            "avatar_uri";

    private AccountStorage() {
        // Không cho phép tạo đối tượng AccountStorage
    }

    // =========================================================
    // LẤY UID NGƯỜI DÙNG ĐANG ĐĂNG NHẬP
    // =========================================================

    public static String getCurrentUserId() {
        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {
            return "";
        }

        return currentUser.getUid();
    }

    // =========================================================
    // LƯU TOÀN BỘ THÔNG TIN TÀI KHOẢN
    // =========================================================

    public static void saveProfile(
            Context context,
            String userId,
            String fullName,
            String email,
            String phone,
            String birthday
    ) {
        if (!isValidUser(context, userId)) {
            return;
        }

        SharedPreferences.Editor editor =
                getPreferences(context)
                        .edit();

        editor.putString(
                createKey(
                        userId,
                        KEY_FULL_NAME
                ),
                safeValue(fullName)
        );

        editor.putString(
                createKey(
                        userId,
                        KEY_EMAIL
                ),
                safeValue(email)
        );

        editor.putString(
                createKey(
                        userId,
                        KEY_PHONE
                ),
                safeValue(phone)
        );

        editor.putString(
                createKey(
                        userId,
                        KEY_BIRTHDAY
                ),
                safeValue(birthday)
        );

        editor.apply();
    }

    // =========================================================
    // LƯU HỌ VÀ TÊN
    // =========================================================

    public static void saveFullName(
            Context context,
            String userId,
            String fullName
    ) {
        saveString(
                context,
                userId,
                KEY_FULL_NAME,
                fullName
        );
    }

    // =========================================================
    // LƯU EMAIL
    // =========================================================

    public static void saveEmail(
            Context context,
            String userId,
            String email
    ) {
        saveString(
                context,
                userId,
                KEY_EMAIL,
                email
        );
    }

    // =========================================================
    // LƯU SỐ ĐIỆN THOẠI
    // =========================================================

    public static void savePhone(
            Context context,
            String userId,
            String phone
    ) {
        saveString(
                context,
                userId,
                KEY_PHONE,
                phone
        );
    }

    // =========================================================
    // LƯU NGÀY SINH
    // =========================================================

    public static void saveBirthday(
            Context context,
            String userId,
            String birthday
    ) {
        saveString(
                context,
                userId,
                KEY_BIRTHDAY,
                birthday
        );
    }

    // =========================================================
    // LƯU ĐƯỜNG DẪN ẢNH ĐẠI DIỆN
    // =========================================================

    public static void saveAvatarUri(
            Context context,
            String userId,
            String avatarUri
    ) {
        saveString(
                context,
                userId,
                KEY_AVATAR_URI,
                avatarUri
        );
    }

    // =========================================================
    // LẤY HỌ VÀ TÊN
    // =========================================================

    public static String getFullName(
            Context context,
            String userId
    ) {
        return getString(
                context,
                userId,
                KEY_FULL_NAME,
                ""
        );
    }

    // =========================================================
    // LẤY EMAIL
    // =========================================================

    public static String getEmail(
            Context context,
            String userId
    ) {
        return getString(
                context,
                userId,
                KEY_EMAIL,
                ""
        );
    }

    // =========================================================
    // LẤY SỐ ĐIỆN THOẠI
    // =========================================================

    public static String getPhone(
            Context context,
            String userId
    ) {
        return getString(
                context,
                userId,
                KEY_PHONE,
                ""
        );
    }

    // =========================================================
    // LẤY NGÀY SINH
    // =========================================================

    public static String getBirthday(
            Context context,
            String userId
    ) {
        return getString(
                context,
                userId,
                KEY_BIRTHDAY,
                ""
        );
    }

    // =========================================================
    // LẤY ĐƯỜNG DẪN ẢNH ĐẠI DIỆN
    // =========================================================

    public static String getAvatarUri(
            Context context,
            String userId
    ) {
        return getString(
                context,
                userId,
                KEY_AVATAR_URI,
                ""
        );
    }

    // =========================================================
    // KIỂM TRA HỒ SƠ ĐÃ TỒN TẠI CHƯA
    // =========================================================

    public static boolean hasProfile(
            Context context,
            String userId
    ) {
        if (!isValidUser(context, userId)) {
            return false;
        }

        SharedPreferences preferences =
                getPreferences(context);

        return preferences.contains(
                createKey(
                        userId,
                        KEY_FULL_NAME
                )
        );
    }

    // =========================================================
    // TẠO HỒ SƠ TỪ FIREBASE NẾU CHƯA CÓ
    // =========================================================

    public static void createProfileFromFirebaseIfNeeded(
            Context context
    ) {
        if (context == null) {
            return;
        }

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String userId =
                currentUser.getUid();

        String firebaseEmail =
                currentUser.getEmail() == null
                        ? ""
                        : currentUser
                          .getEmail()
                          .trim();

        String firebaseName =
                currentUser.getDisplayName() == null
                        ? ""
                        : currentUser
                          .getDisplayName()
                          .trim();

        if (!hasProfile(
                context,
                userId
        )) {
            saveProfile(
                    context,
                    userId,
                    firebaseName,
                    firebaseEmail,
                    "",
                    ""
            );

            return;
        }

        String savedEmail =
                getEmail(
                        context,
                        userId
                );

        if (savedEmail.isEmpty() &&
                !firebaseEmail.isEmpty()) {

            saveEmail(
                    context,
                    userId,
                    firebaseEmail
            );
        }

        String savedFullName =
                getFullName(
                        context,
                        userId
                );

        if (savedFullName.isEmpty() &&
                !firebaseName.isEmpty()) {

            saveFullName(
                    context,
                    userId,
                    firebaseName
            );
        }
    }

    // =========================================================
    // CẬP NHẬT EMAIL TỪ FIREBASE
    // =========================================================

    public static void updateEmailFromFirebase(
            Context context
    ) {
        if (context == null) {
            return;
        }

        FirebaseUser currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String email =
                currentUser.getEmail() == null
                        ? ""
                        : currentUser
                          .getEmail()
                          .trim();

        saveEmail(
                context,
                currentUser.getUid(),
                email
        );
    }

    // =========================================================
    // XÓA HỒ SƠ CỦA MỘT NGƯỜI DÙNG
    // =========================================================

    public static void clearProfile(
            Context context,
            String userId
    ) {
        if (!isValidUser(context, userId)) {
            return;
        }

        SharedPreferences.Editor editor =
                getPreferences(context)
                        .edit();

        editor.remove(
                createKey(
                        userId,
                        KEY_FULL_NAME
                )
        );

        editor.remove(
                createKey(
                        userId,
                        KEY_EMAIL
                )
        );

        editor.remove(
                createKey(
                        userId,
                        KEY_PHONE
                )
        );

        editor.remove(
                createKey(
                        userId,
                        KEY_BIRTHDAY
                )
        );

        editor.remove(
                createKey(
                        userId,
                        KEY_AVATAR_URI
                )
        );

        editor.apply();
    }

    // =========================================================
    // XÓA HỒ SƠ NGƯỜI ĐANG ĐĂNG NHẬP
    // =========================================================

    public static void clearCurrentUserProfile(
            Context context
    ) {
        String userId =
                getCurrentUserId();

        if (userId.isEmpty()) {
            return;
        }

        clearProfile(
                context,
                userId
        );
    }

    // =========================================================
    // HÀM LƯU CHUỖI
    // =========================================================

    private static void saveString(
            Context context,
            String userId,
            String field,
            String value
    ) {
        if (!isValidUser(context, userId)) {
            return;
        }

        getPreferences(context)
                .edit()
                .putString(
                        createKey(
                                userId,
                                field
                        ),
                        safeValue(value)
                )
                .apply();
    }

    // =========================================================
    // HÀM ĐỌC CHUỖI
    // =========================================================

    private static String getString(
            Context context,
            String userId,
            String field,
            String defaultValue
    ) {
        if (!isValidUser(context, userId)) {
            return defaultValue;
        }

        String value =
                getPreferences(context)
                        .getString(
                                createKey(
                                        userId,
                                        field
                                ),
                                defaultValue
                        );

        if (value == null) {
            return defaultValue;
        }

        return value.trim();
    }

    // =========================================================
    // LẤY SHAREDPREFERENCES
    // =========================================================

    private static SharedPreferences getPreferences(
            Context context
    ) {
        return context.getSharedPreferences(
                PREF_ACCOUNT,
                Context.MODE_PRIVATE
        );
    }

    // =========================================================
    // TẠO KEY RIÊNG THEO UID
    // =========================================================

    private static String createKey(
            String userId,
            String field
    ) {
        return userId
                + "_"
                + field;
    }

    // =========================================================
    // KIỂM TRA CONTEXT VÀ UID
    // =========================================================

    private static boolean isValidUser(
            Context context,
            String userId
    ) {
        return context != null
                && userId != null
                && !userId.trim().isEmpty();
    }

    // =========================================================
    // CHỐNG GIÁ TRỊ NULL
    // =========================================================

    private static String safeValue(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}