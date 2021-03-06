/*
 * Copyleft (c) 2014. This code is for learning purposes only. Do whatever you like with it but don't take it as perfect code.
 * Michel Racic (http://rac.su/+) => github.com/rac2030
 */

package ch.racic.selenium.helper.download;

import ch.racic.testing.annotation.TargetOS;
import ch.racic.testing.junit.runner.OSSensitiveRunner;
import net.anthavio.phanbedder.Phanbedder;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.net.URL;

/**
 * Created by rac on 08.06.14.
 */
@RunWith(OSSensitiveRunner.class)
public class SeleniumDownloadHelperTest {

    private WebDriver driver;

    // Server testdata
    private static String baseUrl;
    private static String indexPage = "";
    private static String testPdf = "SeleniumDownloadHelper.pdf";
    private File testPdfFile;
    private static File serverBaseFolder = new File("src/test/resources/testServer");
    private byte[] referenceContent;
    private static Server server;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Server server = new Server(0);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase(serverBaseFolder.toString());
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{resource_handler});
        server.setHandler(handlers);
        server.start();
        baseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort() + "/";
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Before
    public void setUpFiles() throws Exception {
        testPdfFile = new File(serverBaseFolder + "/" + testPdf);
        referenceContent = FileUtils.readFileToByteArray(testPdfFile);
    }

    @After
    public void tearDownTestDriver() throws Exception {
        if (driver != null)
            driver.quit();
    }

    @Test
    @Ignore("Known to fail")
    public void testGetFileFromUrlHtmlUnit() throws Exception {
        driver = new HtmlUnitDriver(true);
        invokeGetFileDataFromUrl();
        invokeGetFileFromUrl();
    }

    @Test
    public void testGetFileFromUrlChrome() throws Exception {
        //TODO put this into pom profiles which are OS specific or build helper lib like phantomjs has
        System.setProperty("webdriver.chrome.driver", "driver/chromedriver");
        driver = new ChromeDriver();
        invokeGetFileDataFromUrl();
        invokeGetFileFromUrl();
    }

    @Test
    @TargetOS(family = "mac")
    public void testGetFileFromUrlSafari() throws Exception {
        driver = new SafariDriver();
        invokeGetFileDataFromUrl();
        invokeGetFileFromUrl();
    }

    @Test
    public void testGetFileFromUrlFireFox() throws Exception {
        driver = new FirefoxDriver();
        invokeGetFileDataFromUrl();
        invokeGetFileFromUrl();
    }

    @Test
    @TargetOS(family = "windows")
    public void testGetFileFromUrlInternetExplorer() throws Exception {
        //TODO Fetch latest binaries and make arch specific profiles
        driver = new InternetExplorerDriver();
        invokeGetFileDataFromUrl();
        invokeGetFileFromUrl();
    }

    @Test
    public void testGetFileFromUrlPhantomJS() throws Exception {
        // See http://blog.anthavio.net/2014/04/phantomjs-embedder-for-selenium.html
        File phantomjs = Phanbedder.unpack();
        DesiredCapabilities dcaps = new DesiredCapabilities();
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs.getAbsolutePath());
        driver = new PhantomJSDriver(dcaps);
        invokeGetFileDataFromUrl();
        invokeGetFileFromUrl();
    }

    private void invokeGetFileDataFromUrl() throws Exception {
        driver.get(baseUrl + indexPage);
        SeleniumDownloadHelper sdlh = new SeleniumDownloadHelper(driver);
        FileData testFileData = sdlh.getFileFromUrlRaw(new URL(baseUrl + testPdf));
        Assert.assertArrayEquals("Raw data is correct", referenceContent, testFileData.getData());
        Assert.assertTrue("Guessed name from URL is correct", testPdf.equals(testFileData.getGuessedFilename()));
    }

    private void invokeGetFileFromUrl() throws Exception {
        driver.get(baseUrl + indexPage);
        SeleniumDownloadHelper sdlh = new SeleniumDownloadHelper(driver);
        File tmpFile = File.createTempFile("testfile", ".pdf");
        tmpFile.deleteOnExit();
        File dlFile = sdlh.getFileFromUrl(new URL(baseUrl + testPdf), tmpFile);
        Assert.assertTrue("File content is correct", FileUtils.contentEquals(testPdfFile, dlFile));
    }

}
