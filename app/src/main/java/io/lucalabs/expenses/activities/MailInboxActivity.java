package io.lucalabs.expenses.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.lucalabs.expenses.R;
import io.lucalabs.expenses.activities.firebase.FirebaseActivity;
import io.lucalabs.expenses.models.Inbox;
import io.lucalabs.expenses.models.Receipt;
import io.lucalabs.expenses.views.adapters.ReceiptListAdapter;

public class MailInboxActivity extends FirebaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_inbox);

        Query query = Inbox.receiptsForExpenseReport(this, null);

        final ListView receiptList = (ListView) findViewById(R.id.mail_list);
        receiptList.setAdapter(new ReceiptListAdapter(this, query));

        registerForContextMenu(receiptList);
        receiptList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Receipt receipt = (Receipt) receiptList.getItemAtPosition(position);
                Intent toReceiptActivity = new Intent(MailInboxActivity.this, ReceiptActivity.class);
                toReceiptActivity.putExtra("firebase_ref", receipt.getFirebase_ref());
                startActivity(toReceiptActivity);
            }
        });

        checkIfInboxIsEmpty(query);
    }

    private void checkIfInboxIsEmpty(Query query) {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    findViewById(R.id.mail_inbox_image).setVisibility(View.GONE);
                    findViewById(R.id.mail_inbox_notice).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.mail_inbox_image).setVisibility(View.VISIBLE);
                    findViewById(R.id.mail_inbox_notice).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}