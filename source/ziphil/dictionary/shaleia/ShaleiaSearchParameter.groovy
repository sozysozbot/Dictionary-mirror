package ziphil.dictionary.shaleia

import groovy.transform.CompileStatic
import ziphil.dictionary.DetailedSearchParameter
import ziphil.dictionary.Dictionary
import ziphil.dictionary.SearchType
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class ShaleiaSearchParameter implements DetailedSearchParameter<ShaleiaWord> {

  private String $name
  private SearchType $nameSearchType
  private String $equivalent
  private SearchType $equivalentSearchType
  private String $description
  private SearchType $descriptionSearchType
  private Boolean $hasName = false
  private Boolean $hasEquivalent = false
  private Boolean $hasDescription = false

  public ShaleiaSearchParameter(String name) {
    $name = name
    $hasName = true
  }

  public ShaleiaSearchParameter() {
  }

  public void prepare(Dictionary dictionary) {
  }

  public Boolean matches(ShaleiaWord word) {
    Boolean predicate = true
    String name = word.getName()
    List<String> equivalents = word.getEquivalents()
    String description = word.getDescription()
    if ($hasName) {
      if (!$nameSearchType.matches(name, $name)) {
        predicate = false
      }
    }
    if ($hasEquivalent) {
      Boolean equivalentPredicate = false
      for (String equivalent : equivalents) {
        if ($equivalentSearchType.matches(equivalent, $equivalent)) {
          equivalentPredicate = true
        }
      }
      if (!equivalentPredicate) {
        predicate = false
      }
    }
    if ($hasDescription) {
      if (!$descriptionSearchType.matches(description, $description)) {
        predicate = false
      }
    }
    return predicate
  }

  public String getName() {
    return $name
  }

  public void setName(String name) {
    $name = name
  }

  public SearchType getNameSearchType() {
    return $nameSearchType
  }

  public void setNameSearchType(SearchType nameSearchType) {
    $nameSearchType = nameSearchType
  }

  public String getEquivalent() {
    return $equivalent
  }

  public void setEquivalent(String equivalent) {
    $equivalent = equivalent
  }

  public SearchType getEquivalentSearchType() {
    return $equivalentSearchType
  }

  public void setEquivalentSearchType(SearchType equivalentSearchType) {
    $equivalentSearchType = equivalentSearchType
  }

  public String getDescription() {
    return $description
  }

  public void setDescription(String description) {
    $description = description
  }

  public SearchType getDescriptionSearchType() {
    return $descriptionSearchType
  }

  public void setDescriptionSearchType(SearchType descriptionSearchType) {
    $descriptionSearchType = descriptionSearchType
  }

  public Boolean hasName() {
    return $hasName
  }

  public void setHasName(Boolean hasName) {
    $hasName = hasName
  }

  public Boolean hasEquivalent() {
    return $hasEquivalent
  }

  public void setHasEquivalent(Boolean hasEquivalent) {
    $hasEquivalent = hasEquivalent
  }

  public Boolean hasDescription() {
    return $hasDescription
  }

  public void setHasDescription(Boolean hasDescription) {
    $hasDescription = hasDescription
  }

}