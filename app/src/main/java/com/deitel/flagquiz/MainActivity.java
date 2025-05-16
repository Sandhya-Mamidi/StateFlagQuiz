package com.deitel.flagquiz;

import android.app.AlertDialog;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

   private ImageView flagImage;
   private TextView questionText;
   private Button[] optionButtons;

   private List<String> fileNameList;
   private String correctAnswer;
   private int totalGuesses;
   private int correctAnswers;
   private int currentQuestion;

   private static final int TOTAL_QUESTIONS = 10;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      flagImage = findViewById(R.id.flagImage);
      questionText = findViewById(R.id.questionText);
      optionButtons = new Button[] {
              findViewById(R.id.option1),
              findViewById(R.id.option2),
              findViewById(R.id.option3),
              findViewById(R.id.option4)
      };

      fileNameList = new ArrayList<>();
      loadFlagImages();
      Collections.shuffle(fileNameList);
      nextQuestion();
   }

   private void loadFlagImages() {
      AssetManager assets = getAssets();
      try {
         String[] files = assets.list("USA");
         if (files != null) {
            Collections.addAll(fileNameList, files);
         }
      } catch (IOException e) {
         Toast.makeText(this, "Failed to load flag images.", Toast.LENGTH_LONG).show();
      }
   }

   private String getCountryName(String name) {
      return name.substring(name.indexOf('-') + 1, name.indexOf('+')).replace('_', ' ');
   }

   private String getCapName(String name) {
      return name.substring(name.indexOf('+') + 1, name.indexOf('.')).replace('_', ' ');
   }

   private void nextQuestion() {
      if (currentQuestion == TOTAL_QUESTIONS) {
         showFinalScore();
         return;
      }

      correctAnswer = fileNameList.get(currentQuestion);
      String displayState = getCountryName(correctAnswer);
      String displayCapital = getCapName(correctAnswer);

      questionText.setText("Question " + (currentQuestion + 1) + " of " + TOTAL_QUESTIONS);

      try {
         InputStream is = getAssets().open("USA/" + correctAnswer);
         Drawable flag = Drawable.createFromStream(is, null);
         flagImage.setImageDrawable(flag);
      } catch (IOException e) {
         e.printStackTrace();
      }

      List<String> options = new ArrayList<>(fileNameList);
      options.remove(correctAnswer);
      Collections.shuffle(options);
      options = options.subList(0, 3);
      options.add(correctAnswer);
      Collections.shuffle(options);

      for (int i = 0; i < optionButtons.length; i++) {
         String state = getCountryName(options.get(i));
         String capital = getCapName(options.get(i));
         optionButtons[i].setText(state + " (" + capital + ")");
         optionButtons[i].setEnabled(true);
         optionButtons[i].setBackgroundColor(0xFFDDDDDD);
         int finalI = i;
         optionButtons[i].setOnClickListener(v -> checkAnswer(optionButtons[finalI]));
      }
   }

   private void checkAnswer(Button selectedButton) {
      totalGuesses++;
      String answerText = selectedButton.getText().toString();
      String correctText = getCountryName(correctAnswer) + " (" + getCapName(correctAnswer) + ")";

      if (answerText.equalsIgnoreCase(correctText)) {
         correctAnswers++;
         selectedButton.setBackgroundColor(0xFF90EE90);
         Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
      } else {
         selectedButton.setBackgroundColor(0xFFFF6961);
         Toast.makeText(this, "Incorrect! Answer: " + correctText, Toast.LENGTH_SHORT).show();
      }

      for (Button b : optionButtons) {
         b.setEnabled(false);
      }

      new Handler().postDelayed(() -> {
         currentQuestion++;
         nextQuestion();
      }, 1500);
   }

   private void showFinalScore() {
      new AlertDialog.Builder(this)
              .setTitle("Quiz Complete")
              .setMessage("You got " + correctAnswers + " out of " + TOTAL_QUESTIONS)
              .setPositiveButton("Restart", (dialog, which) -> {
                 Collections.shuffle(fileNameList);
                 currentQuestion = 0;
                 correctAnswers = 0;
                 totalGuesses = 0;
                 nextQuestion();
              })
              .setCancelable(false)
              .show();
   }
}
