package ziphil.main

import groovy.transform.CompileStatic
import java.security.Policy
import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import ziphil.Launcher
import ziphil.controller.MainController
import ziphil.custom.FontRenderingType
import ziphil.module.AllPermissionPolicy
import ziphil.module.Setting
import ziphilib.transform.Ziphilify


@CompileStatic @Ziphilify
public class MainApplication extends Application {

  public void start(Stage stage) {
    makeDirectories()
    setupSecurityManager()
    setupFontRendering()
    setupScriptProperty()
    load(stage)
    setupStylesheet()
  }

  private void load(Stage stage) {
    Boolean keepsMainOnTop = Setting.getInstance().getKeepsMainOnTop()
    MainController controller = MainController.new(stage)
    stage.setAlwaysOnTop(keepsMainOnTop)
    controller.prepare()
    stage.show()
  }

  public void stop() {
    Setting.getInstance().save()
  }

  private void makeDirectories() {
    File.new(Launcher.BASE_PATH + "data/setting").mkdirs()
    File.new(Launcher.BASE_PATH + "data/setting/individual").mkdirs()
    File.new(Launcher.BASE_PATH + "data/log").mkdirs()
  }

  private void setupSecurityManager() {
    Policy.setPolicy(AllPermissionPolicy.new())
    System.setSecurityManager(SecurityManager.new())
  }

  private void setupFontRendering() {
    FontRenderingType fontRenderingType = Setting.getInstance().getFontRenderingType()
    if (fontRenderingType == FontRenderingType.DEFAULT_GRAY) {
      System.setProperty("prism.lcdtext", "false")
    } else if (fontRenderingType == FontRenderingType.PRISM_LCD) {
      System.setProperty("prism.lcdtext", "true")
      System.setProperty("prism.text", "t2k")
    } else if (fontRenderingType == FontRenderingType.PRISM_GRAY) {
      System.setProperty("prism.lcdtext", "false")
      System.setProperty("prism.text", "t2k")
    }
  }

  private void setupScriptProperty() {
    System.setProperty("org.jruby.embed.localvariable.behavior", "persistent")
  }

  private void setupStylesheet() {
    setUserAgentStylesheet(STYLESHEET_MODENA)
  }

}