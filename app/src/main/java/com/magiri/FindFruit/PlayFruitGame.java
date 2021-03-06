package com.magiri.FindFruit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class PlayFruitGame extends AppCompatActivity {
    private static final String TAG = "FIND_FRUIT_GAME";
    ImageView FruitImageView,backBtn;
    ImageView speechImageView,textImageView;
    CardView imageView;
    TextView FruitImageAnswer,showAnswerTxt,scoreTxt,gameLevelTxt,answerEditText;
    boolean isGivenHint = false;
    AlertDialog wrongDialog,successDialog,completeGameDialog;
    Animation shake_vertically,shake_horizontally;
    Handler imageChangeHandler;
    private Button doneBtn;
    private static final String FINDFRUIT_GAME ="com.magiri.FindFruit.FruitGame";
    private static final String Fruit_Game_Score ="FruitGame_Score";
    private static final String GameLevel="FindFruit_GameLevel";
    MediaPlayer mp;
    private RadioGroup MCQ_RadioGroup;
    private RadioButton firstOption,secondOption,thirdOption,radioButton;
    private static final int SPEECH_REQUEST_CODE=1;
    int FruitImageIds[]={R.drawable.banana_img,R.drawable.apple_img,R.drawable.orange_image,R.drawable.green_apple,R.drawable.growing_apple,R.drawable.banana,
                        R.drawable.cut_apple,R.drawable.banana_bunch,R.drawable.orange};
    String FruitLabels[]={"Banana","Apple","Orange","Apple","Apple","Banana","Apple","Banana","Orange"};
    String AnswerDistractors[][]={{"Apple","Banana","Orange"},{"Orange","Apple","Banana"},
                                    {"Apple","Banana","Orange"},{"Banana","Apple","Orange"},
                                    {"Apple","Banana","Orange"},{"Apple","Orange","Banana"},
                                    {"Banana","Apple","Orange"},{"Apple","Orange","Banana"},
                                    {"Banana","Orange","Apple"}};
    int count,score=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_fruit_game);
        backBtn=findViewById(R.id.FruitGameBackBtn);
        backBtn.setOnClickListener(v -> finish());
        FruitImageView=findViewById(R.id.fruitImg);
        imageChangeHandler=new Handler();
        scoreTxt=findViewById(R.id.scoreTxt);
        showAnswerTxt=findViewById(R.id.answerTxt);
        answerEditText=findViewById(R.id.answerEditTxt);
        answerEditText.setCursorVisible(false);
        speechImageView=findViewById(R.id.SpeechIconImageView);
//        textImageView=findViewById(R.id.textIconImageView);
        FruitImageAnswer=findViewById(R.id.FruitNameTxt);
        gameLevelTxt=findViewById(R.id.GameLevel);
        doneBtn=findViewById(R.id.submitBtn);
        MCQ_RadioGroup=findViewById(R.id.answers_Mcq);
        imageView=findViewById(R.id.fruitImageView);
        firstOption=findViewById(R.id.firstOption);
        secondOption=findViewById(R.id.secondOption);
        thirdOption=findViewById(R.id.thirdOption);
        SharedPreferences storedPref=getSharedPreferences(FINDFRUIT_GAME,Context.MODE_PRIVATE);
        score=storedPref.getInt(Fruit_Game_Score,0);
        count=storedPref.getInt(GameLevel,0);
        if(count+1>=FruitLabels.length){
            displayGameComplete();
        }
        scoreTxt.setText(String.valueOf(score));
        FruitImageView.setImageResource(FruitImageIds[count]);
        gameLevelTxt.setText(count+1+"/"+FruitImageIds.length);
        firstOption.setText(AnswerDistractors[count][0]);
        secondOption.setText(AnswerDistractors[count][1]);
        thirdOption.setText(AnswerDistractors[count][2]);
        shake_horizontally= AnimationUtils.loadAnimation(this,R.anim.shake_horizontally);
        shake_vertically= AnimationUtils.loadAnimation(this,R.anim.shake_vertically);
        speechImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent speechIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Identify the Fruit");
                try {
                    startActivityForResult(speechIntent,SPEECH_REQUEST_CODE);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        answerEditText.setOnClickListener(v -> answerEditText.setCursorVisible(true));
        MCQ_RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton=findViewById(checkedId);
                Toast toast=Toast.makeText(getApplicationContext(),radioButton.getText(),Toast.LENGTH_SHORT);
                View view=toast.getView();
                view.getBackground().setColorFilter(Color.parseColor("#FD612F"), PorterDuff.Mode.SRC_IN);
                TextView text=view.findViewById(android.R.id.message);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                text.setTextSize(16);
                toast.setGravity(Gravity.CENTER,0,0);
//                toast.show();
                answerEditText.setVisibility(View.VISIBLE);
                answerEditText.setText(radioButton.getText());
            }
        });
        showAnswerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MCQ_RadioGroup.setVisibility(View.GONE);
                answerEditText.setVisibility(View.VISIBLE);
                answerEditText.setText(FruitLabels[count]);
                isGivenHint=true;
                String currentScore=scoreTxt.getText().toString();
                reduceScore(currentScore);
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getAnswer From the EditText
                String Answer=answerEditText.getText().toString().trim();
                if(TextUtils.isEmpty(Answer)){
                    Toast toast=Toast.makeText(getApplicationContext(),"Enter your Answer",Toast.LENGTH_SHORT);
                    View view=toast.getView();
                    view.getBackground().setColorFilter(Color.parseColor("#FD612F"), PorterDuff.Mode.SRC_IN);
                    TextView text=view.findViewById(android.R.id.message);
                    text.setTextColor(Color.parseColor("#FFFFFF"));
                    text.setTextSize(16);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }else{
                    validateAnswer(Answer);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SPEECH_REQUEST_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> Answer=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                answerEditText.setVisibility(View.VISIBLE);
                answerEditText.setText(Objects.requireNonNull(Answer).get(0));
            }
        }
    }

    private void validateAnswer(String userAnswer) {
        if(userAnswer.equalsIgnoreCase(FruitLabels[count])){
           handleCorrectAnswer(userAnswer);
        }else{
            handleWrongAnswer(userAnswer);
        }
    }

    private void handleWrongAnswer(String userAnswer) {
        Context context=PlayFruitGame.this;
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.wrong_answer,null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);
        TextView FruitName=view.findViewById(R.id.FruitNameTxt);
        Button tryAgainBtn=view.findViewById(R.id.tryAgainBtn);
        ImageView wrongImageView=view.findViewById(R.id.wrongImageView);
        wrongImageView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.continuous_zoom_animation));
        tryAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseAlertUpdateUI();
            }
        });
        FruitName.setText(userAnswer);
        mp=MediaPlayer.create(getApplicationContext(),R.raw.wrong_answer_audio);
        mp.start();
        wrongDialog=alertDialogBuilder.create();
        //set the background FrameLayout to transparent
        wrongDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        wrongDialog.show();
    }

    private void handleCorrectAnswer(String userAnswer) {
        Context context=PlayFruitGame.this;
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.correct_alert_dialog,null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);
        ImageView correctImageView=view.findViewById(R.id.correctImageView);
        correctImageView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.continuous_zoom_animation));
        TextView FruitName=view.findViewById(R.id.FruitNameTxt);
        FruitName.setText(userAnswer);
        Button ContinueBtn=view.findViewById(R.id.continueBtn);
        ContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
            }
        });
        mp= MediaPlayer.create(getApplicationContext(),R.raw.correct_answer_audio);
        mp.start();
        String currentScore=scoreTxt.getText().toString();
        addScore(currentScore);
        successDialog=alertDialogBuilder.create();
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        successDialog.show();
    }

    private void changeFruitImage() {
        if(count<FruitImageIds.length-1){
            count++;
//            radioButton.setChecked(false);
            imageChangeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences FindFruit=getSharedPreferences(FINDFRUIT_GAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=FindFruit.edit();
                    editor.putInt(GameLevel,count);
                    editor.apply();
                    firstOption.setText(AnswerDistractors[count][0]);
                    secondOption.setText(AnswerDistractors[count][1]);
                    thirdOption.setText(AnswerDistractors[count][2]);
                    FruitImageView.setImageResource(FruitImageIds[count]);
                    imageView.startAnimation(shake_horizontally);
                }
            },500);
        }else{
            //display Finish Game Alert Dialog
            displayGameComplete();
        }
    }

    private void displayGameComplete() {
        Context context=PlayFruitGame.this;
        AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(context);
        LayoutInflater layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.game_completion_dialog,null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);
        Button backHomeBtn=view.findViewById(R.id.backHomeBtn);
        TextView overallScoreTxt=view.findViewById(R.id.congratStatusTxt);
        overallScoreTxt.setText("You scored "+scoreTxt.getText().toString()+" / 9");
        ImageView achievementImageView=view.findViewById(R.id.successImageView);
        achievementImageView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.continuous_zoom_animation));
        backHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),FindFruitDashBoard.class));
                SharedPreferences FindFruitGame_Pref=getSharedPreferences(FINDFRUIT_GAME,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=FindFruitGame_Pref.edit();
                editor.clear();
                editor.apply();
                finish();
            }
        });
        completeGameDialog=alertDialogBuilder.create();
        completeGameDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        completeGameDialog.show();
    }

    private void CloseAlertUpdateUI() {
        answerEditText.setText("");
        gameLevelTxt.setText(count+1+"/"+FruitImageIds.length);
        wrongDialog.dismiss();
    }

    private void updateUI() {
        changeFruitImage();
        answerEditText.setText("");
        successDialog.dismiss();
        gameLevelTxt.setText(count+1+"/"+FruitImageIds.length);
        isGivenHint=false;
    }

    private void reduceScore(String currentScore) {
        int Score=Integer.parseInt(currentScore);
        if(Score>0){
            scoreTxt.setText(String.valueOf(--Score));
        }
        SharedPreferences FindFruit=getSharedPreferences(FINDFRUIT_GAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=FindFruit.edit();
        editor.putInt(Fruit_Game_Score,Score);
        editor.apply();

    }

    private void addScore(String currentScore) {
        int Score=Integer.parseInt(currentScore);
        if(!isGivenHint){
            scoreTxt.setText(String.valueOf(++Score));
        }
        SharedPreferences FindFruit=getSharedPreferences(FINDFRUIT_GAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=FindFruit.edit();
        editor.putInt(Fruit_Game_Score,Score);
        editor.apply();

    }
}