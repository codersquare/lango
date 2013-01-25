package com.mit6570.lango;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ExercisePagerAdapter extends FragmentStatePagerAdapter {
  // Instances of this class are fragments representing a single
  // object in our collection.
  public static class ExericseFragment extends Fragment {

    private MediaPlayer questionPlayer, responsePlayer, answerPlayer;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
                             Bundle savedInstanceState) {
      // The last two arguments ensure LayoutParams are inflated
      // properly.
      View rootView = inflater.inflate(R.layout.fragment_exercise, container, false);
      Bundle args = getArguments();
      return createView(rootView, args);
    }
    
    private View createView(View rootView, Bundle b) {
      int index =  b.getInt(KEY_INDEX);

      String instruction = b.getString(KEY_INSTRUCTION);
      setupText(rootView, R.id.text_instruction, instruction);
      
      // Set exercise description.
      String description = b.getString(getString(R.string.ex_description));
      setupText(rootView, R.id.text_description, description);
      
      String questionAudio = b.getString(getString(R.string.ex_description_audio));
      setupPlayQuestionButton(rootView, R.id.button_playquestion, questionAudio);
      
      String answerAudio = b.getString(getString(R.string.ex_answer_audio));
      String answer = b.getString(getString(R.string.ex_answer));
      setupAnswerButton(rootView, R.id.button_playanswer, answerAudio, answer);
      
      String imgFile = b.getString(getString(R.string.ex_image));
      setupDescriptionImage(rootView, imgFile);
      
      File appDir = new File(Environment.getExternalStorageDirectory(), 
                             getString(R.string.app_name));
      appDir.mkdirs();
      String srcBaseName = b.getString(KEY_BASENAME);
      String audioFile = String.format(Locale.US, "%s_%d.3gp", srcBaseName, index);
      final String audioFileAbsPath = (new File(appDir, audioFile)).getAbsolutePath();

      final Recorder recorder = new Recorder();
      
      final ToggleButton recordButton = (ToggleButton) rootView.findViewById(R.id.button_record);
      recordButton.setChecked(false);
      recordButton.setOnClickListener(new OnClickListener() {
        
        @Override
        public void onClick(View button) {
          if (recordButton.isChecked()) {
            recorder.startRecording(audioFileAbsPath);
          } else {
            recorder.stopRecording();
          }
        }
      });
      
      setupPlaybackButton(rootView, R.id.button_playback, audioFileAbsPath);
      return rootView;
    }
    
    private MediaPlayer createPlayer(final ToggleButton tb) {
      MediaPlayer player = new MediaPlayer();
      player.setOnCompletionListener(new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          tb.setChecked(false);
          stopPlaying(mp);
        }
      });
      return player;
    }
    
    /**
     * Plays an audio file.
     * @param file absolute path of an audio file.
     */
    private MediaPlayer startPlaying(String file, final ToggleButton tb) {
      MediaPlayer player = createPlayer(tb);
      try {
        player.setDataSource(file);
        player.prepare();
      } catch (IllegalStateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      player.start();
      return player;
    }
    
    /**
     * 
     * @param afd
     * @param tb
     * @return the new {@code MediaPlayer} created.
     */
    private MediaPlayer startPlaying(AssetFileDescriptor afd, ToggleButton tb) {
      MediaPlayer player = createPlayer(tb);
      try {
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        player.prepare();
      } catch (IllegalStateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      player.start();
      return player;
    }
    
    private void stopPlaying(MediaPlayer player) {
      if (player != null) {
        player.stop();
        player.release();
      }
    } 
    
    private void setupPlaybackButton(View rootView, int buttonId, final String audioFile) {
      final ToggleButton tb = (ToggleButton) rootView.findViewById(buttonId);
      tb.setOnClickListener(new OnClickListener() {
        
        @Override
        public void onClick(View v) {
          if (tb.isChecked()) {
            responsePlayer = startPlaying(audioFile, tb);
          } else {
            stopPlaying(responsePlayer);
          }
        }
      });
    }
    
    private void setupPlayQuestionButton(View rootView, int buttonId, String audioFile) {
      try {
        final AssetFileDescriptor afd = getActivity().getAssets().openFd(audioFile);
        final ToggleButton tb = (ToggleButton) rootView.findViewById(buttonId);
        
        tb.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (tb.isChecked()) {
              questionPlayer = startPlaying(afd, tb);
            } else {
              stopPlaying(questionPlayer);
            }
          }
        });
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    private void setupAnswerButton(final View rootView, int buttonId, String audioFile, 
                                   final String answer) {
      final ToggleButton tb = (ToggleButton) rootView.findViewById(buttonId);
      try {
        final AssetFileDescriptor afd = getActivity().getAssets().openFd(audioFile);
        tb.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (tb.isChecked()) {
              answerPlayer = startPlaying(afd, tb);
              setupText(rootView, R.id.text_answer, answer);
            } else {
              stopPlaying(answerPlayer);
              setupText(rootView, R.id.text_answer, "");
            }
          }
        });
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    private void setupDescriptionImage(View rootView, String imgFile) {
      ImageView iv = (ImageView) rootView.findViewById(R.id.image_description);
      try {   
        Bitmap image = getBitmapFromAsset(imgFile);
        iv.setImageBitmap(image);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
    }
    
    /**
     * Helper Functions
     * @throws IOException 
     */
    private Bitmap getBitmapFromAsset(String strName) throws IOException {
        AssetManager assetManager = getActivity().getAssets();

        InputStream istr = assetManager.open(strName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        istr.close();

        return bitmap;
    }
    
    private void setupText(View rootView, int id, String text) {
      TextView tv = (TextView) rootView.findViewById(id);
      tv.setText(text);
    }
  }

  private static final String KEY_INDEX = "index";
  private static final String KEY_BASENAME = "base_name";
  private static final String KEY_INSTRUCTION = "instruction";
  private final String baseName;
  private final Context context;
  private final List<Bundle> exercises;
  private final String instruction;
  
  public ExercisePagerAdapter(FragmentActivity fa, List<Bundle> exercises, 
                              String exerciseBaseName, String instruction) {
    super(fa.getSupportFragmentManager());
    context = fa;
    this.exercises = exercises;
    this.baseName = exerciseBaseName;
    this.instruction = instruction;
  }

  @Override
  public Fragment getItem(int i) {
    Fragment fragment = Fragment.instantiate(context, ExericseFragment.class.getName());
    Bundle b = exercises.get(i);
    b.putInt(KEY_INDEX, i + 1);
    b.putString(KEY_BASENAME, baseName);
    b.putString(KEY_INSTRUCTION, instruction);
    fragment.setArguments(b);
    return fragment;
  }

  @Override
  public int getCount() {
    return exercises.size();
  }

  @Override
  public CharSequence getPageTitle(int position) {
      return String.format(Locale.US, "%d / %d", position + 1, exercises.size());
  }
}