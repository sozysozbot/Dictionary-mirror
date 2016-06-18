package ziphil.controller

import groovy.transform.CompileStatic
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.StageStyle
import ziphil.dictionary.Dictionary
import ziphil.dictionary.DictionaryType
import ziphil.dictionary.ShaleiaDictionary
import ziphil.dictionary.PersonalDictionary
import ziphil.custom.CustomBuilderFactory
import ziphil.custom.Measurement
import ziphil.module.DictionarySetting
import ziphil.module.Setting
import ziphil.node.DictionaryTableModel
import ziphil.node.UtilityStage


@CompileStatic @Newify
public class DictionaryTableController {

  private static final String RESOURCE_PATH = "resource/fxml/dictionary_table.fxml"
  private static final String TITLE = "登録辞書一覧"
  private static final Double DEFAULT_WIDTH = Measurement.rpx(640)
  private static final Double DEFAULT_HEIGHT = Measurement.rpx(320)

  @FXML private TableView<DictionaryTableModel> $table
  private ObservableList<DictionaryTableModel> $dictionaries = FXCollections.observableArrayList()
  private UtilityStage<Dictionary> $stage
  private Scene $scene

  public DictionaryTableController(UtilityStage<Dictionary> stage) {
    $stage = stage
    loadResource()
  }

  @FXML
  public void initialize() {
    createModels()
    setupTable()
  }

  @FXML
  private void loadNewDictionary() {
    UtilityStage<Boolean> stage = UtilityStage.new(StageStyle.UTILITY)
    DictionaryLoaderController controller = DictionaryLoaderController.new(stage)
    stage.initModality(Modality.WINDOW_MODAL)
    stage.initOwner($stage)
    Boolean isDone = stage.showAndWaitResult()
    if (isDone != null && isDone) {
      createModels()
    }
  }

  @FXML
  private void commitShow() {
    DictionaryTableModel selectedModel = $table.getSelectionModel().getSelectedItem()
    if (selectedModel != null) {
      DictionaryType type = selectedModel.getType()
      Dictionary dictionary
      if (type == DictionaryType.SHALEIA) {
        dictionary = ShaleiaDictionary.new(selectedModel.getName(), selectedModel.getPath())
      } else if (type == DictionaryType.PERSONAL) {
        dictionary = PersonalDictionary.new(selectedModel.getName(), selectedModel.getPath())
      }
      $stage.close(dictionary)
    } else {
      $stage.close()
    }
  }

  @FXML
  private void cancelShow() {
    $stage.close()
  }

  private void createModels() {
    $dictionaries.clear()
  }

  private void setupTable() {
    $table.setItems($dictionaries)
  }

  private void loadResource() {
    FXMLLoader loader = FXMLLoader.new(getClass().getClassLoader().getResource(RESOURCE_PATH), null, CustomBuilderFactory.new())
    loader.setController(this)
    Parent root = (Parent)loader.load()
    $scene = Scene.new(root, DEFAULT_WIDTH, DEFAULT_HEIGHT)
    $stage.setScene($scene)
    $stage.setTitle(TITLE)
    $stage.sizeToScene()
  }

}