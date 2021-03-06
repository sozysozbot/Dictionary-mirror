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
import ziphil.custom.PlainWordCell
import ziphil.custom.UtilityStage
import ziphil.dictionary.NormalSearchParameter
import ziphil.dictionary.SearchMode
import ziphil.dictionary.slime.SlimeDictionary
import ziphil.dictionary.slime.SlimeWord
import ziphil.module.Setting
import ziphilib.transform.VoidClosure
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class SlimeWordChooserController extends Controller<SlimeWord> {

  private static final String RESOURCE_PATH = "resource/fxml/controller/slime_word_chooser.fxml"
  private static final String TITLE = "単語選択"
  private static final Double DEFAULT_WIDTH = Measurement.rpx(480)
  private static final Double DEFAULT_HEIGHT = Measurement.rpx(320)

  @FXML private ListView<SlimeWord> $wordView
  @FXML private TextField $searchControl
  @FXML private ComboBox<SearchMode> $searchModeControl
  @FXML private ToggleButton $searchTypeControl
  private SlimeDictionary $dictionary

  public SlimeWordChooserController(UtilityStage<? super SlimeWord> stage) {
    super(stage)
    loadResource(RESOURCE_PATH, TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT)
  }

  public void prepare(SlimeDictionary dictionary) {
    $dictionary = dictionary
    setupWordView()
    setupSearchTypeControl()
  }

  @FXML
  private void search() {
    if ($dictionary != null) {
      String search = $searchControl.getText()
      SearchMode searchMode = $searchModeControl.getValue()
      Boolean strict = $searchTypeControl.isSelected()
      NormalSearchParameter parameter = NormalSearchParameter.new(search, searchMode, strict, false)
      $dictionary.search(parameter)
      $wordView.scrollTo(0)
    }
  }

  @FXML
  private void changeSearchMode() {
    $searchControl.setText("")
    $searchControl.requestFocus()
    search()
  }

  @FXML
  private void toggleSearchType() {
    $searchControl.requestFocus()
    search()
  }

  @FXML
  protected void commit() {
    SlimeWord word = $wordView.getSelectionModel().getSelectedItem()
    $stage.commit(word)
  }

  @VoidClosure
  private void setupWordView() {
    $wordView.setItems($dictionary.getWords())
    $wordView.setCellFactory() { ListView<SlimeWord> view ->
      PlainWordCell cell = PlainWordCell.new()
      cell.addEventHandler(MouseEvent.MOUSE_CLICKED) { MouseEvent event ->
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
          commit()
        }
      }
      return cell
    }
  }

  private void setupSearchTypeControl() {
    Callable<String> textFunction = (Callable){
      return ($searchTypeControl.isSelected()) ? "完全一致" : "部分一致"
    }
    Callable<BooleanClass> disableFunction = (Callable){
      SearchMode searchMode = $searchModeControl.getValue()
      if (searchMode == SearchMode.NAME) {
        return false
      } else if (searchMode == SearchMode.EQUIVALENT) {
        return false
      } else if (searchMode == SearchMode.CONTENT) {
        return true
      } else {
        return true
      }
    }
    StringBinding textBinding = Bindings.createStringBinding(textFunction, $searchTypeControl.selectedProperty())
    BooleanBinding disableBinding = Bindings.createBooleanBinding(disableFunction, $searchModeControl.valueProperty())    
    $searchTypeControl.textProperty().bind(textBinding)
    $searchTypeControl.disableProperty().bind(disableBinding)
  }

}