package io.lucalabs.expenses.models;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;

import io.lucalabs.expenses.managers.EnvironmentManager;
import io.lucalabs.expenses.network.Routes;
import io.lucalabs.expenses.network.database.UserDatabase;
import io.lucalabs.expenses.network.webapi.ApiRequest;

/**
 * Object that stores information about Firebase tasks.
 * These tasks are persisted to Firebase, and run from the ApiRequestQueue
 */
@IgnoreExtraProperties
public class Task {
    private String url; // can potentially be generated
    private String requestMethod;
    private String objectRef;
    private String className;
    private String classLongName;
    private Object object;
    private String firebaseRef;

    private final CountDownLatch setupLatch = new CountDownLatch(1);
    private Context context;
    private final static String TAG = Task.class.getSimpleName();

    public Task() {
    }

    public Task(Context context, String requestMethod, FirebaseObject object) {
        this.context = context;
        setRequestMethod(requestMethod);
        setUrl(Routes.objectsPath(object, requestMethod.equals("POST")));
        setObjectRef(object.getFirebase_ref());
        setClassName(object.getClass().getSimpleName());
        setClassLongName(object.getClass().getName());
        create(context);
    }

    private void create(Context context) {
        DatabaseReference ref = UserDatabase.newTaskReference(User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context));
        firebaseRef = ref.getKey();
        ref.setValue(this);
    }

    private void delete(Context context) {
        DatabaseReference ref = UserDatabase.getUserReference(
                User.getCurrentUser(),
                EnvironmentManager.currentEnvironment(context)).child("tasks").child(firebaseRef);
        ref.removeValue();
    }

    public void performAsync() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public boolean perform() {
        try {
            return execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean execute() throws InterruptedException {
        DatabaseReference ref = Inbox.findObject(context, className, objectRef);

        // Update on deleted object
        if(ref == null)
            return true;

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.w(TAG, "class long name: " + classLongName);
                    object = dataSnapshot.getValue(Class.forName(classLongName));

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                setupLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setupLatch.await();

        ApiRequest apiRequest = new ApiRequest(context, Task.this);
        boolean success = apiRequest.start();

        if (success)
            delete(context);

        return success;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setFirebaseRef(String firebaseRef) {
        this.firebaseRef = firebaseRef;
    }

    public String getFirebaseRef() {
        return firebaseRef;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getObjectRef() {
        return objectRef;
    }

    public void setObjectRef(String objectRef) {
        this.objectRef = objectRef;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassLongName() {
        return classLongName;
    }

    public void setClassLongName(String classLongName) {
        this.classLongName = classLongName;
    }

    public Object getObject() {
        return object;
    }
}
