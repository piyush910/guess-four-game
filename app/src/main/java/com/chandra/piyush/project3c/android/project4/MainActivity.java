package com.chandra.piyush.project3c.android.project4;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    Handler player2Handler;
    Handler player1Handler;
    public static boolean isHandler1Initiated = false;
    private TextView p1ActualNum;
    private TextView p1GuessedNum;
    private TextView p2ActualNum;
    private TextView p2GuessedNum;
    private TextView chanceCountText;
    private TextView winnerText;
    public static final int PLAYER1_ACTUAL = 1;
    public static final int PLAYER2_ACTUAL = 2;
    public static final int RCV_FDBK_MODIFY_NUM_AND_SEND_P1 = 3;
    public static final int RCV_FDBK_MODIFY_NUM_AND_SEND_P2 = 4;
    public static final int PLAYER1_GUESS = 5;
    public static final int PLAYER2_GUESS = 6;
    public static boolean firstTimeMsgRcv = true;
    public Handler mainHandler;
    private Thread t1 = null;
    private Thread t2 = null;
    private static boolean isNewStart = true;
    public static String p1GuessNum = "XXXX";
    public static String p2GuessNum = "XXXX";
    public static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = findViewById(R.id.startButton);
        p1ActualNum = findViewById(R.id.originalNumber1);
        p1GuessedNum = findViewById(R.id.guessedNumber1);
        p2ActualNum = findViewById(R.id.originalNumber2);
        p2GuessedNum = findViewById(R.id.guessedNumber2);
        chanceCountText = findViewById(R.id.chanceCount);
        startButton.setOnClickListener(onClickListener1);
        winnerText = findViewById(R.id.winnerDeclareText);
        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PLAYER1_ACTUAL:
                        p1ActualNum.setText(String.valueOf(msg.arg1));
                        break;
                    case PLAYER2_ACTUAL:
                        p2ActualNum.setText(String.valueOf(msg.arg1));
                        break;
                    case PLAYER1_GUESS:
                        p1GuessedNum.setText(String.valueOf(msg.obj));
                        break;
                    case PLAYER2_GUESS:
                        p2GuessedNum.setText(String.valueOf(msg.obj));
                        break;
                }
            }
        };
    }


    private View.OnClickListener onClickListener1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isNewStart) {
                mainHandler.removeCallbacksAndMessages(null);
                chanceCountText.setText("" + 0);
                player1Handler.getLooper().quit();
                player2Handler.getLooper().quit();
                p1ActualNum.setText("");
                p2ActualNum.setText("");
                p1GuessedNum.setText("");
                p2GuessedNum.setText("");
                isHandler1Initiated = false;
                firstTimeMsgRcv = true;
                t1.interrupt();
                t2.interrupt();
                t1 = null;
                t2 = null;
                p1GuessNum = "XXXX";
                p2GuessNum = "XXXX";
                winnerText.setText("");
            }
            count = 0;
            t1 = new Thread(new Player1Runnable());
            t1.start();
            t2 = new Thread(new Player2Runnable());
            t2.start();
            startButton.setText("Restart Game");
            isNewStart = false;
        }
    };

    /**
     * This method is used to generate numbers randomly for each X, it will randomly determine digits.
     *
     * @param numberStr
     * @return
     */
    public synchronized String numberGeneration(String numberStr) {
        StringBuilder replaceNum = new StringBuilder(numberStr);
        for (int i = 0; i < numberStr.length(); i++) {
            if (numberStr.charAt(i) == 'X') {
                boolean flag = true;
                int temp = 0;
                while (flag) {
                    if (i == 0) {
                        temp = new Random().nextInt(9) + 1;
                    } else {
                        temp = new Random().nextInt(10);
                    }
                    if (replaceNum.toString().contains(String.valueOf(temp))) {
                        continue;
                    }
                    flag = false;
                }
                replaceNum.setCharAt(i, (char) (temp + '0'));
            }
        }
        return replaceNum.toString();
    }

    /**
     * This method is like numberGeneration method, but it also make sures that the number is not repeated.
     *
     * @param numberStr
     * @param prevNumber
     * @return
     */
    public synchronized String numberGenerationAnotherStrategy(String numberStr, String prevNumber) {
        StringBuilder replaceNum = new StringBuilder(numberStr);
        for (int i = 0; i < numberStr.length(); i++) {
            if (numberStr.charAt(i) == 'X') {
                boolean flag = true;
                int temp = 0;
                while (flag) {
                    if (i == 0) {
                        temp = new Random().nextInt(9) + 1;
                        if (prevNumber.charAt(i) == temp) {
                            temp = temp + 1;
                            if (temp == 10) {
                                temp = 0;
                            }
                        }
                    } else {
                        temp = new Random().nextInt(10);
                        if (prevNumber.charAt(i) == temp) {
                            temp = temp + 1;
                            if (temp == 10) {
                                temp = 0;
                            }
                        }
                    }
                    if (replaceNum.toString().contains(String.valueOf(temp))) {
                        continue;
                    }
                    flag = false;
                }
                replaceNum.setCharAt(i, (char) (temp + '0'));
            }
        }
        return replaceNum.toString();
    }

    /**
     * Player 1 thread class.
     */
    public class Player1Runnable implements Runnable {

        public void run() {
            Message msg = mainHandler.obtainMessage(MainActivity.PLAYER1_ACTUAL);
            final String p1Number = numberGeneration("XXXX");
            msg.arg1 = Integer.valueOf(p1Number);
            mainHandler.sendMessage(msg);
            msg = mainHandler.obtainMessage(MainActivity.PLAYER1_GUESS);
            p1GuessNum = numberGeneration("XXXX");
            msg.obj = p1GuessNum;
            mainHandler.sendMessage(msg);
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted!");
            }
            msg = player2Handler.obtainMessage(RCV_FDBK_MODIFY_NUM_AND_SEND_P2);
            ArrayList<String> firstListToSendToP2 = new ArrayList<>();
            firstListToSendToP2.add(p1GuessNum);
            msg.obj = firstListToSendToP2;
            player2Handler.sendMessage(msg);
            Looper.prepare();
            player1Handler = new Handler() {
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    switch (what) {
                        case RCV_FDBK_MODIFY_NUM_AND_SEND_P1: // this is used to check the digits guessed by player 2 and tell it what all it guessed right
                            if (count > 20) { // if count is greater than 20 stop everything.
                                player1Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player1Handler.getLooper().quit();
                                    }
                                });
                                player2Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player2Handler.getLooper().quit();
                                    }
                                });
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "The game is drawn", Toast.LENGTH_LONG).show();
                                        winnerText.setText("Game Is Draw");
                                    }
                                });
                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                            }
                            List<String> getData = (ArrayList<String>) msg.obj;
                            String newNum = "";
                            String currGuessOf1 = getData.get(0);
                            String currGuessOf2 = getData.get(1);
                            String feedBackOfP1 = getData.get(2);
                            // New number formation based on feedback from P2
                            for (int i = 0; i < 4; i++) {
                                if (feedBackOfP1.charAt(i) == '1') {
                                    newNum = newNum + currGuessOf1.charAt(i);
                                } else {
                                    newNum = newNum + 'X';
                                }
                            }
                            if (newNum.contains("X")) { // if number guessed by player 1 contains X, that means it still has not predicted number of player1.
                                newNum = numberGenerationAnotherStrategy(newNum, currGuessOf1);
                                final String tempNewNum = newNum;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Player 1 Guess of P2: " + tempNewNum, Toast.LENGTH_LONG).show();
                                        p1GuessedNum.setText(tempNewNum);
                                    }
                                });
                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                            } else { // if number guessed by player 1 does not contain X, that means it predicted correctly and is the winner.
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Player 1 Wins", Toast.LENGTH_LONG).show();
                                        winnerText.setText("Player 1");
                                    }
                                });

                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                                t1.interrupt();
                                t2.interrupt();
                                player1Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player1Handler.getLooper().quit();
                                    }
                                });
                                player2Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player2Handler.getLooper().quit();
                                    }
                                });
                            }
                            String feedBackForP2 = "";
                            // create feedback for P2
                            for (int i = 0; i < currGuessOf2.length(); i++) {
                                if (currGuessOf2.charAt(i) == p1Number.charAt(i)) {
                                    feedBackForP2 = feedBackForP2 + '1';
                                } else {
                                    feedBackForP2 = feedBackForP2 + '0';
                                }
                            }
                            // add to the feedback the number guessed at wrong position but present in the original string with its position
                            for (int i = 0; i < currGuessOf2.length(); i++) {
                                if (feedBackForP2.charAt(i) == 0 && p1Number.contains(String.valueOf(currGuessOf2.charAt(i)))) {
                                    feedBackForP2 = feedBackForP2 + currGuessOf2.charAt(i) + i;
                                }
                            }
                            List<String> sendObjToP1List = new ArrayList<>();
                            sendObjToP1List.add(newNum);
                            sendObjToP1List.add(currGuessOf2);
                            sendObjToP1List.add(feedBackForP2);
                            msg = player2Handler.obtainMessage(RCV_FDBK_MODIFY_NUM_AND_SEND_P2);
                            msg.obj = sendObjToP1List;
                            player2Handler.sendMessage(msg);
                            break;
                    }
                }
            };
            isHandler1Initiated = true;
            Looper.loop();
        }
    }

    /**
     * Player 2 Thread Class.
     */
    public class Player2Runnable implements Runnable {

        public void run() {
            Message msg = mainHandler.obtainMessage(MainActivity.PLAYER2_ACTUAL);
            final String p2Number = numberGeneration("XXXX");
            msg.arg1 = Integer.valueOf(p2Number);
            mainHandler.sendMessage(msg);
            msg = mainHandler.obtainMessage(MainActivity.PLAYER2_GUESS);
            p2GuessNum = numberGeneration("XXXX");
            msg.obj = p2GuessNum;
            mainHandler.sendMessage(msg);

            Looper.prepare();
            player2Handler = new Handler() {
                public void handleMessage(Message msg) {
                    int what = msg.what;
                    String currGuessOf1 = "";
                    String currGuessOf2 = "";
                    String feedBackOfP2 = "";
                    switch (what) {
                        case RCV_FDBK_MODIFY_NUM_AND_SEND_P2: // this is used to check the digits guessed by player 1 and tell it what all it guessed right
                            List<String> getDataOfP2 = (ArrayList<String>) msg.obj;
                            if (count > 20) { // if count is greater than 20 stop everything.
                                player1Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player1Handler.getLooper().quit();
                                    }
                                });
                                player2Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player2Handler.getLooper().quit();
                                    }
                                });
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "The game is drawn", Toast.LENGTH_LONG).show();
                                        winnerText.setText("None Wins");
                                    }
                                });
                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                            }
                            if (firstTimeMsgRcv) { // for the first time when message is received at player 2.
                                currGuessOf1 = getDataOfP2.get(0);
                                final String currrGuessOf1Temp = p2GuessNum;
                                currGuessOf2 = p2GuessNum;
                                String feedBackForP1 = "";
                                for (int i = 0; i < currGuessOf1.length(); i++) {
                                    if (currGuessOf1.charAt(i) == p2Number.charAt(i)) {
                                        feedBackForP1 = feedBackForP1 + '1';
                                    } else {
                                        feedBackForP1 = feedBackForP1 + '0';
                                    }
                                }
                                // add to the feedback the number guessed at wrong position but present in the original string with its position
                                for (int i = 0; i < currGuessOf1.length(); i++) {
                                    if (feedBackForP1.charAt(i) == 0 && p2Number.contains(String.valueOf(currGuessOf1.charAt(i)))) {
                                        feedBackForP1 = feedBackForP1 + currGuessOf1.charAt(i) + i;
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Player 2 Guess of P1: " + currrGuessOf1Temp, Toast.LENGTH_LONG).show();
                                        p2GuessedNum.setText(currrGuessOf1Temp);
                                        count++;
                                        chanceCountText.setText("" + count);
                                    }
                                });
                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                                List<String> sendObjToP2List = new ArrayList<>();
                                sendObjToP2List.add(currGuessOf1);
                                sendObjToP2List.add(currGuessOf2);
                                sendObjToP2List.add(feedBackForP1);
                                while (!isHandler1Initiated) {
                                    try {
                                        Thread.currentThread().sleep(1000);
                                    } catch (InterruptedException e) {
                                        System.out.println("Thread interrupted!");
                                    }
                                }
                                msg = player1Handler.obtainMessage(RCV_FDBK_MODIFY_NUM_AND_SEND_P1);
                                msg.obj = sendObjToP2List;
                                player1Handler.sendMessage(msg);
                                firstTimeMsgRcv = false;
                                break;
                            }
                            // below is the code for not the first time receiving of message from player 1
                            currGuessOf1 = getDataOfP2.get(0);
                            if (getDataOfP2.size() == 3) {
                                currGuessOf2 = getDataOfP2.get(1);
                                feedBackOfP2 = getDataOfP2.get(2);
                            }
                            // New number formation based on feedback from P2
                            String newNumOfP2 = "";
                            for (int i = 0; i < 4; i++) {
                                if (feedBackOfP2.charAt(i) == '1') {
                                    newNumOfP2 = newNumOfP2 + currGuessOf2.charAt(i);
                                } else {
                                    newNumOfP2 = newNumOfP2 + 'X';
                                }
                            }
                            if (newNumOfP2.contains("X")) { // if number guessed by player 2 contains X, that means it still has not predicted number of player1.
                                newNumOfP2 = numberGeneration(newNumOfP2);
                                final String tempNewNum = newNumOfP2;
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Player 2 Guess of P1: " + tempNewNum, Toast.LENGTH_LONG).show();
                                        p2GuessedNum.setText(tempNewNum);
                                        count++;
                                        chanceCountText.setText("" + count);
                                    }
                                });
                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                            } else { // if number guessed by player 2 does not contain X, that means it predicted correctly and is the winner.
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Player 2 Wins", Toast.LENGTH_LONG).show();
                                        winnerText.setText("Player 2");
                                    }
                                });
                                try {
                                    Thread.currentThread().sleep(3000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread interrupted!");
                                }
                                t1.interrupt();
                                t2.interrupt();
                                player1Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player1Handler.getLooper().quit();
                                    }
                                });
                                player2Handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        player2Handler.getLooper().quit();
                                    }
                                });
                            }
                            String feedBackForP1 = "";
                            // create feedback for P1
                            for (int i = 0; i < currGuessOf1.length(); i++) {
                                if (currGuessOf1.charAt(i) == p2Number.charAt(i)) {
                                    feedBackForP1 = feedBackForP1 + '1';
                                } else {
                                    feedBackForP1 = feedBackForP1 + '0';
                                }
                            }
                            // also add to the feedback the number guessed at wrong position but present in the original string alongwith its position
                            for (int i = 0; i < currGuessOf1.length(); i++) {
                                if (feedBackForP1.charAt(i) == 0 && p2Number.contains(String.valueOf(currGuessOf1.charAt(i)))) {
                                    feedBackForP1 = feedBackForP1 + currGuessOf1.charAt(i) + i;
                                }
                            }
                            List<String> sendObjToP2List = new ArrayList<>();
                            sendObjToP2List.add(currGuessOf1);
                            sendObjToP2List.add(newNumOfP2);
                            sendObjToP2List.add(feedBackForP1);
                            msg = player1Handler.obtainMessage(RCV_FDBK_MODIFY_NUM_AND_SEND_P1);
                            msg.obj = sendObjToP2List;
                            player1Handler.sendMessage(msg);
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
}
