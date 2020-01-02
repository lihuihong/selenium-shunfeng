package com.heylhh.crawler;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContexts;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * selenium破解顺丰滑动验证码
 */
public class TencentCrawler {
    private static String BASE_PATH = "D:/javaprojectmyself/selenium-geetest-crack-master/";
    //小方块距离左边界距离，对应到原图的距离
    private static int START_DISTANCE = (22 + 16) * 2;

    private static ChromeDriver driver = null;
    static {
        System.setProperty("webdriver.chrome.driver", "C:/Users/Administrator/AppData/Local/Google/Chrome/Application/chromedriver.exe");
    }
    public static void main(String[] args) {
        crawl();
    }


    public static void crawl(){
        long startTime=System.currentTimeMillis();   //获取开始时间
        ChromeOptions chromeOptions = new ChromeOptions();
        // 设置禁止加载项
        Map<String, Object> prefs = new HashMap<String, Object>();
        // 禁止加载js
        prefs.put("profile.default_content_settings.javascript", 2); // 2就是代表禁止加载的意思
        // 禁止加载css
        prefs.put("profile.default_content_settings.images", 2); // 2就是代表禁止加载的意思
        chromeOptions.setExperimentalOption("prefs", prefs);
        //chromeOptions.addArguments("disable-infobars");
        driver = new ChromeDriver(chromeOptions);
        for(int i = 0; i <1; i++) {
            try {
                driver.manage().window().setSize(new Dimension(1024, 768));
                driver.manage().timeouts().implicitlyWait(4, TimeUnit.SECONDS);
                driver.manage().timeouts().pageLoadTimeout(4, TimeUnit.SECONDS);
                long startTime5=System.currentTimeMillis();   //获取开始时间
                driver.get("https://www.sf-express.com/cn/sc/dynamic_function/waybill/#search/bill-number/294579639081");
                Thread.sleep( 2000);
                Actions actions = new Actions(driver);
                driver.switchTo().frame("tcaptcha_popup");
                String originalUrl = "https://captcha.guard.qcloud.com"+Jsoup.parse(driver.getPageSource()).select("[id=slideBkg]").first().attr("src");
                long endTime5=System.currentTimeMillis(); //获取结束时间
                System.out.println("加载网页以及图片程序运行时间： "+(endTime5-startTime5)+"ms");
                long startTime3=System.currentTimeMillis();   //获取开始时间
                downloadOriginalImg(i, originalUrl, driver.manage().getCookies());
                long endTime3=System.currentTimeMillis(); //获取结束时间
                System.out.println("下载图片程序运行时间： "+(endTime3-startTime3)+"ms");
                float bgWrapWidth = driver.findElement(By.className("tcaptcha-img")).getSize().getWidth();
                long startTime1=System.currentTimeMillis();   //获取开始时间
                int distance = calcMoveDistance(i, bgWrapWidth);
                long endTime1=System.currentTimeMillis(); //获取结束时间
                System.out.println("计算小方块需要移动的距离程序运行时间： "+(endTime1-startTime1)+"ms");
                long startTime2=System.currentTimeMillis(); //获取开始时间
                List<MoveEntity> list = getMoveEntity1(distance);
                long endTime2=System.currentTimeMillis(); //获取结束时间
                System.out.println("计算移动算法程序运行时间： "+(endTime2-startTime2)+"ms");
                WebElement element = driver.findElement(By.id("tcaptcha_drag_button"));
                actions.clickAndHold(element).perform();
                int d = 0;
                long startTime4=System.currentTimeMillis(); //获取开始时间
                for (MoveEntity moveEntity : list) {
                    actions.moveByOffset(moveEntity.getX(), moveEntity.getY()).perform();
                    //System.out.println("向右总共移动了:" + (d = d + moveEntity.getX()));
                    Thread.sleep(moveEntity.getSleepTime());
                }
                long endTime4=System.currentTimeMillis(); //获取结束时间
                System.out.println("执行移动程序运行时间： "+(endTime4-startTime4)+"ms");
                actions.release(element).perform();
                Thread.sleep(2000);
                List<WebElement> elements = driver.findElements(By.className("route-list"));
                for (WebElement webElement : elements) {
                    System.out.println(webElement.getText());
                }
                long endTime=System.currentTimeMillis(); //获取结束时间
                System.out.println("程序总运行时间： "+(endTime-startTime)+"ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        driver.quit();
    }
    private static void downloadOriginalImg(int i, String originalUrl, Set<Cookie> cookieSet) throws IOException {
        CookieStore cookieStore = new BasicCookieStore();
        cookieSet.forEach( c -> {
            BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
            cookie.setPath(c.getPath());
            cookie.setDomain(c.getDomain());
            cookie.setExpiryDate(c.getExpiry());
            cookie.setSecure(true);
            cookieStore.addCookie(cookie);
        });
        InputStream is = null;
        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType())
                            , (chain, authType) -> true).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .register("https", new SSLConnectionSocketFactory(sslContext))
                            .build();
            is = HttpClients.custom()
//                    .setProxy(new HttpHost("127.0.0.1", 8888))
                    .setUserAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36")
                    .setDefaultCookieStore(cookieStore)
                    .setConnectionManager(new PoolingHttpClientConnectionManager(socketFactoryRegistry))
                    .build()
                    .execute(new HttpGet(originalUrl))
                    .getEntity().getContent();
            FileUtils.copyInputStreamToFile(is, new File(BASE_PATH + "tencent-original" + i + ".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算小方块需要移动的距离
     * @param i
     * @param bgWrapWidth 背景图片div对应的width
     * @return
     * @throws IOException
     */
    public static int calcMoveDistance(int i, float bgWrapWidth) throws IOException {
        BufferedImage fullBI = ImageIO.read(new File(BASE_PATH + "tencent-original" + i + ".png"));
        for(int w = 340 ; w < fullBI.getWidth() - 18; w++){
            int whiteLineLen = 0;
            for (int h = 0; h < fullBI.getHeight(); h++){
                int[] fullRgb = new int[3];
                fullRgb[0] = (fullBI.getRGB(w, h)  & 0xff0000) >> 16;
                fullRgb[1] = (fullBI.getRGB(w, h)  & 0xff00) >> 8;
                fullRgb[2] = (fullBI.getRGB(w, h)  & 0xff);
                if (isBlack28(fullBI, w, h) && isWhite(fullBI, w, h)) {
                    whiteLineLen++;
                } else {
//                    whiteLineLen = 0;
                    continue;
                }
                if (whiteLineLen >= 50){
                    return (int) ((w - START_DISTANCE) / (fullBI.getWidth() / bgWrapWidth)+20);
                }
            }

        }
        throw new RuntimeException("计算缺口位置失败");
    }
    /**
     * 当前点的后28个是不是黑色
     *
     * @return 后28个中有80%是黑色返回true, 否则返回false
     */
    private static boolean isBlack28(BufferedImage fullBI, int w, int h) {
        int[] fullRgb = new int[3];
        double blackNum = 0;
        int num = Math.min(fullBI.getWidth() - w, 28);
        for (int i = 0; i < num; i++) {
            fullRgb[0] = (fullBI.getRGB(w + i, h) & 0xff0000) >> 16;
            fullRgb[1] = (fullBI.getRGB(w + i, h) & 0xff00) >> 8;
            fullRgb[2] = (fullBI.getRGB(w + i, h) & 0xff);
            if (isBlack(fullRgb)) {
                blackNum = blackNum + 1;
            }
        }
        return blackNum / num > 0.8;
    }

    /**
     * 当前点是不是白色
     *
     * @param fullBI
     * @param w
     * @param h
     * @return
     */
    private static boolean isWhite(BufferedImage fullBI, int w, int h) {
        int[] fullRgb = new int[3];
        fullRgb[0] = (fullBI.getRGB(w, h) & 0xff0000) >> 16;
        fullRgb[1] = (fullBI.getRGB(w, h) & 0xff00) >> 8;
        fullRgb[2] = (fullBI.getRGB(w, h) & 0xff);
        return isWhite(fullRgb);
    }

    private static boolean isWhite(int[] fullRgb) {
        return (Math.abs(fullRgb[0] - 0xff) + Math.abs(fullRgb[1] - 0xff) + Math.abs(fullRgb[2] - 0xff)) < 125;
    }

    private static boolean isBlack(int[] fullRgb) {
        return fullRgb[0] * 0.3 + fullRgb[1] * 0.6 + fullRgb[2] * 0.1 <= 125;
    }

    /**
     * 默认移动算法
     * @param distance
     * @return
     */
    public static List<MoveEntity> getMoveEntity(int distance){
        List<MoveEntity> list = new ArrayList<>();
        for (int i = 0 ;i < distance; i++){

            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setX(1);
            moveEntity.setY(0);
            moveEntity.setSleepTime(0);
            list.add(moveEntity);
        }
        return list;
    }

    /**
     * 移动算法
     * @param distance
     * @return
     */
    public static List<MoveEntity> getMoveEntity1(int distance){

        List<MoveEntity> list = new ArrayList<>();
        for (int i = 0 ;i < distance / 5; i++){
            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setX(5);
            moveEntity.setY(ThreadLocalRandom.current().nextBoolean() ? 10 : -10);
            moveEntity.setSleepTime(1);
            list.add(moveEntity);
        }

        MoveEntity moveEntity = new MoveEntity();
        moveEntity.setX(distance % 5);
        moveEntity.setY(0);
        moveEntity.setSleepTime(1);
        list.add(moveEntity);
        return list;
    }
}
