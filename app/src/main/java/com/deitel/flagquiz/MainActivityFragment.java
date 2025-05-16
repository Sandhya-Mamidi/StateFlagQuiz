package com.deitel.flagquiz;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivityFragment extends androidx.fragment.app.Fragment {

   private List<String> fileNameList = new ArrayList<>();
   private List<String> quizCountriesList = new ArrayList<>();
   private int correctAnswers;
   private int totalGuesses;
   private final int FLAGS_IN_QUIZ = 10;

   private TextView questionNumberTextView;
   private ImageView flagImageView;
   private Button[] answerButtons = new Button[4];
   private String correctAnswer;

   private final Random random = new Random();

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);

      questionNumberTextView = rootView.findViewById(R.id.questionNumberTextView);
      flagImageView = rootView.findViewById(R.id.flagImageView);
      answerButtons[0] = rootView.findViewById(R.id.answerButton1);
      answerButtons[1] = rootView.findViewById(R.id.answerButton2);
      answerButtons[2] = rootView.findViewById(R.id.answerButton3);
      answerButtons[3] = rootView.findViewById(R.id.answerButton4);

      for (Button button : answerButtons) {
         button.setOnClickListener(answerButtonListener);
      }

      loadFlagFileNames();
      resetQuiz();

      return rootView;
   }

   private void loadFlagFileNames() {
      AssetManager assets = getActivity().getAssets();
      try {
         String[] paths = assets.list("USA");
         if (paths != null) {
            for (String path : paths) {
               fileNameList.add(path.replace(".png", ""));
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void resetQuiz() {
      correctAnswers = 0;
      totalGuesses = 0;
      quizCountriesList.clear();

      Collections.shuffle(fileNameList);
      for (int i = 0; i < FLAGS_IN_QUIZ; i++) {
         quizCountriesList.add(fileNameList.get(i));
      }

      loadNextFlag();
   }

   private void loadNextFlag() {
      if (quizCountriesList.isEmpty()) {
         showQuizResults();
         return;
      }

      String nextImage = quizCountriesList.remove(0);
      correctAnswer = nextImage;
      questionNumberTextView.setText("Question " + (correctAnswers + 1) + " of " + FLAGS_IN_QUIZ);

      AssetManager assets = getActivity().getAssets();
      try (InputStream stream = assets.open("USA/" + nextImage + ".png")) {
         flagImageView.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(stream, nextImage));
      } catch (IOException e) {
         e.printStackTrace();
      }

      List<String> choices = new ArrayList<>(fileNameList);
      choices.remove(correctAnswer);
      Collections.shuffle(choices);
      choices = choices.subList(0, 3);
      choices.add(correctAnswer);
      Collections.shuffle(choices);

      for (int i = 0; i < 4; i++) {
         answerButtons[i].setText(choices.get(i));
         answerButtons[i].setTextColor(Color.BLACK);
      }
   }

   private final View.OnClickListener answerButtonListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
         Button clickedButton = (Button) view;
         String guess = clickedButton.getText().toString();

         if (guess.equals(correctAnswer)) {
            correctAnswers++;
            clickedButton.setTextColor(Color.GREEN);
            Toast.makeText(getActivity(), "Correct!", Toast.LENGTH_SHORT).show();
         } else {
            clickedButton.setTextColor(Color.RED);
            Toast.makeText(getActivity(), "Wrong! Correct: " + correctAnswer, Toast.LENGTH_SHORT).show();
         }

         totalGuesses++;

         view.postDelayed(() -> loadNextFlag(), 1000);
      }
   };

   private void showQuizResults() {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle("Quiz Results");
      builder.setMessage("You got " + correctAnswers + " out of " + FLAGS_IN_QUIZ + " correct!");
      builder.setPositiveButton("OK", (dialog, which) -> resetQuiz());
      builder.setCancelable(false);
      builder.show();
   }
}