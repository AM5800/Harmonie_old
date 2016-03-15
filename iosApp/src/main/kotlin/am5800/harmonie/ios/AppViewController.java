package am5800.harmonie.ios;

import com.intel.inde.moe.natj.general.Pointer;
import com.intel.inde.moe.natj.general.ann.Owned;
import com.intel.inde.moe.natj.general.ann.RegisterOnStartup;
import com.intel.inde.moe.natj.objc.ObjCRuntime;
import com.intel.inde.moe.natj.objc.ann.ObjCClassName;
import com.intel.inde.moe.natj.objc.ann.Property;
import com.intel.inde.moe.natj.objc.ann.Selector;

import am5800.harmonie.app.model.dbAccess.KeyValueDatabase;
import am5800.harmonie.app.model.dbAccess.sql.PermanentDb;
import am5800.harmonie.ios.model.dbAccess.IosPermanentDb;
import am5800.harmonie.ios.model.dbAccess.KeyValueDatabaseImpl;
import ios.NSObject;
import ios.uikit.UIButton;
import ios.uikit.UILabel;
import ios.uikit.UIViewController;

@com.intel.inde.moe.natj.general.ann.Runtime(ObjCRuntime.class)
@ObjCClassName("AppViewController")
@RegisterOnStartup
public class AppViewController extends UIViewController {

  @Owned
  @Selector("alloc")
  public static native AppViewController alloc();

  @Selector("init")
  public native AppViewController init();

  protected AppViewController(Pointer peer) {
    super(peer);
  }

  public UILabel statusText = null;
  public UILabel resultText = null;
  public UIButton helloButton = null;

  @Override
  public void viewDidLoad() {
    statusText = getLabel();
    resultText = getResultText();
    helloButton = getHelloButton();
  }

  @Selector("statusText")
  @Property
  public native UILabel getLabel();

  @Selector("helloButton")
  @Property
  public native UIButton getHelloButton();

  @Selector("BtnPressedCancel_helloButton:")
  public void BtnPressedCancel_button(NSObject sender) {
    statusText.setText("Hello Intel Multi-OS Engine!");
    PermanentDb permanentDb = new IosPermanentDb();
    KeyValueDatabase keyValueDb = new KeyValueDatabaseImpl(permanentDb);
    resultText.setText("kuku ok!");
  }

  @Property
  @Selector("resultText")
  public native UILabel getResultText();
}
