package ziphil.dictionary

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import net.arnx.jsonic.JSONHint
import ziphil.custom.Measurement
import ziphil.module.Setting
import ziphil.module.Strings


@CompileStatic @Newify
public class SlimeWord extends Word {

  public static final String SLIME_HEAD_NAME_CLASS = "slime-head-name"
  public static final String SLIME_TAG_CLASS = "slime-tag"
  public static final String SLIME_EQUIVALENT_CLASS = "slime-equivalent"
  public static final String SLIME_EQUIVALENT_TITLE_CLASS = "slime-equivalent-title"
  public static final String SLIME_TITLE_CLASS = "slime-title"
  public static final String SLIME_LINK_CLASS = "slime-link"
  public static final String SLIME_ID_CLASS = "slime-id"

  private SlimeDictionary $dictionary
  private Integer $id = -1
  private List<SlimeEquivalent> $rawEquivalents = ArrayList.new()
  private List<String> $tags = ArrayList.new()
  private List<SlimeInformation> $informations = ArrayList.new()
  private List<SlimeVariation> $variations = ArrayList.new()
  private List<SlimeRelation> $relations = ArrayList.new()
  private VBox $simpleContentPane = VBox.new()
  private Boolean $isSimpleChanged = true

  public SlimeWord(Integer id, String name, List<SlimeEquivalent> rawEquivalents, List<String> tags, List<SlimeInformation> informations, List<SlimeVariation> variations,
                   List<SlimeRelation> relations) {
    update(id, name, rawEquivalents, tags, informations, variations, relations)
    setupContentPane()
  }

  public SlimeWord() {
    setupContentPane()
  }

  public void update(Integer id, String name, List<SlimeEquivalent> rawEquivalents, List<String> tags, List<SlimeInformation> informations, List<SlimeVariation> variations,
                     List<SlimeRelation> relations) {
    $id = id
    $name = name
    $rawEquivalents = rawEquivalents
    $equivalents = (List)rawEquivalents.inject([]) { List<String> result, SlimeEquivalent equivalent ->
      result.addAll(equivalent.getNames())
      return result
    }
    $tags = tags
    $informations = informations
    $variations = variations
    $relations = relations
    $isChanged = true
  }

  public void createContentPane() {
    HBox headBox = HBox.new()
    VBox equivalentBox = VBox.new()
    VBox informationBox = VBox.new()
    VBox relationBox = VBox.new()
    Boolean hasInformation = false
    Boolean hasRelation = false
    Boolean modifiesPunctuation = Setting.getInstance().getModifiesPunctuation() ?: false
    $contentPane.getChildren().clear()
    $contentPane.getChildren().addAll(headBox, equivalentBox, informationBox, relationBox)
    addNameNode(headBox, $name)
    addTagNode(headBox, $tags)
    $rawEquivalents.each() { SlimeEquivalent equivalent ->
      String equivalentString = equivalent.getNames().join(", ")
      addEquivalentNode(equivalentBox, equivalent.getTitle(), equivalentString)
    }
    $informations.each() { SlimeInformation information ->
      addInformationNode(informationBox, information.getTitle(), information.getText(), modifiesPunctuation)
      hasInformation = true
    }
    $relations.groupBy{relation -> relation.getTitle()}.each() { String title, List<SlimeRelation> relationGroup ->
      List<Integer> ids = relationGroup.collect{relation -> relation.getId()}
      List<String> names = relationGroup.collect{relation -> relation.getName()}
      addRelationNode(relationBox, title, ids, names)
      hasRelation = true
    }
    if (hasInformation) {
      $contentPane.setMargin(equivalentBox, Insets.new(0, 0, Measurement.rpx(3), 0))
    }
    if (hasRelation) {
      $contentPane.setMargin(informationBox, Insets.new(0, 0, Measurement.rpx(3), 0))
    }
    $isChanged = false
  }

  public void createSimpleContentPane() {
    HBox headBox = HBox.new()
    HBox equivalentBox = HBox.new()
    $simpleContentPane.getChildren().clear()
    $simpleContentPane.getChildren().addAll(headBox, equivalentBox)
    Text nameText = Text.new($name + " ")
    Text idText = Text.new("#${$id}")
    Text equivalentText = Text.new($equivalents.join(", "))
    nameText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SLIME_HEAD_NAME_CLASS)
    idText.getStyleClass().addAll(CONTENT_CLASS, SLIME_ID_CLASS)
    equivalentText.getStyleClass().addAll(CONTENT_CLASS, SLIME_EQUIVALENT_CLASS)
    headBox.getChildren().addAll(nameText, idText)
    equivalentBox.getChildren().add(equivalentText)
    $isSimpleChanged = false
  }

  private void addNameNode(HBox box, String name) {
    Text nameText = Text.new(name + "  ")
    nameText.getStyleClass().addAll(CONTENT_CLASS, HEAD_NAME_CLASS, SLIME_HEAD_NAME_CLASS)
    box.getChildren().add(nameText)
    box.setAlignment(Pos.CENTER_LEFT)
  }

  private void addTagNode(HBox box, List<String> tags) {
    tags.each() { String tag ->
      Label tagText = Label.new(tag)
      Text spaceText = Text.new(" ")
      tagText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TAG_CLASS)
      spaceText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
      box.getChildren().addAll(tagText, spaceText)
    }
  }

  private void addEquivalentNode(VBox box, String title, String equivalent) {
    TextFlow textFlow = TextFlow.new()
    Label titleText = Label.new(title)
    Text equivalentText = Text.new(" " + equivalent)
    titleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_EQUIVALENT_TITLE_CLASS)
    equivalentText.getStyleClass().addAll(CONTENT_CLASS, SLIME_EQUIVALENT_CLASS)
    textFlow.getChildren().addAll(titleText, equivalentText)
    box.getChildren().add(textFlow)
  }

  private void addInformationNode(VBox box, String title, String information, Boolean modifiesPunctuation) {
    String newInformation = (modifiesPunctuation) ? Strings.modifyPunctuation(information) : information
    TextFlow titleTextFlow = TextFlow.new()
    TextFlow textFlow = TextFlow.new()
    Text titleText = Text.new("【${title}】")
    Text dammyText = Text.new(" ")
    Text informationText = Text.new(newInformation)
    titleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
    informationText.getStyleClass().add(CONTENT_CLASS)
    titleTextFlow.getChildren().addAll(titleText, dammyText)
    textFlow.getChildren().add(informationText)
    box.getChildren().addAll(titleTextFlow, textFlow)
  }

  private void addRelationNode(VBox box, String title, List<Integer> ids, List<String> names) {
    TextFlow textFlow = TextFlow.new()
    Text formerTitleText = Text.new("cf:")
    Text titleText = Text.new("〈${title}〉" + " ")
    formerTitleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
    titleText.getStyleClass().addAll(CONTENT_CLASS, SLIME_TITLE_CLASS)
    textFlow.getChildren().addAll(formerTitleText, titleText)
    (0 ..< names.size()).each() { Integer i ->
      Integer id = ids[i]
      String name = names[i]
      Text nameText = Text.new(name)
      nameText.getStyleClass().addAll(CONTENT_CLASS, SLIME_LINK_CLASS)
      nameText.setOnMouseClicked() {
        if ($dictionary.getOnLinkClicked() != null) {
          $dictionary.getOnLinkClicked().accept(id)
        }
      }
      textFlow.getChildren().add(nameText)
      if (i < names.size() - 1) {
        Text punctuationText = Text.new(", ")
        punctuationText.getStyleClass().add(CONTENT_CLASS)
        textFlow.getChildren().add(punctuationText)
      }      
    }
    box.getChildren().add(textFlow)
  }

  public List<Integer> listForComparison(String order) {
    List<String> splittedString = $name.split("").toList()
    List<Integer> convertedString = splittedString.collect{character -> order.indexOf(character)}
    return convertedString
  }

  private void setupContentPane() {
    $contentPane.getStyleClass().add(CONTENT_PANE_CLASS)
  }

  public Boolean isSimpleChanged() {
    return $isSimpleChanged
  }

  public SlimeDictionary getDictionary() {
    return $dictionary
  }

  public void setDictionary(SlimeDictionary dictionary) {
    $dictionary = dictionary
  }

  public Integer getId() {
    return $id
  }

  public void setId(Integer id) {
    $id = id
  }

  @JSONHint(name="translations")
  public List<SlimeEquivalent> getRawEquivalents() {
    return $rawEquivalents
  }

  @JSONHint(name="translations")
  public void setRawEquivalents(List<SlimeEquivalent> rawEquivalents) {
    $rawEquivalents = rawEquivalents
    $equivalents = (List)rawEquivalents.inject([]) { List<String> result, SlimeEquivalent equivalent ->
      result.addAll(equivalent.getNames())
      return result
    }
  }

  public List<String> getTags() {
    return $tags
  }

  public void setTags(List<String> tags) {
    $tags = tags
  }

  @JSONHint(name="contents")
  public List<SlimeInformation> getInformations() {
    return $informations
  }

  @JSONHint(name="contents")
  public void setInformations(List<SlimeInformation> informations) {
    $informations = informations
  }

  public List<SlimeVariation> getVariations() {
    return $variations
  }

  public void setVariations(List<SlimeVariation> variations) {
    $variations = variations
  }

  public List<SlimeRelation> getRelations() {
    return $relations
  }

  public void setRelations(List<SlimeRelation> relations) {
    $relations = relations
  }

  public Map<String, Object> getEntry() {
    return [("id"): (Object)$id, ("form"): (Object)$name]
  }

  public void setEntry(Map<String, Object> entry) {
    $id = (Integer)entry["id"]
    $name = (String)entry["form"]
  }

  public Pane getSimpleContentPane() {
    return $simpleContentPane
  }

}