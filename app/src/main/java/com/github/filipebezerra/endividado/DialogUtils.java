package com.github.filipebezerra.endividado;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.afollestad.materialdialogs.MaterialDialog;

import static android.text.TextUtils.isEmpty;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 23/10/2015
 * @since #
 */
public class DialogUtils {
    public static MaterialDialog showIndeterminateProgress(@NonNull Context context,
            @Nullable CharSequence title,
            @Nullable CharSequence message) {
        final MaterialDialog.Builder dialog = new MaterialDialog.Builder(context).progress(true, 0);
        if (!isEmpty(title)) {
            dialog.title(title);
        }
        if (!isEmpty(message)) {
            dialog.content(message);
        }
        return dialog.show();
    }
}
