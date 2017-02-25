package ziphil.dictionary

import groovy.transform.CompileStatic
import java.security.AccessController
import java.security.AccessControlContext
import java.security.CodeSource
import java.security.Permissions
import java.security.PrivilegedExceptionAction
import java.security.ProtectionDomain
import java.security.cert.Certificate
import java.util.concurrent.Callable
import java.util.function.Predicate
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent
import javax.script.ScriptEngineManager
import javax.script.ScriptEngine
import javax.script.ScriptException
import ziphil.custom.ShufflableList
import ziphil.module.NoSuchScriptEngineException
import ziphil.module.Setting
import ziphil.module.Strings
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public abstract class DictionaryBase<W extends Word, S extends Suggestion> implements Dictionary<W> {

  private static final AccessControlContext ACCESS_CONTROL_CONTEXT = createAccessControlContext()

  protected String $name = ""
  protected String $path = ""
  protected ObservableList<W> $words = FXCollections.observableArrayList()
  protected FilteredList<W> $filteredWords
  protected SortedList<W> $sortedWords
  protected ShufflableList<W> $shufflableWords
  protected ObservableList<S> $suggestions = FXCollections.observableArrayList()
  protected FilteredList<S> $filteredSuggestions
  protected SortedList<S> $sortedSuggestions
  private ObservableList<Element> $wholeWords = FXCollections.observableArrayList()
  private Task<?> $loader
  private Task<?> $saver
  protected Boolean $isChanged = false
  protected Boolean $isFirstEmpty = false

  public DictionaryBase(String name, String path) {
    $name = name
    $path = path
    $isChanged = (path == null) ? true : false
    $isFirstEmpty = path == null
    setupSortedWords()
    setupWholeWords()
  }

  public void searchByName(String search, Boolean isStrict) {
    Setting setting = Setting.getInstance()
    Boolean ignoresAccent = setting.getIgnoresAccent()
    Boolean ignoresCase = setting.getIgnoresCase()
    Boolean searchesPrefix = setting.getSearchesPrefix()
    try {
      Pattern pattern = (isStrict) ? null : Pattern.compile(search)
      String convertedSearch = Strings.convert(search, ignoresAccent, ignoresCase)
      resetSuggestions()
      checkWholeSuggestion(search, convertedSearch)
      updateWordPredicate() { Word word ->
        if (word.isDisplayed()) {
          if (isStrict) {
            String name = word.getName()
            String convertedName = Strings.convert(name, ignoresAccent, ignoresCase)
            checkSuggestion(word, search, convertedSearch)
            if (search != "") {
              if (searchesPrefix) {
                return convertedName.startsWith(convertedSearch)
              } else {
                return convertedName == convertedSearch
              }
            } else {
              return true
            }
          } else {
            Matcher matcher = pattern.matcher(word.getName())
            return matcher.find()
          }
        } else {
          return false
        }
      }
    } catch (PatternSyntaxException exception) {
    }
  }

  public void searchByEquivalent(String search, Boolean isStrict) {
    Setting setting = Setting.getInstance()
    Boolean ignoresAccent = setting.getIgnoresAccent()
    Boolean ignoresCase = setting.getIgnoresCase()
    Boolean searchesPrefix = setting.getSearchesPrefix()
    try {
      Pattern pattern = Pattern.compile(search)
      resetSuggestions()
      updateWordPredicate() { Word word ->
        if (word.isDisplayed()) {
          if (isStrict) {
            if (search != "") {
              return word.getEquivalents().any() { String equivalent ->
                if (searchesPrefix) {
                  return equivalent.startsWith(search)
                } else {
                  return equivalent == search
                }
              }
            } else {
              return true
            }
          } else {
            return word.getEquivalents().any() { String equivalent ->
              Matcher matcher = pattern.matcher(equivalent)
              return matcher.find()
            }
          }
        } else {
          return false
        }
      }
    } catch (PatternSyntaxException exception) {
    }
  }

  public void searchByContent(String search) {
    try {
      Pattern pattern = Pattern.compile(search)
      resetSuggestions()
      updateWordPredicate() { Word word ->
        if (word.isDisplayed()) {
          Matcher matcher = pattern.matcher(word.getContent())
          return matcher.find()
        } else {
          return false
        }
      }
    } catch (PatternSyntaxException exception) {
    }
  }

  public void searchScript(String script) {
    String scriptName = Setting.getInstance().getScriptName()
    ScriptEngineManager scriptEngineManager = ScriptEngineManager.new()
    ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(scriptName)
    resetSuggestions()
    if (scriptEngine != null) {
      Exception suppressedException
      updateWordPredicate() { Word word ->
        try {
          PrivilegedExceptionAction<Boolean> action = (PrivilegedExceptionAction<Boolean>){
            try {
              if (suppressedException == null) {
                scriptEngine.put("word", plainWord(word))
                Object result = scriptEngine.eval(script)
                return (result) ? true : false
              } else {
                return false
              }
            } catch (Exception exception) {
              suppressedException = exception
              return false
            }
          }
          return AccessController.doPrivileged(action, ACCESS_CONTROL_CONTEXT)
        } catch (Exception exception) {
          suppressedException = exception
          return false
        }
      }
      if (suppressedException != null) {
        throw suppressedException
      }
    } else {
      updateWordPredicate() { Word word ->
        return false
      }
      throw NoSuchScriptEngineException.new(scriptName)
    }
  }

  protected void updateWordPredicate(Predicate<? super W> predicate) {
    $filteredWords.setPredicate(predicate)
    $filteredSuggestions.setPredicate() { Suggestion suggestion ->
      return suggestion.isDisplayed()
    }
    $shufflableWords.unshuffle()
  }

  protected void resetSuggestions() {
    for (Suggestion suggestion : $suggestions) {
      suggestion.getPossibilities().clear()
      suggestion.setDisplayed(false)
    }
  }

  protected void checkWholeSuggestion(String search, String convertedSearch) {
  }

  protected void checkSuggestion(W word, String search, String convertedSearch) {
  }

  public void shuffleWords() {
    $shufflableWords.shuffle()
  }

  public abstract Object plainWord(W word)

  public abstract void update()

  public abstract void updateMinimum()

  protected void load() {
    $loader = createLoader()
    $loader.addEventFilter(WorkerStateEvent.WORKER_STATE_SUCCEEDED) { WorkerStateEvent event ->
      if (!$isFirstEmpty) {
        $isChanged = false
      }
    }
    Thread thread = Thread.new($loader)
    thread.setDaemon(true)
    thread.start()
  }

  public void save() {
    $saver = createSaver()
    $saver.run()
    if ($path != null) {
      $isChanged = false
    }
  }

  private void setupSortedWords() {
    $filteredWords = FilteredList.new($words)
    $sortedWords = SortedList.new($filteredWords)
    $shufflableWords = ShufflableList.new($sortedWords)
    $filteredSuggestions = FilteredList.new($suggestions){suggestion -> false}
    $sortedSuggestions = SortedList.new($filteredSuggestions)
  }

  private void setupWholeWords() {
    ListChangeListener<?> listener = (ListChangeListener){ Change<?> change ->
      $wholeWords.clear()
      $wholeWords.addAll($sortedSuggestions)
      $wholeWords.addAll($shufflableWords)
    }
    $filteredWords.addListener(listener)
    $filteredSuggestions.addListener(listener)
    $shufflableWords.addListener(listener)
  }

  public Integer hitSize() {
    return $shufflableWords.size()
  }

  public Integer totalSize() {
    return $words.size()
  }

  protected abstract Task<?> createLoader()

  protected abstract Task<?> createSaver()

  private static AccessControlContext createAccessControlContext() {
    CodeSource codeSource = CodeSource.new(null, (Certificate[])null)
    Permissions permissions = Permissions.new()
    permissions.add(PropertyPermission.new("*", "read"))
    permissions.add(RuntimePermission.new("accessDeclaredMembers"))
    permissions.add(RuntimePermission.new("createClassLoader"))
    permissions.add(RuntimePermission.new("getProtectionDomain"))
    ProtectionDomain domain = ProtectionDomain.new(codeSource, permissions)
    ProtectionDomain[] domains = [domain]
    AccessControlContext context = AccessControlContext.new(domains)
    return context
  }

  public String getName() {
    return $name
  }

  public void setName(String name) {
    $name = name
  }

  public String getPath() {
    return $path
  }

  public void setPath(String path) {
    $path = path
  }

  public ObservableList<Element> getWholeWords() {
    return $wholeWords
  }

  public ObservableList<W> getWords() {
    return $shufflableWords
  }

  public ObservableList<W> getRawWords() {
    return $words
  }

  public Task<?> getLoader() {
    return $loader
  }

  public Task<?> getSaver() {
    return $saver
  }

  public Boolean isChanged() {
    return $isChanged
  }

}