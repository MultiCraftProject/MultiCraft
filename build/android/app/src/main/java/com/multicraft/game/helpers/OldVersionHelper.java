package com.multicraft.game.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.multicraft.game.helpers.PreferencesHelper.TAG_COPY_OLD_WORLDS;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.FalseFileFilter.FALSE;

public class OldVersionHelper {
    private final static String OLD_PACKAGE = "mobi.MultiCraft";
    public final static int REQUEST_UNINSTALL = 100;

    public static boolean isPackageInstalled(Activity activity) {
        try {
            activity.getPackageManager().getPackageInfo(OLD_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void uninstallOldVersion(Activity activity) {
        Uri packageURI = Uri.parse("package:" + OLD_PACKAGE);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        activity.startActivityForResult(uninstallIntent, REQUEST_UNINSTALL);
    }

    public static boolean isOldVersionExists(Activity activity, String unzipLocation) {
        PreferencesHelper pf = PreferencesHelper.getInstance(activity);
        String oldWorlds = Environment.getExternalStorageDirectory() + "/Android/data/mobi.MultiCraft/files/worlds";
        File folder = new File(oldWorlds);
        if (folder.exists() && !pf.isWorldsCopied()) {
            moveWorldsToNewFolder(oldWorlds, pf, unzipLocation);
        }
        return isPackageInstalled(activity);
    }

    private static void moveWorldsToNewFolder(String folder, PreferencesHelper pf, String unzipLocation) {
        File source = new File(folder);
        String destination = unzipLocation + "worlds";
        Collection<File> files = FileUtils.listFilesAndDirs(source, FALSE, DIRECTORY);
        for (File file : files) {
            String newName;
            if (!file.getName().equals("worlds")) {
                newName = file.getName() + " OLD";
                try {
                    File dest = new File(destination, newName);
                    FileUtils.moveDirectory(file, dest);
                } catch (IOException e) {
                    // nothing
                }
            }
        }
        pf.saveSettings(TAG_COPY_OLD_WORLDS, true);
    }
}
