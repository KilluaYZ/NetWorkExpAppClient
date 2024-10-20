package com.example.client.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.client.utils.MSocketClient;

public class MainViewModel extends AndroidViewModel {

	public static final int SEND = 1;
	public static final int RECEIVE = 2;

	public final MutableLiveData<Integer> packageSum = new MutableLiveData<>();
	public final MutableLiveData<Integer> packageCount = new MutableLiveData<>();
	public final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
	public int action;
	public Uri outFileUriPath;
	public MSocketClient mSocketClient = null;

	public MainViewModel(@NonNull Application application) {
		super(application);
		packageCount.setValue(-1);
		packageSum.setValue(-1);
		isConnected.setValue(false);
	}
}
