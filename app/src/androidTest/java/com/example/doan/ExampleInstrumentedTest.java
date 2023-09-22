//package com.example.doan;
//
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.platform.app.InstrumentationRegistry;
//import org.robolectric.Robolectric;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.Shadows;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//
//@RunWith(AndroidJUnit4.class)
//public class ExampleInstrumentedTest {
//    private MainActivity activity;
//    private EditText t1, t2, t3, t4;
//    private TextView v1;
//    private Button b1, b2;
//    private List<String> danhSach;
//
//    @Before
//    public void setUp() throws Exception {
//        activity = Robolectric.buildActivity(MainActivity.class).create().resume().get();
//        t1 = activity.findViewById(R.id.hoten);
//        t2 = activity.findViewById(R.id.toan);
//        t3 = activity.findViewById(R.id.van);
//        t4 = activity.findViewById(R.id.nn);
//        v1 = activity.findViewById(R.id.sum);
//        b1 = activity.findViewById(R.id.list);
//        b2 = activity.findViewById(R.id.nhaplai);
//        danhSach = new ArrayList<>();
//    }
//
//    @Test
//    public void Nutthemkhikhongnhapten() {
//        t1.setText("");
//        t2.setText("9");
//        t3.setText("7");
//        t4.setText("9");
//
//        // Kích hoạt sự kiện click trên nút b1
//        b1.performClick();
//
//        // Tạo một đối tượng Toast với thông báo "Vui lòng nhập lại họ tên"
//        Toast toast = Toast.makeText(activity.getApplicationContext(), "Vui lòng nhập lại họ tên", Toast.LENGTH_SHORT);
//        toast.show();
//
//        // Lấy nội dung của Toast mới nhất
//        String latestToast = Shadows.shadowOf(toast).getTextOfLatestToast();
//
//        // Kiểm tra xem nội dung của Toast có phù hợp với mong đợi hay không
//        assertEquals("Vui lòng nhập lại họ tên", latestToast);
//    }
//}