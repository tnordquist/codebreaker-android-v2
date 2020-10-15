package edu.cnm.deepdive.codebreaker.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import edu.cnm.deepdive.codebreaker.R;
import edu.cnm.deepdive.codebreaker.model.Code.Guess;
import edu.cnm.deepdive.codebreaker.model.Game;
import edu.cnm.deepdive.codebreaker.model.IllegalGuessCharacterException;
import edu.cnm.deepdive.codebreaker.model.IllegalGuessLengthException;
import edu.cnm.deepdive.codebreaker.service.GameRepository;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

public class MainViewModel extends AndroidViewModel {

  public static final String POOL = "ROYGBIV";

  private final MutableLiveData<Game> game;
  private final MutableLiveData<Guess> guess;
  private final MutableLiveData<Boolean> solved;
  private final MutableLiveData<Throwable> throwable;
  private final Random rng;
  private final String codeLengthPrefKey;
  private final int codeLengthPrefDefault;
  private final SharedPreferences preferences;
  private final GameRepository repository;

  private Date timestamp;
  private int previousGuessCount;

  public MainViewModel(@NonNull Application application) {
    super(application);
    repository = new GameRepository(application);
    game = new MutableLiveData<>();
    guess = new MutableLiveData<>();
    solved = new MutableLiveData<>();
    throwable = new MutableLiveData<>();
    rng = new SecureRandom();
    codeLengthPrefKey = application.getString(R.string.code_length_pref_key);
    codeLengthPrefDefault =
        application.getResources().getInteger(R.integer.code_length_pref_default);
    preferences = PreferenceManager.getDefaultSharedPreferences(application);
    preferences.registerOnSharedPreferenceChangeListener((prefs, key) -> {
      if (key.equals(codeLengthPrefKey)) {
        startGame();
      }
    });
    startGame();
  }

  public LiveData<Game> getGame() {
    return game;
  }

  public LiveData<Guess> getGuess() {
    return guess;
  }

  public LiveData<Boolean> getSolved() {
    return solved;
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void startGame() {
    throwable.setValue(null);
    guess.setValue(null);
    solved.setValue(false);
    int codeLength = preferences.getInt(codeLengthPrefKey, codeLengthPrefDefault);
    Game game = new Game(POOL, codeLength, rng);
    timestamp = new Date();
    previousGuessCount = 0;
    this.game.setValue(game);
  }

  public void restartGame() {
    throwable.setValue(null);
    guess.setValue(null);
    solved.setValue(false);
    Game game = this.game.getValue();
    //noinspection ConstantConditions
    previousGuessCount += game.getGuessCount();
    game.restart();
    this.game.setValue(game);
  }

  public void guess(String text) {
    throwable.setValue(null);
    try {
      Game game = this.game.getValue();
      //noinspection ConstantConditions
      Guess guess = game.guess(text);
      this.guess.setValue(guess);
      this.game.setValue(game);
      if (guess.getCorrect() == game.getLength()) {
        solved.setValue(true);
        save(game);
      }
    } catch (IllegalGuessLengthException | IllegalGuessCharacterException e) {
      throwable.setValue(e);
    }
  }

  private void save(Game game) {
    edu.cnm.deepdive.codebreaker.model.entity.Game newGame =
        new edu.cnm.deepdive.codebreaker.model.entity.Game();
    newGame.setCodeLength(game.getLength());
    newGame.setTimestamp(timestamp);
    newGame.setGuessCount(game.getGuessCount() + previousGuessCount);
    repository.save(newGame)
        .subscribe(
            () -> {},
            throwable::postValue
        );
  }

}
