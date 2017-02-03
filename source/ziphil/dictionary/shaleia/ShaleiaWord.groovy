package ziphil.dictionary.shaleia

import groovy.transform.CompileStatic
import java.util.regex.Matcher
import ziphil.dictionary.WordBase
import ziphil.module.Setting
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class ShaleiaWord extends WordBase {

  private String $uniqueName = ""
  private String $data = ""
  private String $comparisonString = ""
  private ShaleiaDictionary $dictionary

  public void update() {
    updateName()
    updateEquivalents()
    updateContent()
    changeContentPaneFactory()
  }

  private void updateName() {
    $name = ($uniqueName.startsWith("\$")) ? "" : $uniqueName.replaceAll(/\+|~/, "")
  }

  private void updateEquivalents() {
    BufferedReader reader = BufferedReader.new(StringReader.new($data))
    try {
      $equivalents.clear()
      for (String line ; (line = reader.readLine()) != null ;) {
        Matcher matcher = line =~ /^\=(?:\:)?\s*(?:〈(.+)〉)?\s*(.+)$/
        if (matcher.matches()) {
          String equivalent = matcher.group(2)
          List<String> equivalents = equivalent.replaceAll(/(\(.+\)|\{|\}|\/|\s)/, "").split(/,/).toList()
          $equivalents.addAll(equivalents)
        }
      }
    } finally {
      reader.close()
    }
  }

  private void updateContent() {
    $content = $uniqueName + "\n" + $data
  }

  public void updateComparisonString(String order) {
    Boolean isApostropheCharacter = order.contains("'")
    StringBuilder comparisonString = StringBuilder.new()
    for (Integer i : 0 ..< $uniqueName.length()) {
      String character = $uniqueName[i]
      if ((isApostropheCharacter || character != "'") && character != "+" && character != "~" && character != "-") {
        Integer position = order.indexOf($uniqueName.codePointAt(i))
        if (position > -1) {
          comparisonString.appendCodePoint(position + 174)
        } else {
          comparisonString.appendCodePoint(10000)
        }
      }
    }
    $comparisonString = comparisonString.toString()
  }

  protected void makeContentPaneFactory() {
    Setting setting = Setting.getInstance()
    Integer lineSpacing = setting.getLineSpacing()
    Boolean modifiesPunctuation = setting.getModifiesPunctuation()
    $contentPaneFactory = ShaleiaWordContentPaneFactory.new(this, $dictionary)
    $contentPaneFactory.setLineSpacing(lineSpacing)
    $contentPaneFactory.setModifiesPunctuation(modifiesPunctuation)
  }

  public Boolean isDisplayed() {
    return !$uniqueName.startsWith("\$")
  }

  public String getUniqueName() {
    return $uniqueName
  }

  public void setUniqueName(String uniqueName) {
    $uniqueName = uniqueName
  }

  public String getData() {
    return $data
  }

  public void setData(String data) {
    $data = data
  }

  public ShaleiaDictionary getDictionary() {
    return $dictionary
  }

  public void setDictionary(ShaleiaDictionary dictionary) {
    $dictionary = dictionary
  }

  public String getComparisonString() {
    return $comparisonString
  }

}