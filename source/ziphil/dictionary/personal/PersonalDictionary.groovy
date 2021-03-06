package ziphil.dictionary.personal

import groovy.transform.CompileStatic
import javafx.concurrent.Task
import ziphil.dictionary.ConjugationResolver
import ziphil.dictionary.Dictionary
import ziphil.dictionary.DictionaryBase
import ziphil.dictionary.DictionaryConverter
import ziphil.dictionary.DictionaryLoader
import ziphil.dictionary.DictionarySaver
import ziphil.dictionary.EditableDictionary
import ziphil.dictionary.EmptyConjugationResolver
import ziphil.dictionary.EmptyDictionaryConverter
import ziphil.dictionary.IdentityDictionaryConverter
import ziphil.dictionary.NormalSearchParameter
import ziphil.dictionary.PseudoWord
import ziphil.dictionary.Suggestion
import ziphil.dictionary.shaleia.ShaleiaDictionary
import ziphil.dictionary.slime.SlimeDictionary
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class PersonalDictionary extends DictionaryBase<PersonalWord, Suggestion> implements EditableDictionary<PersonalWord, PersonalWord> {

  public PersonalDictionary(String name, String path) {
    super(name, path)
  }

  public PersonalDictionary(String name, String path, Dictionary oldDictionary) {
    super(name, path, oldDictionary)
  }

  protected void prepare() {
    setupWords()
  }

  public void modifyWord(PersonalWord oldWord, PersonalWord newWord) {
    $changed = true
  }

  public void addWord(PersonalWord word) {
    $words.add(word)
    $changed = true
  }

  public void addWords(List<? extends PersonalWord> words) {
    $words.addAll(words)
    $changed = true
  }

  public void removeWord(PersonalWord word) {
    $words.remove(word)
    $changed = true
  }

  public void mergeWord(PersonalWord mergedWord, PersonalWord removedWord) {
    $words.remove(removedWord)
    $changed = true
  }

  public void update() {
    $changed = true
  }

  public void updateFirst() {
    $changed = true
  }

  public void updateMinimum() {
    $changed = true
  }

  public PersonalWord createWord(String defaultName) {
    PersonalWord word = PersonalWord.new()
    word.setName(defaultName ?: "")
    word.update()
    return word
  }

  public PersonalWord copyWord(PersonalWord oldWord) {
    PersonalWord newWord = PersonalWord.new()
    newWord.setName(oldWord.getName())
    newWord.setPronunciation(oldWord.getPronunciation())
    newWord.setTranslation(oldWord.getTranslation())
    newWord.setUsage(oldWord.getUsage())
    newWord.setLevel(oldWord.getLevel())
    newWord.setMemory(oldWord.getMemory())
    newWord.setModification(oldWord.getModification())
    newWord.setDictionary(this)
    newWord.update()
    return newWord
  }

  public PersonalWord inheritWord(PersonalWord oldWord) {
    return copyWord(oldWord)
  }

  public PersonalWord determineWord(String name, PseudoWord pseudoWord) {
    PersonalWord word = PersonalWord.new()
    List<String> pseudoEquivalents = pseudoWord.getEquivalents()
    String pseudoContent = pseudoWord.getContent()
    word.setName(name)
    word.setTranslation(pseudoEquivalents.join(", "))
    if (pseudoContent != null) {
      word.setUsage(pseudoContent)
    }
    word.setDictionary(this)
    word.update()
    return word
  }

  public Object createPlainWord(PersonalWord oldWord) {
    PersonalPlainWord newWord = PersonalPlainWord.new()
    newWord.setName(oldWord.getName())
    newWord.setPronunciation(oldWord.getPronunciation())
    newWord.setTranslation(oldWord.getTranslation())
    newWord.setUsage(oldWord.getUsage())
    newWord.setLevel(oldWord.getLevel())
    newWord.setMemory(oldWord.getMemory())
    newWord.setModification(oldWord.getModification())
    return newWord
  }

  public PersonalDictionary copy() {
    PersonalDictionary dictionary = PersonalDictionary.new($name, null)
    dictionary.setPath($path)
    dictionary.getRawWords().addAll($words)
    return dictionary
  }

  private void setupWords() {
    $sortedWords.setComparator() { PersonalWord firstWord, PersonalWord secondWord ->
      return firstWord.getName() <=> secondWord.getName()
    }
  }

  protected ConjugationResolver createConjugationResolver() {
    EmptyConjugationResolver conjugationResolver = EmptyConjugationResolver.new($suggestions)
    return conjugationResolver
  }

  protected DictionaryLoader createLoader() {
    PersonalDictionaryLoader loader = PersonalDictionaryLoader.new(this, $path)
    return loader
  }

  protected DictionaryConverter createConverter(Dictionary oldDictionary) {
    if (oldDictionary instanceof ShaleiaDictionary) {
      PersonalShaleiaDictionaryConverter converter = PersonalShaleiaDictionaryConverter.new(this, oldDictionary)
      return converter
    } else if (oldDictionary instanceof PersonalDictionary) {
      IdentityDictionaryConverter converter = IdentityDictionaryConverter.new(this, (PersonalDictionary)oldDictionary)
      return converter
    } else if (oldDictionary instanceof SlimeDictionary) {
      PersonalSlimeDictionaryConverter converter = PersonalSlimeDictionaryConverter.new(this, oldDictionary)
      return converter
    } else {
      EmptyDictionaryConverter converter = EmptyDictionaryConverter.new(this, oldDictionary)
      return converter
    }
  } 

  protected DictionarySaver createSaver() {
    PersonalDictionarySaver saver = PersonalDictionarySaver.new(this, $path)
    return saver
  }

}