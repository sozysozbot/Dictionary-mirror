package ziphil.controller

import groovy.transform.CompileStatic
import java.util.concurrent.Callable
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.StringBinding
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import ziphil.custom.Measurement
import ziphil.custom.SimpleWordCell
import ziphil.custom.UtilityStage
import ziphil.dictionary.SlimeDictionary
import ziphil.dictionary.SlimeWord
import ziphil.module.Setting


@CompileStatic @Newify
public class SlimeWordChooserController extends Controller<SlimeWord> {

  private static final String RESOURCE_PATH = "resource/fxml/slime_word_chooser.fxml"
  private static final String TITLE = "単語選択"
  private static final Double DEFAULT_WIDTH = Measurement.rpx(480)
  private static final Double DEFAULT_HEIGHT = Measurement.rpx(320)

  @FXML private ListView<SlimeWord> $wordList
  @FXML private TextField $searchText
  @FXML private ComboBox<String> $searchMode
  @FXML private ToggleButton $searchType
  private SlimeDictionary $dictionary

  public SlimeWordChooserController(UtilityStage<SlimeWord> stage) {
    super(stage)
    loadResource(RESOURCE_PATH, TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT)
  }

  public void prepare(SlimeDictionary dictionary) {
    $dictionary = dictionary
    setupWordList()
    setupSearchType()
  }

  @FXML
  private void search() {
    if ($dictionary != null) {
      String search = $searchText.getText()
      String searchMode = $searchMode.getValue()
      Boolean isStrict = $searchType.getText() == "完全一致"
      if (searchMode == "単語") {
        $dictionary.searchByName(search, isStrict)
      } else if (searchMode == "訳語") {
        $dictionary.searchByEquivalent(search, isStrict)
      } else if (searchMode == "全文") {
        $dictionary.searchByContent(search)
      }
      $wordList.scrollTo(0)
    }
  }

  @FXML
  private void changeSearchMode() {
    $searchText.setText("")
    $searchText.requestFocus()
    search()
  }

  @FXML
  private void toggleSearchType() {
    $searchText.requestFocus()
    search()
  }

  @FXML
  protected void commit() {
    SlimeWord word = $wordList.getSelectionModel().getSelectedItem()
    $stage.close(word)
  }

  private void setupWordList() {
    $wordList.setItems($dictionary.getWords())
    $wordList.setCellFactory() { ListView<SlimeWord> list ->
      SimpleWordCell cell = SimpleWordCell.new()
      cell.setOnMouseClicked() { MouseEvent event ->
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
          commit()
        }
      }
      return cell
    }
  }

  private void setupSearchType() {
    Callable<String> textFunction = (Callable){
      return ($searchType.selectedProperty().get()) ? "完全一致" : "部分一致"
    }
    Callable<Boolean> disableFunction = (Callable){
      String searchMode = $searchMode.getValue()
      if (searchMode == "単語") {
        return false
      } else if (searchMode == "訳語") {
        return false
      } else if (searchMode == "全文") {
        return true
      } else {
        return true
      }
    }
    StringBinding textBinding = Bindings.createStringBinding(textFunction, $searchType.selectedProperty())
    BooleanBinding disableBinding = Bindings.createBooleanBinding(disableFunction, $searchMode.valueProperty())    
    $searchType.textProperty().bind(textBinding)
    $searchType.disableProperty().bind(disableBinding)
  }

}