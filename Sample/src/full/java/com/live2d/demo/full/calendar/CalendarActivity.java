package com.live2d.demo.full.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.live2d.demo.full.adapter.EventAdapter;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.live2d.demo.R;
import com.live2d.demo.full.MainActivity;
import com.live2d.demo.full.calendar.SettingActivity;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends Activity {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private TextView tvSelectedDate;
    private TextView tvEvents;
    private ImageView btnAdd;
    private EventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();
    private GoogleAccountCredential mCredential;

    private final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
    private static final int REQUEST_ADD_EVENT = 2001;

    private Calendar currentSelectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // 하단 버튼 처리
        ImageButton buttonMain = findViewById(R.id.button_main);
        ImageButton buttonCalendar = findViewById(R.id.button_calendar);
        ImageButton buttonSetting = findViewById(R.id.button_setting);

        buttonMain.setOnClickListener(v -> {
            ProcessPhoenix.triggerRebirth(CalendarActivity.this);
        });

        buttonCalendar.setOnClickListener(v -> {
            // 현재 화면이므로 아무 동작 없음
        });

        buttonSetting.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
            finish();
        });

        // Calendar View 및 RecyclerView 초기화
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.eventRecyclerView);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        tvEvents = findViewById(R.id.tvEvents);
        btnAdd = findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddActivity.class);
            startActivityForResult(intent, REQUEST_ADD_EVENT);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(eventList, event -> {
            DateTime startDateTime = event.getStart().getDateTime() != null ?
                    event.getStart().getDateTime() : event.getStart().getDate();
            DateTime endDateTime = event.getEnd().getDateTime() != null ?
                    event.getEnd().getDateTime() : event.getEnd().getDate();

            Date startDate = new Date(startDateTime.getValue());
            Date endDate = new Date(endDateTime.getValue());

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Intent editIntent = new Intent(CalendarActivity.this, EditActivity.class);
            editIntent.putExtra("eventId", event.getId());
            editIntent.putExtra("title", event.getSummary() != null ? event.getSummary() : "");
            editIntent.putExtra("date", dateFormat.format(startDate));
            editIntent.putExtra("startTime", timeFormat.format(startDate));
            editIntent.putExtra("endTime", timeFormat.format(endDate));
            startActivity(editIntent);
        });
        recyclerView.setAdapter(adapter);

        // Google Calendar 인증
        mCredential = GoogleAccountCredential.usingOAuth2(
                this, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

        String accountName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("accountName", null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
        }

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            currentSelectedDate.set(date.getYear(), date.getMonth(), date.getDay());

            String[] weekDays = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
            int dayOfWeek = currentSelectedDate.get(Calendar.DAY_OF_WEEK);
            String dayOfWeekStr = weekDays[dayOfWeek - 1];

            tvSelectedDate.setText(
                    String.format("%d년 %d월 %d일 %s", date.getYear(), date.getMonth() + 1, date.getDay(), dayOfWeekStr)
            );
            fetchEvents(currentSelectedDate);
        });

        // 초기 날짜 설정
        calendarView.setSelectedDate(currentSelectedDate);
        fetchEvents(currentSelectedDate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_EVENT && resultCode == RESULT_OK) {
            fetchEvents(currentSelectedDate);
        }
    }

    private void fetchEvents(final Calendar date) {
        new Thread(() -> {
            try {
                com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        mCredential
                ).setApplicationName("My Calendar App").build();

                TimeZone seoulTimeZone = TimeZone.getTimeZone("Asia/Seoul");

                Calendar startCalendar = Calendar.getInstance(seoulTimeZone);
                startCalendar.setTime(date.getTime());
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                startCalendar.set(Calendar.MINUTE, 0);
                startCalendar.set(Calendar.SECOND, 0);
                startCalendar.set(Calendar.MILLISECOND, 0);

                Calendar endCalendar = (Calendar) startCalendar.clone();
                endCalendar.add(Calendar.DAY_OF_MONTH, 1);

                DateTime startDateTime = new DateTime(startCalendar.getTime(), seoulTimeZone);
                DateTime endDateTime = new DateTime(endCalendar.getTime(), seoulTimeZone);

                List<Event> events = service.events().list("primary")
                        .setTimeMin(startDateTime)
                        .setTimeMax(endDateTime)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                        .getItems();

                runOnUiThread(() -> {
                    eventList.clear();
                    eventList.addAll(events);
                    adapter.notifyDataSetChanged();
                    tvEvents.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (GooglePlayServicesAvailabilityIOException e) {
                Log.e("CalendarAPI", "Google Play Services 오류", e);
            } catch (UserRecoverableAuthIOException e) {
                Log.e("CalendarAPI", "인증 오류 발생", e);
                startActivityForResult(e.getIntent(), 1000);
            } catch (Exception e) {
                Log.e("CalendarAPI", "이벤트 가져오기 실패", e);
            }
        }).start();
    }
}
