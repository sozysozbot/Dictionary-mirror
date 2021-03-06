package ziphil.custom

import groovy.transform.CompileStatic
import java.util.concurrent.Callable
import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.SplitPane
import javafx.scene.control.TextField
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.WindowEvent
import javafx.util.Callback
import ziphilib.transform.VoidClosure
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class FileChooserSkin extends CustomSkinBase<FileChooser, VBox> {

  private static final String RESOURCE_PATH = "resource/fxml/custom/file_chooser.fxml"
  private static final Comparator<File> FILE_COMPARATOR = createFileComparator()
  private static final ExtensionFilter DEFAULT_EXTENSION_FILTER = ExtensionFilter.new("全てのファイル", null)

  @FXML private TreeView<File> $directoryView
  @FXML private ListView<File> $fileView
  @FXML private SplitPane $splitPane
  @FXML private TextField $directoryControl
  @FXML private TextField $fileControl
  @FXML private ComboBox<ExtensionFilter> $fileTypeControl

  public FileChooserSkin(FileChooser control) {
    super(control)
    $node = VBox.new()
    loadResource(RESOURCE_PATH)
    setupNode()
  }

  @FXML
  private void initialize() {
    setupDirectoryView()
    setupFileView()
    setupDirectoryControl()
    setupFileControl()
    setupFileTypeControl()
    setupSplitPane()
    bindDirectoryControlProperty()
    bindFileControlProperty()
    bindFileTypeControlProperty()
    applyDefaultValues()
  }

  private void changeCurrentFile(File file) {
    if (file != null && file.isFile()) {
      $fileControl.setText(file.getName())
    }
  }

  private void changeCurrentDirectory(File file) {
    if (file != null) {
      if (file.isDirectory()) {
        $control.setCurrentDirectory(file)
      } else if (file.isFile()) {
        $fileControl.setText(file.getName())
      }
    }
  }

  @FXML
  private void moveToHome() {
    $control.moveToHome()
  }

  @FXML
  private void moveToParent() {
    $control.moveToParent()
  }

  @VoidClosure
  private void setupDirectoryView() {
    DirectoryItem root = DirectoryItem.new(null)
    for (File file : File.listRoots()) {
      root.getChildren().add(DirectoryItem.new(file))
    }
    $directoryView.setRoot(root)
    $directoryView.setShowRoot(false)
    $directoryView.setCellFactory() { TreeView<File> view ->
      Callback<TreeView<File>, TreeCell<File>> cellFactory = $control.getDirectoryCellFactory()
      TreeCell<File> cell = (cellFactory != null) ? cellFactory.call(view) : DirectoryCell.new()
      cell.addEventHandler(MouseEvent.MOUSE_CLICKED) { MouseEvent event ->
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
          changeCurrentDirectory(cell.getItem())
        }
      }
      return cell
    }
  }

  @VoidClosure
  private void setupFileView() {
    $fileView.setCellFactory() { ListView<File> view ->
      Callback<ListView<File>, ListCell<File>> cellFactory = $control.getFileCellFactory()
      ListCell<File> cell = (cellFactory != null) ? cellFactory.call(view) : FileCell.new()
      cell.addEventHandler(MouseEvent.MOUSE_CLICKED) { MouseEvent event ->
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
          changeCurrentFile(cell.getItem())
        } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
          changeCurrentDirectory(cell.getItem())
        }
      }
      return cell
    }
    Callable<ObservableList<File>> function = (Callable){
      File directory = $control.getCurrentDirectory()
      ObservableList<File> files = FXCollections.observableArrayList()
      if (directory != null) {
        File[] innerFiles = $control.getCurrentDirectory().listFiles()
        if (innerFiles != null) {
          for (File innerFile : innerFiles) {
            if ($control.isShowsHidden() || !innerFile.isHidden()) {
              if (innerFile.isDirectory() || $fileTypeControl.getValue() == null || $fileTypeControl.getValue().accepts(innerFile)) {
                files.add(innerFile)
              }
            }
          }
        }
      }
      SortedList sortedFiles = SortedList.new(files, FILE_COMPARATOR)
      return sortedFiles
    }
    ObjectBinding<ObservableList<File>> binding = Bindings.createObjectBinding(function, $control.currentDirectoryProperty(), $control.showsHiddenProperty(), $fileTypeControl.valueProperty())
    $fileView.itemsProperty().bind(binding)
  }

  private void setupDirectoryControl() {
    $directoryControl.setOnAction() {
      File file = File.new($directoryControl.getText())
      changeCurrentDirectory(file)
    }
  }

  private void setupFileControl() {
    $fileControl.sceneProperty().addListener() { ObservableValue<? extends Scene> observableValue, Scene oldValue, Scene newValue ->
      if (oldValue == null && newValue != null) {
        $fileControl.requestFocus()
      }
    }
  }

  private void setupFileTypeControl() {
    Callable<ObservableList<ExtensionFilter>> function = (Callable){
      ObservableList<ExtensionFilter> items = FXCollections.observableArrayList()
      items.add(DEFAULT_EXTENSION_FILTER)
      items.addAll($control.getExtensionFilters())
      return items
    }
    ObjectBinding<ObservableList<ExtensionFilter>> binding = Bindings.createObjectBinding(function, $control.extensionFiltersProperty())
    $fileTypeControl.itemsProperty().bind(binding)
    $fileTypeControl.setOnAction() {
      $control.setCurrentFileType($fileTypeControl.getValue())
    }
    $fileTypeControl.getSelectionModel().selectFirst()
  }

  private void setupSplitPane() {
    $splitPane.setDividerPositions(0.3)
  }

  private void bindDirectoryControlProperty() {
    ChangeListener<File> listener = { ObservableValue<? extends File> observableValue, File oldValue, File newValue ->
      $directoryControl.setText(newValue.getAbsolutePath())
      $fileView.scrollTo(0)
    }
    $control.currentDirectoryProperty().addListener(listener)
  }

  private void bindFileControlProperty() {
    $fileControl.textProperty().bindBidirectional($control.inputtedFileNameProperty())
  }

  private void bindFileTypeControlProperty() {
    ChangeListener<ExtensionFilter> listener = { ObservableValue<? extends ExtensionFilter> observableValue, ExtensionFilter oldValue, ExtensionFilter newValue ->
      $fileTypeControl.getSelectionModel().select(newValue)
    }
    $control.currentFileTypeProperty().addListener(listener)
  }

  private void applyDefaultValues() {
    if ($control.getCurrentDirectory() != null) {
      $directoryControl.setText($control.getCurrentDirectory().getAbsolutePath())
      $fileView.scrollTo(0)
    } else {
      moveToHome()
    }
    if ($control.getCurrentFileType() != null) {
      $fileTypeControl.getSelectionModel().select($control.getCurrentFileType())
    } else {
      $control.setCurrentFileType($fileTypeControl.getValue())
    }
  }

  private static Comparator<File> createFileComparator() {
    Comparator<File> comparator = (Comparator){ File firstFile, File secondFile ->
      if (firstFile.isDirectory()) {
        if (secondFile.isDirectory()) {
          return firstFile.getName().compareToIgnoreCase(secondFile.getName())
        } else {
          return -1
        }
      } else {
        if (secondFile.isDirectory()) {
          return 1
        } else {
          return firstFile.getName().compareToIgnoreCase(secondFile.getName())
        }
      }
    }
    return comparator
  }

}