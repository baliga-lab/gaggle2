package org.systemsbiology.gaggle.boss;


import net.sf.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by Ning Jiang on 6/3/14.
 */
public class SeleniumChromeHandler {
    WebDriver myDriver;
    private Logger Log = Logger.getLogger(this.getClass().getName());

    public SeleniumChromeHandler()
    {
        ChromeOptions options = new ChromeOptions();
        options.addExtensions(new File("C:/GitHub/BaligaLab/Geese/ChromeGoose.crx"));
        myDriver = new ChromeDriver(options);
    }

    private ArrayList<WebElement> findElements(String elementId, String elementClass)
    {
        Log.info("Find elements " + elementId + " " + elementClass);
        ArrayList<WebElement> elements = new ArrayList<WebElement>();

        if (elementId != null && !elementId.isEmpty()) {
            WebElement element = myDriver.findElement(By.id(elementId));
            if (element == null)
            {
                elements.addAll(myDriver.findElements(By.className(elementClass)));
            }
            else {
                elements.add(element);
            }
        }
        else
            elements.addAll(myDriver.findElements(By.className(elementClass)));
        return elements;
    }

    private void clickElements(ArrayList<WebElement> elements)
    {
        Log.info("Clicing elements " + elements.size());
        if (elements != null)
        {
            for (int i = 0; i < elements.size(); i++) {
                WebElement element = elements.get(i);
                try {
                    element.click();
                }
                catch (Exception e) {
                    Log.severe("Failed to click element " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void clickElementsOnPage(String pageUrl, String elementId, String elementClass)
    {
        Log.info("Click page " + pageUrl);
        myDriver.get(pageUrl);
        ArrayList<WebElement> elements = this.findElements(elementId, elementClass);
        clickElements(elements);
    }

    private void clickElementsInIFrame(String iframeId, String elementId, String elementClass)
    {
        Log.info("Click elements in iframe " + iframeId);
        //myDriver.switchTo().frame(myDriver.findElement(By.id("testframe")));
        //WebElement button = myDriver.findElement(By.id("testbutton"));
        //button.click();

        myDriver.switchTo().frame(myDriver.findElement(By.id(iframeId)));
        ArrayList<WebElement> elements = this.findElements(elementId, elementClass);
        clickElements(elements);
        myDriver.switchTo().defaultContent();
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
                if (command.equalsIgnoreCase("openpage")) {
                    String pageUrl = dataJsonObject.getString("PageUrl");
                    Log.info("Opening page " + pageUrl);
                    myDriver.get(pageUrl);

                    // Test
                    //myDriver.switchTo().frame(myDriver.findElement(By.id("testframe")));
                    //WebElement button = myDriver.findElement(By.id("testbutton"));
                    //button.click();
                }
                else if (command.equalsIgnoreCase("clickpage")) {

                    String pageUrl = dataJsonObject.getString("PageUrl");
                    String elementId = dataJsonObject.getString("ElementId");
                    String elementClass = dataJsonObject.getString("ElementClass");
                    clickElementsOnPage(pageUrl, elementId, elementClass);
                }
                else if (command.equalsIgnoreCase("clickiframe")) {
                    String iframeId = dataJsonObject.getString("IFrameId");
                    String elementId = dataJsonObject.getString("ElementId");
                    String elementClass = dataJsonObject.getString("ElementClass");
                    clickElementsInIFrame(iframeId, elementId, elementClass);
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
