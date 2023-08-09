package com.example.whatsapp_share;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor; // Import Cursor
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

import java.util.ArrayList; // Import ArrayList

public class ContactHelper {
    public static boolean isPhoneNumberValid(String phoneNumber) {
        // Define a regular expression for a valid phone number format
        String phonePattern = "^[+]?[0-9]{10,14}$"; // Modify the pattern according to your needs

        return phoneNumber.matches(phonePattern);
    }

    public static void saveContact(Context context, String displayName, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            if (results != null && results.length > 0) {
                long contactId = ContentUris.parseId(results[0].uri);
                Toast.makeText(context, "Contact saved with ID: " + contactId,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isContactExists(Context context, String phoneNumber) {
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

        try (Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[] { phoneNumber },
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return true; // Contact exists
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // Contact doesn't exist
    }
}