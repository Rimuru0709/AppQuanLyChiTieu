package com.quanlychitieu.doan.chatbot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiChatActivity extends AppCompatActivity {

    /*
     * 10.0.2.2 là địa chỉ để máy ảo Android
     * truy cập localhost của máy tính.
     */
    private static final String CHAT_API_URL =
            "http://10.0.2.2:3000/api/chat";

    private static final String TAG =
            "AiChatActivity";

    private RecyclerView recyclerChat;

    private EditText edtChatMessage;

    private TextView btnSendMessage;

    /*
     * Khu vực chứa các câu hỏi gợi ý.
     */
    private LinearLayout layoutSuggestions;

    private TextView suggestion1;
    private TextView suggestion2;
    private TextView suggestion3;
    private TextView suggestion4;

    private final List<ChatMessage> messageList =
            new ArrayList<>();

    private ChatAdapter chatAdapter;

    private final Handler handler =
            new Handler(
                    Looper.getMainLooper()
            );

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private boolean waitingForAi = false;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_ai_chat
        );

        setupSafeArea();

        bindViews();

        setupRecyclerView();

        setupEvents();

        showWelcomeMessage();

        setupSuggestions();
    }


    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        ImageView imgChatBack =
                findViewById(
                        R.id.imgChatBack
                );

        recyclerChat =
                findViewById(
                        R.id.recyclerChat
                );

        edtChatMessage =
                findViewById(
                        R.id.edtChatMessage
                );

        btnSendMessage =
                findViewById(
                        R.id.btnSendMessage
                );

        /*
         * Khu vực gợi ý.
         */
        layoutSuggestions =
                findViewById(
                        R.id.layoutSuggestions
                );

        suggestion1 =
                findViewById(
                        R.id.suggestion1
                );

        suggestion2 =
                findViewById(
                        R.id.suggestion2
                );

        suggestion3 =
                findViewById(
                        R.id.suggestion3
                );

        suggestion4 =
                findViewById(
                        R.id.suggestion4
                );

        /*
         * Nút quay lại.
         */
        imgChatBack.setOnClickListener(
                view -> finish()
        );
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        chatAdapter =
                new ChatAdapter(
                        messageList
                );

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(
                        this
                );

        recyclerChat.setLayoutManager(
                layoutManager
        );

        recyclerChat.setAdapter(
                chatAdapter
        );
    }


    // =========================================================
    // EVENTS
    // =========================================================

    private void setupEvents() {

        /*
         * Bấm nút gửi.
         */
        btnSendMessage.setOnClickListener(
                view -> sendMessage()
        );

        /*
         * Bấm Enter / Send trên bàn phím.
         */
        edtChatMessage.setOnEditorActionListener(
                (textView, actionId, event) -> {

                    if (actionId
                            == EditorInfo.IME_ACTION_SEND) {

                        sendMessage();

                        return true;
                    }

                    return false;
                }
        );
    }


    // =========================================================
    // CÂU HỎI GỢI Ý
    // =========================================================

    private void setupSuggestions() {

        /*
         * Gợi ý 1
         */
        suggestion1.setOnClickListener(
                view -> sendSuggestion(
                        suggestion1
                                .getText()
                                .toString()
                )
        );

        /*
         * Gợi ý 2
         */
        suggestion2.setOnClickListener(
                view -> sendSuggestion(
                        suggestion2
                                .getText()
                                .toString()
                )
        );

        /*
         * Gợi ý 3
         */
        suggestion3.setOnClickListener(
                view -> sendSuggestion(
                        suggestion3
                                .getText()
                                .toString()
                )
        );

        /*
         * Gợi ý 4
         */
        suggestion4.setOnClickListener(
                view -> sendSuggestion(
                        suggestion4
                                .getText()
                                .toString()
                )
        );
    }


    // =========================================================
    // GỬI CÂU HỎI GỢI Ý
    // =========================================================

    private void sendSuggestion(
            String suggestion
    ) {

        if (suggestion == null
                || suggestion.trim().isEmpty()) {

            return;
        }

        /*
         * Cho câu hỏi vào ô nhập.
         */
        edtChatMessage.setText(
                suggestion
        );

        /*
         * Gửi luôn.
         */
        sendMessage();
    }


    // =========================================================
    // WELCOME MESSAGE
    // =========================================================

    private void showWelcomeMessage() {

        addMessage(
                new ChatMessage(
                        "Xin chào! Mình là trợ lý tài chính của bạn. "
                                + "Bạn có thể hỏi mình về thu nhập, "
                                + "chi tiêu hoặc kế hoạch tiết kiệm.",
                        ChatMessage.TYPE_AI
                )
        );
    }


    // =========================================================
    // SEND MESSAGE
    // =========================================================

    private void sendMessage() {

        String message =
                edtChatMessage
                        .getText()
                        .toString()
                        .trim();

        if (message.isEmpty()
                || waitingForAi) {

            return;
        }

        /*
         * Ẩn gợi ý sau khi người dùng
         * bắt đầu gửi câu hỏi.
         */
        hideSuggestions();



        /*
         * Hiển thị tin nhắn của người dùng.
         */
        addMessage(
                new ChatMessage(
                        message,
                        ChatMessage.TYPE_USER
                )
        );

        /*
         * Xóa ô nhập.
         */
        edtChatMessage.setText("");

        /*
         * Khóa nút gửi trong lúc chờ server.
         */
        setWaitingForAi(true);

        /*
         * Gửi lên Node.js server.
         */
        sendMessageToServer(
                message
        );
    }


    // =========================================================
    // ẨN GỢI Ý
    // =========================================================

    private void hideSuggestions() {

        if (layoutSuggestions != null) {

            layoutSuggestions.setVisibility(
                    LinearLayout.GONE
            );
        }
    }

    private void showSuggestions() {

        if (layoutSuggestions != null) {

            layoutSuggestions.setVisibility(
                    LinearLayout.VISIBLE
            );
        }
    }

    // =========================================================
    // GỬI ĐẾN SERVER
    // =========================================================

    private void sendMessageToServer(
            String message
    ) {

        executorService.execute(() -> {

            HttpURLConnection connection =
                    null;

            try {

                URL url =
                        new URL(
                                CHAT_API_URL
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

                connection.setConnectTimeout(
                        10000
                );

                connection.setReadTimeout(
                        15000
                );

                connection.setDoOutput(
                        true
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                /*
                 * JSON gửi lên server.
                 */
                JSONObject requestBody =
                        new JSONObject();

                requestBody.put(
                        "message",
                        message
                );

                try (
                        OutputStream outputStream =
                                connection
                                        .getOutputStream()
                ) {

                    byte[] requestData =
                            requestBody
                                    .toString()
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    );

                    outputStream.write(
                            requestData
                    );

                    outputStream.flush();
                }

                /*
                 * Nhận mã phản hồi.
                 */
                int responseCode =
                        connection
                                .getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200
                        && responseCode < 300) {

                    inputStream =
                            connection
                                    .getInputStream();

                } else {

                    inputStream =
                            connection
                                    .getErrorStream();
                }

                /*
                 * Đọc response.
                 */
                String responseText =
                        readInputStream(
                                inputStream
                        );

                if (responseText.isEmpty()) {

                    throw new Exception(
                            "Máy chủ không trả về dữ liệu"
                    );
                }

                /*
                 * Parse JSON.
                 */
                JSONObject responseJson =
                        new JSONObject(
                                responseText
                        );

                boolean success =
                        responseJson.optBoolean(
                                "success",
                                false
                        );

                if (success) {

                    String reply =
                            responseJson.optString(
                                    "reply",
                                    "Mình chưa thể trả lời câu hỏi này."
                            );

                    showServerReply(
                            reply
                    );

                } else {

                    String error =
                            responseJson.optString(
                                    "error",
                                    "Máy chủ xảy ra lỗi."
                            );

                    showServerReply(
                            "Có lỗi: " + error
                    );
                }

            } catch (Exception exception) {

                Log.e(
                        TAG,
                        "Không thể kết nối máy chủ",
                        exception
                );

                showServerReply(
                        "Không thể kết nối với máy chủ. "
                                + "Hãy kiểm tra terminal "
                                + "node server.mjs đang chạy."
                );

            } finally {

                if (connection != null) {

                    connection.disconnect();
                }
            }
        });
    }


    // =========================================================
    // READ INPUT STREAM
    // =========================================================

    private String readInputStream(
            InputStream inputStream
    ) throws Exception {

        if (inputStream == null) {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                result.append(line);
            }
        }

        return result.toString();
    }


    // =========================================================
    // SHOW SERVER REPLY
    // =========================================================

    private void showServerReply(String reply) {

        handler.post(() -> {

            if (isFinishing()
                    || isDestroyed()) {

                return;
            }

            // Hiển thị câu trả lời của AI
            addMessage(
                    new ChatMessage(
                            reply,
                            ChatMessage.TYPE_AI
                    )
            );

            // Cho phép người dùng tiếp tục gửi câu hỏi
            setWaitingForAi(false);

            // Hiện lại các câu hỏi gợi ý
            showSuggestions();
        });
    }


    // =========================================================
    // ADD MESSAGE
    // =========================================================

    private void addMessage(
            ChatMessage chatMessage
    ) {

        chatAdapter.addMessage(
                chatMessage
        );

        scrollToBottom();
    }


    // =========================================================
    // SCROLL
    // =========================================================

    private void scrollToBottom() {

        recyclerChat.post(() -> {

            if (!messageList.isEmpty()) {

                recyclerChat.smoothScrollToPosition(
                        messageList.size() - 1
                );
            }
        });
    }


    // =========================================================
    // WAITING
    // =========================================================

    private void setWaitingForAi(
            boolean waiting
    ) {

        waitingForAi =
                waiting;

        btnSendMessage.setEnabled(
                !waiting
        );

        btnSendMessage.setAlpha(
                waiting
                        ? 0.5f
                        : 1.0f
        );
    }


    // =========================================================
    // SAFE AREA
    // =========================================================

    private void setupSafeArea() {

        ViewCompat
                .setOnApplyWindowInsetsListener(
                        findViewById(
                                R.id.chatRootLayout
                        ),
                        (view, insets) -> {

                            Insets systemBars =
                                    insets.getInsets(
                                            WindowInsetsCompat
                                                    .Type
                                                    .systemBars()
                                    );

                            view.setPadding(
                                    0,
                                    systemBars.top,
                                    0,
                                    systemBars.bottom
                            );

                            return insets;
                        }
                );
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        executorService.shutdownNow();

        super.onDestroy();
    }
}