package com.izforge.izpack.integration;

import com.izforge.izpack.bootstrap.IInstallerContainer;
import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.installer.base.GuiId;
import com.izforge.izpack.installer.base.LanguageDialog;
import com.izforge.izpack.installer.data.GUIInstallData;
import org.apache.commons.io.FileUtils;
import org.fest.swing.exception.ScreenLockException;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Test;

import java.awt.*;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for an installation using mock data
 */
public class InstallationTest extends AbstractInstallationTest {

    @After
    public void tearBinding() {
        applicationContainer.dispose();
        try {
            if (dialogFrameFixture != null) {
                dialogFrameFixture.cleanUp();
                dialogFrameFixture = null;
            }
            if (installerFrameFixture != null) {
                installerFrameFixture.cleanUp();
                installerFrameFixture = null;
            }
        } catch (ScreenLockException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHelloAndFinishPanels() throws Exception {
        compileAndUnzip("helloAndFinish.xml", getWorkingDirectory("samples"));
        installerContainer = applicationContainer.getComponent(IInstallerContainer.class);
        installerContainer.getComponent(LanguageDialog.class).initLangPack();
        prepareFrameFixture();

        // Hello panel
        installerFrameFixture.requireSize(new Dimension(640, 480));
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.requireVisible();
        // Finish panel
    }


    @Test
    public void testBasicInstall() throws Exception {
        compileAndUnzip("basicInstall.xml", getWorkingDirectory("samples/basicInstall"));
        GUIInstallData installData = applicationContainer.getComponent(GUIInstallData.class);

        installerContainer = applicationContainer.getComponent(IInstallerContainer.class);
        File installPath = new File(installData.getInstallPath());
        FileUtils.deleteDirectory(installPath);
        assertThat(installPath.exists(), Is.is(false));
        // Lang picker
        prepareDialogFixture();
        dialogFrameFixture.button(GuiId.BUTTON_LANG_OK.id).click();
        // Seems necessary to unlock window
        dialogFrameFixture.cleanUp();
        dialogFrameFixture = null;

        prepareFrameFixture();
        // Hello panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Info Panel
        installerFrameFixture.textBox(GuiId.INFO_PANEL_TEXT_AREA.id).requireText("A readme file ...");
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Licence Panel
        installerFrameFixture.textBox(GuiId.LICENCE_TEXT_AREA.id).requireText("(Consider it as a licence file ...)");
        installerFrameFixture.radioButton(GuiId.LICENCE_NO_RADIO.id).requireSelected();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).requireDisabled();
        installerFrameFixture.radioButton(GuiId.LICENCE_YES_RADIO.id).click();
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Target Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        installerFrameFixture.optionPane().requireWarningMessage();
        installerFrameFixture.optionPane().okButton().click();
        // Packs Panel
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();
        // Install Panel
        while (!installData.isCanClose()) {
            Thread.sleep(500);
        }
        installerFrameFixture.button(GuiId.BUTTON_NEXT.id).click();

        assertThat(installPath.exists(), Is.is(true));
        UninstallData u = UninstallData.getInstance();
        for (String p : u.getInstalledFilesList()) {
            File f = new File(p);
            assertThat(f.exists(), Is.is(true));
        }
        // Finish panel
        installerFrameFixture.button(GuiId.FINISH_PANEL_AUTO_BUTTON.id).click();
        installerFrameFixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).fileNameTextBox().enterText("auto.xml");
        installerFrameFixture.fileChooser(GuiId.FINISH_PANEL_FILE_CHOOSER.id).approve();
        assertThat(new File(installPath, "auto.xml").exists(), Is.is(true));
    }

}
