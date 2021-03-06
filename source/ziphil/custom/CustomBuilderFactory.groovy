package ziphil.custom

import groovy.transform.CompileStatic
import javafx.fxml.JavaFXBuilderFactory
import javafx.util.Builder
import javafx.util.BuilderFactory
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class CustomBuilderFactory implements BuilderFactory {

  private BuilderFactory $baseFactory = JavaFXBuilderFactory.new()

  public Builder getBuilder(Class clazz) {
    if (clazz == DoubleClass) {
      return Measurement.new()
    } else {
      return $baseFactory.getBuilder(clazz)
    }
  }

}