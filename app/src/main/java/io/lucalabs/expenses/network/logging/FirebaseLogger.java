//
//import android.content.Context;
//
//import com.google.firebase.analytics.FirebaseAnalytics;
//
//import org.apache.commons.lang3.time.StopWatch;
//
//public class FirebaseLogger {
//
//    private StopWatch timer;
//    private FirebaseAnalytics analytics;
//    private final String ORIGINAL_IMAGE_UPLOADED = "original_uploaded";
//    private final String THUMBNAIL_UPLOADED = "thumbnail_uploaded";
//    private final String RESPONSE_FROM_SERVER = "response_from_server";
//    private final String RECEIPT_UPLOADED = "receipt_uploaded";
//
//    private long thumbnailStartTime;
//    private long originalStartTime;
//
//    private final String TAG = "FIREBASE_LOGGER";
//
//    private String uid;
//    private String filename;
//
//    public FirebaseLogger(Context context, String uid, String filename) {
//        timer = new StopWatch();
//        analytics = FirebaseAnalytics.getInstance(context);
//        this.uid = uid;
//        this.filename = filename;
//    }
//
//    public void start() {
//        timer.start();
//    }
//
//    public void startUploadingThumb() {
//        thumbnailStartTime = timer.getTime();
//    }
//
//    public void startUploadingOriginal() {
//        originalStartTime = timer.getTime();
//    }
//
//    public void onThumbUploaded() {
//        long timeSpent = timer.getTime() - thumbnailStartTime;
//        System.out.println(THUMBNAIL_UPLOADED + " in " + timeSpent);
//        logEvent(THUMBNAIL_UPLOADED, timeSpent);
//    }
//
//    public void onOriginalUploaded() {
//        long timeSpent = timer.getTime() - originalStartTime;
//        System.out.println(ORIGINAL_IMAGE_UPLOADED + " in " + timeSpent);
//        logEvent(ORIGINAL_IMAGE_UPLOADED, timeSpent);
//    }
//
//    public void onReceiptUploaded() {
//        long timeSpent = timer.getTime();
//        System.out.println(RECEIPT_UPLOADED + " in " + timeSpent);
//        logEvent(RECEIPT_UPLOADED, timer.getTime());
//    }
//
//    private void logEvent(String eventType, long timeSpent) {
//        new LogUploader(analytics, uid, timeSpent, filename, eventType).run();
//    }
//}
