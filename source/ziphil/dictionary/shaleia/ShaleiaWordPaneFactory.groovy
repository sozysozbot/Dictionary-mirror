package ziphil.dictionary.shaleia

import groovy.transform.CompileStatic
import java.util.regex.Matcher
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import ziphil.custom.Measurement
import ziphil.dictionary.NormalSearchParameter
import ziphil.dictionary.PaneFactoryBase
import ziphil.dictionary.SearchMode
import ziphil.dictionary.SearchParameter
import ziphil.module.Setting
import ziphil.module.Strings
import ziphilib.transform.InnerClass
import ziphilib.transform.VoidClosure
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class ShaleiaWordPaneFactory extends PaneFactoryBase<ShaleiaWord, ShaleiaDictionary> {

  private static final String SHALEIA_HEAD_NAME_CLASS = "shaleia-head-name"
  private static final String SHALEIA_PRONUNCIATION_CLASS = "shaleia-pronunciation"
  private static final String SHALEIA_EQUIVALENT_CLASS = "shaleia-equivalent"
  private static final String SHALEIA_TOTAL_PART_CLASS = "shaleia-total-part"
  private static final String SHALEIA_PART_CLASS = "shaleia-part"
  private static final String SHALEIA_CREATION_DATE_CLASS = "shaleia-creation-date"
  private static final String SHALEIA_TITLE_CLASS = "shaleia-title"
  private static final String SHALEIA_NAME_CLASS = "shaleia-name"
  private static final String SHALEIA_LINK_CLASS = "shaleia-link"
  private static final String SHALEIA_ITALIC_CLASS = "shaleia-italic"
  private static final String START_NAME_CHARACTER = "["
  private static final String END_NAME_CHARACTER = "]"
  private static final String START_LINK_CHARACTER = "{"
  private static final String END_LINK_CHARACTER = "}"
  private static final String START_ITALIC_CHARACTER = "/"
  private static final String END_ITALIC_CHARACTER = "/"
  private static final String START_ESCAPE_CHARACTER = "&"
  private static final String END_ESCAPE_CHARACTER = ";"
  private static final String PUNCTUATIONS = " .,?!-"

  public ShaleiaWordPaneFactory(ShaleiaWord word, ShaleiaDictionary dictionary, Boolean persisted) {
    super(word, dictionary, persisted)
  }

  public ShaleiaWordPaneFactory(ShaleiaWord word, ShaleiaDictionary dictionary) {
    super(word, dictionary)
  }

  protected Pane doCreate() {
    Int lineSpacing = Setting.getInstance().getLineSpacing()
    TextFlow pane = TextFlow.new()
    Boolean hasContent = false
    Boolean hasSynonym = false
    pane.getStyleClass().add(CONTENT_PANE_CLASS)
    pane.setLineSpacing(lineSpacing)
    ShaleiaDescriptionReader reader = ShaleiaDescriptionReader.new($word.getDescription())
    try {
      while (reader.readLine() != null) {
        if (pane.getChildren().isEmpty()) {
          String name = ($word.getUniqueName().startsWith("\$")) ? "" : $word.getName()
          addNameNode(pane, name, $word.createPronunciation())
        }
        if (reader.findCreationDate()) {
          String totalPart = reader.lookupTotalPart()
          String creationDate = reader.lookupCreationDate()
          addCreationDateNode(pane, totalPart, creationDate)
        }
        if (reader.findEquivalent()) {
          String part = reader.lookupPart()
          String equivalent = reader.lookupEquivalent()
          addEquivalentNode(pane, part, equivalent)
        }
        if (reader.findContent()) {
          String title = reader.title()
          String content = reader.lookupContent()
          addContentNode(pane, title, content)
          hasContent = true
        }
        if (reader.findSynonym()) {
          String synonym = reader.lookupSynonym()
          addSynonymNode(pane, synonym)
          hasSynonym = true
        }
      }
      modifyBreak(pane)
    } finally {
      reader.close()
    }
    return pane
  }

  private void addNameNode(TextFlow pane, String name, String pronunciation) {
    if (pronunciation != "") {
      Text nameText = Text.new(name + " ")
      Text pronunciationText = Text.new(pronunciation)
      Text spaceText = Text.new("  ")
      nameText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SHALEIA_HEAD_NAME_CLASS)
      pronunciationText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_PRONUNCIATION_CLASS)
      spaceText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SHALEIA_HEAD_NAME_CLASS)
      pane.getChildren().addAll(nameText, pronunciationText, spaceText)
    } else {
      Text nameText = Text.new(name + "  ")
      nameText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SHALEIA_HEAD_NAME_CLASS)
      pane.getChildren().add(nameText)
    }
  }
 
  private void addCreationDateNode(TextFlow pane, String totalPart, String creationDate) {
    Label totalPartText = Label.new(totalPart)
    Text creationDateText = Text.new(" " + creationDate)
    Text breakText = Text.new("\n")
    totalPartText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_TOTAL_PART_CLASS)
    creationDateText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_CREATION_DATE_CLASS)
    pane.getChildren().addAll(totalPartText, creationDateText, breakText)
  }

  private void addEquivalentNode(TextFlow pane, String part, String equivalent) {
    Label partText = Label.new(part)
    Text breakText = Text.new("\n")
    List<Text> equivalentTexts = createRichTexts(" " + equivalent)
    partText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_PART_CLASS)
    for (Text equivalentText : equivalentTexts) {
      equivalentText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_EQUIVALENT_CLASS)
    }
    pane.getChildren().add(partText)
    pane.getChildren().addAll(equivalentTexts)
    pane.getChildren().add(breakText)
  }

  private void addContentNode(TextFlow pane, String title, String content) {
    Boolean modifiesPunctuation = Setting.getInstance().getModifiesPunctuation()
    String modifiedContent = (modifiesPunctuation) ? Strings.modifyPunctuation(content) : content
    Text titleText = Text.new("【${title}】")
    Text dammyText = Text.new(" \n")
    Text breakText = Text.new("\n")
    List<Text> contentTexts = createRichTexts(modifiedContent)
    titleText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_TITLE_CLASS)
    dammyText.getStyleClass().add(CONTENT_CLASS)
    for (Text contentText : contentTexts) {
      contentText.getStyleClass().add(CONTENT_CLASS)
    }
    pane.getChildren().addAll(titleText, dammyText)
    pane.getChildren().addAll(contentTexts)
    pane.getChildren().add(breakText)
  }

  private void addSynonymNode(TextFlow pane, String synonym) {
    TextFlow textFlow = TextFlow.new()
    Text titleText = Text.new("cf:")
    Text breakText = Text.new("\n")
    List<Text> synonymTexts = createRichTexts(" " + synonym, true)
    titleText.getStyleClass().addAll(CONTENT_CLASS, SHALEIA_TITLE_CLASS)
    for (Text synonymText : synonymTexts) {
      synonymText.getStyleClass().add(CONTENT_CLASS)
    }
    pane.getChildren().add(titleText)
    pane.getChildren().addAll(synonymTexts)
    pane.getChildren().add(breakText)
  }

  private List<Text> createRichTexts(String string, Boolean decoratesLink) {
    List<Text> texts = ArrayList.new()
    List<Text> unnamedTexts = ArrayList.new()
    StringBuilder currentString = StringBuilder.new()
    StringBuilder currentEscapeString = StringBuilder.new()
    StringBuilder currentName = StringBuilder.new()
    TextMode currentMode = TextMode.NORMAL
    for (String character : string) {
      if (currentMode == TextMode.NORMAL && character == START_LINK_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }
        currentMode = TextMode.LINK
      } else if ((currentMode == TextMode.LINK || currentMode == TextMode.LINK_ITALIC) && character == END_LINK_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().add(SHALEIA_NAME_CLASS)
          if (decoratesLink) {
            text.getStyleClass().add(SHALEIA_LINK_CLASS)
          }
          unnamedTexts.add(text)
          texts.add(text)
          currentString.setLength(0)
        }
        if (currentName.length() > 0) {
          String name = currentName.toString()
          for (Text unnamedText : unnamedTexts) {
            unnamedText.addEventHandler(MouseEvent.MOUSE_CLICKED, createLinkEventHandler(name))
          }
          currentName.setLength(0)
          unnamedTexts.clear()
        }
        currentMode = TextMode.NORMAL
      } else if ((currentMode == TextMode.LINK || currentMode == TextMode.LINK_ITALIC) && PUNCTUATIONS.indexOf(character) >= 0) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().add(SHALEIA_NAME_CLASS)
          if (decoratesLink) {
            text.getStyleClass().add(SHALEIA_LINK_CLASS)
          }
          unnamedTexts.add(text)
          texts.add(text)
          currentString.setLength(0)
        }
        if (currentName.length() > 0) {
          String name = currentName.toString()
          for (Text unnamedText : unnamedTexts) {
            unnamedText.addEventHandler(MouseEvent.MOUSE_CLICKED, createLinkEventHandler(name))
          }
          currentName.setLength(0)
          unnamedTexts.clear()
        }
        Text characterText = Text.new(character)
        characterText.getStyleClass().add(SHALEIA_NAME_CLASS)
        texts.add(characterText)    
      } else if (currentMode == TextMode.LINK && character == START_ITALIC_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().add(SHALEIA_NAME_CLASS)
          if (decoratesLink) {
            text.getStyleClass().add(SHALEIA_LINK_CLASS)
          }
          unnamedTexts.add(text)
          texts.add(text)
          currentString.setLength(0)
        }
        currentMode = TextMode.LINK_ITALIC
      } else if (currentMode == TextMode.LINK_ITALIC && character == END_ITALIC_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().addAll(SHALEIA_NAME_CLASS, SHALEIA_ITALIC_CLASS)
          if (decoratesLink) {
            text.getStyleClass().add(SHALEIA_LINK_CLASS)
          }
          unnamedTexts.add(text)
          texts.add(text)
          currentString.setLength(0)
        }
        currentMode = TextMode.LINK
      } else if (currentMode == TextMode.NORMAL && character == START_NAME_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }      
        currentMode = TextMode.NAME
      } else if ((currentMode == TextMode.NAME || currentName == TextMode.NAME_ITALIC) && character == END_NAME_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().add(SHALEIA_NAME_CLASS)
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }
        currentMode = TextMode.NORMAL
      } else if (currentMode == TextMode.NAME && character == START_ITALIC_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().add(SHALEIA_NAME_CLASS)
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }
        currentMode = TextMode.NAME_ITALIC
      } else if (currentMode == TextMode.NAME_ITALIC && character == END_ITALIC_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().addAll(SHALEIA_NAME_CLASS, SHALEIA_ITALIC_CLASS)
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }      
        currentMode = TextMode.NAME
      } else if (currentMode == TextMode.NORMAL && character == START_ITALIC_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }
        currentMode = TextMode.NORMAL_ITALIC
      } else if (currentMode == TextMode.NORMAL_ITALIC && character == END_ITALIC_CHARACTER) {
        if (currentString.length() > 0) {
          Text text = Text.new(currentString.toString())
          text.getStyleClass().add(SHALEIA_ITALIC_CLASS)
          texts.add(text)
          currentString.setLength(0)
          currentName.setLength(0)
        }
        currentMode = TextMode.NORMAL
      } else {
        if (character == START_ESCAPE_CHARACTER || currentEscapeString.length() > 0) {
          currentEscapeString.append(character)
          if (character == END_ESCAPE_CHARACTER && currentEscapeString.length() > 0) {
            Matcher matcher = currentEscapeString.toString() =~ /^&#x([0-9A-Fa-f]+);$/
            if (matcher.matches()) {
              String convertedEscapeString = CharacterClass.toChars(IntegerClass.parseInt(matcher.group(1), 16))[0]
              currentString.append(convertedEscapeString)
              currentName.append(convertedEscapeString)
              currentEscapeString.setLength(0)
            }
          }
        } else {
          currentString.append(character)
          currentName.append(character)
        }
      }
    }
    Text text = Text.new(currentString.toString())
    texts.add(text)
    return texts
  }

  private List<Text> createRichTexts(String string) {
    return createRichTexts(string, false)
  }

  private EventHandler<MouseEvent> createLinkEventHandler(String name) {
    EventHandler<MouseEvent> handler = { MouseEvent event ->
      if ($dictionary.getOnLinkClicked() != null) {
        if ($linkClickType != null && $linkClickType.matches(event)) {
          SearchParameter parameter = NormalSearchParameter.new(name, SearchMode.NAME, true, true)
          $dictionary.getOnLinkClicked().accept(parameter)
        }
      }
    }
    return handler
  }


  @InnerClass @Ziphilify
  private static enum TextMode {

    NORMAL,
    NORMAL_ITALIC,
    NAME,
    NAME_ITALIC,
    LINK,
    LINK_ITALIC

  }

}