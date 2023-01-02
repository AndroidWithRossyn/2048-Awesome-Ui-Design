package m2048.banrossyn.game2048;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import m2048.banrossyn.Game.Game;
import m2048.banrossyn.R;
import m2048.banrossyn.Utils.DataBaseUtils;
import m2048.banrossyn.Utils.SerializeUtils;
import m2048.banrossyn.viewFor2048.AboutPage;
import m2048.banrossyn.viewFor2048.AchievementDialog;
import m2048.banrossyn.viewFor2048.GameOverDialog;

public class MainActivity extends AppCompatActivity {

    Game game = new Game();
    DataBaseUtils db = null;

    TextView[] tvList = null;
    TextView tv_score = null;
    TextView tv_bestScore = null;

    int bestScore = 0;

    class GameTouchListener implements View.OnTouchListener {

        private float pressX;
        private float pressY;
        private float moveX;
        private float moveY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    pressX = motionEvent.getX();
                    pressY = motionEvent.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    moveX = motionEvent.getX();
                    moveY = motionEvent.getY();
                    break;

                case MotionEvent.ACTION_UP:
                    if (moveX - pressX > 0 && Math.abs(moveY - pressY) < 150) {
                        game.moveOperation(Game.Key.RIGHT);
                    } else if (moveX - pressX < 0 && Math.abs(moveY - pressY) < 150) {
                        game.moveOperation(Game.Key.LEFT);
                    } else if (moveY - pressY > 0 && Math.abs(moveX - pressX) < 150) {
                        game.moveOperation(Game.Key.DOWN);
                    } else if (moveY - pressY < 0 && Math.abs(moveX - pressX) < 150) {
                        game.moveOperation(Game.Key.UP);
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    class GameOverBtnClickedListener implements GameOverDialog.OnBtnClickedListener {

        @Override
        public void onCancelBtnClicked() {

        }

        @Override
        public void onRestartBtnClicked() {
            db.insertScore(game.getGameData().score, game.getMaxNum(), game.getGameFlag().hasUndo);
            game.start();
        }
    }

    class GameStateChangedListener implements Game.OnGameStateChangedListener {

        @Override
        public void printer() {
            Game.GameData data = game.getGameData();

            for (int row = 0; row < 4; row++)
                for (int col = 0; col < 4; col++) {

                    TextView tv = tvList[row * 4 + col];

                    if (data.map[row][col] != 0) {
                        tv.setText(String.valueOf(data.map[row][col]));
                        ((GradientDrawable) tv.getBackground()).setColor(getItemColor(data.map[row][col]));
                    } else {
                        tv.setText(" ");
                        //TODO ?显示异常？？？
                        ((GradientDrawable) tv.getBackground()).setColor(Color.parseColor("#EFC998"));
                    }
                }
            tv_score.setText(String.valueOf(data.score));
            if (data.score > bestScore) {
                bestScore = data.score;
                tv_bestScore.setText(String.valueOf(bestScore));
            }
        }

        @Override
        public void overHandler() {
            new GameOverDialog(MainActivity.this, new GameOverBtnClickedListener(), game.getGameData().score).show();
        }

        void startAnimation() {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DataBaseUtils(this);

        View gameView = findViewById(R.id.gameView);

        tvList = new TextView[16];
        findViewsBySameIds(tvList, "gametv_");
        tv_score = findViewById(R.id.tv_score);
        tv_bestScore = findViewById(R.id.tv_bestScore);

        bestScore = db.queryBestScore();
        tv_bestScore.setText(String.valueOf(bestScore));

        game.setManualFunc(new GameStateChangedListener());
        gameView.setOnTouchListener(new GameTouchListener());

        Map<String, String> record = db.queryLastGameRecord();
        if (record != null)
            game.goOn(record.get("gameData"), record.get("gameFlag"));
        else
            game.start();
    }

    @Override
    public void finish() {
        try {
            db.insertLastGameRecord(SerializeUtils.serializeObject(game.getGameData()), SerializeUtils.serializeObject(game.getGameFlag()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.finish();
    }


    protected <T> void findViewsBySameIds(T[] views, String samePhase) {
        for (int i = 0; i < views.length; i++) {
            String m_resid = samePhase + String.valueOf(i);
            int resID = getResources().getIdentifier(m_resid, "id", getPackageName());
            views[i] = (T) findViewById(resID);
        }
    }

    private int getItemColor(int num) {
        switch (num) {
            case 2:
                return Color.rgb(199, 237, 253);
            case 4:
                return Color.rgb(175, 215, 237);
            case 8:
                return Color.rgb(92, 167, 186);
            case 16:
                return Color.rgb(147, 224, 255);
            case 32:
                return Color.rgb(174, 221, 129);
            case 64:
                return Color.rgb(107, 194, 53);
            case 128:
                return Color.rgb(6, 126, 67);
            case 256:
                return Color.rgb(38, 157, 128);
            case 512:
                return Color.parseColor("#FF9A9E");
            case 1024:
                return Color.parseColor("#FFD0C4");
            case 2048:
                return Color.parseColor("#FFECD2");
            case 4096:
                return Color.parseColor("#FCB69F");
            case 8192:
                return Color.rgb(160, 191, 124);
            case 16384:
                return Color.rgb(101, 147, 74);
            case 32768:
                return Color.rgb(64, 116, 52);
            case 65536:
                return Color.rgb(3, 35, 14);
            default:
                return Color.rgb(255, 255, 255);
        }
    }

    public void onUndoBtnClicked(View v) {
        game.undo();
    }

    public void onAchievementBtnClicked(View v) {
        new AchievementDialog(this, db.queryScore()).show();
    }

    public void onRestartBtnClicked(View v) {
        game.start();
    }

    public void onAboutBtnClicked(View v) {
        new AboutPage(this).show();
    }

    public void onReadmeBtnClicked(View v) {
        startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/BanRossyn")));
    }
}
/*
 *  TODO
 *  1.  动效（有缘再说）
 *  4.  网络交互（有缘再说）
 *
 * */