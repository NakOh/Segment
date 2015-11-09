package com.myApplication.segment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class SegmentActivity extends AppCompatActivity {
    /** Called when the activity is first created. */
    BackThread thread = new BackThread();
    protected static final int THREAD_FLAGS_PRINT = 0; // Countdown
    protected static final int THREAD_FLAGS_CLOCK = 1; // Clock
    protected static final int DIALOG_SIMPLE_MESSAGE = 1;
    int flag = -1;
    boolean stop = false;
    int count = 0;
    int	LedData;
    int compare[] = {0x01, 0x02, 0x04, 0x08, 0x10 ,0x20, 0x40, 0x80};
    int i = 0;
    int add = -1;
    int preResult = 0;
    public native int SegmentControl(int value);
    public native int SegmentIOControl(int value);
    public native int LEDControl(int value);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        System.loadLibrary("7segment");

        LedData = 0;
        LEDControl(LedData);
        // Thread Start
        thread.setDaemon(true);
        thread.start();

        final EditText digit = (EditText) findViewById(R.id.digit);
        final Button Input = (Button) findViewById(R.id.start);
        final Button clock = (Button) findViewById(R.id.clock);
        final Button segOff = (Button) findViewById(R.id.segOff);

        Input.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (Integer.parseInt(digit.getText().toString()) > 1000000) {
                    showDialog(DIALOG_SIMPLE_MESSAGE);
                    return;
                }
                count = Integer.parseInt(digit.getText().toString());
                flag = THREAD_FLAGS_PRINT;
            }
        });
        clock.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                flag = THREAD_FLAGS_CLOCK;
            }
        });
        segOff.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                flag = -1;
            }
        });



    }

    class BackThread extends Thread {
        public void run() {
            while (!stop) {
                switch (flag) {
                    default:
                        // do nothing
                        LedData = 0;
                        LEDControl(LedData);
                        break;
                    case THREAD_FLAGS_PRINT:
                        // Countdown
                        SegmentIOControl(THREAD_FLAGS_PRINT);
                        while (count > 0 && flag == THREAD_FLAGS_PRINT) {
                            for (int i = 0; i < 14 && flag == THREAD_FLAGS_PRINT; i++) {
                                SegmentControl(count);
                            }
                            if(i==0){
                                LedData &= ~(compare[7]); //끄기
                                LedData |= compare[i]; //켜기
                            }else {
                                LedData &= ~(compare[i - 1]); //끄기
                                LedData |= compare[i]; //켜기
                            }
                            i++;
                            if(i == 8){
                                i = 0;
                            }
                            LEDControl(LedData);
                            count--;
                        }
                        // flag = 0;
                        break;
                    case THREAD_FLAGS_CLOCK:
                        // Clock
                        SegmentIOControl(THREAD_FLAGS_CLOCK);
                        int result = 0;
                        Time t = new Time();
                        t.set(System.currentTimeMillis());
                        result = t.hour * 10000 + t.minute * 100 + t.second;
                        // result += 1000000;
                        for (int i = 0; i < 20; i++)
                            SegmentControl(result);

                        if(preResult == 0){
                            preResult = result;
                        }

                        if(result - preResult == 1){

                            if(i == 8){
                                add = -add;
                            } else if( i == 0){
                                add = -add;
                            }

                            if(add > 0){
                                //오른쪽으로 진행할때
                                if((i-1)<0){
                                    //i가 0일때
                                    LedData |= compare[i]; //켜기
                                }else {
                                    LedData &= ~(compare[i - 1]); //끄기
                                    LedData |= compare[i]; //켜기
                                }
                            }
                            else {
                                //왼쪽으로 진행할때
                                if(i==8){
                                }else {
                                    LedData &= ~(compare[i]); //끄기
                                    LedData |= compare[i - 1]; //켜기
                                }
                            }

                            i = i + add;
                            LEDControl(LedData);
                            preResult = result;
                        }

                        break;

                }
            }
        }
    }
    // Program exit
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            flag = -1;
            stop = true;
            thread.interrupt();
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        Dialog d = new Dialog(SegmentActivity.this);
        Window window = d.getWindow();
        window.setFlags(WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW,
                WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW);

        switch (id) {
            case DIALOG_SIMPLE_MESSAGE:
                d.setTitle("Maximum input digit is 6");
                d.show();
                return d;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_segment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
