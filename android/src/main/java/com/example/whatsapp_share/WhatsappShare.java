package com.example.whatsapp_share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.database.Cursor;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.example.whatsapp_share.ContactHelper;

/** WhatsappShare */
public class WhatsappShare implements FlutterPlugin, MethodCallHandler {
    private Context context;
    private MethodChannel methodChannel;

    public WhatsappShare() {
    }

    /** Plugin registration. */
    @SuppressWarnings("deprecation")
    public static void registerWith(io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
        final WhatsappShare instance = new WhatsappShare();
        instance.onAttachedToEngine(registrar.context(), registrar.messenger());
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    private void onAttachedToEngine(Context applicationContext, BinaryMessenger messenger) {
        this.context = applicationContext;
        methodChannel = new MethodChannel(messenger, "whatsapp_share");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        context = null;
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("shareFile")) {
            shareFile(call, result);
        } else if (call.method.equals("share")) {
            share(call, result);
        } else if (call.method.equals("isInstalled")) {
            isInstalled(call, result);
        } else {
            result.notImplemented();
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void isInstalled(MethodCall call, Result result) {
        try {
            String packageName = call.argument("package");

            if (packageName == null || packageName.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Package name null or empty");
                result.error("FlutterShare:Package name cannot be null or empty", null, null);
                return;
            }

            PackageManager pm = context.getPackageManager();
            boolean isInstalled = isPackageInstalled(packageName, pm);
            result.success(isInstalled);
        } catch (Exception ex) {
            Log.println(Log.ERROR, "", "FlutterShare: Error");
            result.error(ex.getMessage(), null, null);
        }
    }

    private void share(MethodCall call, Result result) {
        try {
            String title = call.argument("title");
            String text = call.argument("text");
            String linkUrl = call.argument("linkUrl");
            String chooserTitle = call.argument("chooserTitle");
            String phone = call.argument("phone");
            String packageName = call.argument("package");

            if (title == null || title.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Title null or empty");
                result.error("FlutterShare: Title cannot be null or empty", null, null);
                return;
            } else if (phone == null || phone.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: phone null or empty");
                result.error("FlutterShare: phone cannot be null or empty", null, null);
                return;
            } else if (packageName == null || packageName.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Package name null or empty");
                result.error("FlutterShare:Package name cannot be null or empty", null, null);
                return;
            }

            ArrayList<String> extraTextList = new ArrayList<>();

            if (text != null && !text.isEmpty()) {
                extraTextList.add(text);
            }
            if (linkUrl != null && !linkUrl.isEmpty()) {
                extraTextList.add(linkUrl);
            }

            String extraText = "";

            if (!extraTextList.isEmpty()) {
                extraText = TextUtils.join("\n\n", extraTextList);
            }

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage(packageName);
            intent.putExtra("jid", phone + "@s.whatsapp.net");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, extraText);

            // Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            result.success(true);
        } catch (Exception ex) {
            Log.println(Log.ERROR, "", "FlutterShare: Error");
            result.error(ex.getMessage(), null, null);
        }
    }

    private void shareFile(MethodCall call, Result result) {
        ArrayList<String> filePaths = new ArrayList<String>();
        ArrayList<Uri> files = new ArrayList<Uri>();
        try {
            String title = call.argument("title");
            String text = call.argument("text");
            filePaths = call.argument("filePath");
            String chooserTitle = call.argument("chooserTitle");
            String phone = call.argument("phone");
            String packageName = call.argument("package");
            String customerName = call.argument("customerName");

            if (filePaths == null || filePaths.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare: ShareLocalFile Error: filePath null or empty");
                result.error("FlutterShare: FilePath cannot be null or empty", null, null);
                return;
            } else if (phone == null || phone.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: phone null or empty");
                result.error("FlutterShare: phone cannot be null or empty", null, null);
                return;
            } else if (packageName == null || packageName.isEmpty()) {
                Log.println(Log.ERROR, "", "FlutterShare Error: Package name null or empty");
                result.error("FlutterShare:Package name cannot be null or empty", null, null);
                return;
            }

            for (int i = 0; i < filePaths.size(); i++) {
                File file = new File(filePaths.get(i));
                Uri fileUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".provider", file);
                files.add(fileUri);
            }

            if (!ContactHelper.isPhoneNumberValid(phone)) {
                // Log
                System.out.println("Invalid phone number format");
            } else if (ContactHelper.isContactExists(context, phone)) {
                // Log
                System.out.println("Contact already exists");
            } else {
                ContactHelper.saveContact(context, customerName, phone);

                // Delayed action after saving contact
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intentReg = new Intent();
                        intentReg.setFlags(intentReg.FLAG_ACTIVITY_CLEAR_TOP);
                        intentReg.setFlags(intentReg.FLAG_ACTIVITY_NEW_TASK);
                        intentReg.setAction(intentReg.ACTION_SEND_MULTIPLE);
                        intentReg.setType("*/*");
                        intentReg.setPackage(packageName);
                        intentReg.putExtra("jid", phone + "@s.whatsapp.net");
                        intentReg.putExtra(intentReg.EXTRA_SUBJECT, title);
                        intentReg.putExtra(intentReg.EXTRA_TEXT, text);
                        intentReg.putExtra(intentReg.EXTRA_STREAM, files);
                        intentReg.addFlags(intentReg.FLAG_GRANT_READ_URI_PERMISSION);

                        // Intent chooserIntent = intentReg.createChooser(intent, chooserTitle);
                        intentReg.setFlags(intentReg.FLAG_ACTIVITY_CLEAR_TOP);
                        intentReg.setFlags(intentReg.FLAG_ACTIVITY_NEW_TASK);

                        Intent intentW4b = new Intent();
                        intentW4b.setFlags(intentW4b.FLAG_ACTIVITY_CLEAR_TOP);
                        intentW4b.setFlags(intentW4b.FLAG_ACTIVITY_NEW_TASK);
                        intentW4b.setAction(intentW4b.ACTION_SEND_MULTIPLE);
                        intentW4b.setType("*/*");
                        intentW4b.setPackage("com.whatsapp.w4b");
                        intentW4b.putExtra("jid", phone + "@s.whatsapp.net");
                        intentW4b.putExtra(intentW4b.EXTRA_SUBJECT, title);
                        intentW4b.putExtra(intentW4b.EXTRA_TEXT, text);
                        intentW4b.putExtra(intentW4b.EXTRA_STREAM, files);
                        intentW4b.addFlags(intentW4b.FLAG_GRANT_READ_URI_PERMISSION);

                        // Intent chooserIntent = intentW4b.createChooser(intent, chooserTitle);
                        intentW4b.setFlags(intentW4b.FLAG_ACTIVITY_CLEAR_TOP);
                        intentW4b.setFlags(intentW4b.FLAG_ACTIVITY_NEW_TASK);

                        try {
                            context.startActivity(intentW4b);
                        } catch (Exception ex) {
                            context.startActivity(intentReg);
                        }

                        result.success(true);
                    }
                }, 1000);
            }

        } catch (Exception ex) {
            result.error(ex.getMessage(), null, null);
            Log.println(Log.ERROR, "", "FlutterShare: Error");
        }
    }
}
