package com.studenthub.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Utility class for monitoring network connectivity.
 * Provides real-time updates on connection status via LiveData.
 */
public class NetworkUtils {
    
    private static NetworkUtils instance;
    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private ConnectivityManager.NetworkCallback networkCallback;
    
    private NetworkUtils(Context context) {
        connectivityManager = (ConnectivityManager) 
            context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Set initial state
        isConnected.postValue(checkConnection());
        
        // Register callback for connectivity changes
        registerNetworkCallback();
    }
    
    public static synchronized NetworkUtils getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtils(context);
        }
        return instance;
    }
    
    /**
     * Check current connection status.
     * @return true if connected to internet
     */
    public boolean checkConnection() {
        if (connectivityManager == null) return false;
        
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) return false;
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
    
    /**
     * Get LiveData for observing connection status.
     */
    public LiveData<Boolean> getConnectionStatus() {
        return isConnected;
    }
    
    /**
     * Register callback for network changes.
     */
    private void registerNetworkCallback() {
        if (connectivityManager == null) return;
        
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isConnected.postValue(true);
            }
            
            @Override
            public void onLost(@NonNull Network network) {
                isConnected.postValue(false);
            }
            
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, 
                                              @NonNull NetworkCapabilities capabilities) {
                boolean connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                isConnected.postValue(connected);
            }
        };
        
        NetworkRequest request = new NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build();
        
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }
    
    /**
     * Unregister network callback.
     * Call this when the app is destroyed.
     */
    public void unregisterCallback() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}
