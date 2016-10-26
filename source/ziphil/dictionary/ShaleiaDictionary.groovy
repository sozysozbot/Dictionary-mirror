package ziphil.dictionary

import groovy.transform.CompileStatic
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Consumer
import java.util.regex.Matcher


@CompileStatic @Newify
public class ShaleiaDictionary extends Dictionary<ShaleiaWord, Suggestion> {

  private String $alphabetOrder = "sztdkgfvpbcqxjrlmnhyaâáàeêéèiîíìoôòuûù"
  private Consumer<String> $onLinkClicked

  public ShaleiaDictionary(String name, String path) {
    super(name, path)
    load()
    setupWords()
  }

  public void searchDetail(ShaleiaSearchParameter parameter) {
    String searchName = parameter.getName()
    SearchType nameSearchType = parameter.getNameSearchType()
    $filteredWords.setPredicate() { ShaleiaWord word ->
      String name = word.getName()
      Boolean predicate = SearchType.matches(nameSearchType, name, searchName)
      return predicate
    }
    $filteredSuggestions.setPredicate() { Suggestion suggestion ->
      return false
    }
  }

  public void modifyWord(ShaleiaWord oldWord, ShaleiaWord newWord) {
    newWord.createComparisonString($alphabetOrder)
    newWord.createContentPane()
  }

  public void addWord(ShaleiaWord word) {
    word.setDictionary(this)
    word.createComparisonString($alphabetOrder)
    $words.add(word)
  }

  public void removeWord(ShaleiaWord word) {
    $words.remove(word)
  }

  public ShaleiaWord emptyWord() {
    Long hairiaNumber = LocalDateTime.of(2012, 1, 23, 6, 0).until(LocalDateTime.now(), ChronoUnit.DAYS) + 1
    String data = "+ ${hairiaNumber} 〈不〉\n\n=〈〉"
    return ShaleiaWord.new("", data)
  }

  public ShaleiaWord copiedWord(ShaleiaWord oldWord) {
    String name = oldWord.getName()
    String data = oldWord.getData()
    ShaleiaWord newWord = ShaleiaWord.new(name, data)
    return newWord
  }

  public ShaleiaWord inheritedWord(ShaleiaWord oldWord) {
    return copiedWord(oldWord)
  }

  private void load() {
    if ($path != null) {
      File file = File.new($path)
      String currentName = null
      StringBuilder currentData = StringBuilder.new()
      file.eachLine() { String line ->
        Matcher matcher = line =~ /^\*\s*(.+)\s*$/
        if (matcher.matches()) {
          if (currentName != null) {
            ShaleiaWord word = ShaleiaWord.new(currentName, currentData.toString())
            word.setDictionary(this)
            word.createComparisonString($alphabetOrder)
            $words.add(word)
          }
          currentName = matcher.group(1)
          currentData.setLength(0)
        } else {
          currentData.append(line)
          currentData.append("\n")
        }
      }
      if (currentName != null) {
        ShaleiaWord word = ShaleiaWord.new(currentName, currentData.toString())
        word.setDictionary(this)
        word.createComparisonString($alphabetOrder)
        $words.add(word)
      }
    }
  }

  public void save() {
    File file = File.new($path)
    StringBuilder output = StringBuilder.new()
    $words.each() { ShaleiaWord word ->
      output.append("* " + word.getUniqueName())
      output.append("\n")
      output.append(word.getData().trim())
      output.append("\n\n")
    }
    file.setText(output.toString(), "UTF-8")
  }

  private void setupWords() {
    $sortedWords.setComparator() { ShaleiaWord firstWord, ShaleiaWord secondWord ->
      String firstString = firstWord.getComparisonString()
      String secondString = secondWord.getComparisonString()
      return firstString <=> secondString
    }
  }

  public Consumer<String> getOnLinkClicked() {
    return $onLinkClicked
  }

  public void setOnLinkClicked(Consumer<String> onLinkClicked) {
    $onLinkClicked = onLinkClicked
  }

}