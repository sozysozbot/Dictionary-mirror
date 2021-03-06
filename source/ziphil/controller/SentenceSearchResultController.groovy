package ziphil.controller

import groovy.transform.CompileStatic
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ListView
import ziphil.custom.Measurement
import ziphil.custom.SentenceSearchResultCell
import ziphil.custom.UtilityStage
import ziphil.dictionary.SentenceSearcher
import ziphil.dictionary.Word
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class SentenceSearchResultController extends Controller<Void> {

  private static final String RESOURCE_PATH = "resource/fxml/controller/sentence_search_result.fxml"
  private static final String TITLE = "文一括検索結果"
  private static final Double DEFAULT_WIDTH = Measurement.rpx(540)
  private static final Double DEFAULT_HEIGHT = Measurement.rpx(240)

  @FXML private ListView<SentenceSearcher.Result> $resultView

  public SentenceSearchResultController(UtilityStage<? super Void> stage) {
    super(stage)
    loadResource(RESOURCE_PATH, TITLE, DEFAULT_WIDTH, DEFAULT_HEIGHT, true)
  }

  @FXML
  public void initialize() {
    setupResultView()
  }

  public void prepare(ObservableList<SentenceSearcher.Result> results) {
    $resultView.setItems(results)
  }

  private void setupResultView() {
    $resultView.setCellFactory() { ListView<SentenceSearcher.Result> view ->
      SentenceSearchResultCell cell = SentenceSearchResultCell.new()
      return cell
    }
  }

}