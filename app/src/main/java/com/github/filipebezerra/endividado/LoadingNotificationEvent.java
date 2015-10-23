package com.github.filipebezerra.endividado;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 23/10/2015
 * @since #
 */
public class LoadingNotificationEvent {
    public enum LoadingType {
        LOADING_STARTED, LOADING_COMPLETE, LOADING_FAILED
    }

    private final LoadingType mLoadingType;

    public LoadingNotificationEvent(LoadingType notificationType) {
        mLoadingType = notificationType;
    }

    public LoadingType getLoadingType() {
        return mLoadingType;
    }
}
