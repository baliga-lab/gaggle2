package org.systemsbiology.gaggle.boss;


import net.sf.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Ning Jiang on 6/3/14.
 */
public class SeleniumChromeHandler {
    WebDriver myDriver;
    private Logger Log = Logger.getLogger(this.getClass().getName());
    private String chromeGooseDir = null;

    public SeleniumChromeHandler(String chromeGooseDir)
    {
        this.chromeGooseDir = chromeGooseDir;
    }

    private ArrayList<WebElement> findElements(String elementId, String elementClass,
                                               String tagName, String targetAttributeName,
                                               String searchText)
    {
        Log.info("Find elements " + elementId + " " + elementClass + " " + tagName + " " + targetAttributeName + " " + searchText);
        ArrayList<WebElement> elements = new ArrayList<WebElement>();

        if (elementId != null && !elementId.isEmpty()) {
            Log.info("Searching element by Id: " + elementId);
            WebElement element = myDriver.findElement(By.id(elementId));
            if (element != null)
                elements.add(element);
        }

        if (elements.size() == 0)
        {
            if (elementClass != null && !elementClass.isEmpty()) {
                Log.info("Searching element by class name: " + elementClass);
                List<WebElement> results = myDriver.findElements(By.className(elementClass));
                if (results != null)
                    elements.addAll(results);
            }

            if (elements.size() == 0) {
                Log.info("Searching element by tag: " + tagName);
                List<WebElement> results = myDriver.findElements(By.tagName(tagName));
                if (results != null) {
                    if (targetAttributeName != null && !targetAttributeName.isEmpty() &&
                            searchText != null && !searchText.isEmpty()) {
                        Log.info("Searching attribute " + targetAttributeName + " text " + searchText);
                        for (int i = 0; i < results.size(); i++) {
                            WebElement element = results.get(i);
                            if (element.getAttribute(targetAttributeName).indexOf(searchText) >= 0) {
                                elements.add(element);
                            }
                        }
                    }
                    else
                        elements.addAll(results);
                }
            }
        }
        return elements;
    }



    private void clickElements(ArrayList<WebElement> elements, boolean onlyOne)
    {
        Log.info("Clicing elements " + elements.size());
        if (elements != null)
        {
            for (int i = 0; i < elements.size(); i++) {
                WebElement element = elements.get(i);
                try {
                    element.click();
                    if (onlyOne)
                        return;
                }
                catch (Exception e) {
                    Log.severe("Failed to click element " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void clickElementsOnPage(String pageUrl, String elementId, String elementClass,
                                     String tagName, String attributeName, String searchText,
                                     boolean onlyOne)
    {
        Log.info("Click page " + pageUrl);
        myDriver.get(pageUrl);
        ArrayList<WebElement> elements = this.findElements(elementId, elementClass, tagName, attributeName, searchText);
        clickElements(elements, onlyOne);
    }

    private void clickElementsInIFrame(String iframeId, String elementId, String elementClass, String tagName,
                                       String attributeName, String searchText, boolean onlyOne)
    {
        Log.info("Click elements in iframe " + iframeId);
        //myDriver.switchTo().frame(myDriver.findElement(By.id("testframe")));
        //WebElement button = myDriver.findElement(By.id("testbutton"));
        //button.click();
        if (iframeId != null) {
            Log.info("Switching to iframe...");
            myDriver.switchTo().frame(myDriver.findElement(By.id(iframeId)));
            Log.info("Searching for elements...");
            ArrayList<WebElement> elements = this.findElements(elementId, elementClass, tagName, attributeName, searchText);
            clickElements(elements, onlyOne);
            myDriver.switchTo().defaultContent();
        }
    }

    public void startSelenium(String startPageUrl)
    {
        Log.info("ChromeGoose directory: " + this.chromeGooseDir);
        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File(this.chromeGooseDir));
        options.addArguments("test-type");
        myDriver = new ChromeDriver(options);
        if (startPageUrl != null && !startPageUrl.isEmpty())
            myDriver.get(startPageUrl);
    }

    public void handleAction(JSONObject jsonActionData)
    {
        if (jsonActionData != null)
        {
            try {
                String command = jsonActionData.getString("Command");
                String dataString = jsonActionData.getString("Data");
                Log.info("Selenium action command " + command + " data: " + dataString);
                JSONObject dataJsonObject = JSONObject.fromObject(dataString);
                if (command.equalsIgnoreCase("Start")) {
                    String pageUrl = dataJsonObject.getString("PageUrl");
                    Log.info("Starting page in Selenium " + pageUrl);
                    //, (bossImpl.GAGGLE_SERVER + "/static/gaggle_output.html"
                    this.startSelenium(pageUrl);
                }
                else if (command.equalsIgnoreCase("openpage")) {
                    String pageUrl = dataJsonObject.getString("PageUrl");
                    Log.info("Opening page " + pageUrl);
                    myDriver.get(pageUrl);

                    // Test
                    //myDriver.switchTo().frame(myDriver.findElement(By.id("testframe")));
                    //WebElement button = myDriver.findElement(By.id("testbutton"));
                    //button.click();
                }
                else if (command.equalsIgnoreCase("clickpage") || command.equalsIgnoreCase("clickiframe")) {
                    String pageUrl = (dataJsonObject.has("PageUrl")) ? dataJsonObject.getString("PageUrl") : null;
                    String elementId = (dataJsonObject.has("ElementId")) ? dataJsonObject.getString("ElementId") : null;
                    String elementClass = dataJsonObject.has("ElementClass") ? dataJsonObject.getString("ElementClass") : null;
                    String tagName = dataJsonObject.has("TagName") ? dataJsonObject.getString("TagName") : null;
                    String attributeName = dataJsonObject.has("AttributeName") ? dataJsonObject.getString("AttributeName") : null;
                    String searchText = dataJsonObject.has("SearchText") ? dataJsonObject.getString("SearchText") : null;
                    String onlyOneText= dataJsonObject.has("OnlyOne") ? dataJsonObject.getString("OnlyOne") : "false";
                    boolean onlyOne = onlyOneText.equalsIgnoreCase("true") ? true : false;
                    Log.info("Clicking only one element: " + onlyOne);
                    if (command.equalsIgnoreCase("clickiframe")) {
                        String iframeId = dataJsonObject.has("IFrameId") ? dataJsonObject.getString("IFrameId") : null;
                        clickElementsInIFrame(iframeId, elementId, elementClass, tagName, attributeName, searchText, onlyOne);
                    }
                    else
                        clickElementsOnPage(pageUrl, elementId, elementClass, tagName, attributeName, searchText, onlyOne);
                }
            }
            catch (Exception e)
            {
                Log.severe("Failed to handle Selenium action " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
