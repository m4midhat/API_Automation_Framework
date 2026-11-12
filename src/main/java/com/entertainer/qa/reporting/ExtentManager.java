package com.entertainer.qa.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.entertainer.qa.config.ConfigManager;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

/**
 * Owns the single {@link ExtentReports} instance for the whole suite and the per-thread
 * {@link ExtentTest} node each test method logs against.
 *
 * <p>Lazily created on first use (thread-safe) rather than wired through a listener's
 * {@code onStart}, so it works the same whether the suite runs single-threaded or with
 * {@code parallel="tests"}/{@code "classes"} - {@link #startTest} is always safe to call, and
 * {@link #getTest()} always returns the right node for the calling thread.
 */
public final class ExtentManager {

    private static volatile ExtentReports extentReports;
    private static final Object INIT_LOCK = new Object();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();

    private ExtentManager() {
    }

    public static ExtentReports getInstance() {
        ExtentReports instance = extentReports;
        if (instance == null) {
            synchronized (INIT_LOCK) {
                instance = extentReports;
                if (instance == null) {
                    instance = createInstance();
                    extentReports = instance;
                }
            }
        }
        return instance;
    }

    public static ExtentTest startTest(String name, String description) {
        ExtentTest test = getInstance().createTest(name, description);
        CURRENT_TEST.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return CURRENT_TEST.get();
    }

    public static void removeTest() {
        CURRENT_TEST.remove();
    }

    /** Writes the HTML report to disk. Safe to call more than once; only the final call matters. */
    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    private static ExtentReports createInstance() {
        String reportPath = ConfigManager.get("extent.report.path", "target/extent-report/ExtentReport.html");
        new File(reportPath).getParentFile().mkdirs();

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle(ConfigManager.get("extent.report.title", "REST API Automation Report"));
        sparkReporter.config().setReportName(ConfigManager.get("extent.report.name", "API Test Execution Report"));
        sparkReporter.config().setTheme(
                "dark".equalsIgnoreCase(ConfigManager.get("extent.report.theme", "standard"))
                        ? Theme.DARK
                        : Theme.STANDARD);

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(sparkReporter);
        applySystemInfo(reports);
        return reports;
    }

    private static void applySystemInfo(ExtentReports reports) {
        // Report metadata requested for every run of this framework.
        reports.setSystemInfo("Author", ConfigManager.get("report.author", "Midhat Rosull Chughtai"));
        reports.setSystemInfo("Email", ConfigManager.get("report.author.email", "m4midhat@gmail.com"));
        reports.setSystemInfo("GitHub", ConfigManager.get("report.author.github", "github.com/m4midhat"));
        reports.setSystemInfo("Purpose", ConfigManager.get("report.purpose", "API Test cases reporting"));

        // Frequently useful execution context, gathered automatically rather than hand-maintained.
        reports.setSystemInfo("Environment", ConfigManager.getEnv());
        reports.setSystemInfo("Test Framework", "RestAssured + TestNG");
        reports.setSystemInfo("Java Version", System.getProperty("java.version"));
        reports.setSystemInfo("Operating System", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        reports.setSystemInfo("Execution Date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        reports.setSystemInfo("ReqRes Base URL", safe("reqres.base.url"));
        reports.setSystemInfo("Restful-Booker Base URL", safe("booker.base.url"));
        reports.setSystemInfo("JSONPlaceholder Base URL", safe("jsonplaceholder.base.url"));
    }

    private static String safe(String key) {
        String value = ConfigManager.get(key);
        return value != null ? value : "unset";
    }
}
