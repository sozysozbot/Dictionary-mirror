package ziphil.dictionary

import groovy.transform.CompileStatic
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import ziphil.module.Setting
import ziphil.module.Strings


@CompileStatic @Newify
public class SlimeDictionary extends Dictionary {

  private String $name = ""
  private String $path = ""
  private ObservableList<SlimeWord> $words = FXCollections.observableArrayList()
  private FilteredList<SlimeWord> $filteredWords
  private SortedList<SlimeWord> $sortedWords

  public SlimeDictionary(String name, String path) {
    $name = name
    $path = path
    load()
    setupWords()
  }

  public void searchByName(String search, Boolean isStrict) {
    Setting setting = Setting.getInstance()
    Boolean ignoresAccent = setting.getIgnoresAccent()
    Boolean ignoresCase = setting.getIgnoresCase()
    try {
      Pattern pattern = Pattern.compile(search)
      $filteredWords.setPredicate() { SlimeWord word ->
        if (isStrict) {
          String newName = word.getName()
          String newSearch = search
          if (ignoresAccent) {
            newName = Strings.unaccent(newName)
            newSearch = Strings.unaccent(newSearch)
          }
          if (ignoresCase) {
            newName = Strings.toLowerCase(newName)
            newSearch = Strings.toLowerCase(newSearch)
          }
          return newName.startsWith(newSearch)
        } else {
          Matcher matcher = pattern.matcher(word.getName())
          return matcher.find()
        }
      }
    } catch (PatternSyntaxException exception) {
    }
  }

  public void searchByEquivalent(String search, Boolean isStrict) {
    try {
      Pattern pattern = Pattern.compile(search)
      $filteredWords.setPredicate() { SlimeWord word ->
        if (isStrict) {
          return word.getEquivalents().any() { String equivalent ->
            return equivalent.startsWith(search)
          }
        } else {
          return word.getEquivalents().any() { String equivalent ->
            Matcher matcher = pattern.matcher(equivalent)
            return matcher.find()
          }
        }
      }
    } catch (PatternSyntaxException exception) {
    }
  }

  public void searchByContent(String search) {
    try {
      Pattern pattern = Pattern.compile(search)
      $filteredWords.setPredicate() { SlimeWord word ->
        Matcher matcher = pattern.matcher(word.getContent())
        return matcher.find()
      }
    } catch (PatternSyntaxException exception) {
    }
  }

  public void addWord(Word word) {
    $words.add((SlimeWord)word)
  }

  public void removeWord(Word word) {
    $words.remove((SlimeWord)word)
  }

  private void load() {
  }

  public void save() {
  }

  private void setupWords() {
    $filteredWords = FilteredList.new($words)
    $sortedWords = SortedList.new($filteredWords)
    $sortedWords.setComparator() { SlimeWord firstWord, SlimeWord secondWord ->
      return firstWord.getName() <=> secondWord.getName()
    }
  }

  public Boolean supportsEquivalent() {
    return true
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

  public DictionaryType getType() {
    return DictionaryType.SLIME
  }

  public ObservableList<? extends Word> getWords() {
    return $sortedWords
  }

  public ObservableList<? extends Word> getRawWords() {
    return $words
  }

}