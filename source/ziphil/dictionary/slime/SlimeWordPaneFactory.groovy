package ziphil.dictionary.slime

import groovy.transform.CompileStatic
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import ziphil.custom.Measurement
import ziphil.dictionary.PaneFactoryBase
import ziphil.dictionary.SearchParameter
import ziphil.module.Setting
import ziphil.module.Strings
import ziphilib.transform.VoidClosure
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class SlimeWordPaneFactory extends PaneFactoryBase<SlimeWord, SlimeDictionary> {

  private static final String SLIME_HEAD_NAME_CLASS = "slime-head-name"
  private static final String SLIME_PRONUNCIATION_CLASS = "slime-pronunciation"
  private static final String SLIME_TAG_CLASS = "slime-tag"
  private static final String SLIME_EQUIVALENT_CLASS = "slime-equivalent"
  private static final String SLIME_EQUIVALENT_TITLE_CLASS = "slime-equivalent-title"
  private static final String SLIME_RELATION_TITLE_CLASS = "slime-relation-title"
  private static final String SLIME_TITLE_CLASS = "slime-title"
  private static final String SLIME_LINK_CLASS = "slime-link"

  public SlimeWordPaneFactory(SlimeWord word, SlimeDictionary dictionary, Boolean persisted) {
    super(word, dictionary, persisted)
  }

  public SlimeWordPaneFactory(SlimeWord word, SlimeDictionary dictionary) {
    super(word, dictionary)
  }

  protected Pane doCreate() {
    Int lineSpacing = Setting.getInstance().getLineSpacing()
    TextFlow pane = TextFlow.new()
    Boolean hasInformation = false
    Boolean hasRelation = false
    pane.getStyleClass().add(CONTENT_PANE_CLASS)
    pane.setLineSpacing(lineSpacing)
    addNameNode(pane, $word.getName(), $word.createPronunciation())
    addTagNode(pane, $word.getTags())
    for (SlimeEquivalent equivalent : $word.getRawEquivalents()) {
      addEquivalentNode(pane, equivalent.getTitle(), equivalent.getNames())
    }
    for (SlimeInformation information : $word.sortedInformations()) {
      addInformationNode(pane, information.getTitle(), information.getText())
      hasInformation = true
    }
    for (Map.Entry<String, List<SlimeRelation>> entry : $word.groupedRelations()) {
      String title = entry.getKey()
      List<SlimeRelation> relationGroup = entry.getValue()
      List<IntegerClass> ids = relationGroup.collect{it.getId()}
      List<String> names = relationGroup.collect{it.getName()}
      addRelationNode(pane, title, ids, names)
      hasRelation = true
    }
    modifyBreak(pane)
    return pane
  }

  private void addNameNode(TextFlow pane, String name, String pronunciation) {
    if (pronunciation != "") {
      Text nameText = Text.new(name + " ")
      Text pronunciationText = Text.new(pronunciation)
      Text spaceText = Text.new("  ")
      nameText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SLIME_HEAD_NAME_CLASS)
      pronunciationText.getStyleClass().addAll(CONTENT_CLASS, SLIME_PRONUNCIATION_CLASS)
      spaceText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SLIME_HEAD_NAME_CLASS)
      pane.getChildren().addAll(nameText, pronunciationText, spaceText)
    } else {
      Text nameText = Text.new(name + "  ")
      nameText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SLIME_HEAD_NAME_CLASS)
      pane.getChildren().add(nameText)
    }
  }

  private void addTagNode(TextFlow pane, List<String> tags) {
    for (String tag : tags) {
      Label tagText = Label.new(tag)
      Text spaceText = Text.new(" ")
      tagText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TAG_CLASS)
      spaceText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
      pane.getChildren().addAll(tagText, spaceText)
    }
    Text breakText = Text.new("\n")
    pane.getChildren().add(breakText)
  }

  private void addEquivalentNode(TextFlow pane, String title, List<String> equivalents) {
    Label titleText = Label.new(title)
    Text spaceText = Text.new(" ")
    Text equivalentText = Text.new(equivalents.join($dictionary.firstPunctuation()))
    Text breakText = Text.new("\n")
    titleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_EQUIVALENT_TITLE_CLASS)
    equivalentText.getStyleClass().addAll(CONTENT_CLASS, SLIME_EQUIVALENT_CLASS)
    if (title != "") {
      pane.getChildren().addAll(titleText, spaceText, equivalentText, breakText)
    } else {
      pane.getChildren().addAll(equivalentText, breakText)
    }
  }

  private void addInformationNode(TextFlow pane, String title, String information) {
    Boolean modifiesPunctuation = Setting.getInstance().getModifiesPunctuation()
    String modifiedInformation = (modifiesPunctuation) ? Strings.modifyPunctuation(information) : information
    Boolean insertsBreak = !$dictionary.getPlainInformationTitles().contains(title)
    Text titleText = Text.new("【${title}】")
    Text innerBreakText = Text.new((insertsBreak) ? " \n" : " ")
    Text informationText = Text.new(modifiedInformation)
    Text breakText = Text.new("\n")
    titleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
    innerBreakText.getStyleClass().add(CONTENT_CLASS)
    informationText.getStyleClass().add(CONTENT_CLASS)
    pane.getChildren().addAll(titleText, innerBreakText, informationText, breakText)
  }

  private void addRelationNode(TextFlow pane, String title, List<IntegerClass> ids, List<String> names) {
    Text formerTitleText = Text.new("cf:")
    Label titleText = Label.new(title)
    Text spaceText = Text.new(" ")
    formerTitleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
    titleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_RELATION_TITLE_CLASS)
    spaceText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
    if (title != "") {
      pane.getChildren().addAll(formerTitleText, titleText, spaceText)
    } else {
      pane.getChildren().addAll(formerTitleText, spaceText)
    }
    for (Int i = 0 ; i < names.size() ; i ++) {
      Int id = ids[i]
      String name = names[i]
      Text nameText = Text.new(name)
      nameText.addEventHandler(MouseEvent.MOUSE_CLICKED, createLinkEventHandler(id))
      nameText.getStyleClass().addAll(CONTENT_CLASS, SLIME_LINK_CLASS)
      pane.getChildren().add(nameText)
      if (i < names.size() - 1) {
        Text punctuationText = Text.new(", ")
        punctuationText.getStyleClass().add(CONTENT_CLASS)
        pane.getChildren().add(punctuationText)
      }      
    }
    Text breakText = Text.new("\n")
    pane.getChildren().add(breakText)
  }

  private EventHandler<MouseEvent> createLinkEventHandler(Int id) {
    EventHandler<MouseEvent> handler = { MouseEvent event ->
      if ($dictionary.getOnLinkClicked() != null) {
        if ($linkClickType != null && $linkClickType.matches(event)) {
          SearchParameter parameter = SlimeSearchParameter.new(id)
          $dictionary.getOnLinkClicked().accept(parameter)
        }
      }
    }
    return handler
  }

}